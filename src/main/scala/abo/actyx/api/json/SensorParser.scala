package abo.actyx.api.json

import java.time.LocalDateTime

import scala.language.implicitConversions
import scala.Vector

import abo.actyx.api.Sensor
import spray.json.DefaultJsonProtocol.jsonFormat3
import spray.json.JsArray
import spray.json.JsNumber
import spray.json.JsString
import spray.json.JsValue
import spray.json.RootJsonFormat
import spray.json.pimpString

object SensorParser {
  
  type TimestampedValue = (String, Double)

  implicit object TimestampedValueFormat extends RootJsonFormat[TimestampedValue] {
    override def read(jsValue: JsValue): TimestampedValue = jsValue match {
      case JsArray(Vector(JsString(timestamp), JsNumber(value))) =>
        (timestamp, value.toDouble)
    }

    override def write(tv: TimestampedValue) =
      JsArray(Vector(JsString(tv._1), JsNumber(tv._2)))
  }

  private[json] implicit val sensorFormat = jsonFormat3(JsonSensor)

  private[json] implicit def toLocalDate(isoDateTimeStr: String): LocalDateTime =
    LocalDateTime.from(isoParser.parse(isoDateTimeStr))

  private[json] def toSensor(s: JsonSensor): Sensor =
    Sensor(s.pressure._2, s.temperature._2, s.humidity._2, s.pressure._1)

  def parse(json: String): Sensor = {
    val jsonSensor = json.parseJson.convertTo[JsonSensor]
    toSensor(jsonSensor)
  }

  private[json] case class JsonSensor(pressure: TimestampedValue, temperature: TimestampedValue,
      humidity: TimestampedValue)
}
