#!/bin/bash
filename="deltacom"
hostnums=(7 10 13 16 19 22 25 28 31 34)
reg1="mySout:\s+\d+\s+\d+\s+10\.2\.0\."
reg2="\s"
if [ ! -d "${filename}_res" ]; then
    mkdir -p ./${filename}_res
fi
i=0
prefix="./${filename}_res/${filename}_"
for num in "${hostnums[@]}"; do
  i=$(expr $i + 10)
  grep "mySout" "${filename}_${i}.txt" > "${prefix}${i}_delay_res.txt"
  if [ ! -d "${prefix}${i}_delay" ]; then
      mkdir -p ${prefix}${i}_delay
  fi
  for ((j=0;j<num;j++)); do
    ((k = j*2+1))
    reg=$reg1$k$reg2
    grep -P ${reg} "${filename}_${i}.txt" > "${prefix}${i}_delay/10.2.0.${k}.txt"
  done
done
