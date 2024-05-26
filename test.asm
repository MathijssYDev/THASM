global setup

size 150
stack 0

function setup
    lda.ram > 7
    sta.ram * 0x8080 // Inactive
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram > 0b00111110
    sta.ram * 0x8280 // Write
    sta.ram * 0x8080 // Inactive

    lda.ram > 8
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram > 0b00001111
    sta.ram * 0x8280 // Write
    sta.ram * 0x8080 // Inactive

    lda.ram > 1
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram > 1
    sta.ram * 0x8280 // Write
    sta.ram * 0x8080 // Inactive

    lda.ram > 0
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram > 223
    sta.ram * 0x8280 // Write
    sta.ram * 0x8080 // Inactive

    brk
    jmp * loop
end
byte $delay = 0
byte $frequency = 0
function loop
    lda.ram > 1
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram > 0
    sta.ram * 0x8280 // Write
    sta.ram * 0x8080 // Inactive

    lda.ram > 0
    sta.ram * $delay
    sta.ram * 0x8380 // Latch
    sta.ram * 0x8080 // Inactive
    lda.ram * $frequency
    sta.ram * 0x8280 // Write
    sub.ram > 15
    sta.ram * $frequency

    sta.ram * 0x8080 // Inactive

    jmp * loop
end

function delay
    lda.ram * $delay
    inc
    sta.ram * $delay
    cmp.ram > 10
    jme * loop
    jmp * delay
end