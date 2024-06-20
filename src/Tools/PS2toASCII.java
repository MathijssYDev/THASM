package src.Tools;

import java.util.ArrayList;

public class PS2toASCII {
    static char[] table = new char[255];

    public static void main(String[] args) {
        table[Integer.parseInt("16",16)] = '1';
        table[Integer.parseInt("1E",16)] = '2';
        table[Integer.parseInt("26",16)] = '3';
        table[Integer.parseInt("25",16)] = '4';
        table[Integer.parseInt("2E",16)] = '5';
        table[Integer.parseInt("46",16)] = '6';
        table[Integer.parseInt("3D",16)] = '7';
        table[Integer.parseInt("3E",16)] = '8';
        table[Integer.parseInt("46",16)] = '9';
        table[Integer.parseInt("45",16)] = '0';

        table[Integer.parseInt("15",16)] = 'Q';
        table[Integer.parseInt("1D",16)] = 'W';
        table[Integer.parseInt("24",16)] = 'E';
        table[Integer.parseInt("2D",16)] = 'R';
        table[Integer.parseInt("2C",16)] = 'T';
        table[Integer.parseInt("35",16)] = 'Y';
        table[Integer.parseInt("3C",16)] = 'U';
        table[Integer.parseInt("43",16)] = 'I';
        table[Integer.parseInt("44",16)] = 'O';
        table[Integer.parseInt("4D",16)] = 'P';

        table[Integer.parseInt("54",16)] = '[';
        table[Integer.parseInt("5B",16)] = ']';
        table[Integer.parseInt("5D",16)] = '\\';

        table[Integer.parseInt("1C",16)] = 'A';
        table[Integer.parseInt("1B",16)] = 'S';
        table[Integer.parseInt("23",16)] = 'D';
        table[Integer.parseInt("2B",16)] = 'F';
        table[Integer.parseInt("34",16)] = 'G';
        table[Integer.parseInt("33",16)] = 'H';
        table[Integer.parseInt("3B",16)] = 'J';
        table[Integer.parseInt("42",16)] = 'K';
        table[Integer.parseInt("4B",16)] = 'L';

        table[Integer.parseInt("4C",16)] = ';';
        table[Integer.parseInt("52",16)] = '\'';
        table[Integer.parseInt("5A",16)] = 0x0A;

        table[Integer.parseInt("1A",16)] = 'Z';
        table[Integer.parseInt("22",16)] = 'X';
        table[Integer.parseInt("21",16)] = 'C';
        table[Integer.parseInt("2A",16)] = 'V';
        table[Integer.parseInt("32",16)] = 'B';
        table[Integer.parseInt("31",16)] = 'N';
        table[Integer.parseInt("3A",16)] = 'M';

        table[Integer.parseInt("29",16)] = ' ';
        table[Integer.parseInt("76",16)] = 0x1B;
        table[Integer.parseInt("71",16)] = 0x7F;

        table[Integer.parseInt("4A",16)] = '/';
        table[Integer.parseInt("7C",16)] = '*';
        table[Integer.parseInt("7B",16)] = '-';
        table[Integer.parseInt("41",16)] = ',';
        table[Integer.parseInt("49",16)] = '.';
        table[Integer.parseInt("79",16)] = '+';

        for(int i = 0; i < table.length; i++) {
            System.out.print(table[i]);
        }
    }
}
