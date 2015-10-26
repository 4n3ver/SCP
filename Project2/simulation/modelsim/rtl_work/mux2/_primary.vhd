library verilog;
use verilog.vl_types.all;
entity mux2 is
    generic(
        DATA_BIT_WIDTH  : integer := 32
    );
    port(
        sel             : in     vl_logic;
        data1           : in     vl_logic_vector;
        data2           : in     vl_logic_vector;
        dataOut         : out    vl_logic_vector
    );
    attribute mti_svvh_generic_type : integer;
    attribute mti_svvh_generic_type of DATA_BIT_WIDTH : constant is 1;
end mux2;
