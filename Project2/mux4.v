module mux4(sel, data0, data1, data2, data3, dataOut);
	parameter DATA_BIT_WIDTH = 32;
	
	input [1:0] sel;
	input [DATA_BIT_WIDTH - 1:0] data0;
	input [DATA_BIT_WIDTH - 1:0] data1;
	input [DATA_BIT_WIDTH - 1:0] data2;
	input [DATA_BIT_WIDTH - 1:0] data3;
	output [DATA_BIT_WIDTH - 1:0] dataOut;
	
	reg [DATA_BIT_WIDTH - 1:0] dataOut;
	always @(*) begin
		if (sel == 2'b00)
			dataOut <= data0;
		else if (sel == 2'b01)
			dataOut <= data1;
		else if (sel == 2'b10)
			dataOut <= data2;
		else
			dataOut <= data3;
	end
endmodule