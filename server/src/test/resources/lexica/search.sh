#!/bin/bash

for i in `cut -f2 $1 | grep -v '#'`; do 
   item=`grep -l -c $i $2`;
	if [[ $item > 0 ]]; then
       echo $i: `grep -l -c $i $2`
	fi
#    if [ $item -ne 0 ]
#   then 
#       echo $i: `grep -l -c $i $2`
#   fi 
#   grep -a 30 -b 30 $i $2 
done

