# Topofaker
TopoFaker is a novel topology obfuscation system to counter tomography-based topology inference in general network topologies. We use Java, Gurobi, Python, and a modified version of the ns-3 source code to implement a prototype of TopoFaker. This code is derived from our paper **_TopoFaker: Topology Obfuscation Against Network Tomography for General Topologies_**, which we have submitted to _TON_.

## 1. System Environment
- Java
  - Windows 10, IDEA 2019.3.4, JDK 11
- Python & ns-3
  - Ubuntu 16.0, Python 3.9.1, ns 3.29

## 2. Preparations
Replace the following [exp] with the corresponding serial number of the number of experiments (_1_, _2_, ..., _5_), [algo] with the corresponding experimental algorithm (_real_, _topofaker_), [topo] with the corresponding topology (_dfn_, _deltacom_, _cogentco_), and [precent] with different percentages (_10_, _20_, ..., _100_).
Take the following file path as an example
`fake_delay/fake_delay_exp[exp]/[algo]/[topo]/[topo]_[precent].py`
In the case of the 1st experiment, topofaker algorithm, dfn topology and 20% probe nodes, the file path is 
`fake_delay/fake_delay_exp1/topofaker/dfn/dfn_20.py`

## 3. Workflow
1) Use the Java file _MyGMLReader.java_ to parse the gml file (in folder _gmlFolder_) and output topology information and edge nodes;
2) Add the result of 1) to _GetRandomEdgeNode.java_ to select random probe nodes;
3) Add the result of 2) to the topology file of ns-3 and the probe node file (_fixed_arr_file/detection_node/link[exp].txt_) of java;
4) Replace _point-to-point-net-device.cc_, _point-to-point-net-device.h_, _udp-client.cc_, _udp-client.h_, _udp-server.cc_ in ns-3, and add _my-tag.cc_ and _my-tag.h_;
5) In the sandwich probe written in _UDP_ and _Point-to-Point_ modules, set the serial number of the packet sent by _UdpClient_ and output the path and delay, here you need to output the console results to a file (_/ns3_res/[algo]/[topo]/[topo]\_[precent].txt_);
6) Run _sort_delay_path_[topo].sh_ under _/ns3_res/[algo]/[topo]/_ to analyze the output results and sort them into different files, and copy the result files to the corresponding folder in java (copy the entire _[topo]_ folder under _/ns3_res_ to the java project under _ns3_res/ns3_res_exp[exp]_);
7) Use _CovdelayToGraph.java_ to read the correlation delay files in 6) to calculate the maximum node degree, node degree variance, maximum edge connectivity, edge connectivity variance and other information of the different forwarding tree with different senders, and at the same time generate the first half of the python file (_fake_delay/fake_delay_exp[exp]/[algo]/[topo]/[topo]\_[precent].py_) used to calculate the fake SLM matrix;
8) Use _getTreeAndMerge.java_ to read the correlation delays from 6) and generate the second half of the Python file (_fake_delay/fake_delay_exp[exp]/[algo]/[topo]/[topo]\_[precent].py_) used to calculate the fake SLM matrix;
9) Run _run_cal_slm.py_ to get the proactive delays and write them to the file (_fake_delay_exp[exp]/[algo]/[topo]/sort_res/[topo]\_[precent].txt_);
10) Run _sort_run_time_and_fake_delay.sh_ to parse the results of 9) and put the file in the corresponding location (_/fake_delay_exp[exp]/[algo]_);
11) Turn on Topofaker to add delays in ns-3's topology file (_/home/myp4/Desktop/fake_delay_exp[exp]/[algo]/[topo]/sort_res/[topo]\_[precent].txt_), run it and output the console information to a file.
12) Run _sort_delay_path_[topo].sh_ under _/ns3_res/[algo]/[topo]/_ to analyze the output and sort the results into different files and copy the result files to the corresponding folder in Java (copy the entire _[topo]_ folder under _/ns3_res_ to the java project under _ns3_res/ns3_res_exp[exp]_);
13) Use _CovdelayToGraph.java_ to read the correlation delay files in 12) to calculate the maximum node degree, node degree variance, maximum edge connectivity, edge connectivity variance, etc. of the different forwarding tree.
14) Use _getTreeAndMerge.java_ to read the correlation delay and real path files in 6) and 12) to calculate the information of node degree maximum, node degree variance, edge connectivity maximum, edge connectivity variance of the graph obtained by merging different trees with and without proactively adding delay.
