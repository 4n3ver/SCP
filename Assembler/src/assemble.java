import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yoel Ivan (yivan3@gatech.edu)
 */
public class assemble {
    public enum Radix {
        DEC("DEC", "d", 10),
        hex("HEX", "x", 16),
        HEX("HEX", "X", 16);

        private final String rep;
        private final String format;
        private final int num_rep;

        Radix(String name, String printFormat, int numRep) {
            this.rep = name;
            this.format = printFormat;
            this.num_rep = numRep;
        }

        public String getName() {
            return rep;
        }

        public String getFormat(int len) {
            return "%0" + len + format;
        }

        public int getDecRep() {
            return num_rep;
        }
    }

    public enum PrimaryOP {
        ALU_R("0000"),
        ALU_I("1000"),
        LOAD("1001"),
        STORE("0101"),
        CMP_R("0010"),
        CMP_I("1010"),
        JAL("1011"),
        BRANCH("0110");

        private final String code;

        PrimaryOP(String encoding) {
            this.code = encoding;
        }

        public String code() {
            return code;
        }
    }   // not actually useful

    private static final int WIDTH = 32;    // multiple of 8 only!
    private static final int DEPTH = 2048;
    private static final Radix ADDR_RADIX = Radix.hex;
    private static final Radix DATA_RADIX = Radix.hex;
    private static final String DEAD_MEAT = "DEAD";
    private static final String ERROR_TAG = "[ERROR] ";

    // read only maps
    private static Map<String, Handler> instrsHandlerDict;
    private static Map<String, Handler> assemblerDirDict;
    private static Map<String, String> opcodeDict;
    private static Map<String, String> regDict;

    // r-w maps
    private static Map<String, Long> constLabelDict;
    private static Map<String, Long> addressLabelDict;

    private static String registerPattern;
    private static DebugVerbose debug;
    private static PrintWriter target;
    private static long byte_addr;

    /**
     * -v is optional to enable debug message.
     *
     * @param args [-v] path_to_a32.a32
     */
    public static void main(String[] args) {    // 1st arg is the .a32 file
        String asmPath = parseArgs(args);

        File asmFile = new File(asmPath);
        target = writeMif(asmPath.replace(".a32", ".mif"));

        buildDictionary();
        process(asmFile);
        target.close();
    }

    /**
     * Parse and assembler assembly code to machine code
     *
     * @param src {@link File} representation of *.a32 file
     */
    private static void process(File src) {
        assert registerPattern != null;
        Scanner asmScanner = openAsm(src);
        target.printf("WIDTH=%d;\n", WIDTH);
        target.printf("DEPTH=%d;\n", DEPTH);
        target.printf("ADDRESS_RADIX=%s;\n", ADDR_RADIX.getName());
        target.printf("DATA_RADIX=%s;\n", DATA_RADIX.getName());
        target.printf("CONTENT BEGIN\n");
        Pattern assemblerParser = Pattern.compile("(?:(\\.[A-Za-z]+)\\s+(?:" +
                "([A-Za-z][a-zA-Z0-9]+)\\s*=\\s*([A-Za-z0-9]+)\\b|" +
                "([A-Za-z0-9]+)\\b))");
        Pattern instructionParser = Pattern.compile(
                "([A-Za-z]+)(?:\\s+?(?:(" + registerPattern +
                        ")\\s*?,\\s*?(?:(" + registerPattern +
                        ")(?:\\s*?,\\s*?(" + registerPattern +
                        "|\\-?[A-Za-z0-9]+)|\\b)|(\\-?[A-Za-z0-9]+)(?:\\((" +
                        registerPattern +
                        ")\\)|\\b))|(\\-?[A-Za-z0-9]+)(?:\\((" +
                        registerPattern + ")\\)|\\b))|\\b)");
        debug.println("Assembler Pattern: " + assemblerParser.pattern());
        debug.println("Instruction Pattern: " + instructionParser.pattern());
        processAssemblerDirective(asmScanner, assemblerParser);
        asmScanner.close();
        asmScanner = openAsm(src);
        processInstruction(asmScanner, assemblerParser, instructionParser);
        asmScanner.close();
        target.print(fillDEADMEAT(byte_addr, DEPTH * WIDTH / 8 - 1));
        target.print("END;");
    }

    /**
     * Second pass of the assembly process, reads CPU instruction and
     * translate it into machine code
     *
     * @param src               {@link Scanner} linked to {@link File}
     *                          representation of *.a32 file
     * @param assemblerParser   regex pattern to parse the assembler
     *                          instruction
     * @param instructionParser regex pattern to parse the cpu instruction
     */
    private static void processInstruction(Scanner src,
                                           Pattern assemblerParser,
                                           Pattern instructionParser) {
        byte_addr = 0;
        for (long line_num = 1; src.hasNextLine(); line_num++) {
            String line = null;
            try {
                line = src.nextLine().trim();
                int limit = line.indexOf(";");
                line = line.substring(0, limit >= 0 ? limit : line.length())
                        .trim();
                if (!line.equals("") && !line.endsWith(":")) {
                    if (line.startsWith(".")) {
                        Matcher parsed = assemblerParser.matcher(line);
                        if (parsed.matches()) {
                            if (!parsed.group(1).equals(".NAME")) {
                                if (parsed.group(1).equals(".ORIG")) {
                                    long newByte_addr =
                                            parseAddr(parsed.group(4));
                                    if (newByte_addr > byte_addr) {
                                        target.print(fillDEADMEAT(byte_addr,
                                                newByte_addr - 1));
                                    }
                                }
                                assemblerDirDict.get(parsed.group(1))
                                        .processArgs(parsed);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Unexpected line: " + line);
                        }
                    } else {
                        Matcher parsed = instructionParser.matcher(line);
                        if (parsed.matches()) {
                            instrsHandlerDict.get(
                                    parsed.group(1).toLowerCase()).processArgs(
                                    parsed);
                            byte_addr += 4;
                        } else {
                            throw new IllegalArgumentException(
                                    "Unexpected line: " + line);
                        }
                    }
                }
            } catch (Exception e) {
                debug.printStackTrace(e);
                System.err.println(
                        ERROR_TAG + "at line " + line_num + ": " +
                                line);
                System.exit(-1);
            }
        }
    }

    /**
     * First pass of the assembly process, reads assembler instruction,
     * and build <code>addressLabelDict</code> and <code>constLabelDict</code>
     *
     * @param src             {@link Scanner} linked to {@link File}
     *                        representation of *.a32 file
     * @param assemblerParser regex pattern to parse the assembler
     *                        instruction
     */
    private static void processAssemblerDirective(Scanner src,
                                                  Pattern assemblerParser) {
        byte_addr = 0;
        for (long line_num = 1; src.hasNextLine(); line_num++) {
            String line = null;
            try {
                line = src.nextLine().trim();
                int limit = line.indexOf(";");
                line = line.substring(0, limit >= 0 ? limit : line.length());
                if (!line.equals("")) {
                    if (line.startsWith(".")) {
                        Matcher parsed = assemblerParser.matcher(line);
                        if (parsed.matches()) {
                            if (parsed.group(1).equals(".WORD")) {
                                byte_addr += 4;
                            } else {
                                assemblerDirDict.get(parsed.group(1))
                                        .processArgs(parsed);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Unexpected line:" + line);
                        }
                    } else if (line.endsWith(":")) {
                        String label = line.substring(0, line.length() - 1);
                        if (isReserved(label)) {
                            throw new IllegalArgumentException(
                                    "invalid used of reserved word");
                        }
                        debug.println(
                                "ADDR_LABEL: new label " + label + " at " +
                                        byte_addr);
                        addressLabelDict.put(label, byte_addr);
                    } else {
                        byte_addr += 4;
                    }
                }
            } catch (Exception e) {
                debug.printStackTrace(e);
                System.err.println(
                        ERROR_TAG + "at line " + line_num + ": " +
                                line);
                System.exit(-1);
            }
        }
        debug.println("ADDR_LABEL: " + addressLabelDict.toString());
    }

    /**
     * Format actual instruction read from *.a32 file as a comment in the
     * *.mif file
     *
     * @param actualInstruction actual instruction (no longer pseudo) read from
     *                          *.a32 file
     * @return formatted instruction ready to be printed as comment on .mif
     * file
     */
    private static String formatComment(String actualInstruction) {
        return String.format("-- @ 0x" + ADDR_RADIX.getFormat(WIDTH / 4) +
                        " : %s", byte_addr,
                actualInstruction.toUpperCase().replaceFirst("\\s+", "\t"));
    }

    /**
     * Format translated instruction to the *.mif file
     *
     * @param codedInstruction translated instruction in 32-bit binary string
     * @return formatted translated instruction ready to be printed on .mif
     */
    private static String formatInstruction(String codedInstruction) {
        assert codedInstruction.length() == WIDTH;
        String data = String.format(ADDR_RADIX.getFormat(WIDTH / 4) + " : " +
                        DATA_RADIX.getFormat(WIDTH / 4) + ";",
                getWordAddress(byte_addr),
                Long.parseLong(codedInstruction, 2));
        debug.println("DATA: " + data + " [" + codedInstruction + "]");
        return data;
    }

    /**
     * Print DEAD from <code>bytefrom</code> to <code>byteuntil</code> in the
     * memory.
     *
     * @param bytefrom  initial address to be filled with DEAD
     * @param byteuntil last address to be filled with DEAD
     * @return DEAD from <code>bytefrom</code> to <code>byteuntil</code>
     * ready to be printed on *.mif
     */
    private static String fillDEADMEAT(long bytefrom, long byteuntil) {
        long delta = getWordAddress(byteuntil) - getWordAddress(bytefrom);
        if (delta > 0) {
            return String.format("[" + ADDR_RADIX.getFormat(
                            bytefrom == 0 ? WIDTH / 4 :
                                    getEffectiveWordAddrLen()) + ".." +
                            ADDR_RADIX.getFormat(
                                    bytefrom == 0 ? WIDTH / 4 :
                                            getEffectiveWordAddrLen()) +
                            "] : %s;\n", getWordAddress(bytefrom),
                    getWordAddress(byteuntil), DEAD_MEAT);
        } else if (delta == 0) {
            return String.format(ADDR_RADIX.getFormat(WIDTH / 4) + " : %s;\n",
                    getWordAddress(byteuntil), DEAD_MEAT);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Check if given <code>name</code> is a reserved word
     *
     * @param name {@link String} to be checked
     * @return <code>true</code> if <code>name</code> is reserved,
     * <code>false</code> otherwise
     */
    private static boolean isReserved(String name) {
        return opcodeDict.containsKey(name) || regDict.containsKey(name);
    }

    /**
     * Same as <code>parseOffset</code> except this does not allow negative
     * arguments.
     *
     * @param number string representation of the address
     * @return parsed address as <code>long</code>
     */
    private static long parseAddr(String number) {
        long val = parseOffset(number);
        if (val < 0) {
            throw new UnsupportedOperationException("Parsed negative value!");
        }
        return val;
    }

    /**
     * Parse various string representation of offset into <code>long</code>.
     *
     * @param number string representation of the offset
     * @return parsed offset as <code>long</code>
     */
    private static long parseOffset(String number) {
        String radixId = number.length() > 2 ? number.substring(0, 2) : "";
        long val;
        if (radixId.equals("0x")) {
            val = Long.parseLong(number.replace("0x", ""), 16);
        } else if (radixId.equals("0b")) {
            val = Long.parseLong(number.replace("0b", ""), 2);
        } else {
            val = Long.parseLong(number);
        }
        debug.println(
                "parseOffset: " + number + " -> " + Long.toBinaryString(val));
        return val;
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value.
     *
     * @param raw     raw immediate value read
     * @param bitMask mask to determined which part of the parsed value to
     *                be returned
     * @return processed immediate value in 16-bit binary
     */
    private static String parseImm(String raw, int bitMask) {
        int imm;
        try {
            long val = parseOffset(raw);
            if (val < -32768 && val > 32767) {
                System.err.printf(
                        "[WARNING] some information in %s might be loss\n",
                        raw);
            }
            imm = (int) (val & bitMask);
        } catch (Exception e) {
            if (addressLabelDict.containsKey(raw)) {
                int delta = (int) (addressLabelDict.get(raw) - byte_addr - 4);
                if (delta < -32768 && delta > 32767) {
                    throw new IllegalArgumentException(
                            "Out of range: " + delta);
                }
                imm = (int) (getWordAddress(delta) & bitMask);
                debug.printf("PC: %d LABEL: %s LABEL_ADDR: %d\n", byte_addr,
                        raw, addressLabelDict.get(raw));
            } else if (constLabelDict.containsKey(raw)) {
                long val = constLabelDict.get(raw);
                if (val < -32768 && val > 32767) {
                    System.err.printf(
                            "[WARNING] some information in %s might be loss\n",
                            raw);
                }
                imm = (int) (val & bitMask);
            } else {
                throw new IllegalArgumentException(
                        raw + " is not on the dictionary: " +
                                constLabelDict.toString());
            }
        }
        String result = String.format("%16s", Integer.toBinaryString(imm))
                .replace(' ', '0');
        debug.printf("IMM: raw: %s -> %s(%d)\n", raw, result, result.length());
        return result;
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value. This will take 16-LSB.
     *
     * @param raw raw immediate value read
     * @return processed immediate value in 16-bit binary
     */
    private static String parseImmLo(String raw) {
        return parseImm(raw, 0xFFFF);
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value. This will take 16-MSB.
     *
     * @param raw raw immediate value read
     * @return processed immediate value in 16-bit binary
     */
    private static String parseImmHi(String raw) {
        return parseImm(raw, 0xFFFF0000).substring(0, 16);
    }

    /**
     * @param args this program arguments
     * @return path to .a32 file
     */
    private static String parseArgs(String[] args) {
        String asmPath = null;
        if (args.length == 2 && args[0].equals("-v")) {
            debug = new DebugVerbose.EnableDebug();
            asmPath = args[1];
        } else if (args.length == 1) {
            debug = new DebugVerbose.DisableDebug();
            asmPath = args[0];
        } else {
            System.err.println(
                    ERROR_TAG + "usage: assemble [-v] PATH_TO_ASM_FILE");
            System.exit(-1);
        }
        return asmPath;
    }

    /**
     * @param asm path to .a32 file
     * @return Scanner that will read .a32 file passed
     */
    private static Scanner openAsm(File asm) {
        Scanner asmScanner = null;
        try {
            asmScanner = new Scanner(asm);
        } catch (FileNotFoundException e) {
            System.err.println(
                    ERROR_TAG + asm.getAbsolutePath() + " not found!");
            debug.printStackTrace(e);
            System.exit(-1);
        }
        return asmScanner;
    }

    /**
     * @param pathMif path where .mif file will be created
     * @return {@link PrintWriter} object to write to .mif file
     */
    private static PrintWriter writeMif(String pathMif) {
        Scanner consoleIn = new Scanner(System.in);
        File targetMif = new File(pathMif);
        if (targetMif.exists() && targetMif.isFile()) {
            System.out.print(targetMif.getPath() +
                    " already exists, proceed? (this will overwrite existing" +
                    " " +
                    "file) (y/n): ");
            String respond = consoleIn.nextLine().trim().toLowerCase();
            while (!respond.equals("y") && !respond.equals("n")) {
                System.out.print("Please respond (y/n): ");
                respond = consoleIn.nextLine().trim().toLowerCase();
            }
            if (respond.equals("n")) {
                System.out.println("Aborting operation...");
                System.exit(-1);
            }
        }
        FileWriter newMifFile = null;
        try {
            newMifFile = new FileWriter(targetMif);
        } catch (IOException e) {
            System.err.println(ERROR_TAG + "Failed to create .mif file: " +
                    e.getMessage());
            debug.printStackTrace(e);
            System.exit(-1);
        }
        return new PrintWriter(newMifFile);
    }

    // DRAGON AHEAD - TOTAL MESS

    private static int getAddrLen() {
        return (int) Math.ceil(WIDTH /
                (Math.log(ADDR_RADIX.getDecRep()) / Math.log(2)));
    }

    private static int getEffectiveWordAddrLen() {
        return (int) Math.ceil(Math.log(DEPTH * getAddrLen()) /
                Math.log(ADDR_RADIX.getDecRep()));
    }

    private static long getWordAddress(long byteaddr) {
        return byteaddr / (WIDTH / 8);
    }

    // BUILDING MAPS - SELF EXPLANATORY
    private static void buildDictionary() {
        opcodeDict = buildOpCodeDict();
        regDict = buildRegDict();
        instrsHandlerDict = buildAllInstrsHandlerDict();
        assemblerDirDict = buildAssemblerDirDict();
        constLabelDict = new HashMap<>();
        addressLabelDict = new HashMap<>();
    }

    private static Map<String, String> buildOpCodeDict() {
        Map<String, String> dict = new HashMap<>(16);
        //TODO: SORRY:((

        // ALU-R
        dict.put("add", "0000" + PrimaryOP.ALU_R.code());
        dict.put("sub", "0001" + PrimaryOP.ALU_R.code());
        dict.put("and", "0100" + PrimaryOP.ALU_R.code());
        dict.put("or", "0101" + PrimaryOP.ALU_R.code());
        dict.put("xor", "0110" + PrimaryOP.ALU_R.code());
        dict.put("nand", "1100" + PrimaryOP.ALU_R.code());
        dict.put("nor", "1101" + PrimaryOP.ALU_R.code());
        dict.put("xnor", "1110" + PrimaryOP.ALU_R.code());

        // ALU-I
        dict.put("addi", "0000" + PrimaryOP.ALU_I.code());
        dict.put("subi", "0001" + PrimaryOP.ALU_I.code());
        dict.put("andi", "0100" + PrimaryOP.ALU_I.code());
        dict.put("ori", "0101" + PrimaryOP.ALU_I.code());
        dict.put("xori", "0110" + PrimaryOP.ALU_I.code());
        dict.put("nandi", "1100" + PrimaryOP.ALU_I.code());
        dict.put("nori", "1101" + PrimaryOP.ALU_I.code());
        dict.put("xnori", "1110" + PrimaryOP.ALU_I.code());
        dict.put("mvhi", "1011" + PrimaryOP.ALU_I.code());

        // LOAD/STORE
        dict.put("sw", "0000" + PrimaryOP.STORE.code());
        dict.put("lw", "0000" + PrimaryOP.LOAD.code());

        // CMP-R
        dict.put("f", "0000" + PrimaryOP.CMP_R.code());
        dict.put("eq", "0001" + PrimaryOP.CMP_R.code());
        dict.put("lt", "0010" + PrimaryOP.CMP_R.code());
        dict.put("lte", "0011" + PrimaryOP.CMP_R.code());
        dict.put("t", "1000" + PrimaryOP.CMP_R.code());
        dict.put("ne", "1001" + PrimaryOP.CMP_R.code());
        dict.put("gte", "1010" + PrimaryOP.CMP_R.code());
        dict.put("gt", "1011" + PrimaryOP.CMP_R.code());

        // CMP-I
        dict.put("fi", "0000" + PrimaryOP.CMP_I.code());
        dict.put("eqi", "0001" + PrimaryOP.CMP_I.code());
        dict.put("lti", "0010" + PrimaryOP.CMP_I.code());
        dict.put("ltei", "0011" + PrimaryOP.CMP_I.code());
        dict.put("ti", "1000" + PrimaryOP.CMP_I.code());
        dict.put("nei", "1001" + PrimaryOP.CMP_I.code());
        dict.put("gtei", "1010" + PrimaryOP.CMP_I.code());
        dict.put("gti", "1011" + PrimaryOP.CMP_I.code());

        // BRANCH
        dict.put("bf", "0000" + PrimaryOP.BRANCH.code());
        dict.put("beq", "0001" + PrimaryOP.BRANCH.code());
        dict.put("blt", "0010" + PrimaryOP.BRANCH.code());
        dict.put("blte", "0011" + PrimaryOP.BRANCH.code());
        dict.put("beqz", "0101" + PrimaryOP.BRANCH.code());
        dict.put("bltz", "0110" + PrimaryOP.BRANCH.code());
        dict.put("bltez", "0111" + PrimaryOP.BRANCH.code());

        dict.put("bt", "1000" + PrimaryOP.BRANCH.code());
        dict.put("bne", "1001" + PrimaryOP.BRANCH.code());
        dict.put("bgte", "1010" + PrimaryOP.BRANCH.code());
        dict.put("bgt", "1011" + PrimaryOP.BRANCH.code());
        dict.put("bnez", "1101" + PrimaryOP.BRANCH.code());
        dict.put("bgtez", "1110" + PrimaryOP.BRANCH.code());
        dict.put("bgtz", "1111" + PrimaryOP.BRANCH.code());

        dict.put("jal", "0000" + PrimaryOP.JAL.code());
        return Collections.unmodifiableMap(dict);
    }

    private static Map<String, String> buildRegDict() {
        Map<String, String> dict = new HashMap<>(16);
        dict.put("a0", "0000");
        dict.put("a1", "0001");
        dict.put("a2", "0010");
        dict.put("a3", "0011");
        dict.put("t0", "0100");
        dict.put("t1", "0101");
        dict.put("s0", "0110");
        dict.put("s1", "0111");
        dict.put("s2", "1000");
        dict.put("r9", "1001");
        dict.put("r10", "1010");
        dict.put("r11", "1011");
        dict.put("gp", "1100");
        dict.put("fp", "1101");
        dict.put("sp", "1110");
        dict.put("ra", "1111");
        registerPattern =
                dict.keySet().toString().replaceAll("\\s*,\\s*", "|");
        registerPattern = registerPattern.substring(1,
                registerPattern.length() - 1);
        registerPattern =
                registerPattern + "|" + registerPattern.toUpperCase();
        debug.println("Register pattern: " + registerPattern);
        return Collections.unmodifiableMap(dict);
    }

    private static Map<String, Handler> buildPseudoInstrsHandlerDict() {
        Map<String, Handler> dict = new HashMap<>();
        dict.put("br", args -> {
            String actual = "beq r6, r6, " + args.group(7);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("beq").processArgs(args);
        });
        dict.put("not", args -> {
            String actual = "nand " + args.group(2) + ", " +
                    args.group(3) + ", " + args.group(3);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("nand").processArgs(args);
        });
        dict.put("call", args -> {
            String actual = "jal ra, " + args.group(7) + "(" +
                    args.group(8) + ")";
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("jal").processArgs(args);
        });
        dict.put("ret", args -> {
            String actual = "jal r9, 0(ra)";
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("jal").processArgs(args);
        });
        dict.put("jmp", args -> {
            String actual = "jal r9, " + args.group(7) + "(" +
                    args.group(8) + ")";
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("jal").processArgs(args);
        });
        dict.put("ble", args -> {
            String actual = "lte r6, " + args.group(2) + ", " +
                    args.group(3);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            debug.println(args.group(0));
            instrsHandlerDict.get("lte").processArgs(args);
            byte_addr += 4;
            actual = "bnez r6, " + args.group(4);
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("bnez").processArgs(args);
        });
        dict.put("ble", args -> {
            String actual = "gte r6, " + args.group(2) + ", " +
                    args.group(3);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            args.reset(actual);
            args.matches();
            debug.println(args.group(0));
            instrsHandlerDict.get("lte").processArgs(args);
            byte_addr += 4;
            actual = "bnez r6, " + args.group(4);
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("bnez").processArgs(args);
        });
        return dict;
    }

    // grouping number can be found at http://regexr.com/3bv78
    private static Map<String, Handler> buildAllInstrsHandlerDict() {
        String[] rd_imm_list = {
                 "beqz", "bltz", "bltez", "bnez", "bgtez", "bgtz"
        };
        Handler rd_imm = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(2).toLowerCase()) + "0000" +
                            parseImmLo(args.group(5)) +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        String[] rd_rs1_imm_list = {
                "addi", "subi", "andi", "ori", "xori", "nandi", "nori",
                "xnori", "fi", "eqi", "lti", "ltei", "ti",
                "nei", "gtei", "gti"
        };
        Handler rd_rs1_imm = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(2).toLowerCase()) +
                            regDict.get(args.group(3).toLowerCase()) +
                            parseImmLo(args.group(4)) +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        String[] rs1_rs2_imm_list = {
                "bf", "beq", "blt", "blte", "bt", "bne", "bgte", "bgt"
        };
        Handler rs1_rs2_imm = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(3).toLowerCase()) +
                            regDict.get(args.group(2).toLowerCase()) +
                            parseImmLo(args.group(4)) +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        String[] rd_rs1_rs2_list = {
                "add", "sub", "and", "or", "xor", "nand", "nor", "xnor", "f",
                "eq", "lt", "lte", "t", "ne", "gte", "gt"
        };
        Handler rd_rs1_rs2 = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(2).toLowerCase()) +
                            regDict.get(args.group(3).toLowerCase()) +
                            regDict.get(args.group(4).toLowerCase()) +
                            "000000000000" +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        String[] rd_imm_rs1_list = {
                "lw", "jal"
        };
        Handler rd_imm_rs1 = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(2).toLowerCase()) +
                            regDict.get(args.group(6).toLowerCase()) +
                            parseImmLo(args.group(5)) +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        String[] rs2_imm_rs1_list = {
                "sw"
        };
        Handler rs2_imm_rs1 = args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    regDict.get(args.group(6).toLowerCase()) +
                            regDict.get(args.group(2).toLowerCase()) +
                            parseImmLo(args.group(5)) +
                            opcodeDict.get(args.group(1).toLowerCase())));
        };

        Map<String, Handler> dict = new HashMap<>();
        for (String name : rd_imm_list) {
            dict.put(name, rd_imm);
        }
        for (String name : rd_rs1_imm_list) {
            dict.put(name, rd_rs1_imm);
        }
        for (String name : rs1_rs2_imm_list) {
            dict.put(name, rs1_rs2_imm);
        }
        for (String name : rd_rs1_rs2_list) {
            dict.put(name, rd_rs1_rs2);
        }
        for (String name : rd_imm_rs1_list) {
            dict.put(name, rd_imm_rs1);
        }
        for (String name : rs2_imm_rs1_list) {
            dict.put(name, rs2_imm_rs1);
        }
        dict.put("mvhi", new Handler() {
            @Override
            public void processArgs(Matcher args) {
                target.println(formatComment(args.group(0)));
                target.println(formatInstruction(
                        regDict.get(args.group(2).toLowerCase()) + "0000" +
                                parseImmHi(args.group(5)) +
                                opcodeDict.get(args.group(1).toLowerCase())));
            }
        });

        dict.putAll(buildPseudoInstrsHandlerDict());
        return Collections.unmodifiableMap(dict);
    }

    // grouping number can be found at http://regexr.com/3bv8i
    private static Map<String, Handler> buildAssemblerDirDict() {
        Map<String, Handler> dict = new HashMap<>();
        dict.put(".NAME", args -> {
            if (isReserved(args.group(2))) {
                throw new IllegalArgumentException(
                        "invalid used of reserved word");
            }
            constLabelDict.put(args.group(2), parseAddr(args.group(3)));
            debug.println("NAME_LABEL: new label " + args.group(2) + "->0x" +
                    Long.toHexString(constLabelDict.get(args.group(2)))
                            .toUpperCase());
        });
        dict.put(".ORIG", args -> {
            long newByte_addr = parseAddr(args.group(4));
            debug.printf("MEM_ADDR set to 0x%x from 0x%x\n", newByte_addr,
                    byte_addr);
            byte_addr = newByte_addr;
        });
        dict.put(".WORD", args -> {
            target.println(formatComment(args.group(0)));
            target.println(formatInstruction(
                    Long.toBinaryString(parseOffset(args.group(4)))));
            byte_addr += 4;
        });
        return Collections.unmodifiableMap(dict);
    }

    public interface Handler {
        void processArgs(Matcher args);
    }
}