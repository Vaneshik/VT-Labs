#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "mem.h"
#include "mem_internals.h"

#define INITIAL_HEAP_SIZE 1024

/*
 * Сценарий 1: Обычное успешное выделение памяти
 */
void test_simple_alloc(void) {
    printf("\n=== Сценарий 1: Обычное успешное выделение памяти ===\n");

    // Инициализируем кучу
    void *heap_start = heap_init(INITIAL_HEAP_SIZE);
    if (!heap_start) {
        fprintf(stderr, "Ошибка: не удалось инициализировать кучу.\n");
        return;
    }

    // Выделяем 128 байт
    void *ptr = _malloc(128);
    if (!ptr) {
        fprintf(stderr, "Ошибка: не удалось выделить 128 байт.\n");
        return;
    }
    printf("Выделили 128 байт: ptr = %p\n", ptr);

    // Можно проверить запись/чтение
    memset(ptr, 0xAA, 128);

    // Освобождаем
    _free(ptr);
    printf("Освободили выделенный блок.\n");

    // Освобождаем кучу
    heap_term();
    printf("Куча освобождена успешно.\n");
}

/*
 * Сценарий 2: Освобождение одного блока из нескольких выделенных
 */
void test_free_one_of_many(void) {
    printf("\n=== Сценарий 2: Освобождение одного блока из нескольких ===\n");

    // Инициализируем кучу
    void *heap_start = heap_init(INITIAL_HEAP_SIZE);
    if (!heap_start) {
        fprintf(stderr, "Ошибка: не удалось инициализировать кучу.\n");
        return;
    }

    // Выделяем несколько блоков
    void *ptr1 = _malloc(64);
    void *ptr2 = _malloc(32);
    void *ptr3 = _malloc(128);
    if (!ptr1 || !ptr2 || !ptr3) {
        fprintf(stderr, "Ошибка: не удалось выделить блоки памяти.\n");
        // Освобождаем кучу, чтобы не утекла память
        heap_term();
        return;
    }

    printf("Выделено ptr1=%p (64 байт), ptr2=%p (32 байт), ptr3=%p (128 байт)\n",
           ptr1, ptr2, ptr3);

    // Освобождаем только один из них (ptr2)
    _free(ptr2);
    printf("Освободили ptr2.\n");

    // Проверяем, что ptr1 и ptr3 всё ещё заняты

    // Освобождаем оставшиеся
    _free(ptr1);
    _free(ptr3);

    // Освобождаем кучу
    heap_term();
    printf("Куча освобождена успешно.\n");
}

/*
 * Сценарий 3: Освобождение двух блоков из нескольких выделенных
 */
void test_free_two_of_many(void) {
    printf("\n=== Сценарий 3: Освобождение двух блоков из нескольких ===\n");

    // Инициализируем кучу
    void *heap_start = heap_init(INITIAL_HEAP_SIZE);
    if (!heap_start) {
        fprintf(stderr, "Ошибка: не удалось инициализировать кучу.\n");
        return;
    }

    // Выделяем несколько блоков
    void *ptr1 = _malloc(64);
    void *ptr2 = _malloc(128);
    void *ptr3 = _malloc(64);
    if (!ptr1 || !ptr2 || !ptr3) {
        fprintf(stderr, "Ошибка: не удалось выделить блоки памяти.\n");
        // Освобождаем кучу
        heap_term();
        return;
    }

    printf("Выделено ptr1=%p (64 байт), ptr2=%p (128 байт), ptr3=%p (64 байт)\n",
           ptr1, ptr2, ptr3);

    // Освобождаем два из них (ptr1 и ptr2), оставляя ptr3 занятым
    _free(ptr1);
    _free(ptr2);
    printf("Освободили ptr1 и ptr2, ptr3 остаётся занятым.\n");

    // Здесь ptr3 ещё занят. Проверяем, что всё работает
    memset(ptr3, 0xCC, 64);

    // Освобождаем последний блок
    _free(ptr3);

    // Освобождаем кучу
    heap_term();
    printf("Куча освобождена успешно.\n");
}

/*
 * Сценарий 4: Память закончилась, новый регион памяти расширяет старый
 */
void test_expand_region(void) {
    printf("\n=== Сценарий 4: Расширение региона при нехватке памяти ===\n");

    // Инициализируем кучу
    void *heap_start = heap_init(INITIAL_HEAP_SIZE);
    if (!heap_start) {
        fprintf(stderr, "Ошибка: не удалось инициализировать кучу.\n");
        return;
    }

    // Выделяем блок, превышающий текущий размер кучи
    size_t big_size = 4096; // больше, чем 1024
    void *ptr = _malloc(big_size);
    if (!ptr) {
        fprintf(stderr, "Ошибка: не удалось расширить кучу и выделить %zu байт.\n", big_size);
    } else {
        printf("Успешно выделено %zu байт: ptr = %p\n", big_size, ptr);
        // Можно проверить запись/чтение
        memset(ptr, 0xBB, big_size);
        // Освобождаем
        _free(ptr);
    }

    // Освобождаем кучу
    heap_term();
    printf("Куча освобождена успешно.\n");
}

/*
 * Сценарий 5: Память закончилась, старый регион не расширить (занят блок),
 * нужно выделить новый регион в другом месте
 */
void test_new_region(void) {
    printf("\n=== Сценарий 5: Новый регион в другом месте ===\n");

    // Инициализируем кучу
    void *heap_start = heap_init(INITIAL_HEAP_SIZE);
    if (!heap_start) {
        fprintf(stderr, "Ошибка: не удалось инициализировать кучу.\n");
        return;
    }

    // Выделим блок, который останется занятым и помешает расширению
    void *ptr_busy = _malloc(512);
    if (!ptr_busy) {
        fprintf(stderr, "Ошибка: не удалось выделить ptr_busy (512 байт).\n");
        heap_term();
        return;
    }
    printf("ptr_busy = %p (512 байт)\n", ptr_busy);

    // Теперь попробуем выделить блок ещё больше (2048),
    // что превысит возможности слияния/расширения в старом месте.
    void *ptr_big = _malloc(2 * INITIAL_HEAP_SIZE);
    if (!ptr_big) {
        fprintf(stderr, "Ошибка: не удалось выделить новый регион на 2048 байт.\n");
    } else {
        printf("Успешно выделен новый регион: ptr_big = %p (2048 байт)\n", ptr_big);
        memset(ptr_big, 0xDD, 2 * INITIAL_HEAP_SIZE);
        // Освобождаем
        _free(ptr_big);
    }

    // Освобождаем занятый блок
    _free(ptr_busy);

    // Освобождаем кучу
    heap_term();
    printf("Куча освобождена успешно.\n");
}

/*
 * Главная функция, последовательно запускает тесты.
 */
int main(void) {
    printf("=== Тесты аллокатора памяти ===\n");

    test_simple_alloc();          // 1. Обычное успешное выделение
    test_free_one_of_many();      // 2. Освобождение одного из нескольких
    test_free_two_of_many();      // 3. Освобождение двух из нескольких
    test_expand_region();         // 4. Расширение региона
    test_new_region();            // 5. Новый регион в другом месте

    printf("\nВсе тесты завершены.\n");
    return 0;
}