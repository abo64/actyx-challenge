package abo.actyx.challenge1

import abo.actyx.api.MachineParkApiImpl

// monitors the current being drawn by all machines in the Actyx Machine Park,
// and alerts a fictitious operator whenever this current goes above the machine
// specific alert threshold.
// In the alert provide the operator with the average current the machine drew
// in the five minutes before it went above the alert threshold (hint: moving average filter).
object PowerUsageAlertApp extends App with PowerUsageAlert {

  override def machineParkApi = new MachineParkApiImpl
}
