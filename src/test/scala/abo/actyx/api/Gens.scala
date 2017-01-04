package abo.actyx.api

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

import scala.collection.concurrent.TrieMap
import scala.language.implicitConversions

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import abo.actyx.config.ActyxConfig
import abo.util.Kestrel.KestrelOps
import json.isoParser

object Gens {
  implicit val arbitraryLocalDateTime = Arbitrary[LocalDateTime] {
    val dateGen: Gen[Date] = Gen.calendar map (_.getTime)
    dateGen map { date =>
      LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }
  }

  val timestampGen: Gen[String] =
      arbitraryLocalDateTime.arbitrary map isoParser.format

  private val timestamps =
    TrieMap[MachineId, LocalDateTime]() withDefaultValue(LocalDateTime.now)

  implicit def arbitraryMachine(id: MachineId): Arbitrary[Machine] = Arbitrary[Machine] {
    val interval = ActyxConfig.ApiMachineQueryInterval
    for {
      current <- Gen.posNum[Double]
      name = s"machine-$id"
      timestamp = timestamps(id).plusNanos(interval.toNanos) kestrel (timestamps += id -> _)
      state = s"state-$id"
      location = s"location-$id"
      current_alert <- Gen.posNum[Double]
      tpe = s"type-$id"
    } yield Machine(id, name, timestamp, current, state, location, current_alert, tpe)
  }
}
