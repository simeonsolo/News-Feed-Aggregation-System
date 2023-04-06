#!/bin/bash

### BASIC FLOW TESTS
# Test 1 - PUT,GET - Expected Output: EO1.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 1
pkill java

sleep 1

# Test 2 - 2 PUT,PUT,GET - Expected Output: EO2.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input2.txt > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 2
pkill java

sleep 1

# Test 3 - PUT,GET,GET - Expected Output: EO3.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 3
pkill java

sleep 1

# Test 4 - PUT,GET,PUT,GET - Expected Output: EO4.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
java ContentServer localhost:4567 input2.txt > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 4
pkill java

sleep 1

### RESPONSE CODES TESTS
# Test 5 - NO CONTENT GET - Expected Output: EO5.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 5
pkill java

sleep 1

# Test 6 - FIRST PUT TO FEED - Expected Output: EO6.txt
> atom.txt
> outputCS.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
./testComparisonCS.sh 6
pkill java

sleep 1

# Test 7 - FUTURE PUTS TO FEED - Expected Output: EO7.txt
> atom.txt
> outputCS.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
./testComparisonCS.sh 7
pkill java

sleep 1

# Test 8 - INVALID PUT - Expected Output: EO8.txt
> atom.txt
> outputCS.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 invalidInput.txt > /dev/null 2>&1 &
sleep 1
./testComparisonCS.sh 8
pkill java

sleep 1

### HEARTBEAT TEST
# Test 9 - PUT, CS DISCONNECT, 12SEC WAIT, GET - Expected Output: EO9.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
PID=$!
kill $PID
sleep 12
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 9
pkill java

sleep 1

### AGGREGATION SERVER FEED PERSISTENCE TEST (FOLLOWING RESTART/CRASH)
# Test 10 - PUT,SERVER CRASH,SERVER RESTART,GET - Expected Output: EO10.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
PID=$!
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
kill $PID
sleep 3
java AggregationServer > /dev/null 2>&1 &
sleep 1
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 10
pkill java

sleep 1

### CONTENT SERVER REPLICATION TEST (FOLLOWING AGGREGATE SERVER CRASH)
# Test 11 - PUT,AS CRASH,AS RESTART,AUTOMATIC RECONNECT - Expected Output: EO11.txt
> atom.txt
> outputCS.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
PID=$!
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
kill $PID # kill AS
sleep 14
java AggregationServer > /dev/null 2>&1 &
sleep 14
./testComparisonCS.sh 11
pkill java

sleep 1

### CONTENT SERVER PERSISTENCE TEST (RETRYING ON FAIL)
# Test 12 - CS TRIES CONNECTING TO AS THAT DOES NOT YET EXIST - Expected Output: EO12.txt
> atom.txt
> outputCS.txt
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 8
java AggregationServer > /dev/null 2>&1 &
sleep 5
./testComparisonCS.sh 12
pkill java

sleep 1

### CLIENT SERVER PERSISTENCE TEST (RETRYING ON FAIL)
# Test 13 - CLIENT TRIES CONNECTING TO AS THAT DOES NOT YET EXIST - Expected Output: EO13.txt
> atom.txt
> output.txt
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 8
java AggregationServer > /dev/null 2>&1 &
sleep 5
./testComparison.sh 13
pkill java

sleep 1

### ONE MINUTE RECONNECTION WINDOW TEST (AGGREGATION SERVER PERSISTENCE & CONTENT SERVER REPLICATION)
# Test 14 - PUT(CS1),PUT(CS2),AS CRASH,CS1 KILLED,AS RESTART,60SEC RECONNECTION WINDOW,GET - Expected Output: EO14.txt
> atom.txt
> output.txt
java AggregationServer > /dev/null 2>&1 &
sleep 1
PID=$!
java ContentServer localhost:4567 input.txt > /dev/null 2>&1 &
sleep 1
PID2=$!
java ContentServer localhost:4567 input2.txt > /dev/null 2>&1 &
sleep 1
kill $PID # kill AS
sleep 2
kill $PID2 # kill CS1
sleep 12
java AggregationServer > /dev/null 2>&1 &
sleep 63
java GETClient localhost:4567 > /dev/null 2>&1 &
sleep 3
./testComparison.sh 14
pkill java
