ORG 0x0
V0: WORD $INT0, 0X180
V1: WORD $INT1, 0X180
V2: WORD $DEFAULT, 0x180
V3: WORD $DEFAULT, 0x180
V4: WORD $DEFAULT, 0X180
V5: WORD $DEFAULT, 0X180
V6: WORD $DEFAULT, 0X180
V7: WORD $DEFAULT, 0X180

DEFAULT: IRET ; Обработка прерывания по умолчанию (по-хорошему переписать)

FLAG: WORD 0

ORG 0x20
START:  DI
        CLA
        OUT 0x5
        OUT 0x7
        OUT 0xB
        OUT 0xD
        OUT 0x11
        OUT 0x15
        OUT 0x19
        OUT 0x1D

        LD #0x8 ; Загрузка в аккумулятор MR (1000|0000=1000)
        OUT 0x1 ; Разрешение прерываний для 0 ВУ

        LD #0x9 ; Загрузка в аккумулятор MR (1000|0001=1001)
        OUT 0x3 ; Разрешение прерываний для 1 ВУ

        LD #7f
        OUT 0x0

        EI

MAIN:   DI
        EI
        JUMP MAIN

INT0:   DI ; Обработка прерывания на ВУ-0
        
        LD FLAG

        CMP #0
        BEQ EI_123
        
        HLT

        EI_123: EI
        IRET

INT1:   DI ; Обработка прерывания на ВУ-1
        LD FLAG
        CMP #0
        BEQ LD_ONE
        
        LD #0
        ST FLAG
        JUMP OUT_

        LD_ONE: LD #1
        ST FLAG
        
        OUT_: OUT 2
        
        EI
        IRET