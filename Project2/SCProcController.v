// Yoel Ivan (yivan3@gatech.edu)

module SCProcController(memWrEn, regFileWrEn, aluAltOp, pcSel, aluSrc2Sel, 
						regFileWrSel, regFileRd0Index, regFileRd1Index, 
						regFileWrIndex, aluFunc, imm, aluOut, instruction);					
	parameter 	aluSrc2Sel_RS2 = 1'b0,
				aluSrc2Sel_IMM = 1'b1;
	// use this value for the pcSel Mux				
	parameter [1:0] pcSel_4			= 2'b00,
					pcSel_4IMM		= 2'b01,
					pcSel_RS1IMM 	= 2'b10;
	// use this value for the regFileWrSel Mux				
	parameter [1:0] regFileWrSel_ALU = 2'b00,
					regFileWrSel_MEM = 2'b01,
					regFileWrSel_PC4 = 2'b10;
	
	input aluOut;
	input [31:0] instruction;
	
	output memWrEn, regFileWrEn, aluAltOp, aluSrc2Sel;
	output [1:0] pcSel, regFileWrSel;
	output [3:0] regFileRd0Index, regFileRd1Index, regFileWrIndex, aluFunc;
	output [15:0] imm;
	
	wire [3:0] opcode;
	assign opcode = instruction[3:0];
	
	assign aluFunc = instruction[7:4];
	assign imm = instruction[23:8];
	assign regFileWrIndex = instruction[31:28];
	
	reg memWrEn, regFileWrEn, aluAltOp, aluSrc2Sel;
	reg [1:0] pcSel, regFileWrSel;
	reg [3:0] regFileRd0Index, regFileRd1Index;

	always@(*) begin
		case (opcode[1:0])
			2'b00: begin
				case (opcode[3:2]) 
					2'b00: begin	// ALU-R
						regFileRd1Index = instruction[23:20];
						aluSrc2Sel = aluSrc2Sel_RS2;
					end
					2'b10: begin	// ALU-I
						regFileRd1Index = 4'bxxxx;
						aluSrc2Sel = aluSrc2Sel_IMM;
					end
					default: begin	// INVALID
						regFileRd1Index = 4'bxxxx;
						aluSrc2Sel = 1'bx;
					end
				endcase		
				regFileWrEn = 1'b1;
				regFileRd0Index = instruction[27:24];		
				aluAltOp = 1'b0;		
				pcSel = pcSel_4;
				regFileWrSel = regFileWrSel_ALU;
				memWrEn = 1'b0;
			end
			2'b01: begin
				case (opcode[3:2])
					2'b01: begin	// STORE
						regFileWrSel = 2'bxx;
						regFileWrEn = 1'b0;
						regFileRd0Index = instruction[31:28];
						regFileRd1Index = instruction[27:24];
						memWrEn = 1'b1;
					end
					2'b10: begin	// LOAD
						regFileWrSel = regFileWrSel_MEM;
						regFileWrEn = 1'b1;
						regFileRd0Index = instruction[27:24];
						regFileRd1Index = 4'bxxxx;
						memWrEn = 1'b0;
					end
					default: begin	// INVALID
						regFileWrSel = 2'bxx;
						regFileWrEn = 1'bx;
						regFileRd0Index = 4'bxxxx;
						regFileRd1Index = 4'bxxxx;
						memWrEn = 1'bx;
					end
				endcase		
				aluAltOp = 1'b0;				
				pcSel = pcSel_4;
				aluSrc2Sel = aluSrc2Sel_IMM;	
			end
			2'b10: begin
				case (opcode[3:2]) 
					2'b01: begin	// BRANCH
						regFileWrEn = 1'b0;
						regFileWrSel = 2'bxx;
						regFileRd0Index = instruction[31:28];
						regFileRd1Index = instruction[27:24];
						aluSrc2Sel = aluSrc2Sel_RS2;
					end
					default: begin
						case (opcode[3:2])
							2'b00: begin	// CMP-R
								regFileRd1Index = instruction[23:20];
								aluSrc2Sel = aluSrc2Sel_RS2;
							end
							2'b10: begin	// CMP-I
								regFileRd1Index = 4'bxxxx;
								aluSrc2Sel = aluSrc2Sel_IMM;
							end
							default: begin
								regFileRd1Index = 4'bxxxx;
								aluSrc2Sel = 1'bx;
							end
						endcase
						regFileWrEn = 1'b1;
						regFileRd0Index = instruction[27:24];
						regFileWrSel = regFileWrSel_ALU;
					end
				endcase
				if (aluOut && opcode[3:2] == 2'b01) begin
					pcSel = pcSel_4IMM;
				end else begin
					pcSel = pcSel_4;
				end
				aluAltOp = 1'b1;
				memWrEn = 1'b0;
			end
			2'b11: begin
				case (opcode[3:2])
					2'b10: begin	// JAL
						regFileRd0Index = instruction[27:24];		
						regFileWrSel = regFileWrSel_PC4;
						regFileWrEn = 1'b1;
						aluSrc2Sel = aluSrc2Sel_IMM;
						aluAltOp = 1'b0;		
						pcSel = pcSel_RS1IMM;
						memWrEn = 1'b0;
					end
					default: begin
						regFileRd0Index = 4'bxxxx;		
						regFileWrSel = 2'bxx;
						regFileWrEn = 1'bx;
						aluSrc2Sel = 1'bx;
						aluAltOp = 1'bx;		
						pcSel = 2'bxx;
						memWrEn = 1'bx;
					end
				endcase
				regFileRd1Index = 4'bxxxx;
			end
			default: begin
				regFileRd0Index = 4'bxxxx;		
				regFileRd1Index = 4'bxxxx;
				regFileWrSel = 2'bxx;
				regFileWrEn = 1'bx;
				aluSrc2Sel = 1'bx;
				aluAltOp = 1'bx;		
				pcSel = 2'bxx;
				memWrEn = 1'bx;
			end
		endcase
	end	
endmodule
