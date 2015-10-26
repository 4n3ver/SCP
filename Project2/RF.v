module RF(clk, regFileWrEn, regFileRd0Index, regFileRd1Index, regFileWrIndex, dataIn, dataOut0, dataOut1);
	parameter indexBit = 4;
	
	input clk, regFileWrEn;
	input [3:0] regFileRd0Index, regFileRd1Index, regFileWrIndex;
	input [31:0] dataIn;
	output [31:0] dataOut0, dataOut1;
	reg [31:0] regData [0:15];
	
	always @(posedge clk)
	begin
		if (regFileWrEn)
		begin
			regData[regFileWrIndex] <= dataIn;
		end
	end
	assign dataOut0 = regData[regFileRd0Index];
	assign dataOut1 = regData[regFileRd1Index];
	
	
//	wire [31:0] out[0:15];
//	reg [31:0] dataOut0, dataOut1;
//	reg regFileWrEn0, regFileWrEn1, regFileWrEn2, regFileWrEn3, regFileWrEn4, regFileWrEn5, regFileWrEn6, regFileWrEn7, regFileWrEn8, regFileWrEn9;
//	reg regFileWrEn10, regFileWrEn11, regFileWrEn12, regFileWrEn13, regFileWrEn14, regFileWrEn15;
//	
//	Register r0(clk, 1'b0, regFileWrEn0, dataIn, out[0]);
//	Register r1(clk, 1'b0, regFileWrEn1, dataIn, out[1]);
//	Register r2(clk, 1'b0, regFileWrEn2, dataIn, out[2]);
//	Register r3(clk, 1'b0, regFileWrEn3, dataIn, out[3]);
//	Register r4(clk, 1'b0, regFileWrEn4, dataIn, out[4]);
//	Register r5(clk, 1'b0, regFileWrEn5, dataIn, out[5]);
//	Register r6(clk, 1'b0, regFileWrEn6, dataIn, out[6]);
//	Register r7(clk, 1'b0, regFileWrEn7, dataIn, out[7]);
//	Register r8(clk, 1'b0, regFileWrEn8, dataIn, out[8]);
//	Register r9(clk, 1'b0, regFileWrEn9, dataIn, out[9]);
//	Register r10(clk, 1'b0, regFileWrEn10, dataIn, out[10]);
//	Register r11(clk, 1'b0, regFileWrEn11, dataIn, out[11]);
//	Register r12(clk, 1'b0, regFileWrEn12, dataIn, out[12]);
//	Register r13(clk, 1'b0, regFileWrEn13, dataIn, out[13]);
//	Register r14(clk, 1'b0, regFileWrEn14, dataIn, out[14]);
//	Register r15(clk, 1'b0, regFileWrEn15, dataIn, out[15]);
//	
//	always @(posedge clk) begin
//		case (regFileWrIndex)
//			
//			default:
//			begin
//				regFileWrEn0 <= 0;
//				regFileWrEn1 <= 0;
//				regFileWrEn2 <= 0;
//				regFileWrEn3 <= 0;
//				regFileWrEn4 <= 0;
//				regFileWrEn5 <= 0;
//				regFileWrEn6 <= 0;
//				regFileWrEn7 <= 0;
//				regFileWrEn8 <= 0;
//				regFileWrEn9 <= 0;
//				regFileWrEn10 <= 0;
//				regFileWrEn11 <= 0;
//				regFileWrEn12 <= 0;
//				regFileWrEn13 <= 0;
//				regFileWrEn14 <= 0;
//				regFileWrEn15 <= 0;
//			end
//		endcase
//	end
//	//need completion
	
endmodule