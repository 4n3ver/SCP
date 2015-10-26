library verilog;
use verilog.vl_types.all;
entity SCProcController is
    port(
        memWrEn         : out    vl_logic;
        regFileWrEn     : out    vl_logic;
        aluAltOp        : out    vl_logic;
        pcSel           : out    vl_logic_vector(1 downto 0);
        aluSrc2Sel      : out    vl_logic;
        regFileWrSel    : out    vl_logic_vector(1 downto 0);
        regFileRd0Index : out    vl_logic_vector(3 downto 0);
        regFileRd1Index : out    vl_logic_vector(3 downto 0);
        regFileWrIndex  : out    vl_logic_vector(3 downto 0);
        aluFunc         : out    vl_logic_vector(3 downto 0);
        imm             : out    vl_logic_vector(15 downto 0);
        aluOut          : in     vl_logic;
        instruction     : in     vl_logic_vector(31 downto 0)
    );
end SCProcController;
