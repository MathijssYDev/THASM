global loop

size 120
stack 0

byte $index = 0
byte $size = 255
byte $changeCount = 0
byte $nextNumber = 0
byte $currNumber = 0

pointer @StartPosition_Ordered = 0x0100
pointer @StartPosition_Ordered_add1 = 0x0101

function loop
    lda.ram *($currNumberHIGH,$currNumberLOW) @StartPosition_Ordered
    sta.ram * $currNumber

    sta.ram * 0x00fe

    lda.ram *($nextNumberHIGH,$nextNumberLOW) @StartPosition_Ordered
    sta.ram * $nextNumber

    sta.ram * 0x00ff

    cmp.ram * $currNumber
    jmo * Swap
    $Swap_Return


    // Increment Index
    lda.ram * $index
    inc
    sta.ram * $currNumberLOW
    sta.ram * $index
    inc
    sta.ram * $nextNumberLOW

    lda.ram * $index
    cmp.ram > $size
    jme * check
    jmp * loop
end
function check
    lda.ram * $changeCount
    cmp.ram > 0
    jme * done
    jmp * loop
end
function done
    brk
end
function Swap
    lda.ram * $changeCount
    inc
    sta.ram * $changeCount

    lda.ram * $currNumber
    sta.ram *($nextNumberHIGH2,$nextNumberLOW2) @StartPosition_make aOrdered_add1

    lda.ram * $nextNumberLOW2
    inc
    sta.ram * $nextNumberLOW2

    lda.ram * $nextNumber
    sta.ram *($currNumberHIGH2,$currNumberLOW2) @StartPosition_Ordered

    lda.ram * $currNumberLOW2
    inc
    sta.ram * $currNumberLOW2

    jmp * $Swap_Return
end
