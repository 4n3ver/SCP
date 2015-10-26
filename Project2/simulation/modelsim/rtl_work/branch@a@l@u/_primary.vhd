library verilog;
use verilog.vl_types.all;
entity branchALU is
    port(
        pc4             : in     vl_logic_vector(31 downto 0);
        pcB             : in     vl_logic_vector(31 downto 0);
        pcOut           : out    vl_logic_vector(31 downto 0)
    );
end branchALU;
