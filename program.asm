global program

size 100
stack 0

pointer @valueRamAddress = 0x00ff

byte $value = 0 // # Starting value, address 0x00FF (255)
byte *0 $valueToAdd = 3
byte *0 $maxValue = 244 // # Value to halt at

function program 
    // # Setup of value > Copy it to RAM
    lda.boot * $value
    sta.ram * @valueRamAddress

    // # Increment 
    jmp * increment
    $incrementReturn
    
    // # If value >= maxValue, halt the cpu
    cmp.ram > $maxValue
    jme * break
    jmo * break
    
    jmp * loop
end
function increment
    // # Add valueToAdd and store to ram.
    add.ram > $valueToAdd
    sta.ram * @valueRamAddress

    jmp * $incrementReturn
end

function break
    brk
end