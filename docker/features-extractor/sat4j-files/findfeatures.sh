#!/bin/bash
SAT4JSOURCE=$1
find $SAT4JSOURCE -name *.java -print >javafiles
javac -d bin $SAT4JSOURCE/org/sat4j/annotations/Feature.java
javac -proc:only -cp bin:featuredetection.jar @javafiles
