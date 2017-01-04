package abo.actyx.challenge1

import abo.actyx.api.MachineParkApiTestImpl

object PowerUsageAlertTestApp extends App with PowerUsageAlert {

  override def machineParkApi = MachineParkApiTestImpl
}
