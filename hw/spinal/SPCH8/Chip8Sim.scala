package SPCH8
import spinal.sim._
import spinal.core._
import spinal.core.sim._
object Chip8Sim {
  SimConfig.withIVerilog.compile(new Chip8()).doSim{ dut =>
    dut.clockDomain.forkStimulus(1)
    while (!dut.fsm.isActive(dut.fsm.unimplemented).toBoolean){

    }
  }
}
