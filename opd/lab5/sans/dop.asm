ORG 0x010
DEBUG: WORD ?
X: WORD ?
Y: WORD ?
EOF: WORD 0x0d
TMP: WORD ?

ORG 0x075
START:          CLA 

GET_NUMBER:     IN 0x1D
                AND #0x40
                BEQ GET_NUMBER

                IN 0x1C
                ST $X

                CMP #0xA    ;check if >= 10
                BGE STOP

PRINT_NUMBERS:  CALL $WAIT_INPUT

                LD $X
                ADD #0x30   ; '0' ord
                OUT 0xC

                CALL $PRINT_SPACE
                CALL $WAIT_INPUT

                LD $X
                ADD #0x30   ; '0' ord
                OUT 0xC

                CALL $PRINT_SPACE
                CALL $WAIT_INPUT
                
                LD $X
                ADD $X
                ST $Y
                ST $TMP
                
                CMP #0xA
                BGE PRINT_TWO_NUMBERS1

                ADD #0x30   ; '0' ord
                OUT 0xC

                LD #0XF
                JUMP $NEXT_NUMBER

                PRINT_TWO_NUMBERS1: 
                    COUNT1: WORD 0x0
                    FIRST_PART1: WORD 0x0

                    LBL1:
                        LD $FIRST_PART1
                        INC
                        ST $FIRST_PART1

                        LD $TMP
                        SUB #0xA
                        ST $TMP
                        
                        CMP #0xA
                        BGE LBL1
                    
                    LD $FIRST_PART1
                    ADD #0x30   ; '0' ord
                    OUT 0xC

                    CALL $WAIT_INPUT
                    
                    LD $TMP
                    ADD #0x30   ; '0' ord
                    OUT 0xC

                NEXT_NUMBER: CALL $PRINT_SPACE

                CALL $WAIT_INPUT
                
                LD $Y
                ADD $X
                ST $TMP
                
                CMP #0xA
                BGE PRINT_TWO_NUMBERS2

                ADD #0x30   ; '0' ord
                OUT 0xC
                CALL $PRINT_SPACE
                JUMP $STOP

                PRINT_TWO_NUMBERS2: 
                    COUNT2: WORD 0x0
                    FIRST_PART2: WORD 0x0
                    LBL2:
                        LD $FIRST_PART2
                        INC
                        ST $FIRST_PART2

                        LD $TMP
                        SUB #0xA
                        ST $TMP
                        
                        CMP #0x0A
                        BGE LBL2
                    
                    LD FIRST_PART2
                    ADD #0x30   ; '0' ord
                    OUT 0xC

                    CALL $WAIT_INPUT
                    
                    LD $TMP
                    ADD #0x30   ; '0' ord
                    OUT 0xC

                CALL $PRINT_SPACE
                JUMP $STOP

WAIT_INPUT:     IN 0xD
                AND #0x40
                BEQ WAIT_INPUT 
                RET
                               
PRINT_SPACE:    CALL $WAIT_INPUT
                
                LD #0x20
                OUT 0xC        
                RET   
 

STOP:           HLT

ORG 0x610
RES: WORD ?