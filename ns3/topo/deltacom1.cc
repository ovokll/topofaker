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
#include <string>

#include "ns3/ipv4-address.h"

using namespace ns3;

const int SANDPACKETSNUM = 1;
const int TOPONODENUM = 113;
const float DELTA_TIME = 0.2;
const int DETECTION_EDGES_INDEX = 9;//0-9
const int REAL = 0;
int add_delay_type = 1;//1: topoFaker

NS_LOG_COMPONENT_DEFINE ("Deltacom");

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

  int linkFrom[] = {0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6, 6, 6, 7, 8, 8, 9, 10, 10, 10, 10, 11, 11, 11, 12, 12, 14, 14, 15, 15, 16, 17, 18, 18, 19, 19, 19, 20, 20, 20, 21, 22, 23, 24, 24, 24, 25, 25, 25, 25, 27, 28, 29, 30, 30, 30, 30, 31, 31, 31, 32, 32, 33, 34, 36, 38, 40, 40, 41, 42, 43, 44, 46, 47, 47, 47, 47, 47, 48, 48, 49, 49, 50, 50, 50, 50, 52, 53, 53, 54, 54, 54, 54, 55, 55, 56, 58, 58, 58, 59, 60, 60, 61, 62, 62, 62, 63, 63, 64, 64, 64, 66, 66, 66, 68, 68, 70, 72, 72, 72, 73, 74, 74, 74, 75, 75, 76, 77, 77, 77, 78, 79, 81, 82, 87, 88, 89, 89, 91, 93, 94, 95, 97, 98, 99, 99, 100, 101, 105, 106, 107, 108, 109};
  int linkTo[] = {64, 1, 9, 8, 63, 65, 55, 87, 88, 4, 45, 86, 47, 86, 5, 6, 7, 46, 80, 81, 62, 8, 86, 63, 63, 16, 104, 11, 13, 16, 104, 13, 19, 13, 17, 84, 97, 47, 104, 43, 19, 35, 104, 36, 37, 21, 22, 23, 26, 83, 105, 25, 27, 95, 89, 26, 28, 92, 83, 29, 92, 103, 36, 77, 31, 112, 36, 37, 33, 34, 38, 35, 37, 39, 41, 49, 42, 43, 45, 47, 47, 73, 51, 84, 111, 60, 57, 49, 84, 54, 57, 91, 52, 51, 53, 59, 60, 56, 90, 111, 55, 94, 111, 57, 105, 61, 110, 60, 104, 97, 82, 80, 70, 85, 85, 70, 65, 66, 67, 67, 69, 85, 69, 71, 71, 73, 75, 81, 98, 112, 75, 103, 104, 76, 100, 81, 100, 103, 101, 102, 101, 83, 88, 93, 96, 90, 92, 94, 95, 96, 98, 99, 104, 112, 102, 102, 106, 107, 108, 109, 110};
  int linksNum = sizeof(linkFrom)/sizeof(int);
  NetDeviceContainer nd[linksNum];
  for(int i = 0; i < linksNum; i++){
    nd[i] = pointToPoint.Install(nodes.Get(linkFrom[i]), nodes.Get(linkTo[i]));
  }

  int* rand_edges[10]; 
  rand_edges[0] = new int[7]{39, 44, 78, 79, 1, 34, 48};
  rand_edges[1] = new int[10]{39, 44, 78, 79, 12, 34, 1, 48, 28, 7};
  rand_edges[2] = new int[13]{39, 44, 78, 79, 48, 34, 35, 27, 7, 17, 28, 12, 1};
  rand_edges[3] = new int[16]{39, 44, 78, 79, 46, 35, 17, 51, 7, 28, 34, 48, 12, 1, 27, 9};
  rand_edges[4] = new int[19]{39, 44, 78, 79, 17, 9, 27, 28, 51, 34, 40, 46, 1, 33, 48, 52, 7, 12, 35};
  rand_edges[5] = new int[22]{39, 44, 78, 79, 7, 46, 22, 27, 40, 48, 33, 51, 12, 28, 34, 52, 35, 17, 45, 9, 32, 1};
  rand_edges[6] = new int[25]{39, 44, 78, 79, 7, 51, 33, 34, 48, 17, 15, 1, 52, 45, 21, 27, 28, 32, 12, 22, 40, 46, 42, 9, 35};
  rand_edges[7] = new int[28]{39, 44, 78, 79, 48, 42, 51, 29, 45, 46, 21, 35, 27, 2, 32, 33, 17, 40, 1, 22, 7, 34, 9, 12, 28, 52, 14, 15};
  rand_edges[8] = new int[31]{39, 44, 78, 79, 18, 17, 48, 42, 27, 51, 14, 40, 5, 12, 9, 2, 28, 22, 15, 1, 23, 21, 33, 29, 7, 35, 46, 34, 52, 32, 45};
  rand_edges[9] = new int[34]{39, 44, 78, 79, 1, 34, 48, 7, 28, 12, 27, 17, 35, 9, 46, 51, 33, 40, 52, 45, 32, 22, 21, 42, 15, 2, 14, 29, 23, 5, 18, 38, 41, 26};
  int rand_edges_len[] = {7, 10, 13, 16, 19, 22, 25, 28, 31, 34};
  
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
  int startTimeDelta = SANDPACKETSNUM*DELTA_TIME + 1;

  UdpServerHelper echoServer (9);
  for(int i = 0; i < hostNum; i++){
    ApplicationContainer serverApps = echoServer.Install (hosts.Get (i));
    serverApps.Start (Seconds (1.0));
    serverApps.Stop (Seconds (99999));
  }

  if(REAL)
    add_delay_type = -1;
  PointToPointNetDevice p2p;
  p2p.SetExperimentInfo(3, DETECTION_EDGES_INDEX, 1, add_delay_type);
  
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
