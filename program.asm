global setup

size 200
stack 0

pointer @MusicInfoStartingAddress = 261 // 256 + 5
pointer @MusicInfoStartingAddressValue = 262 // 256 + 1 + 5

pointer @Inactive = 0x8080
pointer @Write = 0x8280
pointer @Latch = 0x8380

function setup
    jmp * loop
end
function loop
    lda.rom *($HIGHAddr_Register,$LOWAddr_Register) @MusicInfoStartingAddress // Register
    cmp.ram > 0x0E
    jme * incrment
    $returnA
    cmp.ram > 16
    jme * delay

    sta.ram * @Latch
    //sta.ram * @Inactive
    lda.rom *($HIGHAddr_Value,$LOWAddr_Value) @MusicInfoStartingAddressValue // Value
    sta.ram * @Write
    //sta.ram * @Inactive

    $AfterDelay

    lda.ram * $LOWAddr_Value
    add.ram > 2
    sta.ram * $LOWAddr_Value
    lda.ram * $HIGHAddr_Value
    adc.ram > 0
    sta.ram * $HIGHAddr_Value

    lda.ram * $LOWAddr_Register
    add.ram > 2
    sta.ram * $LOWAddr_Register
    lda.ram * $HIGHAddr_Register
    adc.ram > 0
    sta.ram * $HIGHAddr_Register

    jmp * loop
end
function incrment
    lda.ram * $LOWAddr_Value
    add.ram > 1
    sta.ram * $LOWAddr_Value
    lda.ram * $HIGHAddr_Value
    adc.ram > 0
    sta.ram * $HIGHAddr_Value
    
    lda.ram * $LOWAddr_Register
    add.ram > 1
    sta.ram * $LOWAddr_Register
    lda.ram * $HIGHAddr_Register
    adc.ram > 0
    sta.ram * $HIGHAddr_Register
    jmp * $returnA
end
byte $delayCountdown = 0
function delay

    lda.rom *($HIGHAddr_Register,$LOWAddr_Register) @MusicInfoStartingAddress

    lda.ram * $LOWAddr_Value
    add.ram > 2
    sta.ram * $LOWAddr_Value
    lda.ram * $HIGHAddr_Value
    adc.ram > 0
    sta.ram * $HIGHAddr_Value

    lda.ram * $LOWAddr_Register
    add.ram > 2
    sta.ram * $LOWAddr_Register
    lda.ram * $HIGHAddr_Register
    adc.ram > 0
    sta.ram * $HIGHAddr_Register
    sta.ram * $delayCountdown

    $delayRound
    lda.ram * $delayCountdown
    dec
    sta.ram * $delayCountdown

    cmp.ram > 0
    jme * $AfterDelay

    jmp * $delayRound
end