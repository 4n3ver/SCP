module RegFile(clk, regFileWrEn, regFileRd0Index, regFileRd1Index, regFileWrIndex, dataIn, dataOut0, dataOut1, rst);
	parameter indexBit = 4;
	
	input clk, regFileWrEn, rst;
	input [3:0] regFileRd0Index, regFileRd1Index, regFileWrIndex;
	input [31:0] dataIn;
	output [31:0] dataOut0, dataOut1;
	
	wire [31:0] out[0:15];
	reg [15:0] WrEn;
	assign dataOut0 = out[regFileRd0Index];
	assign dataOut1 = out[regFileRd1Index];

	always @(posedge clk) begin
		case (regFileWrIndex)
			0	:	WrEn <= 16'b0000000000000001;
			1	:	WrEn <= 16'b0000000000000010;
			2	:	WrEn <= 16'b0000000000000100;
			3	:	WrEn <= 16'b0000000000001000;
			4	:	WrEn <= 16'b0000000000010000;
			5	:	WrEn <= 16'b0000000000100000;
			6	:	WrEn <= 16'b0000000001000000;
			7	:	WrEn <= 16'b0000000010000000;
			8	:	WrEn <= 16'b0000000100000000;
			9	:	WrEn <= 16'b0000001000000000;
			10	:	WrEn <= 16'b0000010000000000;
			11	:	WrEn <= 16'b0000100000000000;
			12	:	WrEn <= 16'b0001000000000000;
			13	:	WrEn <= 16'b0010000000000000;
			14	:	WrEn <= 16'b0100000000000000;
			15	:	WrEn <= 16'b1000000000000000;
			default: WrEn <= 16'b0;	
		endcase
	end
	
	Register r0(clk, rst, WrEn[0], dataIn, out[0]);
	Register r1(clk, rst, WrEn[1], dataIn, out[1]);
	Register r2(clk, rst, WrEn[2], dataIn, out[2]);
	Register r3(clk, rst, WrEn[3], dataIn, out[3]);
	Register r4(clk, rst, WrEn[4], dataIn, out[4]);
	Register r5(clk, rst, WrEn[5], dataIn, out[5]);
	Register r6(clk, rst, WrEn[6], dataIn, out[6]);
	Register r7(clk, rst, WrEn[7], dataIn, out[7]);
	Register r8(clk, rst, WrEn[8], dataIn, out[8]);
	Register r9(clk, rst, WrEn[9], dataIn, out[9]);
	Register r10(clk, rst, WrEn[10], dataIn, out[10]);
	Register r11(clk, rst, WrEn[11], dataIn, out[11]);
	Register r12(clk, rst, WrEn[12], dataIn, out[12]);
	Register r13(clk, rst, WrEn[13], dataIn, out[13]);
	Register r14(clk, rst, WrEn[14], dataIn, out[14]);
	Register r15(clk, rst, WrEn[15], dataIn, out[15]);
endmodule
