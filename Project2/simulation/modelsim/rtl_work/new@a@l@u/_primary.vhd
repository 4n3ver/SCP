library verilog;
use verilog.vl_types.all;
entity newALU is
    generic(
        ADD             : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi0, Hi0);
        SUB             : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi0, Hi1);
        \AND\           : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi0, Hi0);
        \OR\            : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi0, Hi1);
        \XOR\           : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi1, Hi0);
        \NAND\          : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi0, Hi0);
        \NOR\           : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi0, Hi1);
        NXOR            : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi1, Hi0);
        MVHI            : vl_logic_vector(3 downto 0) := (Hi1, Hi0, Hi1, Hi1);
        F               : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi0, Hi0);
        EQ              : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi0, Hi1);
        LT              : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi1, Hi0);
        LTE             : vl_logic_vector(3 downto 0) := (Hi0, Hi0, Hi1, Hi1);
        EQZ             : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi0, Hi1);
        LTZ             : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi1, Hi0);
        LTEZ            : vl_logic_vector(3 downto 0) := (Hi0, Hi1, Hi1, Hi1);
        T               : vl_logic_vector(3 downto 0) := (Hi1, Hi0, Hi0, Hi0);
        NE              : vl_logic_vector(3 downto 0) := (Hi1, Hi0, Hi0, Hi1);
        GTE             : vl_logic_vector(3 downto 0) := (Hi1, Hi0, Hi1, Hi0);
        GT              : vl_logic_vector(3 downto 0) := (Hi1, Hi0, Hi1, Hi1);
        NEZ             : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi0, Hi1);
        GTEZ            : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi1, Hi0);
        GTZ             : vl_logic_vector(3 downto 0) := (Hi1, Hi1, Hi1, Hi1)
    );
    port(
        \out\           : out    vl_logic_vector(31 downto 0);
        a               : in     vl_logic_vector(31 downto 0);
        b               : in     vl_logic_vector(31 downto 0);
        func            : in     vl_logic_vector(4 downto 0)
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of ADD : constant is 2;
    attribute mti_svvh_generic_type of SUB : constant is 2;
    attribute mti_svvh_generic_type of \AND\ : constant is 2;
    attribute mti_svvh_generic_type of \OR\ : constant is 2;
    attribute mti_svvh_generic_type of \XOR\ : constant is 2;
    attribute mti_svvh_generic_type of \NAND\ : constant is 2;
    attribute mti_svvh_generic_type of \NOR\ : constant is 2;
    attribute mti_svvh_generic_type of NXOR : constant is 2;
    attribute mti_svvh_generic_type of MVHI : constant is 2;
    attribute mti_svvh_generic_type of F : constant is 2;
    attribute mti_svvh_generic_type of EQ : constant is 2;
    attribute mti_svvh_generic_type of LT : constant is 2;
    attribute mti_svvh_generic_type of LTE : constant is 2;
    attribute mti_svvh_generic_type of EQZ : constant is 2;
    attribute mti_svvh_generic_type of LTZ : constant is 2;
    attribute mti_svvh_generic_type of LTEZ : constant is 2;
    attribute mti_svvh_generic_type of T : constant is 2;
    attribute mti_svvh_generic_type of NE : constant is 2;
    attribute mti_svvh_generic_type of GTE : constant is 2;
    attribute mti_svvh_generic_type of GT : constant is 2;
    attribute mti_svvh_generic_type of NEZ : constant is 2;
    attribute mti_svvh_generic_type of GTEZ : constant is 2;
    attribute mti_svvh_generic_type of GTZ : constant is 2;
end newALU;
