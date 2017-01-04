package abo.actyx.api.json

import java.time.LocalDateTime
import java.time.ZoneId

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbDouble
import org.scalacheck.Arbitrary.arbString
import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks

import MachineParser.JsonMachine
import MachineParser.machineFormat
import MachineParser.parse
import MachineParser.toMachine
import abo.actyx.api.Machine
import abo.actyx.api.MachineId
import spray.json.pimpAny
import abo.actyx.api.Gens

class MachineParserSpec extends FlatSpec with PropertyChecks {
  behavior of "parse"

  it should "parse a Machine" in {
    val json =
      """{"name":"Trumpf TruLaser Cell 3000 [#12]","timestamp":"2017-01-02T08:05:38.295880","current":21.300000000000001,"state":"working","location":"0.0,0.0","current_alert":20.0,"type":"laser-cutter"}"""
    val id = "someId"
    val timestamp = LocalDateTime.of(2017, 1, 2, 8, 5, 38, 295880000)
    val expected =
      Machine(id, "Trumpf TruLaser Cell 3000 [#12]", timestamp, 21.3, "working", "0.0,0.0" , 20.0 , "laser-cutter")

    val machine = parse(id)(json)

    assert(machine == expected)
  }

  private implicit val arbitraryJsonMachine = Arbitrary[JsonMachine] {
    for {
      name <- arbString.arbitrary
      timestamp <- Gens.timestampGen
      current <- arbDouble.arbitrary
      state <- arbString.arbitrary
      location <- arbString.arbitrary
      current_alert <- arbDouble.arbitrary
      `type` <- arbString.arbitrary
    } yield JsonMachine(name, timestamp, current, state, location, current_alert, `type`)
  }

  it should "parse random Machines" in {
    forAll ("id", "jsonMachine") { (id: MachineId, jsonMachine: JsonMachine) =>
      val expected = toMachine(id, jsonMachine)
      val json = jsonMachine.toJson.compactPrint
      val result = parse(id)(json)
      assert(result == expected)
    }
  }
}