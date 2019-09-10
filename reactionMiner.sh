#!/bin/sh

#  orgs_specific.sh
#  
#
#  Created by Aravind Sankar on 8/13/16.
#
find src -name "*.class" -type f -delete
javac -cp src:dependencies/Motif:lib/*:dependencies/jbliss/:dependencies/jbliss/lib/* src/pathwayPrediction/QueryTester.java
java -cp src:dependencies/Motif:lib/*:dependencies/jbliss/:dependencies/jbliss/lib/* -Djava.library.path=dependencies/jbliss/lib/ pathwayPrediction.QueryTester "$@"
