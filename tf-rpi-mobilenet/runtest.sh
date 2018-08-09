#!/bin/bash

LOG=testingtfmobilenet.log

for i in {1..10}
do
	python3.5 test.py >> $LOG
	echo "" >> $LOG
done
echo "" >> $LOG

