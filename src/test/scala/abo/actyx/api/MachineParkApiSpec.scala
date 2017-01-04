package abo.actyx.api

import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks

import MachineParkApiImpl.getId

class MachineParkApiSpec extends FlatSpec with PropertyChecks {

  private def machineDescription(id: String) =
    """"$API_ROOT/machine/""" + s"""$id""""

  behavior of "getId"

  it should "parse an id" in {
    val id = s"foo"
    assert(getId(machineDescription(id)) == id)
  }

  it should "parse random ids" in {
    forAll("id") { id: String =>
      whenever(id.nonEmpty) {
        assert(getId(machineDescription(id)) == id)
      }
    }
  }
}
