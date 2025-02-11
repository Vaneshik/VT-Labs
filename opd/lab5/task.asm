ORG 0x010
RES_ADDRESS: WORD $RES              ; current string address
EOF: WORD 0x0d                      ; EOF char
TMP: WORD ?                         ; tmp

ORG 0x068
START:          CLA                 ; clear 

FIRST_SYMBOL:   IN 7                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ FIRST_SYMBOL    ; /

                IN 6                ; \
                SWAB                ;  -> read char, save to first byte of the cell
                ST (RES_ADDRESS)    ; /

                SWAB                ; \
                CMP EOF             ;  -> if eof then stop
                BEQ STOP            ; /

SECOND_SYMBOL:  IN 7                ; \
                AND #0x40           ;  -> spin-loop while not ready
                BEQ SECOND_SYMBOL   ; /

                IN 6                ; \
                ST TMP              ;  | -> read char, save to second byte of the cell
                ADD (RES_ADDRESS)   ;  |
                ST (RES_ADDRESS)+   ; /
                
                LD TMP              ; \
                CMP EOF             ;  -> if eof then stop
                BEQ STOP            ; /
                
                JUMP FIRST_SYMBOL  ; while (true)

STOP:           HLT                ; exit programm

ORG 0x5B9
RES: WORD ?                        ; string beginning