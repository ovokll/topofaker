#!/bin/bash
topos=("cogentco" "deltacom" "dfn")
for topo in "${topos[@]}"; do
  for ((j=10;j<=100;j=j+10)); do
    filepath="${topo}/${topo}_${j}.py"
    if [ ! -f $filepath ]; then
      continue;
    fi
    python3.5 ${filepath} > "res/${topo}/${topo}_${j}_fake_delay.txt"
  done
done

