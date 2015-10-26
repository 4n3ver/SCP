module Bug(out, rfWrEn, aluFunc, imm);
	input rfWrEn;
	input [3:0] aluFunc;
	input [31:0] imm;
	output out;
	

	wire [31:0] rfDataIn, rfDataOut1, rfDataOut2, aluOut;
	assign out = aluOut;
	RF regFile(clk, rfWrEn, 4'b0, 4'b0, 4'b0, rfDataIn, rfDataOut1, rfDataOut2);
	newALU alu1(aluOut, rfDataOut1, imm, {1'b0, aluFunc});
	mux4 dataMux (2'b00, aluOut, 0, 0, rfDataIn);

endmodule