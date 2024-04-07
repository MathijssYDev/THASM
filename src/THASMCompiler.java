package src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.io.FileWriter;

public class THASMCompiler {
    public static HashMap<String, String[]> Functionality = new HashMap<String, String[]>();
    public static final int MinProgramAddresses = 0;
    public static final int MaxProgramAddresses = 32768;
    public static final int ReservedSpotsFront = 3;
    public static HashMap<String, Object[]> Variables = new HashMap<String, Object[]>();
    public static HashMap<String, Object[]> Functions = new HashMap<String, Object[]>();
    public static String globalReference = "";
    public static int globalLine = 0;
    public static HashMap<String, String> codeblocksNameReference = new HashMap<String, String>();
    public static HashMap<String, String[]> codeblocks = new HashMap<String, String[]>();
    public static int currentAddress = 0;
    public static byte[] PROGRAM = new byte[MaxProgramAddresses];
    public static void main(String[] args) {
        Functionality.put("lda.ram",new String[]{"0x26","0x36","AV","A"});
        Functionality.put("lda.rom",new String[]{"0xA6","0xB6","AV","A"});
        Functionality.put("lda.boot",new String[]{"0x66","0x76","AV","A"});
        Functionality.put("sta.ram",new String[]{"0x06","0x06","A","A"});
        Functionality.put("sta.rom",new String[]{"0x86","0x86","A","A"});
        Functionality.put("nop",new String[]{"0x00","0x00"});
        Functionality.put("cnu",new String[]{"0x10","0x10"});
        if (args.length != 2) {
            System.err.println("Missing argument: file location, output location");
            return;
        }
        try {
            FileReader fileReader = new FileReader(args[0]);
            parse1(fileReader);
            parse2();
            finalizeCompile(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String parse1ErrorDefault(int linecount, String line) {
        return "(Parse1): On line " + linecount + " a error occurred while parsing the file. \"" + line + "\" | ->";
    }
    public static String parse2ErrorDefault(int linecount, String line) {
        return "(Parse2): On line " + linecount + " a error occurred while parsing the file. \"" + line + "\" | ->";
    }
    public static void parse1(FileReader fileReader) throws Exception {
        System.out.println("(THASM) first parse starting...");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        String[] linearguments;
        int linecount = 0;

        boolean multilineComment = false;
        boolean insideFunction = false;
        String insideFunctionName = "";

        while ((line = bufferedReader.readLine()) != null) {
            linecount++;
            linearguments = Arrays.stream(line.split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new); // Removes spaces and tabs before the code, as well as splits the line into small parts

            if (linearguments.length == 0) continue;

            // ####### IMPLEMENTING COMMENTS #######

            if (linearguments[0].startsWith("//*")) {
                multilineComment = true;
            }else if (linearguments[linearguments.length-1].contains("*//")) {
                multilineComment = false;
                continue;
            }
            if (multilineComment) continue;
            if (line.contains("//")) {
                line = line.substring(0,line.indexOf("//"));
                if (line.endsWith(" ")) line = line.substring(0,line.length()-1);
                linearguments = line.split(" ");
            }



            // ####### IMPLEMENTING BASE FUNCTIONALITY #######

            if (Objects.equals(linearguments[0].toLowerCase(), "byte")) {

                if (linearguments.length != 4) throw new Exception(parse1ErrorDefault(linecount,line) + " A variable has to be declared with 3 arguments: Type, Name, =, Value");
                if (!linearguments[1].startsWith("$")) throw new Exception(parse1ErrorDefault(linecount,line) + " A assignment or call of a variable always has to begin with the '$' symbol");
                if (Variables.get(linearguments[1]) != null) throw new Exception(parse1ErrorDefault(linecount,line) + " The variable name: \"" + linearguments[1] + "\" is already declared!");

                Object[] variable = new Object[2];
                byte radix = 10;
                if (linearguments[3].startsWith("0x")) radix = 16;
                if (linearguments[3].startsWith("0b")) radix = 2;
                if (radix != 10) linearguments[3] = linearguments[3].substring(2);

                variable[1] = Integer.parseInt(linearguments[3],radix);
                if ((int)variable[1] > 255 || (int)variable[1] < 0) throw new Exception(parse1ErrorDefault(linecount,line) + " Provided value isn't a 8 bit value!");
                variable[0] = 0; // Address (to be set)

                Variables.put(linearguments[1],variable);
            } else if (Objects.equals(linearguments[0].toLowerCase(), "function")) {
                if (insideFunction) throw new Exception(parse1ErrorDefault(linecount,line) + " A function can not be declared in another function!");
                if (linearguments[1].startsWith("$")) throw new Exception(parse1ErrorDefault(linecount,line) + " A function name can't begin with the '$' symbol!");
                if (Functions.get(linearguments[1]) != null) throw new Exception(parse1ErrorDefault(linecount,line) + " The function name: \"" + linearguments[1] + "\" is already declared!");

                Object[] function = new Object[2];
                function[0] = 0; // Address (to be set)
                ArrayList<String> functionContainedInstructions = new ArrayList<String>();
                function[1] = functionContainedInstructions;

                insideFunction = true;
                insideFunctionName = linearguments[1];
                Functions.put(linearguments[1],function);
            } else if (Objects.equals(linearguments[0].toLowerCase(), "end")) {
                if (linearguments.length != 1)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " \"end\" Takes no arguments!");
                insideFunction = false;
            } else if (Objects.equals(linearguments[0].toLowerCase(), "#")) {
                if (!insideFunction) throw new Exception(parse1ErrorDefault(linecount,line) + " Can't add custom hex code outside of a function!");
            } else if (Objects.equals(linearguments[0].toLowerCase(), "global")) {
                if (linearguments.length != 2) throw new Exception(parse1ErrorDefault(linecount,line) + " global takes only 1 argument: function");
                globalReference = linearguments[1];
                globalLine = linecount;
            } else {
                if (insideFunction) {
                    Object[] function = Functions.get(insideFunctionName);
                    ArrayList<String> functionContainedInstructions = (ArrayList<String>)function[1];

                    functionContainedInstructions.add(line.toString());
                    function[0] = linecount;
                    function[1] = functionContainedInstructions;
                    Functions.put(insideFunctionName,function);
                    continue;
                }
                throw new Exception(parse1ErrorDefault(linecount,line) + " Unknown command! Outside function!");
            }
        }
        System.out.println("(THASM) first parse successful!");
        bufferedReader.close();
    }
    public static void parse2() throws Exception {
        System.out.println("(THASM) second parse starting...");

        if (Functions.get(globalReference) == null)  throw new Exception(parse2ErrorDefault(globalLine,"global " + globalReference) + " Referenced function: "+globalReference+" isn't declared!");
        Variables.forEach((key, value) -> {
            int Address = MinProgramAddresses+currentAddress+ReservedSpotsFront;
            value[0] = Address;
            Variables.put(key, value);
            PROGRAM[Address] = ((Integer) (value[1])).byteValue();
            currentAddress++;
        });
        Functions.forEach((key, value) -> {
            int Address = MinProgramAddresses+currentAddress+ReservedSpotsFront;
            value[0] = Address;
            Functions.put(key, value);
            ArrayList<String> Lines = (ArrayList<String>) (value)[1];
            int lineCount = (int) (value)[0];

            for(String Line : Lines) {
                String[] line = Arrays.stream(Line.split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new); // Removes spaces and tabs before the code, as well as splits the line into small parts
                String[] Function = Functionality.get((line[0]));
                int ArgumentLength = 1;
                int[] Arguments = new int[]{0,0};

                int FunctionOpcode = 0;
                if (line[1].contains("jm")) {
                    continue;
                }
                if (line.length != 3) try {
                    throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Missing arguments! Command, Indentivation, Value/Pointer");
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (line[1].equals(">")) {
                    if (Functionality.get(line[0]) == null) try {
                        throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Invalid command: \"" + line[0] +"\"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!Objects.equals(Functionality.get(line[0])[2], "AV")) try {
                        throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Invalid argument, \"" + line[0] + "\" doesn't support direct value assignment!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    FunctionOpcode = Integer.parseInt(Functionality.get(line[0])[1].substring(2),16);
                    int v = 0;
                    try {
                        byte radix = 10;
                        if (line[2].startsWith("0x")) radix = 16;
                        if (line[2].startsWith("0b")) radix = 2;
                        v = Integer.parseInt(radix != 10?line[2].substring(2):line[2], radix);
                    } catch (Exception e) {
                        if (Variables.get(line[2]) == null) try {
                            throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " Referenced variable doesn't exist! : " + line[2]);
                        } catch (Exception e_) {
                            e_.printStackTrace();
                        }

                        v = (int)Variables.get(line[2])[1];
                    }
                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)FunctionOpcode;
                    currentAddress++;

                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)v;
                    currentAddress++;
                    //                Object[] variable = new Object[2];
//                byte radix = 10;
//                if (linearguments[3].startsWith("0x")) radix = 16;
//                if (linearguments[3].startsWith("0b")) radix = 2;
//                if (radix != 10) linearguments[3] = linearguments[3].substring(2);
//
//                variable[1] = Integer.parseInt(linearguments[3],radix);
                } else if (line[1].startsWith("*")) {

                } else {

                }
            }
        });
        int globalAddress = ((int) Functions.get(globalReference)[0]);
        PROGRAM[0] = 7;
        PROGRAM[1] = (byte)(globalAddress/16);
        PROGRAM[2] = (byte)(globalAddress%16);

        System.out.println("(THASM) second parse successful!");
    }
    public static void finalizeCompile(String filelocation) throws Exception {
        try {
            FileWriter myWriter = new FileWriter(filelocation);
            int prevRow = 0;
            StringBuilder line = new StringBuilder();

            for (int Address = 0; Address < MaxProgramAddresses; Address++) {
                int Row = Address/16;
                if (prevRow != Row) {
                    myWriter.write("0x"+String.format("%04X", Address-16).toUpperCase().substring(0, 3)+"0\t" + line + "\n");
                    line = new StringBuilder();
                }
                line.append("0x"+String.format("%02X",PROGRAM[Address]& 0xFF)).append("\t");
                prevRow = Row;
            }

            myWriter.close();
            System.out.println("(THASM) Succesfully wrote the program to: " + filelocation);
        } catch (Exception e) {
            System.out.println("An error occurred");
            e.printStackTrace();
        }
    }
}
