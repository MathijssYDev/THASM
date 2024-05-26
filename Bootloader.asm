global setup
size 255
stack 0

byte *0 $StartAddressRAM_HIGH = 0x0
byte *0 $StartAddressRAM_LOW = 0x0

byte *0 $StartAddressROM_HIGH = 0x0
byte *0 $StartAddressROM_LOW = 0x0

byte *0x7ffe $EndAddressROM_HIGH = 0x0
byte *0x7fff $EndAddressROM_LOW = 0xff // 1 higher than is copied

pointer @ROM_ProgramSize_HIGH = 0x0004
pointer @ROM_ProgramSize_LOW = 0x0005

pointer @LoadAddressCommand = 0x7ff0
pointer @LoadAddressHIGH = 0x7ff1
pointer @LoadAddressLOW = 0x7ff2

pointer @SetAddressCommand = 0x7ff3
pointer @SetAddressHIGH = 0x7ff4
pointer @SetAddressLOW = 0x7ff5

pointer @AddressJumpBackCommand = 0x7ff6
pointer @AddressJumpBackHIGH = 0x7ff7
pointer @AddressJumpBackLOW = 0x7ff8

pointer @LoadStore = @LoadAddressCommand
pointer @CNULocation = @SetAddressLOW

function loop
    // ############## COPY VALUE ON ADDRESS FROM ROM TO RAM ##############

    jmp * @LoadStore
    $LoadStoreReturn

    // ############## Increment ROM address LOW ##############
    lda.boot * @LoadAddressLOW
    inc
    sta.boot * @LoadAddressLOW

    // ############## Increment ROM address HIGH ##############
    lda.boot * @LoadAddressHIGH
    adc.ram > 0
    sta.boot * @LoadAddressHIGH

    // ############## Increment RAM address LOW & HIGH ##############
    lda.boot * @SetAddressLOW
    inc
    sta.boot * @SetAddressLOW

    lda.boot * @SetAddressHIGH
    adc.ram > 0
    sta.boot * @SetAddressHIGH

    // ############## Check if end is reached ##############
    lda.boot * @LoadAddressLOW
    cmp.ram * $EndAddressROM_LOW
    jme * overflowLow

    jmp * loop
end

function overflowLow

    lda.boot * @LoadAddressHIGH
    cmp.ram * $EndAddressROM_HIGH
    jmp * done

    jmp * loop
end

function done
    lda.boot > cnu*
    sta.boot * @CNULocation

    lda.boot > 0
    sta.boot * @AddressJumpBackHIGH
    sta.boot * @AddressJumpBackLOW

    jmp * @CNULocation
end

function setup
    // ############## LOAD SIZE ##############

    lda.rom * @ROM_ProgramSize_LOW
    sta.boot * $EndAddressROM_LOW

    sta.ram * 1


    lda.rom * @ROM_ProgramSize_HIGH
    sta.boot * $EndAddressROM_HIGH

    // ############## LOAD INCREMENTING FUNCTION ##############

    // LOAD from ROM
    lda.boot > lda.rom*
    sta.boot * @LoadAddressCommand

    lda.boot > $StartAddressROM_HIGH
    sta.boot * @LoadAddressHIGH

    lda.boot > $StartAddressROM_LOW
    sta.boot * @LoadAddressLOW

    // STORE in RAM
    lda.boot > sta.ram*
    sta.boot * @SetAddressCommand

    lda.boot > $StartAddressROM_HIGH
    sta.boot * @SetAddressHIGH

    lda.boot > $StartAddressROM_LOW
    sta.boot * @SetAddressLOW

    // JUMP BACK
    lda.boot > jmp*
    sta.boot * @AddressJumpBackCommand

    lda.boot > $LoadStoreReturn.HIGH
    sta.boot * @AddressJumpBackHIGH

    lda.boot > $LoadStoreReturn.LOW
    sta.boot * @AddressJumpBackLOW

    // ############## START WITH LOOP ##############

    jmp * loop
end
