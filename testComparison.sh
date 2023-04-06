#!/bin/bash
# colour for visibility
green='\033[0;32m'
red='\033[0;31m'
clear='\033[0m'
# compare & print result
diff output.txt "EO$1.txt"
DIFF=$(diff output.txt "EO$1.txt")
if [ "$DIFF" != "" ]
then
	echo -e "${red}Test case $1 failed.${clear}"
else
	echo -e "${green}Test case $1 passed.${clear}"
fi
