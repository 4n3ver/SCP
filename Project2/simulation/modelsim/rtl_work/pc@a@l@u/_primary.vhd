library verilog;
use verilog.vl_types.all;
entity pcALU is
    port(
        pcIn            : in     vl_logic_vector(31 downto 0);
        pcOut           : out    vl_logic_vector(31 downto 0)
    );
end pcALU;
