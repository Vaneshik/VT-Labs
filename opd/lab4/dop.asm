ORG 0x10
vals_address: WORD $vals
funcs_address: WORD $funcs
vals_length: WORD 0x0004
func_length: WORD 0x0004

start:
    CLA
    
    LD vals_address
    PUSH ; push vals address
    LD funcs_address
    PUSH ; push funcs address
    
    CALL map
    
    POP
    POP
    
    HLT

ORG 0x50
map: ; &1 = funcs_address, &2 = vals_address
    current_val_address: WORD ?
    LD &2
    ST current_val_address 
 
 
    outer_loop:
        current_func_address: WORD ?
        LD &1
        ST current_func_address
        
        inner_loop_counter: WORD ?
        LD func_length
        ST inner_loop_counter

        inner_loop: 
            LD (current_val_address)
            PUSH

            func_address: WORD ?        ; \
            LD (current_func_address)+  ;  - set call address
            ST func_address            ; /
            
            CALL (func_address)

            POP ; get f(x)
            ST (current_val_address)

            LOOP $inner_loop_counter
            JUMP inner_loop

    LD current_val_address  ; \
    INC                     ;  - current_val_address++
    ST current_val_address ; /


    LOOP $vals_length
    JUMP outer_loop
    RET

ORG 0x100
dec_var: 
    LD &1
    DEC
    ST &1
    RET

ORG 0x130
inc_var:
    LD &1
    INC
    ST &1
    RET

ORG 0x160
mul_2:
    LD &1
    ROL ; x * 2
    ST &1
    RET

ORG 0x200
vals: WORD 0x000A, 0x000C, 0x0007, 0x0003

ORG 0x300
funcs: WORD $mul_2, $mul_2, $mul_2, $inc_var
