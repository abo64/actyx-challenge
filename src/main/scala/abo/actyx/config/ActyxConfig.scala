package abo.actyx.config

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File

object ActyxConfig {

  private val conf = ConfigFactory.load()
  private val rootPath = "actyx"

  val AverageCurrentInterval: FiniteDuration =
    Duration(conf.getString(s"$rootPath.average-current-interval")).asInstanceOf[FiniteDuration]

  val MLDataFile: File =
    new File(conf.getString(s"$rootPath.ml-data-file"))

  // api config
  private val apiPath = s"$rootPath.api"

  val ApiUrl = conf.getString(s"$apiPath.url")

  val ApiTimeout: FiniteDuration =
    Duration(conf.getString(s"$apiPath.timeout")).asInstanceOf[FiniteDuration]

  val ApiMachineQueryInterval: FiniteDuration =
    Duration(conf.getString(s"$apiPath.machine-query-interval")).asInstanceOf[FiniteDuration]

  val ApiSensorQueryInterval: FiniteDuration =
    Duration(conf.getString(s"$apiPath.sensor-query-interval")).asInstanceOf[FiniteDuration]

}
