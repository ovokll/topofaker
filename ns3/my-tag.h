/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */

#include "ns3/core-module.h"
#include "ns3/tag.h"

#ifndef MY_MODULE_H
#define MY_MODULE_H

namespace ns3 {

class MyTag : public Tag
{
public:
  /**
   * \brief Get the type ID.
   * \return the object TypeId
   */
  static TypeId GetTypeId (void);
  virtual TypeId GetInstanceTypeId (void) const;
  virtual uint32_t GetSerializedSize (void) const;
  virtual void Serialize (TagBuffer i) const;
  virtual void Deserialize (TagBuffer i);
  virtual void Print (std::ostream &os) const;

  // these are our accessors to our tag structure
  /**
   * Set the tag value
   * \param value The tag value.
   */
  void SetSimpleValue (uint32_t value);
  /**
   * Get the tag value
   * \return the tag value.
   */
  uint32_t GetSimpleValue (void) const;
private:
  uint32_t m_simpleValue;  //!< tag value
};

}

#endif /* MY_MODULE_H */

