ORG 0x1E0
test1_n1:   WORD 0x1234
test1_n2:   WORD 0x0007
test1_ans: WORD 0x0000

test2_n1:   WORD 0x1717
test2_n2:   WORD 0x1000
test2_ans: WORD 0x0000

test3_n1:   WORD 0x7FFF
test3_n2:   WORD 0x1234
test3_ans: WORD 0x0000
test3_of1: WORD 0x0000
test3_of2: WORD 0x0000

test1_res:  WORD ?
test2_res:  WORD ?
test3_res:  WORD ?

ORG 0x1F6
start:  CLA
       LD $test1_n1
       ADD $test1_n2
       ST $test1_ans


       LD $test1_n1
       WORD 0x91E1; MADC $test1_n2
      
       LD $test1_ans
       CMP $test1_n2
       BEQ test1_p
       JUMP test1_f


test1_p:    LD #0x1
   ST $test1_res
   JUMP test_2
test1_f:    CLA
   ST $test1_res
   JUMP test_2


test_2: LD $test2_n1
       ADD $test2_n2
       INC
       ST $test2_ans


       CLC
       CMC
       LD $test2_n1
       WORD 0x91E4; MADC $test2_n2


       LD $test2_ans
       CMP $test2_n2
       BEQ test2_p
       JUMP test2_f


test2_f:    CLA
   ST $test2_res
   JUMP test_3
test2_p:    LD #0x1
   ST $test2_res
   JUMP test_3


test_3: LD $test3_n1
       ADD $test3_n2
       INC
       ST $test3_ans
       BVC SKIP1
       LD $test3_of1
       INC
       ST $test3_of1


       SKIP1: NOP


       CLC
       CMC
       LD $test3_n1
       WORD 0x91E7; MADC $test3_n2


       BVC SKIP2
       LD $test3_of2
       INC
       ST $test3_of2


       SKIP2: NOP
      
       LD $test3_of1
       CMP $test3_of2
       BNE test3_p
       JUMP test3_f


test3_f:    CLA
   ST $test3_res
   JUMP main
test3_p:    LD #0x1
   ST $test3_res
   JUMP main
          
main:   LD $test1_res
   AND $test2_res
   AND $test3_res
   CMP #0x1
   BEQ success
   LD #0xFF
   HLT
success:    LD #0x1
   HLT