module branchALU(pc4, pcB, pcOut);
	input [31:0] pc4;
	input [31:0] pcB;
	output [31:0] pcOut;
	
//	wire signed [31:0] signedPC;
//	assign signedPC = pcB;
//	assign pcOut = pc4 + (signedPC << 2);
	
	assign pcOut = pc4 + (pcB << 2);
endmodule