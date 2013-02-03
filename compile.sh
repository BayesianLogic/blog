#!/bin/bash
#script for compiling blog

if [ "$1" == "debug" ];
then
    debug=" -g";
else
    debug="";
fi    

mkdir -p bin

find src -name \*.java -print >javafiles

javac $debug -verbose -cp lib/java_cup.jar:lib/JFlex.jar:lib/commons-math-2.2.jar:lib/junit.jar:lib/Jama.jar:lib/hmmlib.jar:lib/javabib.jar:bin/ -d bin/ @javafiles

rm javafiles
