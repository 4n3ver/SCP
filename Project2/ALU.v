module ALU(aluAltOp, data1, func, data2, dataOut, beqOut);
	input [31:0] data1, data2;
	input [3:0] func;
	input aluAltOp;
	
	output beqOut;
	output [31:0] dataOut;
	reg [31:0] dataOut;
	reg beqOut;
	
	wire signed [31:0] d1, d2;
	assign d1 = data1;
	assign d2 = data2;
	
	always @(*)
	begin
		case (aluAltOp)
			1'b0:
			begin
				case (func)
					4'b0000:
					begin
						dataOut = d1 + d2;
						beqOut = 0;
					end
					4'b0001:
					begin
						dataOut = d1 - d2;
						beqOut = 0;
					end
					4'b0100:
					begin
						dataOut = d1 & d2;
						beqOut = 0;
					end
					4'b1100:
					begin
						dataOut = ~(d1 & d2);
						beqOut = 0;
					end
					4'b0101:
					begin
						dataOut = d1 | d2;
						beqOut = 0;
					end
					4'b0110:
					begin
						dataOut = d1 ^ d2;
						beqOut = 0;
					end
					4'b1011:
					begin
						dataOut = ((d2 & 32'h0000FFFF)<< 16);
 						beqOut = 0;
					end
					4'b1101:
					begin
						dataOut = ~(d1 | d2);
						beqOut = 0;
					end
					4'b1110:
					begin
						dataOut = ~(d1 ^ d2);
						beqOut = 0;
					end
					default:
					begin
						beqOut = 1'b0;
						dataOut = 32'd0;
					end
				endcase
			end
			1'b1:
			begin
				case (func)
					4'b0000: //f, fi
					begin
						dataOut = 32'd0;
						beqOut = 0;
					end
					4'b0001: //eq
					begin
						if (d1 == d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end
					end
					4'b0010: //lt
					begin
						if (d1 < d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end
					end
					4'b0011: //lte
					begin
						if (d1 <= d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end
					end
					4'b1000://t
					begin
						dataOut = 32'd1;
						beqOut = 1'b1;
					end
					4'b1001: //ne
					begin
						if (d1 != d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end						
					end
					4'b1010: //gte
					begin
						if (d1 >= d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end
					end
					4'b1011: //gt
					begin
						if (d1 > d2)
						begin
							dataOut = 32'd1;
							beqOut = 1'b1;
						end
						else
						begin
							dataOut = 32'd0;
							beqOut = 1'b0;
						end
					end
					4'b0101: //beqz
					begin
						if (d1 == 32'd0)
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					4'b0110: //bltz
					begin
						if (1'd1 == d1[31])
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					4'b0111: //bltez
					begin
						if (d1 == 32'd0 || d1[31] == 1'b1)
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					4'b1101: //bnez
					begin
						if (d1 != 32'd0)
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					4'b1110: //bgtez
					begin
						if (d1 == 32'd0 || d1[31] == 1'b0)
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					4'b1111:
					begin
						if (d1[31] == 1'b0 && d1[30:0] != 31'd0)
						begin
							beqOut = 1'b1;
							dataOut = 32'd0;
						end
						else
						begin
							beqOut = 1'b0;
							dataOut = 32'd0;
						end
					end
					default:
					begin
						beqOut = 1'b0;
						dataOut = 32'd0;
					end
				endcase
			end
		endcase
	end
endmodule