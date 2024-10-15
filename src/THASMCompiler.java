package src;
import javax.swing.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.stream.Collectors;

public class THASMCompiler {
    public THASMCompiler(String[] args, JTextArea textArea) {
        Compiler compiler = new Compiler(args,textArea);
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMB = heapMemoryUsage.getUsed()/(1024*1024);
//        System.out.println("Used Memory: " + usedMB + "MB");
    }

    public static void main(String[] args) {
        JTextArea textArea = new JTextArea();
        Compiler compiler = new Compiler(args,textArea);
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMB = heapMemoryUsage.getUsed()/(1024*1024);
        System.out.println("Used Memory: " + usedMB + "MB");
    }
}
class Compiler {
    public HashMap<String, String[]> Functionality = new HashMap<String, String[]>();
    public final int MinProgramAddresses = 0;
    public final int MaxProgramAddresses = 32768;
    public final int ReservedSpotsFront = 5;
    public int amountOfOverriddenVariableAdrresses = 0;
    public HashMap<String, Object[]> Variables = new HashMap<String, Object[]>();
    public ArrayList<String> VariablesOrder = new ArrayList<>();
    public HashMap<String, Object[]> Functions = new HashMap<String, Object[]>();
    public ArrayList<String> FunctionsOrder = new ArrayList<>();
    public HashMap<String, Object[]> Pointers = new HashMap<String, Object[]>();
    public HashMap<String, Integer> NonExistantNames = new HashMap<String, Integer>();
    public String globalReference = "";
    public int globalLine = 0;
    public int size = 0;
    public int currentAddress = 0;
    public byte[] PROGRAM = new byte[MaxProgramAddresses];
    public byte[] dataTables = new byte[MaxProgramAddresses];
    public ArrayList<Integer> dataTableSizes = new ArrayList<Integer>();
    public int currentDataTableAddress = -1;
    public int stack = 0;
    public String TemplateFile;
    JTextArea textArea;

    public Compiler(String[] args, JTextArea textArea) {
        this.textArea = textArea;

        Functionality.put("lda.ram",new String[]{"0x26","0x36","AV","A"});
        Functionality.put("lda.rom",new String[]{"0xA6","0xB6","AV","A"});
        Functionality.put("lda.boot",new String[]{"0x66","0x76","AV","A"});
        Functionality.put("sta.ram",new String[]{"0x06","0x06","A"});
        Functionality.put("sta.rom",new String[]{"0x86","0x86","A"});
        Functionality.put("sta.boot",new String[]{"0x46","0x46","A"});

        Functionality.put("nop",new String[]{"0x00","0x00"});
        Functionality.put("cnu",new String[]{"0x10","0x10"});
        Functionality.put("brk",new String[]{"0x20","0x20"});

        Functionality.put("inc",new String[]{"0x01","0x01"});
        Functionality.put("dec",new String[]{"0x11","0x11"});

        Functionality.put("rol",new String[]{"0x03","0x03"});
        Functionality.put("ror",new String[]{"0x13","0x13"});

        Functionality.put("add.ram",new String[]{"0x02","0x12","AV","A"});
        Functionality.put("sub.ram",new String[]{"0x22","0x32","AV","A"});
        Functionality.put("adc.ram",new String[]{"0x42","0x52","AV","A"});
        Functionality.put("suc.ram",new String[]{"0x62","0x72","AV","A"});
        Functionality.put("add.rom",new String[]{"0x82","0x92","AV","A"});
        Functionality.put("sub.rom",new String[]{"0xA2","0xB2","AV","A"});
        Functionality.put("adc.rom",new String[]{"0xC2","0xD2","AV","A"});
        Functionality.put("suc.rom",new String[]{"0xE2","0xF2","AV","A"});

        Functionality.put("and.ram",new String[]{"0x04","0x14","AV","A"});
        Functionality.put("or.ram",new String[]{"0x24","0x34","AV","A"});
        Functionality.put("xor.ram",new String[]{"0x44","0x54","AV","A"});
        Functionality.put("and.rom",new String[]{"0x84","0x94","AV","A"});
        Functionality.put("or.rom",new String[]{"0xA4","0xB4","AV","A"});
        Functionality.put("xor.rom",new String[]{"0xC4","0xD4","AV","A"});

        Functionality.put("cmp.ram",new String[]{"0x05","0x15","AV","A"});
        Functionality.put("cmp.rom",new String[]{"0x85","0x95","AV","A"});

        Functionality.put("jmp",new String[]{"0x07","0x07","A"});
        Functionality.put("jme",new String[]{"0x27","0x27","A"});
        Functionality.put("jmc",new String[]{"0x47","0x47","A"});
        Functionality.put("jmo",new String[]{"0x67","0x67","A"});

        if (args.length != 2 && args.length != 3) {
            System.err.println("Missing argument: file location, output location, (Template File)");
            return;
        }

        if (args.length == 3) TemplateFile = args[2];

        try {
            FileReader fileReader = new FileReader(args[0]);

            parse1(fileReader);
            parse2();

            finalizeCompile(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String parse1ErrorDefault(int linecount, String line) {
        return "(Parse1): On line " + linecount + " a error occurred while parsing the file. \"" + line + "\" | ->";
    }
    public String parse2ErrorDefault(int linecount, String line) {
        return "(Parse2): On line " + linecount + " a error occurred while parsing the file. \"" + line + "\" | ->";
    }
    public void parse1(FileReader fileReader) throws Exception {
        System.out.println("(THASM) first parse starting...");
        textArea.append("(THASM) first parse starting...\n");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        ArrayList<String> linearguments = new ArrayList<>();
        int linecount = 0;

        boolean multilineComment = false;
        boolean insideFunction = false;
        boolean insideData = false;
        String insideFunctionName = "";

        while ((line = bufferedReader.readLine()) != null) {
            linecount++;
            linearguments = Arrays.stream(line.split(" ")).filter(list -> !list.isEmpty()).collect(Collectors.toCollection(ArrayList::new));

            if (linearguments.isEmpty()) continue;

            // ####### IMPLEMENTING COMMENTS #######

            if (linearguments.getFirst().startsWith("//*")) {
                multilineComment = true;
            }else if (linearguments.getLast().contains("*//")) {
                multilineComment = false;
                continue;
            }
            if (multilineComment) continue;
            if (line.contains("//")) {
                line = line.substring(0,line.indexOf("//"));
                if (line.endsWith(" ")) line = line.substring(0,line.length()-1);

                linearguments = Arrays.stream(line.split(" ")).collect(Collectors.toCollection(ArrayList::new));
            }
            ArrayList<String> lineArgumentsFiltered = new ArrayList<String>();
            for (String arguments : linearguments) {
                if (!arguments.isEmpty()) {
                    lineArgumentsFiltered.add(arguments);
                }
            }
            if (lineArgumentsFiltered.isEmpty()) continue;
            linearguments = lineArgumentsFiltered;


            // ####### IMPLEMENTING BASE FUNCTIONALITY #######
            if (insideData && !Objects.equals(linearguments.get(0).toLowerCase(), "data")) {
                if (linearguments.size() != 1) throw new Exception(parse1ErrorDefault(linecount,line) + " Content of a data table has be declared as follows: <data>,<data>,<data>...");
                String dataString = linearguments.get(0).toLowerCase();
                String[] dataArray = dataString.split(",");
                for (String data : dataArray) {

                    Object[] variable = new Object[2];
                    byte radix = 10;
                    if (data.startsWith("0x")) radix = 16;
                    if (data.startsWith("0b")) radix = 2;
                    if (radix != 10) {
                        data = data.substring(2);
                    }
                    int dataContent = Integer.parseInt(data, radix);
                    if (dataContent > 256 || dataContent < 0) {
                        throw new Exception(parse1ErrorDefault(linecount, line) + " Data can't be greater than 256 (0xff) or less than 0 (0x00)! Current data:" + data + " -> " + currentAddress);
                    }
                    dataTables[currentDataTableAddress] = (byte)dataContent;

                    currentDataTableAddress++;
                }

            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "byte")) {
                if (linearguments.size() != 4 && linearguments.size() != 5) throw new Exception(parse1ErrorDefault(linecount,line) + " A variable has to be declared with 4 or 5 arguments: Type, (Pointer), Name, =, Value");

                boolean customAdrress = false;
                int pointerLocation = -1;

                if (linearguments.size() == 5) {
                    customAdrress = true;
                    String pointer = linearguments.get(1);

                    if (!pointer.startsWith("*")) throw new Exception(parse1ErrorDefault(linecount,line) + " A pointer has to start with '*', directly followed by the 16 bit value!");
                    pointer = pointer.substring(1);

                    linearguments.remove(1);

                    Object[] variable = new Object[2];
                    byte radix = 10;
                    if (pointer.startsWith("0x")) radix = 16;
                    if (pointer.startsWith("0b")) radix = 2;
                    if (radix != 10) {
                        pointer = pointer.substring(2);
                    }
                    pointerLocation = Integer.parseInt(pointer,radix);
                    if (pointerLocation > 65535 || pointerLocation < 0) throw new Exception(parse1ErrorDefault(linecount,line) + " A pointer can't be greater than 65535 (0xffff) or less than 0 (0x0000)! Current pointer:" + pointer + " -> " + pointerLocation);
                    amountOfOverriddenVariableAdrresses++;
                }

                if (!linearguments.get(1).startsWith("$")) throw new Exception(parse1ErrorDefault(linecount,line) + " A assignment or call of a variable always has to begin with the '$' symbol");
                if (Variables.get(linearguments.get(1)) != null) throw new Exception(parse1ErrorDefault(linecount,line) + " The variable/section name: \"" + linearguments.get(1) + "\" is already declared!");

                Object[] variable = new Object[2];
                byte radix = 10;
                if (linearguments.get(3).startsWith("0x")) radix = 16;
                if (linearguments.get(3).startsWith("0b")) radix = 2;
                if (radix != 10) {
                    linearguments.set(3, linearguments.get(3).substring(2));
                }
                variable[1] = Integer.parseInt(linearguments.get(3),radix);
                if ((int)variable[1] > 255 || (int)variable[1] < 0) throw new Exception(parse1ErrorDefault(linecount,line) + " Provided value isn't a 8 bit value!");
                variable[0] = pointerLocation; // Address


                Variables.put(linearguments.get(1),variable);
                VariablesOrder.add(linearguments.get(1));
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "function")) {
                if (insideFunction) throw new Exception(parse1ErrorDefault(linecount,line) + " A function can not be declared in another function!");
                if (linearguments.get(1).startsWith("$")) throw new Exception(parse1ErrorDefault(linecount,line) + " A function name can't begin with the '$' symbol!");
                if (Functions.get(linearguments.get(1)) != null) throw new Exception(parse1ErrorDefault(linecount,line) + " The function name: \"" + linearguments.get(1) + "\" is already declared!");

                Object[] function = new Object[2];
                function[0] = -1; // Address (to be set)
                ArrayList<String> functionContainedInstructions = new ArrayList<String>();
                function[1] = functionContainedInstructions;

                insideFunction = true;
                insideFunctionName = linearguments.get(1);
                Functions.put(linearguments.get(1),function);
                FunctionsOrder.add(linearguments.get(1));
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "end")) {
                if (linearguments.size() != 1)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " \"end\" Takes no arguments!");
                insideFunction = false;
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "#")) {
                if (!insideFunction) throw new Exception(parse1ErrorDefault(linecount,line) + " Can't add custom hex code outside of a function!");
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "global")) {
                if (linearguments.size() != 2) throw new Exception(parse1ErrorDefault(linecount,line) + " global directive takes only 1 argument: function");
                globalReference = linearguments.get(1);
                globalLine = linecount;
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "size")) {
                if (linearguments.size() != 2) throw new Exception(parse1ErrorDefault(linecount,line) + " size directive takes only 1 argument: size (16bit)");
                try {
                    size = Integer.parseInt(linearguments.get(1));
                } catch (NumberFormatException e) {
                    throw new Exception(parse1ErrorDefault(linecount,line) + " invalid input for size directive: " + linearguments.get(1));
                }
                if (size > MaxProgramAddresses) throw new Exception(parse1ErrorDefault(linecount,line) + " invalid input for size directive, maximum size = " + MaxProgramAddresses + ", input size = " + size);
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "stack")) {
                if (linearguments.size() != 2)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " stack takes only 1 argument: size (decimal)");

                try {
                    stack = Integer.parseInt(linearguments.get(1));
                } catch (Exception e) {
                    throw new Exception(parse1ErrorDefault(linecount, line) + " An error occurred while trying to interpret the stack size. Is the argument a valid decimal integer?");
                }
                if (stack < 0)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " stack size cant be less than zero!");
                if (stack > 255)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " stack size cant be more than 256!");
            }else if (linearguments.get(0).startsWith("$")) {
                if (linearguments.size() != 1)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " A section has to be declared with 1 argument: $(Name)");
                if (Variables.get(linearguments.get(0)) != null)
                    throw new Exception(parse1ErrorDefault(linecount, line) + " The variable/section name: \"" + linearguments.get(1) + "\" is already declared!");
                if (!insideFunction) throw new Exception(parse1ErrorDefault(linecount, line) + " Outside function!");

                Object[] variable = new Object[2];
                variable[0] = 0;
                variable[1] = -2;
                Variables.put(linearguments.get(0), variable);
                VariablesOrder.add(linearguments.get(0));
                Object[] function = Functions.get(insideFunctionName);
                ArrayList<String> functionContainedInstructions = (ArrayList<String>) function[1];

                functionContainedInstructions.add(line);
                function[0] = -1;
                function[1] = functionContainedInstructions;
                Functions.put(insideFunctionName, function);
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "data")) {
                if (linearguments.size() != 2) throw new Exception(parse1ErrorDefault(linecount,line) + " A data table has to be declared with 1 arguments: data *<Pointer>");
                if (Objects.equals(linearguments.get(1), "end")) {
                    dataTableSizes.add(currentDataTableAddress);
                    insideData=false;
                } else {


                    String pointer = linearguments.get(1);
                    int pointerLocation = -1;

                    if (!pointer.startsWith("*"))
                        throw new Exception(parse1ErrorDefault(linecount, line) + " A data table location has to start with '*', directly followed by the 16 bit value!");
                    pointer = pointer.substring(1);

                    if (Functions.get(pointer) != null) pointerLocation = (int) Functions.get(pointer)[0];
                    else if (Variables.get(pointer) != null) pointerLocation = (int) Variables.get(pointer)[0];
                    else if (Pointers.get(pointer) != null) pointerLocation = (int) Pointers.get(pointer)[0];
                    else {
                        linearguments.remove(1);

                        Object[] variable = new Object[2];
                        byte radix = 10;
                        if (pointer.startsWith("0x")) radix = 16;
                        if (pointer.startsWith("0b")) radix = 2;
                        if (radix != 10) {
                            pointer = pointer.substring(2);
                        }
                        pointerLocation = Integer.parseInt(pointer, radix);
                        if (pointerLocation > 65535 || pointerLocation < 0)
                            throw new Exception(parse1ErrorDefault(linecount, line) + " A pointer can't be greater than 65535 (0xffff) or less than 0 (0x0000)! Current pointer:" + pointer + " -> " + pointerLocation);
                    }
                    currentDataTableAddress = pointerLocation;
                    dataTableSizes.add(pointerLocation);
                    insideData = true;
                }
            } else if (Objects.equals(linearguments.get(0).toLowerCase(), "pointer")) {
                if (linearguments.size() != 4) throw new Exception(parse1ErrorDefault(linecount,line) + " A pointer has to be declared with 4 arguments: 'pointer', Name, =, Location");
                if (!linearguments.get(1).startsWith("@")) throw new Exception(parse1ErrorDefault(linecount,line) + " A assignment or call of a pointer always has to begin with the '@' symbol");
                if (Pointers.get(linearguments.get(1)) != null) throw new Exception(parse1ErrorDefault(linecount,line) + " The pointer: \"" + linearguments.get(1) + "\" is already declared!");

                Object[] pointer = new Object[3];
                int pointerLocation = -1;
                try {
                    byte radix = 10;
                    if (linearguments.get(3).startsWith("0x")) radix = 16;
                    if (linearguments.get(3).startsWith("0b")) radix = 2;
                    if (radix != 10) linearguments.set(3, linearguments.get(3).substring(2));

                    pointerLocation = Integer.parseInt(linearguments.get(3), radix);
                    pointer[2] = 0;
                } catch (Exception e) {
                    switch (linearguments.get(3).substring(0, 1)) {
                        case "$":
                            pointer[2] = 0;
                            break;
                        case "@":
                            pointer[2] = 1;
                            break;
                        default:
                            pointer[2] = 2;
                            break;
                    }
                }
                if ((pointerLocation > 65535 || pointerLocation < 0) && pointerLocation != -1) throw new Exception(parse1ErrorDefault(linecount,line) + " Provided pointer isn't a 16 bit address! Current address: " + pointerLocation);

                pointer[0] = pointerLocation;
                pointer[1] = linearguments.get(3);
                Pointers.put(linearguments.get(1),pointer);
            } else {
                if (insideFunction) {
                    Object[] function = Functions.get(insideFunctionName);
                    ArrayList<String> functionContainedInstructions = (ArrayList<String>)function[1];

                    functionContainedInstructions.add(line);
                    function[0] = linecount;
                    function[1] = functionContainedInstructions;
                    Functions.put(insideFunctionName,function);
                    continue;
                }
                if (line.equals("")) continue;
                throw new Exception(parse1ErrorDefault(linecount,line) + " Unknown command! Outside function!");
            }
        }
        System.out.println("(THASM) first parse successful!");
        textArea.append("(THASM) first parse successful!\n");
        bufferedReader.close();
    }
    public void parse2() throws Exception {
        System.out.println("(THASM) second parse starting...");
        textArea.append("(THASM) second parse starting...\n");

        if (TemplateFile!=null) {
            int row = 0;
            File myFile = new File(TemplateFile);
            Scanner myReader = new Scanner(myFile);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                int columb = 0;
                for(String hexString : line.split("\t")) {
                    int value = Integer.parseInt(hexString.substring(2),16);
                    PROGRAM[(columb+row*16)] = (byte)value;
                    columb++;
                }
                row++;
            }
            myReader.close();
        }
        for (int i = 0; i < dataTableSizes.size()/2; i++) {
           for (int dc = 0; dc < dataTableSizes.get(i*2+1) -dataTableSizes.get(i*2); dc++) {
               PROGRAM[dataTableSizes.get(i*2) + dc] = dataTables[dataTableSizes.get(i*2) + dc];
           }
        }


        PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte) stack;
        currentAddress++;

        if (Functions.get(globalReference) == null)  throw new Exception(parse2ErrorDefault(globalLine,"global " + globalReference) + " Referenced function: "+globalReference+" isn't declared!");
        for(String key : VariablesOrder) {
            Object[] value = Variables.get(key);
            int Address = MinProgramAddresses+currentAddress+ReservedSpotsFront;

            if (value[0].equals(-1)) {
                value[0] = Address;
                Variables.put(key, value);
                currentAddress++;
            } else {
                Address = (int)value[0];
            }

            PROGRAM[Address] = ((Integer) (value[1])).byteValue();
        }

        int AddressT = MinProgramAddresses+currentAddress+ReservedSpotsFront;
        for(String key : FunctionsOrder) {
            Object[] value = Functions.get(key);
            value[0] = AddressT;
            Functions.put(key, value);
            ArrayList<String> Lines = (ArrayList<String>) (value)[1];

            for(String Line : Lines) {
                if (Arrays.stream(Line.split(" ")).filter(list -> !list.isEmpty()).collect(Collectors.toCollection(ArrayList::new)).getFirst().startsWith("$")) continue;
                if (!Line.contains("byte")&&!Line.contains("=")) {
                    if (Line.contains("*")) {
                        AddressT+=3;
                    } else if (Line.contains(">")) {
                        AddressT+=2;
                    } else {
                        AddressT++;
                    }
                }
            }
        }

        Pointers.forEach((k,v) -> {
            if ((int)v[0] == -1) {
                switch ((int)v[2]) {
                    case 0:
                        v[0] = Variables.get(Pointers.get(k)[1])[0];
                        break;
                    case 1:
                        v[0] = Pointers.get(Pointers.get(k)[1])[0];
                        break;
                    case 2:
                        v[0] = Functions.get(Pointers.get(k)[1])[0];
                        break;
                }
                Pointers.put(k,v);
            }
        });

        int Address = MinProgramAddresses+currentAddress+ReservedSpotsFront;
        for(String key : FunctionsOrder) {
            Object[] value = Functions.get(key);

            value[0] = Address;

            Functions.put(key, value);
            ArrayList<String> Lines = (ArrayList<String>) (value)[1];
            int lineCount = (int) (value)[0];

            String sectionName = "";
            boolean nextIsSection = false;

            for(String Line : Lines) {

                String[] line = Arrays.stream(Line.split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new); // Removes spaces and tabs before the code, as well as splits the line into small parts
                String[] Function = Functionality.get(line[0]);
                if (line[0].startsWith("$")) {
                    sectionName = line[0];
                    nextIsSection = true;
                    continue;
                }
                if (Function == null && !line[0].startsWith("$")) throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Command not found: \""+line[0]+"\"");
                String ExpectedArgument1 = Function.length>2?Function[2]:null;
                String ExpectedArgument2 = Function.length>3?Function[3]:null;

                int ArgumentLength = 1;
                int[] Arguments = new int[]{0,0};

                int FunctionOpcode = 0;

                if (nextIsSection) {
                    nextIsSection = false;
                    Object[] x = new Object[2];
                    x[0] = MinProgramAddresses+currentAddress+ReservedSpotsFront;
                    Variables.put(sectionName,x);
                }

                if (line.length != 3 && ExpectedArgument1 != null && ExpectedArgument2 != null) throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Missing arguments! Command, Indentivation, Value/Pointer");
                if (line.length != 2 && ExpectedArgument1 == "A" && ExpectedArgument2 != null) throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Missing arguments! Command, Address/Pointer");
                if (line.length == 1) {
                    FunctionOpcode = Integer.parseInt(Functionality.get(line[0])[1].substring(2),16);
                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)FunctionOpcode;
                    currentAddress++;
                } else if (line[1].equals(">")) {
                    if (Functionality.get(line[0]) == null) throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Invalid command: \"" + line[0] +"\"");
                    if (!Objects.equals(Functionality.get(line[0])[2], "AV")) throw new Exception(parse2ErrorDefault(lineCount,String.join(" ",line)) + " Invalid argument, \"" + line[0] + "\" doesn't support direct value assignment!");

                    FunctionOpcode = Integer.parseInt(Functionality.get(line[0])[1].substring(2),16);
                    int v = 0;
                    try {
                        byte radix = 10;
                        if (line[2].startsWith("0x")) radix = 16;
                        if (line[2].startsWith("0b")) radix = 2;
                        v = Integer.parseInt(radix != 10?line[2].substring(2):line[2], radix);
                    } catch (Exception e) {
                        boolean HIGHVariable = false,LOWVariable= false,HIGHPointer= false,LOWPointer= false,HIGHFunction= false,LOWFunction = false;

                        try {
                            HIGHVariable = (Variables.get(line[2].substring(0, line[2].length() - 5)) != null);
                            LOWVariable = (Variables.get(line[2].substring(0, line[2].length() - 4)) != null);

                            HIGHPointer = (Pointers.get(line[2].substring(0, line[2].length() - 5)) != null);
                            LOWPointer = (Pointers.get(line[2].substring(0, line[2].length() - 4)) != null);

                            HIGHFunction = (Functions.get(line[2].substring(0, line[2].length() - 5)) != null);
                            LOWFunction = (Functions.get(line[2].substring(0, line[2].length() - 4)) != null);
                        } catch (Exception g) {

                        }

                        if (Functionality.get(line[2].substring(0,line[2].length()-1)) != null) {
                            String lastCharacter = line[2].substring(line[2].length()-1);
                            if (lastCharacter.equals("*")) {
                                v = Integer.parseInt(Functionality.get(line[2].substring(0,line[2].length()-1))[0].substring(2),16);
                            } else if (lastCharacter.equals(">")) {
                                v = Integer.parseInt(Functionality.get(line[2].substring(0,line[2].length()-1))[1].substring(2));
                            } else {
                                throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " Function type not valid: " + lastCharacter + ". Only '*' and '>' can be used for address and direct respectfully");
                            }

                        } else if (HIGHVariable || LOWVariable || HIGHPointer || LOWPointer || HIGHFunction || LOWFunction) {
                            if (HIGHVariable || LOWVariable) {
                                v = (int) Variables.get(line[2].substring(0, line[2].length() - (HIGHVariable?5:4)))[0];
                                String hex = String.format("%04x", v);
                                String portion = HIGHVariable?hex.substring(0,2):hex.substring(2);
                                v = Integer.parseUnsignedInt(portion,16);
                            } else if (HIGHFunction || LOWFunction) {
                                    v = (int) Functions.get(line[2].substring(0, line[2].length() - (HIGHFunction?5:4)))[0];
                                    String hex = String.format("%04x", v);
                                    String portion = HIGHFunction?hex.substring(0,2):hex.substring(2);
                                    v = Integer.parseUnsignedInt(portion,16);
                            } else {
                                v = (int) Pointers.get(line[2].substring(0, line[2].length() - (HIGHPointer?5:4)))[0];
                                String hex = String.format("%04x", v);
                                String portion = HIGHPointer?hex.substring(0,2):hex.substring(2);
                                v = Integer.parseUnsignedInt(portion,16);
                            }
                        } else {
                            if (Variables.get(line[2]) == null) throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " Referenced variable, function or pointer doesn't exist! : " + line[2]);

                            v = (int) Variables.get(line[2])[1];
                        }
                    }
                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)FunctionOpcode;
                    currentAddress++;

                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)v;
                    currentAddress++;
                } else if (line[1].startsWith("*")) {
                    if (line.length != 3) throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " 3 Arguments must be given! function *() pointer. Arguments provided: " + line.length);
                    int v = 0;
                    int MSBValue = 0;
                    int LSBValue = 0;

                    try {
                        byte radix = 10;
                        if (line[2].startsWith("0x")) radix = 16;
                        if (line[2].startsWith("0b")) radix = 2;
                        v = Integer.parseInt(radix != 10?line[2].substring(2):line[2], radix);
                    } catch (Exception e) {
                        //if (Variables.get(line[2]) == null && Functions.get(line[2]) == null && Pointers.get(line[2]) == null) throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " Referenced variable, function or pointer doesn't exist! : " + line[2]);
                        if (Variables.get(line[2]) == null && Functions.get(line[2]) == null && Pointers.get(line[2]) == null) {
                            v = 0;
                            NonExistantNames.put(line[2],currentAddress);
                        } else {
                            if (Functions.get(line[2]) != null) v = (int) Functions.get(line[2])[0];
                            else if (Variables.get(line[2]) != null) v = (int) Variables.get(line[2])[0];
                            else v = (int) Pointers.get(line[2])[0];
                        }
                    }
                    MSBValue = Integer.parseInt((String.format("%04X",v).substring(0,2)),16);
                    LSBValue = Integer.parseInt((String.format("%04X",v).substring(2)),16);

                    FunctionOpcode = Integer.parseInt(Functionality.get(line[0])[0].substring(2),16);
                    PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte)FunctionOpcode;
                    currentAddress++;


                    if (line[1].contains("(")) {
                        String[] arguments = line[1].substring(1).replace("(","").replace(")","").split(",");

                        if (arguments.length != 2) throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " 2 Arguments must be given! *(@M,@L). Arguments provided: " + arguments.length);
                        
                        int addr = -1;
                        if (NonExistantNames.containsKey(arguments[0])) addr = NonExistantNames.get(arguments[0]);
                        if (NonExistantNames.containsKey(arguments[1])) addr = NonExistantNames.get(arguments[1]);
                        if (addr != -1) {
                            int MSBValue2 = Integer.parseInt((String.format("%04X",MinProgramAddresses+currentAddress+ReservedSpotsFront).substring(0,2)),16);
                            int LSBValue2 = Integer.parseInt((String.format("%04X",MinProgramAddresses+currentAddress+ReservedSpotsFront+1).substring(2)),16);

                            addr++;
                            PROGRAM[MinProgramAddresses+addr+ReservedSpotsFront] = (byte) MSBValue2;
                            addr++;
                            PROGRAM[MinProgramAddresses+addr+ReservedSpotsFront] = (byte) LSBValue2;
                        }
                        
                        Object[] MSB = new Object[2];
                        Object[] LSB = new Object[2];
                        LSB[1] = MSBValue;
                        MSB[0] = MinProgramAddresses+currentAddress+ReservedSpotsFront;
                        PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte) MSBValue;
                        currentAddress++;
                        LSB[1] = LSBValue;
                        LSB[0] = MinProgramAddresses+currentAddress+ReservedSpotsFront;
                        PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte) LSBValue;
                        currentAddress++;

                        Variables.put(arguments[0],MSB);
                        Variables.put(arguments[1],LSB);
                    } else {
                        PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte) MSBValue;
                        currentAddress++;
                        PROGRAM[MinProgramAddresses+currentAddress+ReservedSpotsFront] = (byte) LSBValue;
                        currentAddress++;
                    }
                } else {
                    throw new Exception(parse2ErrorDefault(lineCount, String.join(" ", line)) + " Unknown command: " + line[0]);

                }
            }
            Address = MinProgramAddresses+currentAddress+ReservedSpotsFront;
        }
        int globalAddress = ((int) Functions.get(globalReference)[0]);
        String hex = String.format("%04x", globalAddress);

        String portionHIGH = hex.substring(0,2);
        String portionLOW = hex.substring(2);

        String hexSize = String.format("%04x", size);

        String portionHIGHSize = hexSize.substring(0,2);
        String portionLOWSize = hexSize.substring(2);

        PROGRAM[0] = 7;
        PROGRAM[1] = (byte)Integer.parseUnsignedInt(portionHIGH,16);
        PROGRAM[2] = (byte)Integer.parseUnsignedInt(portionLOW,16);
        PROGRAM[4] = (byte)Integer.parseUnsignedInt(portionHIGHSize,16);
        PROGRAM[5] = (byte)Integer.parseUnsignedInt(portionLOWSize,16);
        System.out.println("(THASM) second parse successful!");
        textArea.append("(THASM) second parse successful...\n");
        System.out.println();
        textArea.append("\n");
        System.err.println("(THASM) Compiled to " + (MinProgramAddresses+currentAddress+ReservedSpotsFront) + " bytes");
        textArea.append("(THASM) Compiled to " + (MinProgramAddresses+currentAddress+ReservedSpotsFront) + " bytes\n");

    }
    public JTextArea finalizeCompile(String filelocation) throws Exception {
//        int LoadBytes = TemplateFile==null?MaxProgramAddresses:MinProgramAddresses+currentAddress+ReservedSpotsFront;
        try {
            filelocation = filelocation.replace("/"," ");
            System.out.println(filelocation);
            FileWriter myWriter = new FileWriter(filelocation);
            int prevRow = 0;
            StringBuilder line = new StringBuilder();

            for (int Address = 0; Address < MaxProgramAddresses; Address++) {
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
            System.out.println("(THASM) Succesfully wrote the program to: " + filelocation);
            textArea.append("(THASM) Succesfully wrote the program to: " + filelocation+"\n");
        } catch (Exception e) {
            System.out.println("An error occurred");
            e.printStackTrace();
        }
        return textArea;
    }
}
