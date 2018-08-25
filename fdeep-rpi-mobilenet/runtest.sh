#!/bin/bash

LOG=out_openmp.log

for app in foo_neon_openmp
do
	for i in {0..50}
	do
		echo $i >> $LOG
		./$app bmp/$i.bmp >> $LOG
		echo "" >> $LOG
	done
	echo "" >> $LOG
done

