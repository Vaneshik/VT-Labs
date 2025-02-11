ORG 0x10
ANS1:       WORD 0x0
ANS2:       WORD 0x0
ANS3:       WORD 0x0
ANS4:       WORD 0x0
 
FINAL_ANS:  WORD 0x0
 
ORG	0x1F6
START:      CLA
            CALL $TEST1
            LD $ANS1
            NOP
            
            ; CALL $TEST2
            ; LD $ANS2
            ; NOP
            
            CALL $TEST3
            LD $ANS3
            NOP

            ; CALL $TEST4
            ; LD $ANS4
            ; NOP


            ; Подсчет количества пройденных тестов
            ; LD #0x0
            ADD ANS1
            ; ADD ANS2
            ADD ANS3
            ; ADD ANS4
            
            ST $FINAL_ANS
            HLT

            ; Проверка на прохождение всех тестов
            ; CMP #0x1
            ; BEQ GET_ANS

            ; LD #0x17
            ; ST $FINAL_ANS

;             ; Вывод результата
; GET_ANS:    LD $FINAL_ANS
            
;             HLT


ORG	0x200
A1:         WORD 0x1234
B1:         WORD 0x1337
RES1:       WORD ?

; MADC без CF: проверка на эквивалентность результату команды ADD тех же чисел
TEST1:
            CLC

            LD $A1
    		ADC $B1
    		ST $RES1

        	LD $A1
        	WORD 0x9201 ; MADC 0x201
        
            ; Проверка на эквивалентность результату команды ADD тех же чисел
            LD $B1
        	CMP RES1
        	BNE ERR1
 
            ; В случае успешного прохождения теста
            LD #0x1
        	ST $ANS1

        	RET

; В случае неуспешного прохождения теста
ERR1:       LD #0x0
        	ST $ANS1
        	RET
 

ORG	0x300
A2:         WORD 0xDEAD
B2:         WORD 0xBEEF
RES2:       WORD ?
; Сравнение ADD двух чисел + 1 с результатами MADC (с выставленным CF)
TEST2:      CLC
            LD A2
    		ADD B2
            INC 
    		ST RES2

        	CLC
            CMC

            LD A2
        	WORD 0x9301; MADC 0x301
        	
            ; Сравнение результатов
            LD B2
            CMP RES2
            BNE ERR2 
 
            ; В случае успешного прохождения теста
            LD #0x1	
        	ST $ANS2   

        	RET

; В случае неуспешного прохождения теста
ERR2:       LD #0x0 	
        	ST $ANS2
        	RET


ORG	0x400
A3:         WORD 0xA234
B3:         WORD 0x0007
RES3:       WORD ?
TEST3:      CLC ; Тест аналогичен первому, но использует относительную адресацию

            LD A3
    		ADC B3
    		ST $RES3

        	LD A3
    		WORD 0x9EF8 ; MADC (IP-8)
        	
            ; Проверка на эквивалентность результату команды ADD тех же чисел
            LD B3
    		CMP RES3
        	BNE ERR3

            ; В случае успешного прохождения теста
            LD #0x1
        	ST $ANS3

        	RET

; В случае неуспешного прохождения теста
ERR3:       LD #0x0
            ST $ANS3
        	RET
 

ORG	0x500
A4:  	    WORD 0xFFFF
B4:  	    WORD 0x0000
OF1:       WORD ?
OF2:       WORD ?
; Случай, когда AC = 0xFFFF, DR = 0x0000, C = 1. Проверка что V = 1 при MADC и V=0 при (AC + 1) + DR.
TEST4:     CLC

            LD A4
            INC 
    		ADC B4
    		
            BVC SET_OF1_0

            LD #0x1
            ST $OF1

SET_OF1_0:  LD #0x0
            ST $OF1
        	
            LD A4
    		WORD   0x9501; MADC 0x501

            BVC SET_OF2_0

            LD #0x1
            ST $OF2

SET_OF2_0:  LD #0x0
            ST $OF2    

            LD OF1
            CMP OF2
            BEQ ERR4

            LD #0x1
            ST $ANS4
            HLT

            RET

ERR4:       LD #0x0
            ST $ANS4

        	RET