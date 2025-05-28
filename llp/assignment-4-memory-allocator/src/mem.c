#define _GNU_SOURCE
#define DEFAULT_PAGE_SIZE 4096UL

#include <assert.h>
#include <fcntl.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/errno.h>
#include <sys/mman.h>
#include <unistd.h>

#include "mem.h"
#include "mem_internals.h"
#include "util.h"

#ifndef MAP_ANONYMOUS
  #ifdef MAP_ANON
    #define MAP_ANONYMOUS MAP_ANON
  #else
    #define MAP_ANONYMOUS 0
  #endif
#endif

#ifndef MAP_FIXED_NOREPLACE
  #define MAP_FIXED_NOREPLACE 0
#endif

#define BLOCK_MIN_CAPACITY 24


// Вспомогательные функции для отладочного вывода (если нужно)
void debug_block(struct block_header* b, const char* fmt, ...);
void debug(const char* fmt, ...);

/*
 * Возвращает размер страницы в байтах, используя sysconf.
 * Если sysconf вернул некорректное значение, используется DEFAULT_PAGE_SIZE.
 */
static size_t get_page_size() {
    long ps = sysconf(_SC_PAGESIZE);
    return (ps > 0) ? (size_t)ps : DEFAULT_PAGE_SIZE;
}

/*
 * Проверяет, достаточно ли свободного места в блоке block,
 * чтобы удовлетворить запрос на память размером query байтов.
 */
static bool block_is_big_enough(size_t query, const struct block_header* block) {
    return block->capacity.bytes >= query;
}

/*
 * Вычисляет количество страниц, необходимых для размещения mem байтов.
 */
static size_t pages_count(size_t mem) {
    return mem / get_page_size() + ((mem % get_page_size()) > 0);
}

/*
 * Округляет mem до ближайшего кратного размера страницы.
 */
static size_t round_pages(size_t mem) {
    return get_page_size() * pages_count(mem);
}

/*
 * Превращает вместимость данных (capacity) в общий размер блока (size).
 * В общем размере учитывается заголовок блока.
 */
block_size size_from_capacity(block_capacity cap) {
    return (block_size){ cap.bytes + offsetof(struct block_header, contents) };
}

/*
 * Обратная операция: из полного размера блока (size) получить
 * вместимость поля данных (capacity).
 */
block_capacity capacity_from_size(block_size sz) {
    return (block_capacity){ sz.bytes - offsetof(struct block_header, contents) };
}

/*
 * Проверяет, является ли регион памяти некорректным.
 * Некорректным считается, если r->addr == NULL.
 */
bool region_is_invalid(const struct region* r) {
    return r->addr == NULL;
}

/*
 * Инициализирует в памяти (addr) структуру block_header.
 * Размер блока = block_sz, следующий блок = next.
 */
static void block_init(void* restrict addr, block_size block_sz, void* restrict next) {
    *((struct block_header*)addr) = (struct block_header){
        .next = next,
        .capacity = capacity_from_size(block_sz),
        .is_free = true
    };
}

/*
 * Вычисляет реальный размер региона для запроса query байтов.
 * Сравнивает округлённое до страницы значение с минимальным размером региона REGION_MIN_SIZE.
 */
static size_t region_actual_size(size_t query) {
    return size_max(round_pages(query), REGION_MIN_SIZE);
}

/*
 * Вызывает mmap для резервирования (или отображения) области памяти.
 * Дополнительные флаги указываются в additional_flags.
 */
static void* map_pages(void const* addr, size_t length, int additional_flags) {
    return mmap((void*)addr, length, PROT_READ | PROT_WRITE,
                MAP_PRIVATE | MAP_ANONYMOUS | additional_flags, -1, 0);
}

/*
 * Аллоцирует регион памяти размером не меньше query, учитывая выравнивание и минимальные ограничения.
 * - Сначала пытается вызвать mmap с флагом MAP_FIXED_NOREPLACE
 * - Если не удалось, вызывает mmap без этого флага.
 * - В случае успеха инициализирует первый блок в регионе.
 */
static struct region alloc_region(void const* addr, size_t query) {
    const size_t needed_for_alloc = region_actual_size(
        size_from_capacity((block_capacity){ query }).bytes
    );
    void* mapped_addr = map_pages(addr, needed_for_alloc, MAP_FIXED_NOREPLACE);
    bool extends = false;

    if (mapped_addr == MAP_FAILED) {
        mapped_addr = map_pages(addr, needed_for_alloc, 0);
        if (mapped_addr == MAP_FAILED) {
            // mmap не смогла зарезервировать память
            return (struct region){ 0 };
        }
    } else {
        // Удалось зарезервировать память с MAP_FIXED_NOREPLACE
        extends = true;
    }

    struct region r = {
        .addr = mapped_addr,
        .size = needed_for_alloc,
        .extends = extends
    };

    block_init(r.addr, (block_size){ needed_for_alloc }, NULL);
    return r;
}

/*
 * Возвращает адрес, идущий сразу за концом данных в данном блоке.
 * Используется для проверки непрерывности блоков и т.п.
 */
static void* block_after(const struct block_header* block) {
    return (void*)(block->contents + block->capacity.bytes);
}

/*
 * Инициализирует кучу заданным объёмом initial байт.
 * Возвращает указатель на начало кучи или NULL в случае неудачи.
 */
void* heap_init(size_t initial) {
    const struct region region = alloc_region(HEAP_START, initial);
    if (region_is_invalid(&region)) return NULL;
    return region.addr;
}

/*
 * Высвобождает всю память, выделенную под кучу.
 * Идёт по списку блоков, освобождая каждый регион с помощью munmap.
 * При слиянии соседних свободных блоков увеличивается размер освобождаемой области.
 */
void heap_term() {
    for (struct block_header* cur = (struct block_header*)HEAP_START; cur; ) {
        struct block_header* next = cur->next;
        size_t unmap_size = size_from_capacity(cur->capacity).bytes;

        // Слияние соседних свободных блоков для одного большого munmap
        while (next && (void*)next == block_after(cur)) {
            unmap_size += size_from_capacity(next->capacity).bytes;
            next = next->next;
        }

        if (munmap(cur, unmap_size) == -1) {
            // Если не удалось выполнить munmap, выводим ошибку
            dprintf(STDERR_FILENO,
                    "munmap error: can't unmap region starting at %p with size of %zu bytes. System error log: %s\n",
                    (void*)cur, unmap_size, strerror(errno));
            return;
        }

        cur = next;
    }
}

/*
 * Проверяет, можно ли разделить блок, если блок слишком велик для запроса query.
 * Условие: блок свободен И (query + заголовок + минимальная вместимость) <= capacity.
 */
static bool block_splittable(const struct block_header* restrict block, size_t query) {
    return block->is_free
           && query + offsetof(struct block_header, contents) + BLOCK_MIN_CAPACITY <= block->capacity.bytes;
}

/*
 * Если блок слишком велик (block_splittable), делим его на два:
 * - первый блок становится нужного размера (query),
 * - второй блок создаётся в конце первого.
 */
static bool split_if_too_big(struct block_header* block, size_t query) {
    if (!block_splittable(block, query)) return false;

    // Адрес начала второго блока
    void* split_ptr = block->contents + query;
    // Размер второго блока
    const block_size blk_sz = (block_size){ block->capacity.bytes - query };

    // Инициализация второго блока
    block_init(split_ptr, blk_sz, block->next);

    // Актуализация текущего блока
    block->next = (struct block_header*)split_ptr;
    block->capacity.bytes = query;

    return true;
}

/*
 * Проверяет, непрерывны ли два блока в памяти (snd идёт сразу за fst).
 */
static bool blocks_continuous(const struct block_header* fst, const struct block_header* snd) {
    return (void*)snd == block_after(fst);
}

/*
 * Можно ли выполнить слияние блоков fst и snd.
 * Условие: оба блока свободны и идут один за другим в памяти.
 */
static bool mergeable(const struct block_header* restrict fst, const struct block_header* restrict snd) {
    return fst->is_free && snd && snd->is_free && blocks_continuous(fst, snd);
}

/*
 * Пытается слить текущий блок block со следующим.
 * Если слияние возможно, увеличиваем capacity первого блока
 * и перенаправляем его next на next->next.
 */
static bool try_merge_with_next(struct block_header* block) {
    struct block_header* next = block->next;
    // Добавляем проверку для анализа:
    if (!next) return false;

    if (!mergeable(block, next)) return false;

    block->next = next->next;
    block->capacity.bytes += size_from_capacity(next->capacity).bytes;
    return true;
}

/*
 * Структура, описывающая результат поиска подходящего блока (или конец списка).
 */
struct block_search_result {
    enum { BSR_FOUND_GOOD_BLOCK, BSR_REACHED_END_NOT_FOUND, BSR_CORRUPTED } type;
    struct block_header* block;
};

/*
 * Ищет в связанном списке блок, удовлетворяющий размеру sz.
 * Если найден подходящий свободный блок (достаточно большой),
 * возвращаем BSR_FOUND_GOOD_BLOCK.
 * Иначе, если дошли до конца списка - BSR_REACHED_END_NOT_FOUND.
 * Если в процессе что-то некорректно (например, первый блок == NULL),
 * возвращаем BSR_CORRUPTED.
 */
static struct block_search_result find_good_or_last(struct block_header* restrict block, size_t sz) {
    if (!block) {
        return (struct block_search_result){ .type = BSR_CORRUPTED, .block = NULL };
    }
    struct block_header* last = block;
    while (block) {
        if (block->is_free) {
            // Попытаемся сливать блоки до тех пор, пока возможно
            while (try_merge_with_next(block)) {}
            // Проверяем, удовлетворяет ли размер
            if (block_is_big_enough(sz, block)) {
                return (struct block_search_result){
                    .type = BSR_FOUND_GOOD_BLOCK,
                    .block = block
                };
            }
        }
        last = block;
        block = block->next;
    }
    return (struct block_search_result){
        .type = BSR_REACHED_END_NOT_FOUND,
        .block = last
    };
}

/*
 * Пытается найти и выделить блок в уже существующем списке блоков,
 * НЕ расширяя кучу. В случае успеха (нашли большой свободный блок),
 * делит его при необходимости (split_if_too_big) и помечает занятым.
 */
static struct block_search_result try_memalloc_existing(size_t query, struct block_header* block) {
    const struct block_search_result search_res = find_good_or_last(block, query);
    if (search_res.type == BSR_FOUND_GOOD_BLOCK) {
        // Если нашли подходящий блок, пробуем разделить (split)
        if (split_if_too_big(search_res.block, query)) {
            search_res.block->is_free = false;
            // Возвращаем именно search_res.block, а не block!
            return (struct block_search_result){
                .type = BSR_FOUND_GOOD_BLOCK,
                .block = search_res.block
            };
        }
    }
    return search_res;
}

/*
 * "Расширяет" кучу, вызывая alloc_region для области,
 * которая начинается сразу после блока last и имеет размер не меньше query.
 * Если region_is_invalid, вернуть NULL.
 * Если получилось добавить регион с флагом extends,
 * пытаемся слить последний блок со следующим (если непрерывно).
 */
static struct block_header* grow_heap(struct block_header* restrict last, size_t query) {
    // Выделяем новый регион после конца текущего блока
    struct region reg = alloc_region(block_after(last), query);
    if (!last || region_is_invalid(&reg)) return NULL;

    last->next = (struct block_header*)reg.addr;

    // Если mmap сработал с MAP_FIXED_NOREPLACE (extends == true),
    // попробуем слить блоки
    if (reg.extends && try_merge_with_next(last)) {
        return last;
    }
    return (struct block_header*)reg.addr;
}

/*
 * Основная логика выделения памяти.
 * 1. Пытаемся найти подходящий блок в текущей куче (try_memalloc_existing).
 * 2. Если подходящий блок не найден и достигнут конец (BSR_REACHED_END_NOT_FOUND),
 *    вызываем grow_heap для расширения кучи, затем повторяем поиск.
 * 3. Возвращаем заголовок выделенного блока или NULL, если неудача.
 */
static struct block_header* memalloc(size_t query, struct block_header* heap_start) {
    // Гарантируем минимальный размер блока
    query = size_max(query, BLOCK_MIN_CAPACITY);
    query = (query + 7U) & ~7U;

    // Пытаемся найти в существующих блоках
    struct block_search_result search_res = try_memalloc_existing(query, heap_start);

    if (search_res.type == BSR_REACHED_END_NOT_FOUND) {
        // Расширяем кучу
        struct block_header* const new_block = grow_heap(search_res.block, query);
        if (!new_block) return NULL;
        // Снова пробуем найти/разделить
        search_res = try_memalloc_existing(query, new_block);
    } else if (search_res.type == BSR_CORRUPTED) {
        // Некорректный список блоков
        return NULL;
    }

    return search_res.block;
}

/*
 * Экспортируемая функция, аналог стандартной malloc.
 * Вызывает memalloc, возвращая указатель на область данных.
 */
void* _malloc(size_t query) {
    struct block_header* const header = memalloc(query, (struct block_header*)HEAP_START);
    return header ? header->contents : NULL;
}

/*
 * По указателю на данные вычисляет указатель на заголовок блока.
 */
static struct block_header* block_get_header(void* contents) {
    return (struct block_header*)((uint8_t*)contents - offsetof(struct block_header, contents));
}

/*
 * Экспортируемая функция, аналог стандартной free.
 * Помечает блок как свободный и пытается слить его со следующим.
 */
void _free(void* mem) {
    if (!mem) return;
    struct block_header* bh = block_get_header(mem);
    bh->is_free = true;
    // Сливаем подряд идущие свободные блоки
    while (try_merge_with_next(bh)) {}
}