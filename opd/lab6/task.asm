ORG 0x0
V0: WORD $DEFAULT, 0X180
V1: WORD $INT1, 0X180
V2: WORD $DEFAULT, 0x180
V3: WORD $INT3, 0X180
V4: WORD $DEFAULT, 0X180
V5: WORD $DEFAULT, 0X180
V6: WORD $DEFAULT, 0X180
V7: WORD $DEFAULT, 0X180

DEFAULT: IRET ; Обработка прерывания по умолчанию (по-хорошему переписать)

ORG 0x01C
X: WORD ?

MIN: WORD 0xFFE0 ; -32
MAX: WORD 0x001F ; 31

ORG 0x20
START:  DI
        CLA
        OUT 0x1
        OUT 0x5
        OUT 0xB
        OUT 0xD
        OUT 0x11
        OUT 0x15
        OUT 0x19
        OUT 0x1D
        LD #0x9 ; Загрузка в аккумулятор MR (1000|0001=1001)
        OUT 0x3 ; Разрешение прерываний для 1 ВУ
        LD #0xB ; Загрузка в аккумулятор MR (1000|0011=1011)
        OUT 0x7 ; Разрешение прерываний для 3 ВУ
        EI

MAIN:   DI
        LD $X
        INC
        INC
        INC
        CALL CHECK
        ST $X
        EI
        JUMP MAIN

CHECK:
        CMP $MIN ; Если x > min переход на max
        BPL CHECK_MAX
        JUMP LD_MIN
        CHECK_MAX: CMP $MAX
        BMI RETURN ; Если x < max переход
        LD_MIN: LD $MIN
        RETURN: RET

INT1:   DI ; Обработка прерывания на ВУ-1
        LD X
        NOP
        ASL
        ASL
        NEG
        SUB #4
        NOP
        OUT 0x2
        EI
        IRET

INT3:   DI ; Обработка прерывания на ВУ-3
        IN 0x6
        NOP
        AND X
        NOT
        ST X
        NOP
        EI
        IRET  ;42