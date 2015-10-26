`timescale 1ns / 1ps
module muxTestBench;
	reg [31:0] data1, data2, data3, data4;
	reg [1:0] sel;
	wire [31:0] dataOut;

	mux4 m(sel, data1, data2, data3, data4, dataOut);
	
	initial begin
		
		#100;
		data1 = 32'd0;
		data2 = 32'd1;
		data3 = 32'd2;
		data4 = 32'd3;
		sel = 2'b00;
		
		#100;
		data1 = 32'd0;
		data2 = 32'd1;
		data3 = 32'd2;
		data4 = 32'd3;
		sel = 2'b01;
		
		

//		#100
//		
//		
//		aluAltOp = 1'b1;
//		func = 4'b1001;
//		data1 = 32'd10;
//		data2 = 32'd10;
		
		#100
		
		
$finish;
		
	end
	
endmodule