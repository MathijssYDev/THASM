global start

loadsize 30
stack 0

pointer @Num1 = 2343
pointer @Num2 = 532


function start
    lda.boot > @Num1.LOW
    add.ram > @Num2.LOW
    lda.boot > @Num1.HIGH
    adc.ram > @Num2.HIGH
    sta.ram * 0x00ff
end
