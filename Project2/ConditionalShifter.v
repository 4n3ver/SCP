module ConditionalShifter(out, opcode, in);
	input [3:0] opcode;
	input [31:0] in;
	
	output [31:0] out;
	reg[31:0] out;
	
	always @(*) begin
		if (opcode == 4'b1011) begin // if equal JAL, we shift
			out = {in[29:0], 2'b00};
		end else begin
			out = in;			
		end
	end
endmodule	
