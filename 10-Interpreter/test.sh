#!/bin/bash

# takes in the path to a java21 jvm 
if [ -z "$1" ]
then
    echo "Usage: ./test.sh <path to java 21 executable>"
    exit 1
fi

# check that the input directory has files in it
if [ ! "$(ls -A tests/text)" ]
then
    echo "No input files found in tests/text"
    exit 1
fi
# check that the normal directory has files in it
if [ ! "$(ls -A tests/normal)" ]
then
    echo "No normal files found in tests/normal"
    exit 1
fi
# check that the backtick directory has files in it
if [ ! "$(ls -A tests/backtick)" ]
then
    echo "No backtick files found in tests/backtick"
    exit 1
fi

# runs each file in the tests/normal directory with awk with every input from tests/text files putting the output in tests/output/actual-textfilename-filename

for file in tests/normal/*
do
    for input in tests/text/*
    do
        # put basename of awk file + basename of input file as header of output file
        echo "$(basename $file) with $(basename $input)" > tests/output/expected-$(basename $input)-$(basename $file)
        awk -f $file $input >> tests/output/expected-$(basename $input)-$(basename $file) 
    done
done

# runs each file in the tests/backtick directory with 10-Interpreter.jar with every input from tests/text files putting the output in tests/output/actual-textfilename-filename

for file in tests/backtick/*
do
    for input in tests/text/*
    do
        # put basename of awk file + basename of input file as header of output file
        echo "$(basename $file) with $(basename $input)" > tests/output/actual-$(basename $input)-$(basename $file)
        $1 --enable-preview -jar tests/10-Interpreter.jar -- $file $input >> tests/output/actual-$(basename $input)-$(basename $file) 
    done
done

# compares each file in the tests/output/expected-filename with the corresponding file in tests/output/actual-filename
# files.txt contains the names of all the files
for file in $(cat tests/files.txt)
do
    for input in tests/text/*
    do
        echo  -e "\nComparing $file with $(basename $input)\n"
        diff -y tests/output/expected-$(basename $input)-$file tests/output/actual-$(basename $input)-$file
    done
done

echo "Summarizing differences"
for file in $(cat tests/files.txt)
do

    for input in tests/text/*
    do
        diff -q tests/output/expected-$(basename $input)-$file tests/output/actual-$(basename $input)-$file
    done
done