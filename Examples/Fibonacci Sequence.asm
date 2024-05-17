global loop

size 75
stack 0

byte $currNum = 0
byte $prevNum = 1
byte $temp = 0

byte $depth_HIGH = 5
byte $depth_LOW = 0

pointer @LocationStart = 0x0100

function loop
    sta.ram *($Location_HIGH,$Location_LOW) @LocationStart

    lda.ram * $Location_LOW

    cmp.ram * $depth_LOW
    jme * check
    $returncheck
    inc

    sta.ram * $Location_LOW

    lda.ram * $Location_HIGH
    adc.ram > 0
    sta.ram * $Location_HIGH


    lda.ram * $currNum
    add.ram * $prevNum
    sta.ram * $temp

    lda.ram * $prevNum
    sta.ram * $currNum

    lda.ram * $temp
    sta.ram * $prevNum

    jmp * loop
end
function check
    lda.ram * $Location_HIGH
    cmp.ram * $depth_HIGH
    jme * done
    jmp * $returncheck
end
function done
    brk
end