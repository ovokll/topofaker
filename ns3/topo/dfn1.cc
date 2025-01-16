/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"

#include "ns3/ipv4-static-routing-helper.h"
#include "ns3/ipv4-list-routing-helper.h"
#include <ctime>
#include <random>
#include <string>

#include "ns3/ipv4-address.h"

using namespace ns3;

const int TOPONODENUM = 58;
const float DELTA_TIME = 0.2;
const int REAL = 0;
const int SANDPACKETSNUM = 1;
const int DETECTION_EDGES_INDEX = 0;//0-9
int add_delay_type = 1;//1: topoFaker

NS_LOG_COMPONENT_DEFINE ("DFN");

int
main (int argc, char *argv[])
{
  CommandLine cmd;
  cmd.Parse (argc, argv);
  
  Time::SetResolution (Time::NS);
  LogComponentEnable ("UdpClient", LOG_LEVEL_INFO);
  LogComponentEnable ("UdpServer", LOG_LEVEL_INFO);

  NodeContainer nodes;
  nodes.Create (TOPONODENUM);

  PointToPointHelper pointToPoint;
  pointToPoint.SetDeviceAttribute ("DataRate", StringValue ("2Gbps"));
  pointToPoint.SetChannelAttribute ("Delay", StringValue ("2ms"));

  int linkFrom[] = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 10, 10, 11, 12, 13, 14, 14, 14, 16, 16, 17, 17, 17, 18, 18, 19, 19, 20, 21, 21, 22, 23, 25, 26, 27, 28, 28, 29, 30, 30, 32, 32, 33, 33, 34, 34, 35, 36, 37, 38, 39, 40, 40, 41, 42, 42, 43, 44, 44, 45, 46, 47, 47, 48, 48, 48, 48, 50, 50, 50, 50, 51, 51, 52, 52, 52, 52, 53, 55, 56};
  int linkTo[]= {1, 3, 53, 6, 15, 56, 49, 52, 53, 51, 5, 10, 7, 53, 51, 51, 51, 11, 36, 43, 52, 52, 24, 50, 27, 50, 23, 25, 50, 31, 19, 38, 51, 20, 46, 22, 23, 51, 24, 27, 50, 44, 51, 36, 51, 48, 31, 33, 50, 50, 37, 35, 52, 50, 51, 52, 39, 44, 41, 43, 53, 43, 53, 51, 50, 45, 46, 51, 51, 53, 56, 49, 52, 57, 51, 52, 53, 55, 52, 53, 53, 54, 55, 56, 54, 56, 57};
  
  int linksNum = sizeof(linkFrom)/sizeof(int);
  NetDeviceContainer nd[linksNum];
  for(int i = 0; i < linksNum; i++){
    nd[i] = pointToPoint.Install(nodes.Get(linkFrom[i]), nodes.Get(linkTo[i]));
  }

  int* rand_edges[10];
  rand_edges[0] = new int[5]{13, 9, 8, 12, 15};
  rand_edges[1] = new int[6]{26, 9, 8, 12, 15, 13};
  rand_edges[2] = new int[8]{8, 9, 12, 13, 15, 26, 29, 0};
  rand_edges[3] = new int[9]{8, 9, 12, 13, 15, 26, 29, 20, 0};
  rand_edges[4] = new int[11]{8, 9, 12, 13, 15, 26, 29, 7, 0, 11, 20};
  rand_edges[5] = new int[12]{8, 9, 12, 13, 15, 26, 29, 0, 7, 11, 20, 16};
  rand_edges[6] = new int[14]{8, 9, 12, 13, 15, 26, 29, 11, 7, 0, 16, 20, 21, 4};
  rand_edges[7] = new int[15]{8, 9, 12, 13, 15, 26, 29, 21, 2, 7, 16, 0, 20, 11, 4};
  rand_edges[8] = new int[17]{8, 9, 12, 13, 15, 26, 29, 18, 11, 16, 4, 21, 20, 5, 0, 2, 7};
  rand_edges[9] = new int[18]{8, 9, 12, 13, 15, 26, 29, 0, 20, 7, 11, 16, 21, 4, 2, 5, 18, 6};
  int rand_edges_len[] = {5, 6, 8, 9, 11, 12, 14, 15, 17, 18};

  int hostNum = rand_edges_len[DETECTION_EDGES_INDEX];
  NodeContainer hosts;
  hosts.Create(hostNum);
  
  NetDeviceContainer hostToEdge[hostNum];
  std::set<int> edgeSet;
  for (int i = 0; i < hostNum; i++){
    hostToEdge[i] = pointToPoint.Install(hosts.Get(i), nodes.Get(rand_edges[DETECTION_EDGES_INDEX][i]));
  }


  Ipv4GlobalRoutingHelper globalRouting;
 
  Ipv4ListRoutingHelper list;
  list.Add (globalRouting, 20);
 
  InternetStackHelper internet;
  internet.SetRoutingHelper (list); 
  internet.Install (nodes);
  internet.Install (hosts);

  Ipv4AddressHelper address;
  Ipv4InterfaceContainer ipv4ic[hostNum];
  address.SetBase ( "10.2.0.0", "255.255.0.0");
  for(int i = 0; i < hostNum; i++){
    ipv4ic[i] = address.Assign (hostToEdge[i]);
  }
  address.SetBase ( "10.1.0.0", "255.255.0.0");
  for(int i = 0; i < linksNum; i++){
    address.Assign (nd[i]);
  }

  int count = 0;
  int startTimeDelta = SANDPACKETSNUM*DELTA_TIME+1;

  UdpServerHelper echoServer (9);
  for(int i = 0; i < hostNum; i++){
    ApplicationContainer serverApps = echoServer.Install (hosts.Get (i));
    serverApps.Start (Seconds (1.0));
    serverApps.Stop (Seconds (99999));
  }

  if(REAL)
    add_delay_type = -1;
  PointToPointNetDevice p2p;
  p2p.SetExperimentInfo(1, DETECTION_EDGES_INDEX, 1, add_delay_type);
  
  int is_output = 0;

  std::cout << "mySout:\tpkt_id\tpkt_size\tpkt_from\tpkt_to\tdelay" << std::endl;
  std::cout << "mypathes:\tpkt_from\tpkt_to\tpath_nodes[]" << std::endl;

  for(int i = 0; i < hostNum; i++){
    for(int j = 0; j < hostNum; j++){
      if(i == j)
        continue;
        if(REAL)
          is_output = 1;
        else
          is_output = 2;
      for(int k = j+1; k < hostNum; k++){
        if(i == k)
          continue;

        UdpClientHelper echoClient (ipv4ic[j].GetAddress (0), 9);
        echoClient.SetAttribute ("MaxPackets", UintegerValue (SANDPACKETSNUM));
        echoClient.SetAttribute ("Interval", TimeValue (Seconds (DELTA_TIME)));
        echoClient.SetAttribute ("PacketSize", UintegerValue (56));
        ApplicationContainer clientApps = echoClient.Install (hosts.Get (i));
        clientApps.Start (Seconds (2.0 + startTimeDelta*count));
        clientApps.Stop (Seconds(startTimeDelta*(count+1)));

        UdpClientHelper echoClient2 (ipv4ic[k].GetAddress (0), 9);
        echoClient2.SetAttribute ("MaxPackets", UintegerValue (SANDPACKETSNUM));
        echoClient2.SetAttribute ("Interval", TimeValue (Seconds (DELTA_TIME)));
        echoClient2.SetAttribute ("PacketSize", UintegerValue (1400));
        ApplicationContainer clientApps2 = echoClient2.Install (hosts.Get (i));
        clientApps2.Start (Seconds (2.1 + startTimeDelta*count));
        clientApps2.Stop (Seconds(startTimeDelta*(count+1)));

        UdpClientHelper echoClient3 (ipv4ic[j].GetAddress (0), 9);
        echoClient3.SetAttribute ("MaxPackets", UintegerValue (SANDPACKETSNUM));
        echoClient3.SetAttribute ("Interval", TimeValue (Seconds (DELTA_TIME)));
        echoClient3.SetAttribute ("PacketSize", UintegerValue (56));
        ApplicationContainer clientApps3 = echoClient3.Install (hosts.Get (i));
        Ptr<UdpClient> udpClient3 = DynamicCast<UdpClient>(clientApps3.Get(0));
        udpClient3->SetPacketTag((j<<8)+k);
        udpClient3->SetOutputPath(is_output);
        clientApps3.Start (Seconds (2.1 + startTimeDelta*count));
        clientApps3.Stop (Seconds(startTimeDelta*(count+1)));

        if(REAL)
          is_output = 3;
        count++;
      }
    }

    if(REAL){
      int dest = hostNum - 1;
      if(i == hostNum-1)
        dest--;
      UdpClientHelper echoClient3 (ipv4ic[dest].GetAddress (0), 9);
      echoClient3.SetAttribute ("MaxPackets", UintegerValue (1));
      echoClient3.SetAttribute ("PacketSize", UintegerValue (56));
      ApplicationContainer clientApps3 = echoClient3.Install (hosts.Get (i));
      Ptr<UdpClient> udpClient3 = DynamicCast<UdpClient>(clientApps3.Get(0));
      udpClient3->SetPacketTag(1);
      udpClient3->SetOutputPath(5);
      clientApps3.Start (Seconds (2.1 + startTimeDelta*count));
      clientApps3.Stop (Seconds(startTimeDelta*(count+1)));
      count++;
    }
  }

  Ipv4GlobalRoutingHelper::PopulateRoutingTables ();

  Simulator::Run ();
  Simulator::Destroy ();
  for(int i = 0; i < 10; i++)
    delete[] rand_edges[i];
  return 0;
}
