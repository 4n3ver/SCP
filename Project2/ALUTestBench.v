`timescale 1ns / 1ps
module ALUTestBench;
	reg aluAltOp;
	reg [31:0] data1, data2;
	reg [3:0] func;
	wire [31:0] dataOut;
	wire beqOut;

	ALU alu(aluAltOp, data1, func, data2, dataOut, beqOut);
	
	initial begin
		
		#100;
		aluAltOp = 1'b0;
		func = 4'b0101;
		data1 = 32'd20;
		data2 = 32'd37;
		
		#100;
		
		aluAltOp = 1'b0;
		func = 4'b0001;
		data1 = 32'h00000008;
		data2 = 32'hFFFFFFDF;
//		
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