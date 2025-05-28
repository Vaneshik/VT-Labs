%include "assignment-1-io-library/lib.inc"

section .text

global find_word
global get_value

; rdi -> pointer to string; rsi -> pointer to dict
find_word:
	mov rdx, rsi

	.loop:
		test rdx, rdx
		je .not_found

		lea rsi, [rdx + 8]
		
		push rdi
		push rsi
		push rdx 
		call string_equals; rdi==rsi?1:0
		pop rdx
		pop rsi
		pop rdi
		
		cmp rax, 1
		je .found

		mov rdx, [rdx]
		jmp .loop

	.found:
		lea rax, [rdx + 8]
		ret

	.not_found:
		xor rax, rax
		ret
