module mux4(sel, data0, data1, data2, data3, dataOut);
	input [1:0] sel;
	input [31:0] data0;
	input [31:0] data1;
	input [31:0] data2;
	input [31:0] data3;
	output [31:0] dataOut;
	
	reg [31:0] dataOut;
	always @(sel or data0 or data1 or data2 or data3) begin
		if (sel == 2'b00)
			dataOut = data0;
		else if (sel == 2'b01)
			dataOut = data1;
		else if (sel == 2'b10)
			dataOut = data2;
		else
			dataOut = data3;
	end
endmodule