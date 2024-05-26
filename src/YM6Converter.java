package src;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class YM6Converter {
    private String songName;
    private String authorName;
    private String comment;
    private int frameCount;
    private int ChangesInRegisters;
    private int[][] frames;

    public YM6Converter(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {

            // Check file signature
            byte[] signatureBytes = new byte[4];
            dis.readFully(signatureBytes);
            String signature = new String(signatureBytes);
            if (!"YM6!".equals(signature)) {
                throw new IOException("Unsupported file format");
            }

            byte[] CheckStringBytes = new byte[8];
            dis.readFully(CheckStringBytes);
            String CheckString = new String(CheckStringBytes);
            if (!"LeOnArD!".equals(CheckString)) {
                throw new IOException("Unsupported File, LeOnArD! not correct");
            }

            this.frameCount = readIntLE(dis);
            dis.skipBytes(18);

            this.songName = readNullTerminatedString(dis);
            this.authorName = readNullTerminatedString(dis);
            this.comment = readNullTerminatedString(dis);

            // Skip more bytes (dumper name, dumper comment)
            dis.skipBytes(2);

            // Read the frame data
            this.frames = new int[frameCount][16];
            for (int j = 0; j < 16; j++) {
                int prevBit = -1;
                for (int i = 0; i < frameCount; i++) {
                    frames[i][j] = dis.readUnsignedByte();

//                    if (frames[i][j] != prevBit) {
//                        ChangesInRegisters++;
//                        prevBit = frames[i][j];
//                    } else {
//                        frames[i][j] = -1;
//                    }
                }
            }
            int[] def = new int[]{0,0,0,0,0,0,0,15,0,0,0,0,0,0,0,0};
            for(int i = 0; i < 500; i++) {
                int[] frame = frames[i];
                for (int j = 0; j < 16; j++) {
                    if (j == 0 || j == 8) {
                        System.out.print("0x"+String.format("%02X",frame[j]& 0xFF)+"\t");
                    } else {
                        System.out.print("0x"+String.format("%02X",def[j]& 0xFF)+"\t");
                    }
                }
                System.out.println();
            }
        }
    }

    private String readNullTerminatedString(DataInputStream dis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b;
        while ((b = dis.readByte()) != 0) {
            baos.write(b);
        }
        return baos.toString("UTF-8");
    }

    private int readIntLE(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[4];
        dis.readFully(bytes);
        return ByteBuffer.wrap(bytes).getInt();
    }

    private int readShortLE(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[2];
        dis.readFully(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
    }

    public String getSongName() {
        return songName;
    }
    public int getChangesInRegisters() {
        return ChangesInRegisters;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getComment() {
        return comment;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int[][] getFrames() {
        return frames;
    }
    public static void main(String args[]) throws Exception {
        File ymFile = new File("Examples/YM/YM_006A.BIN");
        YM6Converter ym = new YM6Converter(ymFile);

        System.out.println("Compiling: " + ym.getSongName() + " by " + ym.getAuthorName());
        System.out.println("\t* Comment: " + ym.getComment());
        System.out.println("\t* Frames: " + ym.getFrameCount());
        System.out.println("\t* Changes in registers: " + ym.getChangesInRegisters());
        System.out.println("\t* Efficiency: " + (100f-((int)(ym.getChangesInRegisters()/(ym.getFrameCount()*14f)*1000))/10f) + "%");

    }
}