; Доп: на ВУ9 вводим рост в см на ВУ8 вывод в формате "Х футов Х дюймов

ORG 0x10
HEIGHT: WORD 0x0
X: WORD 0x0

ORG 0x100
START:          CLA 

GET_NUMBER:     CALL WAIT1 ; spin loop

                IN 0x1C
                
                ST $X

                CMP #0xA    ; if >= 10, than print answer
                BGE PRINT_ANSWER

                ; multiply on 10
                LD $HEIGHT

                ASL
                ASL 
                ASL
                ADD $HEIGHT
                ADD $HEIGHT
                
                ; add digit to height
                ADD $X
                ST $HEIGHT

                JUMP GET_NUMBER

; Х футов Х дюймов
CNT: WORD 0x0
PRINT_ANSWER:   LD $HEIGHT
                CMP #0x1F    ;check if >= 31
                BGE _INC

                JUMP NEXT
                _INC:       LD $CNT 
                            INC
                            ST $CNT

                            LD $HEIGHT
                            SUB #0x1F
                            ST $HEIGHT
                            
                            JUMP PRINT_ANSWER


            NEXT:           LD $CNT
                            CMP #0xA
                            BGE _INC1

                            JUMP PRINT1
                            CNT1: WORD 0x0
                            _INC1:  LD $CNT1
                                    INC
                                    ST $CNT1

                                    LD $CNT
                                    SUB #0xA
                                    ST $CNT
                                    JUMP NEXT

                            PRINT1:
                            CALL WAIT2
                            LD $CNT1
                            
                            CMP #0x0
                            BEQ _SKIP

                            ADD #0x30
                            OUT 0xC

                            _SKIP:  CALL WAIT2
                                    LD $CNT
                                    ADD #0x30
                                    OUT 0xC

                            ; futov
                            phrase_futov: WORD 0x20, 0xC6, 0xD5, 0xD4, 0xCF, 0xD7, 0x20 
                            len_futov: WORD 0x7
                            cur: word $phrase_futov
                    
                            cycle_for:  CALL WAIT2        
                                        LD (cur)+
                                        OUT 0xC
                                        LOOP len_futov
                                        JUMP cycle_for

                      meow: LD $HEIGHT
                            CMP #0xA
                            BGE _INC2

                            JUMP PRINT2

                            CNT2: WORD 0x0
                            _INC2:  LD $CNT2
                                    INC
                                    ST $CNT2

                                    LD $HEIGHT
                                    SUB #0xA
                                    ST $HEIGHT
                                    JUMP meow

                            PRINT2: 
                            CALL WAIT2
                            LD $CNT2
                            CMP #0x0
                            BEQ _SKIP1
                            ADD #0x30
                            OUT 0xC  

                            _SKIP1: CALL WAIT2
                                    LD $HEIGHT
                                    ADD #0x30
                                    OUT 0xC

                            ; dyumov
                            phrase1: WORD 0x20, 0xC4, 0xC0, 0xCA, 0xCD, 0xCF, 0xD7
                            LEN1: WORD 0x7
                            cur1: word $phrase1
                            
                            cycle_for1: CALL WAIT2 
                                        LD (cur1)+
                                        OUT 0xC
                                        LOOP LEN1
                                        JUMP cycle_for1
                            
                            HLT

WAIT1:  IN 0x1D
        AND #0x40
        BEQ WAIT1
        RET


WAIT2:  IN 0xD
        AND #0x40
        BEQ WAIT2
        RET