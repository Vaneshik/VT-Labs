ORG 0x250
CURRENT_ADDRESS: WORD $STRING
EOF: WORD 0x00                                  ; end of file char

ORG 0x271
START:                  CLA                     ; clear AC 

PRINT_FIRST_SYMBOL:     IN 3                    ; \
                        AND #0x40               ;  -> spin-loop while not ready
                        BEQ PRINT_FIRST_SYMBOL  ; /

                        LD (CURRENT_ADDRESS)

                        OUT 2                   ; print low byte of word

                        CMP EOF                 ; if EOF then stop
                        BEQ STOP

PRINT_SECOND_SYMBOL:    IN 3                    ; \
                        AND #0x40               ;  -> spin-loop while not ready
                        BEQ PRINT_SECOND_SYMBOL ; /

                        LD (CURRENT_ADDRESS)+   ; \
                        SWAB                    ;  -> read cell, swap bytes
                        
                        OUT 2                   ; print high byte of word

                        CMP EOF                 
                        BEQ STOP                ; if EOF then stop
                
                        JUMP PRINT_FIRST_SYMBOL ; while (true)

STOP:                   HLT                     ; exit programm

ORG 0x5B3
STRING: WORD э                       ; string array encoded in UTF-8 (БИБИ)