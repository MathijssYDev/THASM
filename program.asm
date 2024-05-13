global start

size 15
stack 0

function start
    lda.ram > 0xff
    sta.ram * 0x00ff
    brk
end
