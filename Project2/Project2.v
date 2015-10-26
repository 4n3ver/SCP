module Project2(SW,KEY,LEDR,LEDG,HEX0,HEX1,HEX2,HEX3,CLOCK_50);
	input  [9:0] SW;
	input  [3:0] KEY;
	input  CLOCK_50;
	output [9:0] LEDR;
	output [7:0] LEDG;
	output [6:0] HEX0,HEX1,HEX2,HEX3;
	 
	parameter DBITS         			 = 32;
	parameter INST_SIZE      			 = 32'd4;
	parameter INST_BIT_WIDTH			 = 32;
	parameter START_PC       			 = 32'h40;
	parameter REG_INDEX_BIT_WIDTH 		 = 4;
	parameter ADDR_KEY  			  	 = 32'hF0000010;
	parameter ADDR_SW   				 = 32'hF0000014;
	parameter ADDR_HEX  				 = 32'hF0000000;
	parameter ADDR_LEDR 				 = 32'hF0000004;
	parameter ADDR_LEDG 				 = 32'hF0000008;
	  
	parameter IMEM_INIT_FILE			 = "Test2.mif";
	parameter IMEM_ADDR_BIT_WIDTH 		 = 11;
	parameter IMEM_DATA_BIT_WIDTH 		 = INST_BIT_WIDTH;
	parameter IMEM_PC_BITS_HI     		 = IMEM_ADDR_BIT_WIDTH + 2;
	parameter IMEM_PC_BITS_LO     		 = 2;
	  
	parameter DMEM_INIT_FILE			 = "Test.mif";  
	parameter DMEMADDRBITS2				 = 32;
	parameter DMEMADDRBITS 				 = 13;
	parameter DMEMWORDBITS				 = 2;
	parameter DMEMWORDS					 = 2048;
	  
	parameter OP1_ALUR 					 = 4'b0000;
	parameter OP1_ALUI 					 = 4'b1000;
	parameter OP1_CMPR 					 = 4'b0010;
	parameter OP1_CMPI 					 = 4'b1010;
	parameter OP1_BCOND					 = 4'b0110;
	parameter OP1_SW   					 = 4'b0101;
	parameter OP1_LW   					 = 4'b1001;
	parameter OP1_JAL  					 = 4'b1011;
	  
	// Add parameters for various secondary opcode values
	  
	//PLL, clock genration, and reset generation
	wire clk, lock, dummy;/*<>*/
	wire pcWrtEn = 1'b1;
	wire[DBITS - 1: 0] pcIn; // Implement the logic that generates pcIn; you may change pcIn to reg if necessary
	wire[DBITS - 1: 0] pcOut;
	wire[31:0] aluDataOut;
	wire memWrEn, rfWrEn, aluAltOp, aluSrc2Sel, aluOut;
	wire [3:0] rfRd0Index, rfRd1Index, rfWrIndex, aluFunc;
	wire[15:0] imm;
	wire[IMEM_DATA_BIT_WIDTH - 1: 0] instWord;
	wire [31:0] rfDataIn, rfDataOut1, rfDataOut2;
	wire [31:0] immSe, aluSrc2, shifted;
	wire [DBITS - 1:0] pcOut4;
	wire [1:0] pcSel, rfWrSel;
	wire [DBITS - 1:0] pc4Imm;
	wire [31:0] memData;
	wire [15:0] hexOut;
	wire [9:0] DEBUG_LEDR; /*<>*/
	wire [7:0] DEBUG_LEDG; /*<>*/
	//Pll pll(.inclk0(CLOCK_50), .c0(clk), .locked(lock));
	PLL	PLL_inst (.inclk0 (CLOCK_50),.c0 (clk),.locked (lock));
	wire reset = ~lock;/*<>*
	//assign clk = ~KEY[0];/*<>*/
	  
	// Create PC and its logic

	
	// This PC instantiation is your starting point
	Register #(.BIT_WIDTH(DBITS), .RESET_VALUE(START_PC)) pc(clk, reset, pcWrtEn, pcIn, pcOut);
	pcALU alu0(pcOut, pcOut4);
	mux4 pcMux(pcSel, pcOut4, pc4Imm, aluDataOut, 32'h00000000, pcIn);
		  
	// Creat instruction memeory

	InstMemory #(IMEM_INIT_FILE, IMEM_ADDR_BIT_WIDTH, IMEM_DATA_BIT_WIDTH) instMem (pcOut[IMEM_PC_BITS_HI - 1: IMEM_PC_BITS_LO], instWord);
	  
	// Put the code for getting opcode1, rd, rs, rt, imm, etc. here 
//	(memWrEn, regFileWrEn, aluAltOp, pcSel, aluSrc2Sel, 
//						regFileWrSel, regFileRd0Index, regFileRd1Index, 
//						regFileWrIndex, aluFunc, imm, aluOut, instruction);
	SCProcController contr(memWrEn, rfWrEn, aluAltOp, pcSel, aluSrc2Sel, rfWrSel, rfRd0Index, rfRd1Index, rfWrIndex, aluFunc, imm, aluOut, instWord);
	// Create the registers

	RF regFile(clk, rfWrEn, rfRd0Index, rfRd1Index, rfWrIndex, rfDataIn, rfDataOut1, rfDataOut2);
	// Create ALU unit
	SignExtension #(.IN_BIT_WIDTH(16), .OUT_BIT_WIDTH(32)) se(imm, immSe);
	ConditionalShifter condS(shifted, instWord[3:0], immSe);
	mux2 aluMux(aluSrc2Sel, rfDataOut2, shifted, aluSrc2);
	ALU alu1(aluAltOp, rfDataOut1, aluFunc, aluSrc2, aluDataOut, aluOut);
	branchALU alu2(pcOut4, immSe, pc4Imm);
	mux4 dataMux (rfWrSel, aluDataOut, memData, pcOut4, 32'd0, rfDataIn);
	// Put the code for data memory and I/O here
	//assign LEDG = pcOut[7:0];
	//assign LEDR = {1'b1, memWrEn, rfWrEn, pcSel[1:0], rfWrSel[1:0], aluSrc2Sel, aluAltOp, aluOut}; /*<>*/
	//assign LEDR = {pcSel, pcOut[15:8]};
	DataMemory #(DMEM_INIT_FILE) dataMem(clk, memWrEn, aluDataOut, rfDataOut2, switches, keyIn, /*DEBUG_*/LEDR, /*DEBUG_*/LEDG, hexOut, memData); /*<>*/
	 
	 
	// KEYS, SWITCHES, HEXS, and LEDS are memeory mapped IO
	SevenSeg seg0(hexOut[3:0] /*pcOut[3:0]*/, HEX0);
	SevenSeg seg1(hexOut[7:4] /*pcOut[7:4]*/, HEX1);
	SevenSeg seg2(hexOut[11:8] /*pcOut[11:8]*/, HEX2);
	SevenSeg seg3(hexOut[15:12] /*pcOut[15:12]*/, HEX3);

	reg [3:0] keyIn;
	reg [9:0] switches, lastSwitches;
	reg [15:0] bounceCount;
	always @(posedge clk) begin
		bounceCount <= 16'd0;
		if (bounceCount == 16'd30) begin
			bounceCount <= 16'd0;
			switches <= SW;
		end else if (switches == lastSwitches) begin
			bounceCount <= bounceCount + 1'b1;
		end else begin
			bounceCount <= 1'b0;
		end		
		keyIn <= KEY;//{KEY[3:2], 1'b1}; /*<>*/
		lastSwitches <= switches;
	end
	//assign reset = ~KEY[0] ? 1'b1 : 1'b0;
endmodule