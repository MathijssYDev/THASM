global setup

size 200
stack 0

pointer @MusicInfoStarting_Register = 261 // 256 + 5
pointer @MusicInfoStarting_Value = 262 // 256 + 1 + 5

pointer @Inactive = 0x8080
pointer @Write = 0x8280
pointer @Latch = 0x8380

byte $LOW_Value_FOR_DELAY = 0
byte $HIGH_Value_FOR_DELAY = 0

function setup
    jmp * loop
end
byte $delay = 0
function loop
    sta.ram * @Inactive
    lda.rom *($HIGH_Register,$LOW_Register) @MusicInfoStarting_Register // # Load register to delay or latch PSG

    cmp.ram > 0x10 // # Check if register equals 16, if so -> Delay
    jme * delay

    sta.ram * @Latch

    sta.ram * @Inactive

    lda.rom *($HIGH_Value,$LOW_Value) @MusicInfoStarting_Value // # Load value or write to PSG
    sta.ram * @Write

    $AfterDelay // # Return after delay to increment

    // ############ INCREMENT ############
    lda.ram * $LOW_Register
    add.ram > 2
    sta.ram * $LOW_Register
    lda.ram * $HIGH_Register
    adc.ram > 0
    sta.ram * $HIGH_Register

    lda.ram * $LOW_Value
    add.ram > 2
    sta.ram * $LOW_Value
    sta.ram * $LOW_Value_FOR_DELAY
    lda.ram * $HIGH_Value
    adc.ram > 0
    sta.ram * $HIGH_Value
    sta.ram * $HIGH_Value_FOR_DELAY
      
    jmp * loop // # Return for new byte / frame
end
byte $delayCounter = 0
byte $second = 0
function delayExitHandle
    lda.ram > 0
    sta.ram * $second
    jmp * $AfterDelay
end
function delay
    lda.rom *($HIGH_Value_Delay,$LOW_Value_Delay) 0 // # Load delay value
    
    lda.ram * $second
    cmp.ram > 0
    jme * delayLoop
    
    lda.ram > 1
    sta.ram * $second
    jmp * delay
end
function delayLoop
    dec
    dec
    inc
    cmp.ram > 0
    jme * delayExitHandle

    jmp * delayLoop
end