#!/bin/bash
topos=("cogentco" "deltacom" "dfn")
for topo2 in "${topos[@]}"; do
  i=0
  for ((k=10;k<=100;k=k+10)); do
    ((i=i+10))
    inputfile="res/${topo2}/${topo2}_${i}_fake_delay.txt"

    if [ ! -f $inputfile ]; then
      continue;
    fi

    outputfile="res/${topo2}/res/${topo2}"

    if [ ! -d "res/${topo2}/res" ]; then
      mkdir -p "res/${topo2}/res"
    fi

    line=$(sed -n '5p' ${inputfile})
OLD_IFS="$IFS"
IFS=","
    array=($line)
    for var in ${array[@]}
    do
      if [ ! -z ${var} ]; then
        echo $var >> "${outputfile}_${k}.txt"
      fi
    done
IFS="$OLD_IFS"
    sed -n '7p' ${inputfile} >> "${outputfile}_total_time.txt"
  done
done


