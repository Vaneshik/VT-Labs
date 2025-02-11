ORG     0x0
TT1:  	WORD    0x0; Тест 1 - Проверка при прямой абсолютной адресации
TT2:   	WORD    0x0; Тест 2 - Проверка на отсутствие изменения NZVC
TT3:  	WORD    0x0; Тест 3 - Проверка на установку флага C 
TT4:  	WORD    0x0; Тест 4 – Проверка при прямой относительной адресации
TT6:  	WORD    0x0; Тест 6 – Крайний случай + косвенная относительная

ORG    0x0074
START: CALL $TEST1
 	LD $TT1
	NOP; Проверка
	CALL    $TEST2
 	LD $TT2
	NOP	; Проверка
	CALL	$TEST3
	LD	$TT3
	NOP	; Проверка
CALL	$TEST4
	LD	$TT4
	NOP	; Проверка
CALL	$TEST5
	NOP	; Проверка
CALL	$TEST6
	LD	$TT6
	NOP	; Проверка
	HLT

ORG    0x500
A1: WORD    0xA234	
B1: WORD    0x0234
RES1: WORD ?
TEST1:  CLA 
    CLC
    LD    A1 
    SUB   B1
    ST    RES1
	LD    B1
    WORD  0x9500
	LD    A1
    CMP   RES1
    BNE   ERR1

    LD #0x1	
    ST $TT1
    RET
ERR1: LD  #0x0
      ST $TT1
      RET
;-----------------------------------------------------------------
ORG    0x600
A2: WORD 0xA234
B2: WORD 0xFFFF	
TEST2: CLA
	CLC
LD 	B2
	WORD 0x9600 
	BCS   ERR2

LD 	#0x1	
	ST   $TT2	
    RET
ERR2: LD #0x0	
    ST   $TT2	
    RET
;--------------------------------------------------------------------
ORG    0x700
A3: 	WORD 	0xA234
B3: 	WORD 	0xFFFF
RES3:	 WORD ?

TEST3: CLA
	CLC
LD 	A3
	WORD 0x9601 
	BLO   ERR3 

LD 	#0x1	
	ST   $TT3	
    RET
ERR3: LD #0x0	
    ST   $TT3	
    RET
;-------------------------------------------------------------------------
ORG    0x750
A4: 	WORD 	0xA234
B4: 	WORD 	0x0234
RES4:	 WORD ?
TEST4:  CLA
    CLC
    LD A4
    SUB B4
    ST $RES4
	LD B4
    WORD   0x9EF6
	LD      A4
    CMP     RES4
    BNE     ERR4

    LD      #0x1
    ST      $TT4
    RET
ERR4: LD      #0x0
    ST 	$TT4
    RET
;---------------------------------------------------------
ORG    0x7A0
A5: 	WORD 	0x0031
B5:     WORD 0x0033
RES5:	 WORD ?
TEST5:  CLA
    CLC
    LD B5
    SUB A5
    ST $RES5
    RET
;--------------------------------------------------------
ORG    0x7C0
A6: 	WORD 	0x0000
B6: 	WORD 	0xFFFF
RES6:	 WORD ?
TEST6:  CLA
    CLC
    CMC
    LD  B6
    SUB (MEM)
    ST $RES6
	LD A6
    WORD   0x9805
	LD      B6
    CMP     RES6
    BNE     ERR6

    LD      #0x1
    ST      $TT6
    RET
ERR6: LD      #0x0
    ST 	$TT6
    RET
MEM: WORD 0x07C0
