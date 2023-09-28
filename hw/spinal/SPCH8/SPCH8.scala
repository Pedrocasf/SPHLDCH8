package SPCH8

import spinal.core._
import spinal.lib._
// Hardware definition
case class SPCH8() extends Component {
  val io = new Bundle {
    val uart_rx = in Bool()
    val uart_tx = out Bool()
    val sclk = out Bool()
    val sdin = out Bool()
    val cs = out Bool()
    val dc = out Bool()
    val reset = out Bool()
  }
}

object SPCH8 extends App {
  Config.spinal.generateVerilog(SPCH8())
}