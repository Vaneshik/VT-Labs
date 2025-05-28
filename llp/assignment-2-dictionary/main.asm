%include "assignment-1-io-library/lib.inc"
%include "dict.inc"
%include "words.inc" ; статически заданный словарь

%define BUFFER_SIZE 256

global _start

section .rodata
hello_string: db "Привет! Введи строку для поиска: ", 0
not_found_error_message: db "Ничего не смог найти =(", 0
buffer_overflow_error_message: db "!!! [PWNED] Buffer OverFlow!!!", 0

section .text

_start:
    mov rdi, hello_string
    call print_string
    
    sub rsp, BUFFER_SIZE

    mov rdi, rsp
    mov rsi, (BUFFER_SIZE-1)
    call read_line ; input_string -> rax, length -> rdx

    mov rcx, rdx   ; cuz we wont break rsp

    test rax, rax
    jz .buffer_overflow_error

    mov rdi, rsp
    mov rsi, last_elem_pointer
    call find_word ; searched_string_pointer or 0 -> rax

    test rax, rax
    je .not_found_error
    
    lea rdi, [rax+rcx+1]    
    call print_string
    call print_newline
    
    jmp .ok

.not_found_error:
    mov rdi, not_found_error_message
    call print_error
    call print_newline

    mov rdi, 1
    jmp .exit

.buffer_overflow_error:
    mov rdi, buffer_overflow_error_message
    call print_error
    call print_newline

    mov rdi, 1337
    jmp .exit

.ok: 
    xor rdi, rdi

.exit:
    add rsp, BUFFER_SIZE 
    call exit