// Yoel Ivan (yivan3@gatech.edu)

`timescale 1ns / 1ps
module RFTestBench;
	reg CLK, WrEn;
	reg [3:0] RS1, RS2, RD;
	wire [31:0] out1, out2;
	reg [31:0] in;
	RF regfile(CLK, WrEn, RS1, RS2, RD, in, out1, out2);
	
	reg [4:0] i, j;
	
	initial begin
		
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		#100;
		
		in = 32'd42;
		RD = 4'b0000;
		WrEn = 1'b0;
		$display("Writing 42 at r0 with WrEn disabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		#100;
		
		in = 32'd1;
		RD = 4'b0000;
		WrEn = 1'b1;
		$display("Writing 1 at r0 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end
		WrEn = 1'b0;
		#100;
		
		in = 32'd1;
		RD = 4'b0001;
		WrEn = 1'b1;
		$display("Writing 1 at r1 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;

		in = 32'd2;
		RD = 4'b0010;
		WrEn = 1'b1;
		$display("Writing 2 at r2 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd3;
		RD = 4'b0011;
		WrEn = 1'b1;
		$display("Writing 3 at r3 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd5;
		RD = 4'b0100;
		WrEn = 1'b1;
		$display("Writing 5 at r4 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd8;
		RD = 4'b0101;
		WrEn = 1'b1;
		$display("Writing 8 at r5 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd13;
		RD = 4'b0110;
		WrEn = 1'b1;
		$display("Writing 13 at r6 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd21;
		RD = 4'b0111;
		WrEn = 1'b1;
		$display("Writing 21 at r7 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		in = 32'd34;
		RD = 4'b1000;
		WrEn = 1'b1;
		$display("Writing 34 at r8 with WrEn enabled");
		#100;
		for (i = 5'b0; i < 5'b10000; i = i + 5'b1) begin
			for (j = 5'b0; j < 5'b10000; j = j + 5'b1) begin
				RS1 = i[3:0];
				RS2 = j[3:0];
				$display("Out1: %d Out2 :%d", out1, out2);
			end
		end		
		WrEn = 1'b0;
		#100;
		
		$finish;
		
	end
	
	always begin
		#5 CLK = !CLK;
	end
endmodule