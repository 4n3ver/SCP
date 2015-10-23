transcript on
if {[file exists rtl_work]} {
	vdel -lib rtl_work -all
}
vlib rtl_work
vmap work rtl_work

vlog -vlog01compat -work work +incdir+E:/Dropbox/Documents/University/CS\ 3220/Assignment/Real\ Project\ 2\ -\ Single\ Cycle\ Processor/Project2 {E:/Dropbox/Documents/University/CS 3220/Assignment/Real Project 2 - Single Cycle Processor/Project2/RF.v}

vlog -vlog01compat -work work +incdir+E:/Dropbox/Documents/University/CS\ 3220/Assignment/Real\ Project\ 2\ -\ Single\ Cycle\ Processor/Project2 {E:/Dropbox/Documents/University/CS 3220/Assignment/Real Project 2 - Single Cycle Processor/Project2/RFTestBench.v}

vsim -t 1ps -L altera_ver -L lpm_ver -L sgate_ver -L altera_mf_ver -L altera_lnsim_ver -L cycloneii_ver -L rtl_work -L work -voptargs="+acc"  RFTestBench

add wave *
view structure
view signals
run -all
