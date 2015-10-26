library verilog;
use verilog.vl_types.all;
entity mux4 is
    port(
        sel             : in     vl_logic_vector(1 downto 0);
        data0           : in     vl_logic_vector(31 downto 0);
        data1           : in     vl_logic_vector(31 downto 0);
        data2           : in     vl_logic_vector(31 downto 0);
        data3           : in     vl_logic_vector(31 downto 0);
        dataOut         : out    vl_logic_vector(31 downto 0)
    );
end mux4;
