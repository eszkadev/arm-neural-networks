#!/bin/bash

LOG=testingmobilennet.log

for app in foo_neon_openmp foo_neon foo_base
do
	for i in {1..10}
	do
		echo $app >> $LOG
		for test in airplane.bmp automobile.bmp bird.bmp ship.bmp dog.bmp cat.bmp car.bmp
		do
			echo $test >> $LOG
			./$app $test >> $LOG
			echo "" >> $LOG
			sleep 5
		done
	done
	echo "" >> $LOG
done

