// Yoel Ivan (yivan3@gatech.edu)

`timescale 1ns / 1ps
module BugTestBench;
	reg rfWrEn;
	reg [3:0] aluFunc;
	reg [31:0] imm;
	wire rfDataOut2;
	Bug crap(rfDataOut2, rfWrEn, aluFunc, imm);
	reg clk;
	reg [4:0] i;
	
	initial begin
		i = 0;
		clk = 0;
		rfWrEn = 0;
		aluFunc = 0;
		imm = 0;
		
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			rfWrEn = 1;
			imm = 5;
			#10
			$display("Out1: %d", rfDataOut2);
			rfWrEn = 0;
		end		
		#100;
		$finish;
		
	end
	
	always begin
		#5 clk = !clk;
	end
endmodule