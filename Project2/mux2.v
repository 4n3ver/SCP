module mux2(sel, data1, data2, dataOut);
	parameter DATA_BIT_WIDTH = 32;
	
	input sel;
	input [DATA_BIT_WIDTH - 1:0] data1;
	input [DATA_BIT_WIDTH - 1:0] data2;
	output [DATA_BIT_WIDTH - 1:0] dataOut;
	reg [DATA_BIT_WIDTH - 1:0] dataOut;
	
	always @(*) begin
		if (sel)
			dataOut <= data2;
		else
			dataOut <= data1;
	end
endmodule