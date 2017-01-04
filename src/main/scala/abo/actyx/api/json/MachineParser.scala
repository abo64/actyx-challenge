package abo.actyx.api.json

import java.time.LocalDateTime

import scala.language.implicitConversions

import abo.actyx.api.Machine
import abo.actyx.api.MachineId
import spray.json.DefaultJsonProtocol.DoubleJsonFormat
import spray.json.DefaultJsonProtocol.StringJsonFormat
import spray.json.DefaultJsonProtocol.jsonFormat7
import spray.json.pimpString

object MachineParser {
  private[json] implicit val machineFormat = jsonFormat7(JsonMachine)

  private[json] implicit def toLocalDate(isoDateTimeStr: String): LocalDateTime =
    LocalDateTime.from(isoParser.parse(isoDateTimeStr))

  private[json] def toMachine(id: MachineId, m: JsonMachine): Machine =
    Machine(id, m.name, m.timestamp, m.current, m.state, m.location, m.current_alert, m.`type`)

  def parse(id: MachineId)(json: String): Machine = {
    val jsonMachine = json.parseJson.convertTo[JsonMachine]
    toMachine(id, jsonMachine)
  }

  private[json] case class JsonMachine(name: String, timestamp: String, current: Double,
    state: String, location: String, current_alert: Double, `type`: String)
}
