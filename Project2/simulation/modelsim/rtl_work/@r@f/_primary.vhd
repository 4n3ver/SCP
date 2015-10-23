library verilog;
use verilog.vl_types.all;
entity RF is
    generic(
        indexBit        : integer := 4
    );
    port(
        clk             : in     vl_logic;
        regFileWrEn     : in     vl_logic;
        regFileRd0Index : in     vl_logic_vector(3 downto 0);
        regFileRd1Index : in     vl_logic_vector(3 downto 0);
        regFileWrIndex  : in     vl_logic_vector(3 downto 0);
        dataIn          : in     vl_logic_vector(31 downto 0);
        dataOut0        : out    vl_logic_vector(31 downto 0);
        dataOut1        : out    vl_logic_vector(31 downto 0)
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of indexBit : constant is 1;
end RF;
