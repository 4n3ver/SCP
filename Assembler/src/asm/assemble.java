package asm;

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

import static asm.AssembleUtil.*;

/**
 * {@link assemble} is a messy class that seems to be able to convert Test2.a32
 * and Sorter2.a32 to Test2.mif and Sorter2.mif correctly.
 *
 * @author Yoel Ivan (yivan3@gatech.edu)
 * @author Wenduo Yang (wyang73@gatech.edu)
 * @version 0.0a
 */
public class assemble {
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

    // read only maps
    static Map<String, Handler> instrsHandlerDict;
    static Map<String, Handler> assemblerDirDict;
    static Map<String, String> opcodeDict;
    static Map<String, String> regDict;

    // r-w maps
    private static Map<String, Long> constLabelDict;
    private static Map<String, Long> addressLabelDict;

    private static String registerPattern;
    private static String mifPath;
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
        mifPath = asmPath.replace(".a32", ".mif");
        target = writeMif(mifPath);

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
                "([A-Za-z][a-zA-Z0-9]+)\\s*=\\s*((?:\\-?[0-9]+|[A-Za-z0-9]+)" +
                ")\\b|" +
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
        target.print(fillDEADMEAT(byte_addr,
                DEPTH * WIDTH / 8 - 1));
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
                                            parseAddr(
                                                    parsed.group(4));
                                    if (newByte_addr > byte_addr) {
                                        target.print(fillDEADMEAT(
                                                byte_addr,
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
                            target.println(formatComment(parsed.group(0)));
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
                src.close();
                cleanExit("at line " + line_num + ": " + line);
            }
        }
    }

    /**
     * If asssembling process fail, remove incomplete mif file
     */
    static void cleanExit(String errMsg) {
        System.err.println(ERROR_TAG + errMsg);
        target.close();
        new File(mifPath).delete();
        System.exit(-1);
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
                        checkReserved(label);
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
                src.close();
                cleanExit("at line " + line_num + ": " + line);
            }
        }
        debug.println("ADDR_LABEL: " + addressLabelDict.toString());
    }

    /**
     * Check if given <code>name</code> is a reserved word
     *
     * @param name {@link String} to be checked
     * <code>false</code> otherwise
     */
    private static void checkReserved(String name) {
        if (opcodeDict.containsKey(name) || regDict.containsKey(name)) {
            throw new IllegalArgumentException(
                    "invalid use of reserved word");
        } else if (constLabelDict.containsKey(name) ||
                addressLabelDict.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate label is used");
        }
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
            cleanExit("usage: assemble [-v] PATH_TO_ASM_FILE");
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
            debug.printStackTrace(e);
            cleanExit(asm.getAbsolutePath() + " not found!");
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
                cleanExit("Aborting operation...");
            }
        }
        FileWriter newMifFile = null;
        try {
            newMifFile = new FileWriter(targetMif);
        } catch (IOException e) {
            debug.printStackTrace(e);
            cleanExit("Failed to create .mif file: " + e.getMessage());
        }
        assert newMifFile != null;
        return new PrintWriter(newMifFile);
    }

    // Accessor methods - self explanatory
    static long getCurrentByteAddr() {
        return byte_addr;
    }

    static Map<String, Long> getAddressLabelDict() {
        return Collections.unmodifiableMap(addressLabelDict);
    }

    static Map<String, Long> getConstLabelDict() {
        return Collections.unmodifiableMap(constLabelDict);
    }

    static DebugVerbose getDebug() {
        return debug;
    }

    // DRAGON AHEAD - TOTAL MESS
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
        dict.put("r6", "0110");
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

    // grouping number can be found at http://regexr.com/3bv8i
    private static Map<String, Handler> buildAssemblerDirDict() {
        Map<String, Handler> dict = new HashMap<>();
        dict.put(".NAME", args -> {
            checkReserved(args.group(2));
            constLabelDict.put(args.group(2),
                    parseOffset(args.group(3), WIDTH));
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
            target.println(
                    formatInstruction(parseImm(args.group(4), 0xFFFFFFFF)));
            byte_addr += 4;
        });
        return Collections.unmodifiableMap(dict);
    }

    // grouping number can be found at http://regexr.com/3bv78
    private static Map<String, Handler> buildAllInstrsHandlerDict() {
        String[] rd_imm_list = {
                "beqz", "bltz", "bltez", "bnez", "bgtez", "bgtz"
        };
        Handler rd_imm = args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) + "0000" +
                        parsePCRel(args.group(5)) +
                        opcodeDict.get(args.group(1).toLowerCase())));

        String[] rd_rs1_imm_list = {
                "addi", "subi", "andi", "ori", "xori", "nandi", "nori",
                "xnori", "fi", "eqi", "lti", "ltei", "ti",
                "nei", "gtei", "gti"
        };
        Handler rd_rs1_imm = args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) +
                        regDict.get(args.group(3).toLowerCase()) +
                        parseImmLo(args.group(4)) +
                        opcodeDict.get(args.group(1).toLowerCase())));

        String[] rs1_rs2_imm_list = {
                "bf", "beq", "blt", "blte", "bt", "bne", "bgte", "bgt"
        };
        Handler rs1_rs2_imm = args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) +
                        regDict.get(args.group(3).toLowerCase()) +
                        parsePCRel(args.group(4)) +
                        opcodeDict.get(args.group(1).toLowerCase())));

        String[] rd_rs1_rs2_list = {
                "add", "sub", "and", "or", "xor", "nand", "nor", "xnor", "f",
                "eq", "lt", "lte", "t", "ne", "gte", "gt"
        };
        Handler rd_rs1_rs2 = args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) +
                        regDict.get(args.group(3).toLowerCase()) +
                        regDict.get(args.group(4).toLowerCase()) +
                        "000000000000" +
                        opcodeDict.get(args.group(1).toLowerCase())));

        String[] rd_imm_rs1_list = {
                "lw", "jal"
        };
        Handler rd_imm_rs1 = args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) +
                        regDict.get(args.group(6).toLowerCase()) +
                        parseImmLo(args.group(5)) +
                        opcodeDict.get(args.group(1).toLowerCase())));

        String[] rs2_imm_rs1_list = {
                "sw"
        };
        Handler rs2_imm_rs1 = args -> target.println(formatInstruction(
                regDict.get(args.group(6).toLowerCase()) +
                        regDict.get(args.group(2).toLowerCase()) +
                        parseImmLo(args.group(5)) +
                        opcodeDict.get(args.group(1).toLowerCase())));

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
        dict.put("mvhi", args -> target.println(formatInstruction(
                regDict.get(args.group(2).toLowerCase()) + "0000" +
                        parseImmHi(args.group(5)) +
                        opcodeDict.get(args.group(1).toLowerCase()))));
        dict.putAll(buildPseudoInstrsHandlerDict());
        return Collections.unmodifiableMap(dict);
    }

    // not actual instruction, implemented by calling handler above
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
            String actual = "lte r9, " + args.group(2) + ", " +
                    args.group(3);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            Matcher first = args.pattern().matcher(actual);
            first.matches();
            instrsHandlerDict.get("lte").processArgs(first);
            byte_addr += 4;
            actual = "bnez r9, " + args.group(4);
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("bnez").processArgs(args);
        });
        dict.put("bge", args -> {
            String actual = "gte r9, " + args.group(2) + ", " +
                    args.group(3);
            debug.println(
                    "converting (" + args.group(0) + ") to (" + actual + ")");
            Matcher first = args.pattern().matcher(actual);
            first.matches();
            instrsHandlerDict.get("lte").processArgs(first);
            byte_addr += 4;
            actual = "bnez r9, " + args.group(4);
            args.reset(actual);
            args.matches();
            instrsHandlerDict.get("bnez").processArgs(args);
        });
        return dict;
    }

    public interface Handler {
        void processArgs(Matcher args);
    }
}