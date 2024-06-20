global loop

size 100
stack 0

pointer @PS2_REG1 = 0xA100
pointer @PS2_REG2 = 0xA200
pointer @PS2_REG3 = 0xA400

byte $lastKey = 0

function loop
    // # CHECK IF REG2 IS EQUAL TO RELEASE
    lda.ram * @PS2_REG2
    cmp.ram > 0xF0
    jme * release // # IF SO, RELEASE KEY
    
    lda.ram * $lastKey
    cmp.ram > 0
    jme * keypress
    
    jmp * loop
end
function keypress
    lda.ram * @PS2_REG2
    cmp.ram * 0xF0
    jme * loop
    
    // # ELSE, SET LAST KEY TO CURRENT KEY
    lda.ram * @PS2_REG1
    sta.ram * $lastKey
    
    // # SET ASCII_L TO CURRENT CHARACTER TO SET ASCII BYTE ADDRESS
    sta.ram * $ASCII_L
    
    // # STORE ASCII KEYCODE IN RAM
    lda.rom *($ASCII_H,$ASCII_L) 0x0100
    sta.ram *($PS2_H,$PS2_L) 0x0100
    
    // # INCREMENT RAM POSITION
    lda.ram * $PS2_L
    inc
    sta.ram * $PS2_L
    
    jmp * loop
end
function release
    lda.ram > 0
    sta.ram * $lastKey
    
    jmp * loop
end
