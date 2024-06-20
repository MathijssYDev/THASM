package src.Tools.ymToSng;

import src.Tools.FileToROM;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;

public class ymConverter {
    final String[] validHeaders = {"YM6!","YM5!","YM4!","YM3!"};
    final int ROMSize = (int) Math.pow(2,15); // ~32kB ROM

    final byte delayByte = 16;

    final byte bitsPerFrame = 16;
    final byte bitsPerFrameOutput = 14;

    private byte[] PROGRAM;

    private long ymSize = 0;
    private byte[][] frames;

    private int byteOffset = 0;

    private float timePerChange = 0f;
    private float timePerDelay = 0f;
    private float timePerDelayAdditional = 0f;

    public ymConverter(String[] args) throws Exception {

        System.out.println("YM to TMFH - Theo Music Format hex\n by Mathijs Klaver - 04/06/2024 \n");

        if (args.length != 6) throw new Exception("(ymConverter) Missing Arguments! Usage: Input file (String), Output file (String), Bytes from start offset (int), Milliseconds per change, Milliseconds per delay, Milliseconds per delay additional");

        File inFile;
        try {
            inFile = new File(args[0]);
        } catch (Exception e) {
            throw new Exception("(ymConverter) An error occurred while initializing files: " + e.getMessage());
        }
        byteOffset = Integer.parseInt(args[2]);

        timePerChange = Float.parseFloat(args[3]);
        timePerDelay = Float.parseFloat(args[4]);
        timePerDelayAdditional = Float.parseFloat(args[5]);

        ymSize = inFile.length();

        try (DataInputStream ym = new DataInputStream(new FileInputStream(inFile))) {
            String header = readString(ym,4);
            if (!Arrays.asList(validHeaders).contains(header)) throw new Exception("(ymConverter) An error occurred while reading file header: \n The first 4 bytes don't resemble a recognized format! Found: " + header + "\n Expected one of these: " + Arrays.asList(validHeaders).toString() + "\n Check if the YM file is a unpacked file?");
            System.out.println("\t Header valid!");

            String checkString = readString(ym,8);
            if (!checkString.equals("LeOnArD!")) throw new Exception("(ymConverter) An error occurred while reading file checkString: \n checkString Found: '" + checkString + "'\n Expected checkString: 'LeOnArD!'\n Check if this is a valid YM file without corruptions!");
            System.out.println("\t Check string valid!");
            System.out.println();

            int frameCount = ym.readInt();
            int songAttributes = ym.readInt();
            short digiDrumSamples = ym.readShort();
            int ymClock = ym.readInt();
            short playerSpeed = ym.readShort();
            int loopFrame = ym.readInt();

            System.out.println("\t* Number of frames: " + frameCount);
            System.out.println("\t* Song attributes: " + songAttributes);
            System.out.println("\t* Number of digidrum samples: " + digiDrumSamples);
            System.out.println("\t* YM master clock speed: " + ymClock + "hz");
            System.out.println("\t* Player Speed: " + playerSpeed + "hz");
            System.out.println("\t* Loop Frame: " + loopFrame);

            short additionnalDataSize = ym.readShort(); // Size, in bytes, of further additional data.

            String songName = readStringUntil(ym,200);
            String autherName = readStringUntil(ym,200);
            String songComment = readStringUntil(ym,500);

            System.out.println();
            System.out.println("\t* Song Name: " + songName);
            System.out.println("\t* Auther Name: " + autherName);
            System.out.println("\t* Song Comment: " + songComment);

            frames = new byte[frameCount][bitsPerFrameOutput];
            for (int bit = 0; bit < bitsPerFrame; bit++) {
                if (bit >= bitsPerFrameOutput) continue;

                for (int frame = 0; frame < frameCount; frame++) {
                    frames[frame][bit] = ym.readByte();
                }
            }

            PROGRAM = new byte[ROMSize];

            int byteCounter = byteOffset+5;
            int[] previousFrame = new int[14];

            final float requiredTimePerFrame = 1000f /playerSpeed;

            float delay = 0f;
            for (int frame = 0; frame < frameCount; frame++) {
                float TimeOfFrame = 0f;

                int changesThisFrame = 0;
                byte[] currentFrame = frames[frame];
                for (int bit = 0; bit < bitsPerFrameOutput; bit++) {
                    if (frame == 0 || previousFrame[bit] != currentFrame[bit]) {
                        if (byteCounter+16 > PROGRAM.length) continue;

                        PROGRAM[byteCounter] = (byte)bit;
                        byteCounter++;
                        PROGRAM[byteCounter] = currentFrame[bit];
                        byteCounter++;

                        previousFrame[bit] = currentFrame[bit];

                        TimeOfFrame += timePerChange;

                        changesThisFrame++;
                    }
                }

                if (TimeOfFrame > requiredTimePerFrame) continue;

                float extraTimeNeeded = requiredTimePerFrame - TimeOfFrame;
                if (extraTimeNeeded-timePerDelay/2 > requiredTimePerFrame-timePerDelay/2) {
                    PROGRAM[byteCounter] = delayByte;
                    byteCounter++;
                    PROGRAM[byteCounter] = 0;
                    byteCounter++;
                    continue;
                }
                delay += extraTimeNeeded/timePerDelayAdditional;
                boolean nextFrameChanges = frame+1==frames.length?false:!Arrays.equals(frames[frame+1],frames[frame]);
                if (delay > 255 || nextFrameChanges) {
                    if (byteCounter+16 > PROGRAM.length) continue;

                    if (delay > 255) {
                        PROGRAM[byteCounter] = delayByte;
                        byteCounter++;
                        PROGRAM[byteCounter] = (byte) (255 - timePerDelay);
                        byteCounter++;

                        delay-=255;
                    }
                    if (nextFrameChanges) {
                        PROGRAM[byteCounter] = delayByte;
                        byteCounter++;
                        PROGRAM[byteCounter] = (byte) delay;
                        byteCounter++;

                        delay = 0;
                    }
                }


            }
            System.out.println("\t* YM file size: " + ymSize + " bytes");
            System.out.println("\t* TMFH file size: " + byteCounter + " bytes");
        }
        try {
            FileWriter myWriter = new FileWriter(args[1]);
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
            System.out.println("An error occurred");
            e.printStackTrace();
        }
    }

    public byte[] getProgram() {
        return PROGRAM;
    }

    public long getYmSize() {
        return ymSize;
    }

    private static String readString(DataInputStream ym, int length) throws Exception{
        byte[] headerBytes = new byte[length];
        ym.read(headerBytes);
        return new String(headerBytes, StandardCharsets.UTF_8);
    }
    private static String readStringUntil(DataInputStream ym, int bufferSize) throws Exception{
        StringBuilder string = new StringBuilder();
        for(int i = 0; i < bufferSize; i++) {
            byte read = ym.readByte();
            if (read == 0) return string.toString();

            string.append((char) read);
        }

        return string.toString();
    }

    public static void main(String args[]) throws Exception {
        ymConverter con = new ymConverter(args);


        System.out.println("Conversion Done! File Location: " + args[1]);
    }

}
