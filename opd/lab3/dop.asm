ORG 0x10
vals_address: WORD $vals
vals_length: WORD 0x14
tmp: WORD ?

start:
    CLA
    
    outer_loop_count: WORD ?    ;
    LD vals_length  ; set i = len
    ST outer_loop_count         ;

    current_val_address: WORD ?
    inner_loop_count: WORD ?
    i: WORD 0x0

    outer_loop:
        LD vals_address
        ST current_val_address  ; set current_val_address = 0 index
        
        LD vals_length      ;
        SUB i  
        DEC                 ; set j = len - i - 1
        ST inner_loop_count ;
        
        inner_loop:
            LD (current_val_address)+ ; +1
            CMP (current_val_address)
            BLT continue

            LD (current_val_address)
            ST tmp ; tmp = arr[i+1]
            LD -(current_val_address) ; acc = arr[i]

            SWAM tmp; tmp, acc = arr[i], arr[i+1]

            ST (current_val_address)+ 
            LD tmp
            ST (current_val_address) ; arr[i+1] = arr[i]

            continue: LOOP inner_loop_count
            JUMP inner_loop

        LD i
        INC
        ST i

        LOOP outer_loop_count
        JUMP outer_loop

    exit: HLT

ORG 0x100
vals: WORD 0x7, 0x6, 0x5, 0x4, 0x3, 0x2, 0x1, 0x30, 0x10, 0x11, 0x15, 0x14, 0x12, 0x13, 0x20, 0x40, 0xFFFF, 0xFFEE, 0xFFDD, 0x80