#!/bin/bash

for i in {0..9999}
do
    convert $i.JPEG -resize 224x224 -compress None $i.bmp
done
