module newALU(out, a, b, func);
	parameter [3:0]	ADD 	= 4'b0000,
					SUB 	= 4'b0001,
					AND 	= 4'b0100,
					OR 		= 4'b0101,
					XOR 	= 4'b0110,
					NAND	= 4'b1100,
					NOR		= 4'b1101,
					NXOR	= 4'b1110,
					MVHI	= 4'b1011;
	parameter [3:0]	F 	= 4'b0000,
					EQ 	= 4'b0001,
					LT 	= 4'b0010,
					LTE = 4'b0011,
					EQZ = 4'b0101,
					LTZ	= 4'b0110,
					LTEZ= 4'b0111,
					T	= 4'b1000,
					NE 	= 4'b1001,
					GTE = 4'b1010,
					GT 	= 4'b1011,
					NEZ = 4'b1101,
					GTEZ= 4'b1110,
					GTZ = 4'b1111;
					
	input signed [31:0] a, b;
	input [4:0] func;
	
	output [31:0] out;
	
	reg [31:0] out;
	always @(*) begin
		case (func[4]) 
			1'b1: begin	// comp ops
				case (func[3:0]) 
					F 	: out = 32'h0;
					EQ 	: out = a == b ? 32'h1 : 32'h0;
					LT 	: out = a <  b ? 32'h1 : 32'h0;
					LTE : out = a <= b ? 32'h1 : 32'h0;
					LTZ	: out = a <  0 ? 32'h1 : 32'h0; 
					LTEZ: out = a <= 0 ? 32'h1 : 32'h0;
					T	: out = 32'h1;
					NE 	: out = a != b ? 32'h1 : 32'h0;
					GTE : out = a >= b ? 32'h1 : 32'h0;
					GT 	: out = a >  b ? 32'h1 : 32'h0;
					NEZ : out = a != 0 ? 32'h1 : 32'h0;
					GTEZ: out = a >= 0 ? 32'h1 : 32'h0;
					GTZ : out = a >  0 ? 32'h1 : 32'h0;
					default: out = 32'hxxxxxxxx;					
				endcase
			end
			default: begin	// normal ALU ops
				case (func[3:0]) 
					ADD	: out = a + b;
					SUB	: out = a - b;
					AND	: out = a & b;
					OR 	: out = a | b;
					XOR	: out = a ^ b;
					NAND: out = ~(a & b);
					NOR	: out = ~(a | b);
					NXOR: out = ~(a ^ b);
					MVHI: out = {b[15:0], 16'h0000};
					default: out = 32'hxxxxxxxx;
				endcase
			end
		endcase 
	end
endmodule