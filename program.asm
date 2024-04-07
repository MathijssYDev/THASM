byte $x = 15
byte $y = 0b110

global start
function start
    lda.ram > $y // Direct Start value assign (LDA, 10)
    lda.ram > 0xF0

    lda.boot * $y // Static Pointer to address. (LDA, 00,01)

    //* Static Pointer

        lda * $y

        'LDA' -> Function
        '*' -> Pointer Indentivation
        '$y' -> Pointer Address ( Must be reference to variable or address by "0xFFFF")
    *//

    lda.boot *(M,L) $y // Editable Pointer to address.

    //* Dynamic Pointer

        lda *(M,L) $y

        'LDA' -> Function
        '*' -> Pointer Indentivation
        'M' -> Most significant byte variable name
        'L' -> Least significant byte variable name
        '$y' -> Default pointer ( Must be reference to variable or address by "0xFFFF")
    *//
end