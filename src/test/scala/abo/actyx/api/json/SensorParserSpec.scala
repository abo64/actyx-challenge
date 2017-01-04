package abo.actyx.api.json

import java.time.LocalDateTime
import java.time.ZoneId

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbDouble
import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks

import SensorParser.JsonSensor
import SensorParser.parse
import SensorParser.toSensor
import abo.actyx.api.Sensor
import spray.json.pimpAny

class SensorParserSpec extends FlatSpec with PropertyChecks {
  behavior of "parse"

  it should "parse a Sensor" in {
    val json =
      """{"pressure":["2017-01-02T11:23:00",1004.3],"temperature":["2016-12-27T11:23:00",16.75],"humidity":["2016-12-27T11:23:00",94.129999999999995]}"""
    val timestamp = LocalDateTime.of(2017, 1, 2, 11, 23)
    val expected = Sensor(1004.3, 16.75, 94.129999999999995, timestamp)

    val sensor = parse(json)

    assert(sensor == expected)
  }

  private implicit val arbitraryJsonSensor = Arbitrary[JsonSensor] {
    val timestampGen: Gen[String] = {
      for {
        date <- Gen.calendar map (_.getTime)
        localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
      } yield isoParser.format(localDateTime)
    }

    for {
      timestamp <- timestampGen
      pressure <- arbDouble.arbitrary
      temperature <- arbDouble.arbitrary
      humidity <- arbDouble.arbitrary
    } yield JsonSensor((timestamp, pressure), (timestamp, temperature), (timestamp, humidity))
  }

  it should "parse random Sensors" in {
    forAll ("jsonSensor") { jsonSensor: JsonSensor =>
      val expected = toSensor(jsonSensor)
      val json = jsonSensor.toJson.compactPrint
      val result = parse(json)
      assert(result == expected)
    }
  }
}
