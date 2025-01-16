/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */

#include "ns3/core-module.h"
#include "my-tag.h"
#include "ns3/tag.h"

namespace ns3 {

TypeId 
MyTag::GetTypeId (void)
{
  static TypeId tid = TypeId ("ns3::MyTag")
    .SetParent<Tag> ()
    .AddConstructor<MyTag> ()
    .AddAttribute ("SimpleValue",
                   "A simple value",
                   EmptyAttributeValue (),
                   MakeUintegerAccessor (&MyTag::GetSimpleValue),
                   MakeUintegerChecker<uint32_t> ())
  ;
  return tid;
}
TypeId 
MyTag::GetInstanceTypeId (void) const
{
  return GetTypeId ();
}
uint32_t 
MyTag::GetSerializedSize (void) const
{
  return 4;
}
void 
MyTag::Serialize (TagBuffer i) const
{
  i.WriteU32 (m_simpleValue);
}
void 
MyTag::Deserialize (TagBuffer i)
{
  m_simpleValue = i.ReadU32 ();
}
void 
MyTag::Print (std::ostream &os) const
{
  os << "v=" << (uint32_t)m_simpleValue;
}
void 
MyTag::SetSimpleValue (uint32_t value)
{
  m_simpleValue = value;
}
uint32_t 
MyTag::GetSimpleValue (void) const
{
  return m_simpleValue;
}

}

