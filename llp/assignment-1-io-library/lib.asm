; Сисколы
%define SYS_READ 0
%define SYS_WRITE 1
%define SYS_EXIT 60

; Потоки ввода вывода
%define STDIN 0
%define STDOUT 1
%define STDERR 2

global exit
global string_length
global print_string
global print_error
global print_newline
global print_char
global print_int
global print_uint
global string_equals
global read_char
global read_word
global read_line
global parse_uint
global parse_int
global string_copy


section .text 

; Принимает код возврата и завершает текущий процесс
exit:
    mov rax, SYS_EXIT
    syscall

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    mov rax, -1

   .loop:
        inc rax
        cmp byte [rdi+rax], 0        ; Проверяем, что пришел нуль-байт
        jne .loop
    
    ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi 
    call string_length           ; Длина строки кладется в rax
    pop rsi

    mov rdx, rax
    mov rax, SYS_WRITE
    mov rdi, STDOUT
    syscall
    ret

; Принимает указатель на нуль-терминированную строку, выводит её в stderr
print_error:
    push rdi 
    call string_length           ; Длина строки кладется в rax
    pop rsi

    mov rdx, rax
    mov rax, SYS_WRITE
    mov rdi, STDERR
    syscall
    ret

; Переводит строку (выводит символ с кодом 0xA)
; Используем Tail Call Optimization
print_newline:
    mov rdi, `\n`                ; Код символа перевода строки

; Принимает код символа и выводит его в stdout
print_char:
    sub rsp, 8                  ; Выравниваем стэк
    mov [rsp], rdi              ; Кладём символ на стэк
    mov byte [rsp+1], 0         ; Клалём нуль байт 
    lea rdi, [rsp]              ; Передаем как аргумент в print_string
    call print_string           ; Вызываем print_string
    add rsp, 8                  ; Освобождаем стек
    ret                         ; Возвраааааат

; Выводит знаковое 8-байтовое число в десятичном формате 
; Воспользуемся Tail Call Optimization
print_int:
    test rdi, rdi
    jge print_uint               ; Если число положительное, то прыгаем на print_uint

    neg rdi                      ; Делаем число положительным

    push rdi
    mov rdi, '-'
    call print_char              ; Выводим минус
    pop rdi

; Выводит беззнаковое 8-байтовое число в десятичном формате 
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    mov rax, rdi                 ; Кладем аргумент функции в rax для деления
    xor rsi, rsi
    sub rsp, 32                  ; Выделяем место и выравниваем стек

    mov rcx, 10                  ; Делитель
    .div_loop:
        xor rdx, rdx
        div rcx                      ; Делим число на 10, сохраняем остаток в rdx

        add rdx, '0'                 ; Превращаем цифру в символ ASCII
        mov [rsp + rsi], dl          ; Кладем символ на стек
        inc rsi                      ; Увеличиваем индекс

        test rax, rax
        jne .div_loop                ; Продолжаем, пока rax не станет нулем

    .print_loop:
        dec rsi
        movzx rdi, byte [rsp + rsi]  ; Загружаем символ для вывода

        push rsi
        call print_char            ; Выводим символ
        pop rsi

        test rsi, rsi
        jnz .print_loop              ; Продолжаем, пока не выведем все цифры

    add rsp, 32                  ; Восстанавливаем стек
    ret

; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    .loop:
        mov al, [rdi]                ; Загружаем байт строки 1
        mov dl, [rsi]                ; Загружаем байт строки 2

        cmp al, dl                   ; Сравниваем символы
        jne .not_equals              ; Если не равны, то сразу переходим на not_equals

        test al, al                  ; Проверяем конец строки (нулевой байт)
        je .equals                   ; Если конец, то строки равны

        ; Сдвигаем указатели
        inc rdi
        inc rsi

        jmp .loop

    .not_equals:
        xor rax, rax                 ; Возвращаем 0 (не равны)
        ret

    .equals:
        mov rax, 1                   ; Возвращаем 1 (равны)
        ret

; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    mov rax, SYS_READ
    mov rdi, STDIN
    lea rsi, [rsp - 1]           ; Адрес буфера для чтения (1 байт на стеке)
    mov rdx, 1                   ; Читаем 1 байт
    syscall

    test rax, rax                ; Проверяем количество прочитанных байт
    jle .end

    movzx rax, byte [rsp - 1]    ; Загружаем прочитанный байт в rax, расширяя знак

    .end:
        ret 

; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале.
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера.
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax.
; Эта функция должна дописывать к слову нуль-терминатор.
read_word:
    xor rax, rax
    xor rdx, rdx

    .skip_spaces:
        push rsi                     ; Сохраняем регистры
        push rdx
        push rdi
        call read_char              ; Читаем символ
        pop rdi
        pop rdx
        pop rsi                      ; Восстанавливаем регистры

        ; Пропускаем пробельные символы
        cmp rax, ' '
        je .skip_spaces

        cmp rax, `\t`
        je .skip_spaces

        cmp rax, `\n`
        je .skip_spaces

    .read_word:
        cmp rdx, rsi                 ; Проверяем размер буфера
        jge .error                   ; Если буфер переполнен, ошибка

        cmp rax, ' '                 
        je .end
        cmp rax, `\t`
        je .end
        cmp rax, `\n`
        je .end
        test rax, rax                ; Проверяем конец ввода
        jz .end

        mov [rdi + rdx], al          ; Записываем символ в буфер
        inc rdx                      ; Увеличиваем длину слова

        push rsi                     ; Сохраняем регистры
        push rdx
        push rdi
        call read_char               ; Читаем следующий символ
        pop rdi                      ; Восстанавливаем регистры
        pop rdx
        pop rsi

        jmp .read_word               ; Повторяем цикл

    .end:
        mov byte [rdi + rdx], 0      ; Добавляем нуль-терминатор
        mov rax, rdi                 ; Возвращаем адрес буфера
        ret

    .error:
        xor rax, rax                 ; Возвращаем 0 в случае ошибки
        ret

read_line:
    xor rax, rax
    xor rdx, rdx

    push rsi                     ; Сохраняем регистры
    push rdx
    push rdi
    call read_char              ; Читаем символ
    pop rdi
    pop rdx
    pop rsi                      ; Восстанавливаем регистры

    .read_line:
        cmp rdx, rsi                 ; Проверяем размер буфера
        jge .error                   ; Если буфер переполнен, ошибка

        cmp rax, `\n`
        je .end
        test rax, rax                ; Проверяем конец ввода
        jz .end

        mov [rdi + rdx], al          ; Записываем символ в буфер
        inc rdx                      ; Увеличиваем длину слова

        push rsi                     ; Сохраняем регистры
        push rdx
        push rdi
        call read_char               ; Читаем следующий символ
        pop rdi                      ; Восстанавливаем регистры
        pop rdx
        pop rsi

        jmp .read_line               ; Повторяем цикл

    .end:
        mov byte [rdi + rdx], 0      ; Добавляем нуль-терминатор
        mov rax, rdi                 ; Возвращаем адрес буфера
        ret

    .error:
        xor rax, rax                 ; Возвращаем 0 в случае ошибки
        ret


; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах.
; rdx = 0 если число прочитать не удалось.
parse_uint:
    xor rax, rax
    xor rdx, rdx

    .loop:
        mov cl, [rdi + rdx]          ; Читаем символ

        cmp cl, '0'                  ; Проверяем, что символ >= '0'
        jl .end                      ; Если нет, конец

        cmp cl, '9'                  ; Проверяем, что символ <= '9'
        jg .end                      ; Если нет, конец

        sub cl, '0'                  ; Преобразуем символ в цифру

        push rdx                     ; Сохраняем rdx

        xor rdx, rdx                 ; Обнуляем rdx
        mov rsi, 10                  ; Ставим множитель в 10
        mul rsi                      ; Умножаем

        pop rdx                      ; Восстанавливаем rdx

        add rax, rcx                 ; Добавляем цифру к числу

        inc rdx                      ; Увеличиваем длину
        jmp .loop                    ; Повторяем цикл

    .end: 
        ret

; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был). 
; rdx = 0 если число прочитать не удалось.
parse_int:
    sub rsp, 8                     ; Выделяем место на стеке (для хранения знака)
    xor rax, rax                   ; Обнуляем rax (число)

    cmp byte [rdi], '+'            ; Проверяем знак '+'
    je .handle_plus                ; Переход на обработку знака '+'
    cmp byte [rdi], '-'            ; Проверяем знак '-'
    je .handle_minus               ; Переход на обработку знака '-'

    mov byte [rsp], 0              ; Если знака нет, сохраняем 0 на стек
    jmp .parse_digit_part          ; Переход к обработке цифр

    .handle_plus:
        mov byte [rsp], '+'            ; Сохраняем знак '+' на стек
        inc rdi                        ; Сдвигаем указатель на следующий символ
        jmp .parse_digit_part

    .handle_minus:
        mov byte [rsp], '-'            ; Сохраняем знак '-' на стек
        inc rdi                        ; Сдвигаем указатель на следующий символ
        jmp .parse_digit_part

    .parse_digit_part:
        call parse_uint                ; Парсим число как беззнаковое
        test rdx, rdx                  ; Проверяем длину числа
        jz .end                        ; Если длина 0, завершаем

        cmp byte [rsp], '-'            ; Проверяем, был ли минус
        jne .check_plus                ; Если минус не найден, проверяем '+'
        inc rdx                        ; Увеличиваем длину на 1 для знака
        neg rax                        ; Если был минус, делаем число отрицательным
        jmp .end

    .check_plus:
        cmp byte [rsp], '+'            ; Проверяем, был ли плюс
        jne .end                       ; Если плюса не было, завершаем
        inc rdx                        ; Увеличиваем длину на 1 для знака

    .end:
        add rsp, 8                     ; Восстанавливаем стек
        ret                            ; Возвращаемся

; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    xor rcx, rcx                 ; Обнуляем индекс

    .loop:
        mov al, byte [rdi + rcx]     ; Читаем байт из исходной строки
        mov [rsi + rcx], al          ; Записываем байт в буфер

        cmp al, 0                    ; Проверяем на конец строки
        je .end

        inc rcx                      ; Увеличиваем индекс

        cmp rcx, rdx                 ; Проверяем размер буфера
        jl .loop                     ; Продолжаем цикл

    mov rcx, 0                       ; Если не нашли нуль-байт, возвращаем 0 (ошибка)

    .end:
        mov rax, rcx                 ; Возвращаем длину строки
        ret
