; вводим символы до тех пор пока не попадется запрещенный символ
; за раз вводим 2 разряда
; конвертация числа -> получить из двоичной записи цифру, умножить на 10^n, прибавить к результату
; число конвертируем в двочное представление

ORG 0x010
TMP: WORD ?
BCD_RES1: WORD 0 
BCD_RES2: WORD 0 
BCD_REV_RES: WORD 0 
DEC_RES: WORD 0


ORG 0x068
START:          CLA                 ; clear 

FIRST_SYMBOL:   IN 7                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ FIRST_SYMBOL    ; /

                IN 6
                ST TMP

                AND #0x000F
            
                ASL
                ASL
                ASL
                ASL                  

                ST BCD_RES1
                
                LD TMP

                AND #0x00F0
                ASR
                ASR
                ASR
                ASR

                ADD BCD_RES1
                SWAB                ; 4444 3333 0000 0000
                ST BCD_RES1
                
SECOND_SYMBOL:  IN 7                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ SECOND_SYMBOL    ; /

                IN 6
                ST TMP
                AND #0x000F
                
                ASL
                ASL
                ASL
                ASL                

                ST BCD_RES2
                
                LD TMP
                
                ASR
                ASR
                ASR
                ASR

                ADD BCD_RES2  ; XXXX XXXX 2222 11111
                ST BCD_RES2

SAVE_BCD:       LD BCD_RES1
                ADD BCD_RES2
                SWAB
                ST BCD_REV_RES


loop_4: WORD 4
BCD_TO_DEC:     LD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ADD $DEC_RES
                ST $DEC_RES

                LD $BCD_REV_RES
                AND #0x000F
                ADD $DEC_RES
                ST $DEC_RES
                
                LD $BCD_REV_RES
                ASR
                ASR
                ASR
                ASR
                ST $BCD_REV_RES

                LOOP $loop_4
                JUMP BCD_TO_DEC

loop_16: WORD 0x10

TO_PRINT: WORD ?
PRINT_BIM_BIM_BAMBAM:   LD $DEC_RES

                        SWAB

                        AND #00F0

                        ASR
                        ASR
                        ASR
                        ASR

                        ASR
                        ASR
                        ASR
                        AND #0X0001
                        ADD #0X30
                        ST TO_PRINT

                        CHECK_CHECK:
                        IN 0xD
                        AND #0x40
                        BEQ CHECK_CHECK
                        
                        LD TO_PRINT
                        OUT 0xC
                        
                        LD $DEC_RES
                        ASL
                        ST $DEC_RES
                        LOOP $loop_16
                        JUMP $PRINT_BIM_BIM_BAMBAM
HLT
