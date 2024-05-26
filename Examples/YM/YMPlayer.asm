global setup

size 100
stack 0

byte $register = 0
byte $value = 0

pointer @MusicInfoStartingAddress = 0x0100

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


    jmp * loop
end
function loop
    lda.ram * $register
    sta.ram * 0x8380 // Latch

    sta.ram * 0x8080 // Inactive

    lda.rom *($HIGHAddr,$LOWAddr) @MusicInfoStartingAddress // Value

    sta.ram * 0x8280 // Write

    lda.ram * $LOWAddr
    add.ram > 64
    sta.ram * $LOWAddr

    lda.ram * $HIGHAddr
    adc.ram > 0
    sta.ram * $HIGHAddr

    $returnRegReset
    lda.ram * $register
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0
    cmp.ram > 0

    jme * loop
    jmp * $returnRegReset
    inc
    sta.ram * $register

end

//*
function loop
    sta.ram * 0x8080 // Inactive

    lda.ram * $register
    sta.ram * 0x8380 // Latch

    inc
    sta.ram * $register
    cmp.ram > 15

    jme * RegReset
    $returnRegReset

    sta.ram * 0x8080 // Inactive

    lda.rom *($HIGHAddr,$LOWAddr) @MusicInfoStartingAddress // Value

    sta.ram * 0x8280 // Write

    lda.ram * $LOWAddr
    inc
    sta.ram * $LOWAddr

    lda.ram * $HIGHAddr
    adc.ram > 0
    sta.ram * $HIGHAddr

    jmp * loop
end
function RegReset
    lda.ram > 0
    sta.ram * $register
    jmp * $returnRegReset
end
byte $delay = 0
function delaya
    lda.ram * $delay
    inc
    sta.ram * $delay
    cmp.ram > 10
    jme * loop
end
*//
