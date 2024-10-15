package src.Tools;

import javax.imageio.IIOException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class PS2toASCII {
    static char[] table = new char[255];

    public static void main(String[] args) throws  Exception{
//        table[Integer.parseInt("16",16)] = '1';
//        table[Integer.parseInt("1E",16)] = '2';
//        table[Integer.parseInt("26",16)] = '3';
//        table[Integer.parseInt("25",16)] = '4';
//        table[Integer.parseInt("2E",16)] = '5';
//        table[Integer.parseInt("46",16)] = '6';
//        table[Integer.parseInt("3D",16)] = '7';
//        table[Integer.parseInt("3E",16)] = '8';
//        table[Integer.parseInt("46",16)] = '9';
//        table[Integer.parseInt("45",16)] = '0';
//
//        table[Integer.parseInt("15",16)] = 'Q';
//        table[Integer.parseInt("1D",16)] = 'W';
//        table[Integer.parseInt("24",16)] = 'E';
//        table[Integer.parseInt("2D",16)] = 'R';
//        table[Integer.parseInt("2C",16)] = 'T';
//        table[Integer.parseInt("35",16)] = 'Y';
//        table[Integer.parseInt("3C",16)] = 'U';
//        table[Integer.parseInt("43",16)] = 'I';
//        table[Integer.parseInt("44",16)] = 'O';
//        table[Integer.parseInt("4D",16)] = 'P';
//
//        table[Integer.parseInt("54",16)] = '[';
//        table[Integer.parseInt("5B",16)] = ']';
//        table[Integer.parseInt("5D",16)] = '\\';
//
//        table[Integer.parseInt("1C",16)] = 'A';
//        table[Integer.parseInt("1B",16)] = 'S';
//        table[Integer.parseInt("23",16)] = 'D';
//        table[Integer.parseInt("2B",16)] = 'F';
//        table[Integer.parseInt("34",16)] = 'G';
//        table[Integer.parseInt("33",16)] = 'H';
//        table[Integer.parseInt("3B",16)] = 'J';
//        table[Integer.parseInt("42",16)] = 'K';
//        table[Integer.parseInt("4B",16)] = 'L';
//
//        table[Integer.parseInt("4C",16)] = ';';
//        table[Integer.parseInt("52",16)] = '\'';
//        table[Integer.parseInt("5A",16)] = 0x0A;
//
//        table[Integer.parseInt("1A",16)] = 'Z';
//        table[Integer.parseInt("22",16)] = 'X';
//        table[Integer.parseInt("21",16)] = 'C';
//        table[Integer.parseInt("2A",16)] = 'V';
//        table[Integer.parseInt("32",16)] = 'B';
//        table[Integer.parseInt("31",16)] = 'N';
//        table[Integer.parseInt("3A",16)] = 'M';
//
//        table[Integer.parseInt("29",16)] = ' ';
//        table[Integer.parseInt("76",16)] = 0x1B;
//        table[Integer.parseInt("71",16)] = 0x7F;
//
//        table[Integer.parseInt("4A",16)] = '/';
//        table[Integer.parseInt("7C",16)] = '*';
//        table[Integer.parseInt("7B",16)] = '-';
//        table[Integer.parseInt("41",16)] = ',';
//        table[Integer.parseInt("49",16)] = '.';
//        table[Integer.parseInt("79",16)] = '+';
        table[Integer.parseInt("16",16)] = 17; //1
        table[Integer.parseInt("1E",16)] = 18; //2
        table[Integer.parseInt("26",16)] = 19; //3
        table[Integer.parseInt("25",16)] = 20; //4
        table[Integer.parseInt("2E",16)] = 21; //5
        table[Integer.parseInt("36",16)] = 22; //6
        table[Integer.parseInt("3D",16)] = 23; //7
        table[Integer.parseInt("3E",16)] = 24; //8
        table[Integer.parseInt("46",16)] = 25; //9
        table[Integer.parseInt("45",16)] = 16; //0

        table[Integer.parseInt("15",16)] = 49; //Q
        table[Integer.parseInt("1D",16)] = 55; //W
        table[Integer.parseInt("24",16)] = 37; //E
        table[Integer.parseInt("2D",16)] = 50;//R
        table[Integer.parseInt("2C",16)] = 52;//T
        table[Integer.parseInt("35",16)] = 57;//Y
        table[Integer.parseInt("3C",16)] = 53;//U
        table[Integer.parseInt("43",16)] = 41;//I
        table[Integer.parseInt("44",16)] = 47;//O
        table[Integer.parseInt("4D",16)] = 48;//O

        table[Integer.parseInt("54",16)] = 59; // [
        table[Integer.parseInt("5B",16)] = 61; // ]
        table[Integer.parseInt("5D",16)] = 60; // \
        System.out.println(Integer.parseInt("1C",16));
        table[Integer.parseInt("1C",16)] = 33; //A
        table[Integer.parseInt("1B",16)] = 51;//S
        table[Integer.parseInt("23",16)] = 36;//D
        table[Integer.parseInt("2B",16)] = 38;//F
        table[Integer.parseInt("34",16)] = 39;//G
        table[Integer.parseInt("33",16)] = 40;//H
        table[Integer.parseInt("3B",16)] = 42;//J
        table[Integer.parseInt("42",16)] = 43;//K
        table[Integer.parseInt("4B",16)] = 44;//L

        table[Integer.parseInt("4C",16)] = 26; // ;
        table[Integer.parseInt("52",16)] = 7; // '
        table[Integer.parseInt("5A",16)] = 0x0A;

        table[Integer.parseInt("1A",16)] = 58; //Z
        table[Integer.parseInt("22",16)] = 56;//X
        table[Integer.parseInt("21",16)] = 35;//C
        table[Integer.parseInt("2A",16)] = 54;//V
        table[Integer.parseInt("32",16)] = 34;//B
        table[Integer.parseInt("31",16)] = 46;//N
        table[Integer.parseInt("3A",16)] = 45;//M

        table[Integer.parseInt("29",16)] = 0;// ' '
        table[Integer.parseInt("76",16)] = 0x1B;
        table[Integer.parseInt("71",16)] = 0x7F;

        table[Integer.parseInt("4A",16)] = 27; // /
        table[Integer.parseInt("7C",16)] = 10; // *
        table[Integer.parseInt("4E",16)] = 13; // -
        table[Integer.parseInt("41",16)] = 12; // ,
        table[Integer.parseInt("49",16)] = 14; // .
        table[Integer.parseInt("55",16)] = 11; // +
        try {
            FileWriter myWriter = new FileWriter("Examples/Programs/TMS9918_CharacterTable.txt");
            for(char c:table) {
                System.out.print(c);
                myWriter.write(c);
            }
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
