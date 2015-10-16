package asm;

import static asm.assemble.*;

/**
 * {@link AssembleUtil} is a messy class to help encapsulate dirty trick used
 * to parse and formatting in the assemble process.
 *
 * @author Yoel Ivan (yivan3@gatech.edu)
 * @version 0.0a
 */
class AssembleUtil {
    enum Radix {
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

    static final int WIDTH = 32;    // multiple of 8 only!
    static final int DEPTH = 2048;
    static final Radix ADDR_RADIX = Radix.hex;
    static final Radix DATA_RADIX = Radix.hex;
    private static final String DEAD_MEAT = "DEAD";
    static final String ERROR_TAG = "[ERROR] ";

    /**
     * Format actual instruction read from *.a32 file as a comment in the
     * *.mif file
     *
     * @param actualInstruction actual instruction (no longer pseudo) read from
     *                          *.a32 file
     * @return formatted instruction ready to be printed as comment on .mif
     * file
     */
    static String formatComment(String actualInstruction) {
        return String.format("-- @ 0x" + ADDR_RADIX.getFormat(WIDTH / 4) +
                        " : %s", getCurrentByteAddr(),
                actualInstruction.toUpperCase().replaceFirst("\\s+", "\t"));
    }

    /**
     * Format translated instruction to the *.mif file
     *
     * @param codedInstruction translated instruction in 32-bit binary string
     * @return formatted translated instruction ready to be printed on .mif
     */
    static String formatInstruction(String codedInstruction) {
        assert codedInstruction.length() == WIDTH;
        String data = String.format(ADDR_RADIX.getFormat(WIDTH / 4) + " : " +
                        DATA_RADIX.getFormat(WIDTH / 4) + ";",
                getWordAddress(getCurrentByteAddr()),
                Long.parseLong(codedInstruction, 2));
        getDebug().println("DATA: " + data + " [" + codedInstruction + "]");
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
    static String fillDEADMEAT(long bytefrom, long byteuntil) {
        long delta = getWordAddress(byteuntil) - getWordAddress(bytefrom);
        if (delta > 0) {
            return String.format("[" + ADDR_RADIX.getFormat(WIDTH / 4) + ".." +
                            ADDR_RADIX.getFormat(WIDTH / 4) +
                            "] : %s;\n", getWordAddress(bytefrom),
                    getWordAddress(byteuntil), DEAD_MEAT);
        } else if (delta == 0) {
            return String.format(ADDR_RADIX.getFormat(WIDTH / 4) + " : %s;\n",
                    getWordAddress(byteuntil), DEAD_MEAT);
        }
        cleanExit("unable to fill " + bytefrom + " to " + byteuntil +
                " with DEAD");
        return null;
    }

    /**
     * Same as <code>parseOffset</code> except this does not allow negative
     * arguments.
     *
     * @param number string representation of the address
     * @return parsed address as <code>long</code>
     */
    static long parseAddr(String number) {
        long val = parseOffset(number, WIDTH);
        if (val < 0) {
            throw new UnsupportedOperationException("Parsed negative value!");
        }
        return val;
    }

    /**
     * Parse various string representation of offset into <code>long</code>.
     *
     * @param number string representation of the offset
     * @param len    expected binary representation length of the result
     * @return parsed offset as <code>long</code>
     */
    static long parseOffset(String number, int len) {
        if (len != 16 && len != 32) {
            cleanExit("unsupported length");
        }
        String radixId = number.length() > 2 ? number.substring(0, 2) : "";
        long val;
        switch (radixId) {
            case "0x": {
                Long temp = Long.valueOf(number.substring(2), 16);
                val = len == 32 ? temp.intValue() : temp.shortValue();
                break;
            }
            case "0b": {
                Long temp = Long.valueOf(number.substring(2), 2);
                val = len == 32 ? temp.intValue() : temp.shortValue();
                break;
            }
            default:
                val = Long.parseLong(number);
                break;
        }
        getDebug().println(
                "parseOffset: " + number + " -> " + Long.toBinaryString(val));
        if (val < Integer.MIN_VALUE && val > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Out of range: " + Long.toBinaryString(val));
        }
        return val;
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value.
     *
     * @param raw raw immediate value read
     * @return processed immediate value in 16-bit binary
     */
    static String parsePCRel(String raw) {
        assert raw != null;
        long delta;
        if (getAddressLabelDict().containsKey(raw)) {
            delta = getWordAddress(
                    getAddressLabelDict().get(raw) - getCurrentByteAddr() - 4);
            getDebug().printf("PC: %d LABEL: %s LABEL_ADDR: %d\n", getCurrentByteAddr(),
                    raw, getAddressLabelDict().get(raw));
        } else {
            delta = raw2Long(raw, 16);
        }
        if (delta < Short.MIN_VALUE && delta > Short.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Out of range: " + delta);
        }
        String result = String.format("%16s",
                Integer.toBinaryString((int) (delta & 0xFFFF)))
                .replace(' ', '0');
        getDebug().printf("PCREL: raw: %s -> %s(%d)\n", raw, result,
                result.length());
        return result;
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value.
     *
     * @param raw     raw immediate value read
     * @param bitMask mask to determined which part of the parsed value to
     *                be returned
     * @return processed immediate value in 32-bit binary
     */
    static String parseImm(String raw, int bitMask) {
        long val = raw2Long(raw, 32);
        if (val < Integer.MIN_VALUE && val > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Out of range: " + val);
        }
        String result = String.format("%32s",
                Integer.toBinaryString((int) (val & bitMask)))
                .replace(' ', '0');
        getDebug().printf("IMM: raw: %s -> %s(%d)\n", raw, result, result.length());
        assert result.length() == 32;
        return result;
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value. This will take 16-LSB.
     *
     * @param raw raw immediate value read
     * @return processed immediate value in 16-bit binary
     */
    static String parseImmLo(String raw) {
        return parseShortImm(raw, 0xFFFF);
    }

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value. This will take 16-MSB.
     *
     * @param raw raw immediate value read
     * @return processed immediate value in 16-bit binary
     */
    static String parseImmHi(String raw) {
        return parseImm(raw, 0xFFFF0000).substring(0, 16);
    }

    // TOO MANY DRAGONs AHEAD - NEVER EVER CALL DIRECTLY

    /**
     * Process IMMEDIATE value which can be label or raw value into a
     * valid value.
     *
     * @param raw raw immediate value read     *
     * @param len    expected binary representation length of the result
     * @return processed immediate value in long integer
     */
    private static long raw2Long(String raw, int len) {
        long val;
        try {
            val = parseOffset(raw, len);
        } catch (Exception e) {
            getDebug().printStackTrace(e);
            if (getAddressLabelDict().containsKey(raw)) {
                val = getWordAddress(getAddressLabelDict().get(raw));
                getDebug().printf("PC: %d LABEL: %s LABEL_ADDR: %d\n", getCurrentByteAddr(),
                        raw, getAddressLabelDict().get(raw));
            } else if (getConstLabelDict().containsKey(raw)) {
                // assume that .NAME value is in word addressing instead of
                // byte addressing
                val = getConstLabelDict().get(raw);
            } else {
                throw new IllegalArgumentException(
                        raw + " is not on the dictionary: " +
                                getConstLabelDict().toString());
            }
        }
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
    private static String parseShortImm(String raw, int bitMask) {
        long val = raw2Long(raw, 16);
        if (val < Short.MIN_VALUE && val > Short.MAX_VALUE) {
            System.err.printf(
                    "[WARNING] some information in %s might be loss\n", raw);
        }
        String result = String.format("%16s",
                Integer.toBinaryString((int) (val & bitMask)))
                .replace(' ', '0');
        getDebug().printf("SHORT_IMM: raw: %s -> %s(%d)\n", raw, result,
                result.length());
        assert result.length() == 16;
        return result;
    }

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
}