library verilog;
use verilog.vl_types.all;
entity ALU is
    port(
        aluAltOp        : in     vl_logic;
        data1           : in     vl_logic_vector(31 downto 0);
        func            : in     vl_logic_vector(3 downto 0);
        data2           : in     vl_logic_vector(31 downto 0);
        dataOut         : out    vl_logic_vector(31 downto 0);
        beqOut          : out    vl_logic
    );
end ALU;
