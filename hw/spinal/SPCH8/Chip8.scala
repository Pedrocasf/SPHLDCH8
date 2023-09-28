package SPCH8

import spinal.core._
import spinal.lib._
case class Chip8() extends Component{
  val io = new Bundle{
    val keys = slave Flow Bits(16 bits)
    val screen_RorW = out Bool()
  }
}
object Chip8 extends App {
  Config.spinal.generateVerilog(Chip8())
}