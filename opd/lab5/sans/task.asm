ORG 0x010
EOF: WORD 0x0a                      ; EOF char
CURRENT_ADDRESS: WORD $STRING
TMP: WORD ?                         ; tmp

ORG 0x075
START:          CLA                 ; clear 

FIRST_SYMBOL:   IN 5                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ FIRST_SYMBOL    ; /

                IN 4                ; \
                SWAB                ;  -> read char, save to first byte of the cell
                ST (CURRENT_ADDRESS); /

                SWAB                ; \
                CMP EOF             ;  -> if eof then stop
                BEQ BREAK           ; /

SECOND_SYMBOL:  IN 5                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ SECOND_SYMBOL   ; /

                IN 4                  ; \
                ST TMP                ;  | -> read char, save to second byte of the cell
                ADD (CURRENT_ADDRESS) ;  |
                ST (CURRENT_ADDRESS)+ ; /
                
                LD TMP              ; \
                CMP EOF             ;  -> if eof then stop
                BEQ BREAK           ; /
                
                JUMP FIRST_SYMBOL   ; while (true)

BREAK:           HLT                 ; exit programm

ORG 0x610
STRING: WORD ?                      ; string beginning