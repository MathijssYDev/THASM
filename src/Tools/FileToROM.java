package src.Tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.MissingFormatArgumentException;

public class FileToROM {
    File inFile;
    String outFile;

    int FileSize;
    int StartByte;

    byte[] PROGRAM;

    int sizeBytes = 0;

    public FileToROM(File inFile, String outFile, int ROMSize, int StartByte) throws Exception{
        this.inFile = inFile;
        this.outFile = outFile;
        this.sizeBytes = ROMSize;
        this.StartByte = StartByte;

        PROGRAM = new byte[ROMSize];

        try (DataInputStream dis = new DataInputStream(new FileInputStream(inFile))) {
            int byteTracker = StartByte;

            while (dis.available() > 0) {
                PROGRAM[byteTracker] = dis.readByte();
                byteTracker++;
            }

            FileSize = byteTracker;
        }
        try {
            FileWriter myWriter = new FileWriter(outFile);
            int prevRow = 0;
            StringBuilder line = new StringBuilder();

            for (int Address = 0; Address < ROMSize; Address++) {
                int Row = Address/16;
                if (prevRow != Row) {
                    myWriter.write( line + "\n");
//                    myWriter.write("0x"+String.format("%04X", Address-16).toUpperCase().substring(0, 3)+"0\t" + line + "\n");
                    line = new StringBuilder();
                }
                line.append("0x"+String.format("%02X",PROGRAM[Address]& 0xFF)).append("\t");
                prevRow = Row;
            }

            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRomSizeBytes() {
        return sizeBytes;
    }
    public int getFileSizeBytes() {
        return FileSize;
    }

    public int getStartByte() {
        return StartByte;
    }

    public static void main(String args[]) throws Exception {
        if (args.length != 4) throw new MissingFormatArgumentException("Missing Arguments! Usage: Input file (String), Output file (String), Rom Size (int), Rom Start Byte (int)");

        File inFile = new File(args[0]);

        int ROMSize = Integer.parseInt(args[2]);
        int StartByte = Integer.parseInt(args[3]);

        FileToROM filetorom = new FileToROM(inFile,args[1],ROMSize, StartByte);

        System.out.println("FileToROM (THEOX)\n by Mathijs Klaver - 26/05/2024 \n");

        System.out.println("\t* ROM size: " + filetorom.getRomSizeBytes() + " bytes");
        System.out.println("\t* File size: " + filetorom.getFileSizeBytes()+ " bytes");
        System.out.println();
        System.out.println("\t* Start byte: " + filetorom.getStartByte()+ " bytes");
        System.out.println();

        System.out.println("Done! File Location: " + args[1]);
    }
}
