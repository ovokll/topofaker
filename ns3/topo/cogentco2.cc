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

const int SANDPACKETSNUM = 1;
const int TOPONODENUM = 197;
const float DELTA_TIME = 0.2;
const int DETECTION_EDGES_INDEX = 1;//0-9
const int REAL = 0;
int add_delay_type = 1;//1: topoFaker

NS_LOG_COMPONENT_DEFINE ("Cogentco");

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

  int linkFrom[] = {0, 0, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 7, 8, 8, 8, 10, 10, 11, 12, 12, 12, 13, 13, 14, 14, 14, 16, 18, 18, 19, 19, 19, 20, 20, 21, 22, 22, 24, 25, 25, 26, 26, 26, 28, 28, 29, 30, 31, 32, 32, 34, 35, 36, 36, 37, 37, 38, 39, 40, 40, 41, 41, 42, 42, 44, 44, 45, 45, 46, 46, 48, 48, 49, 49, 49, 50, 50, 51, 52, 52, 53, 54, 56, 56, 58, 60, 60, 61, 61, 62, 62, 62, 63, 63, 64, 64, 64, 65, 66, 67, 69, 70, 70, 71, 71, 72, 73, 74, 75, 75, 76, 77, 77, 77, 78, 78, 80, 80, 80, 82, 82, 82, 83, 84, 84, 86, 87, 89, 90, 91, 91, 92, 92, 92, 94, 95, 95, 96, 97, 98, 98, 99, 100, 101, 101, 101, 102, 103, 103, 105, 105, 105, 107, 107, 108, 109, 110, 111, 111, 112, 113, 113, 115, 115, 117, 117, 119, 119, 120, 121, 121, 123, 123, 123, 127, 127, 129, 131, 132, 133, 134, 134, 134, 136, 137, 138, 138, 139, 140, 142, 143, 144, 145, 146, 146, 147, 147, 148, 149, 151, 152, 153, 153, 154, 154, 155, 155, 155, 156, 157, 158, 158, 158, 161, 162, 162, 163, 163, 165, 165, 165, 165, 166, 166, 167, 168, 169, 178, 181, 182, 183, 183, 186, 190, 192, 193, 195};
  int linkTo[] = {176, 9, 8, 176, 114, 116, 175, 76, 77, 4, 77, 6, 135, 131, 6, 7, 8, 174, 194, 9, 191, 11, 13, 16, 32, 13, 30, 16, 15, 64, 129, 15, 17, 19, 30, 89, 68, 82, 21, 23, 26, 188, 23, 27, 171, 55, 27, 28, 29, 51, 54, 78, 35, 37, 33, 37, 37, 37, 38, 39, 160, 38, 196, 181, 41, 42, 43, 189, 43, 143, 45, 47, 48, 164, 49, 47, 155, 181, 177, 147, 165, 57, 51, 188, 53, 55, 58, 55, 57, 59, 59, 61, 69, 128, 122, 144, 86, 63, 68, 149, 65, 67, 68, 66, 67, 69, 144, 79, 183, 72, 79, 73, 74, 183, 173, 183, 173, 152, 162, 133, 94, 79, 81, 86, 87, 88, 83, 150, 148, 148, 85, 87, 88, 150, 172, 99, 92, 96, 93, 183, 171, 96, 171, 97, 172, 194, 131, 100, 132, 104, 180, 102, 109, 104, 106, 106, 107, 179, 108, 129, 179, 110, 128, 112, 140, 137, 114, 191, 116, 118, 120, 118, 176, 175, 175, 122, 124, 124, 125, 126, 128, 130, 130, 142, 135, 173, 137, 138, 135, 139, 172, 174, 141, 174, 141, 143, 185, 149, 157, 152, 154, 166, 177, 154, 150, 152, 153, 160, 154, 159, 183, 195, 156, 165, 157, 158, 183, 196, 165, 162, 163, 167, 187, 164, 177, 181, 184, 186, 170, 183, 168, 169, 185, 179, 196, 189, 184, 186, 187, 191, 193, 194, 196};

  int linksNum = sizeof(linkFrom)/sizeof(int);
  NetDeviceContainer nd[linksNum];
  for(int i = 0; i < linksNum; i++){
    nd[i] = pointToPoint.Install(nodes.Get(linkFrom[i]), nodes.Get(linkTo[i]));
  }

  int* rand_edges[10];
  rand_edges[0] = new int[9]{33, 34, 85, 31, 24, 17, 93, 81, 90};
  rand_edges[1] = new int[15]{34, 17, 81, 145, 125, 151, 31, 136, 85, 24, 33, 126, 93, 90, 159};
  rand_edges[2] = new int[21]{90, 170, 17, 145, 159, 190, 136, 126, 161, 93, 33, 180, 24, 31, 125, 85, 34, 182, 151, 178, 81};
  rand_edges[3] = new int[26]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 27, 71, 40, 66};
  rand_edges[4] = new int[32]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 18, 40, 66, 43, 9, 27, 35, 71, 70, 54};
  rand_edges[5] = new int[38]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 43, 54, 15, 25, 66, 18, 39, 36, 5, 9, 71, 35, 40, 27, 70, 72};
  rand_edges[6] = new int[43]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 35, 54, 18, 5, 11, 66, 44, 9, 40, 15, 72, 21, 25, 43, 39, 27, 52, 36, 2, 70, 71};
  rand_edges[7] = new int[49]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 5, 15, 10, 22, 2, 71, 40, 57, 18, 65, 54, 52, 36, 25, 70, 72, 20, 11, 0, 35, 21, 27, 39, 44, 43, 9, 66};
  rand_edges[8] = new int[55]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 40, 18, 50, 60, 66, 10, 35, 53, 15, 54, 58, 2, 25, 52, 0, 11, 43, 39, 21, 27, 65, 71, 22, 3, 36, 44, 56, 57, 20, 5, 9, 72, 70};
  rand_edges[9] = new int[60]{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192, 66, 71, 27, 40, 54, 70, 43, 9, 18, 35, 36, 5, 39, 72, 25, 15, 11, 2, 52, 44, 21, 0, 20, 65, 57, 22, 10, 53, 56, 50, 58, 3, 60, 23, 29, 46, 59, 47};
  int rand_edges_len[] = {9, 15, 21, 26, 32, 38, 43, 49, 55, 60};

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
    serverApps.Stop (Seconds (999999));
  }

  if(REAL)
    add_delay_type = -1;
  PointToPointNetDevice p2p;
  p2p.SetExperimentInfo(2, DETECTION_EDGES_INDEX, 2, add_delay_type);
  
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
        udpClient3->SetPacketTag((j<<8)+k);//don't use high 4 bit
        udpClient3->SetOutputPath(is_output);//0x1:don't add delay ; 0x2:don't output path; 0x4: don't output delay
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
