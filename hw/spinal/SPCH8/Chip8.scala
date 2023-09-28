package SPCH8

import spinal.core._
import spinal.lib._
import spinal.lib.misc.BinTools
case class Chip8() extends Component{
  val io = new Bundle{
    val keys = slave Flow Bits(16 bits)
    val screen_RorW = out Bool()
    val screen_addr = master Flow UInt(10 bits)
    val screen_read = slave Flow Bits(8 bits)
    val screen_write = master Flow Bits(8 bits)
  }
  val memory = Mem(Bits(8 bits),4096)
  BinTools.initRam(memory, "1-chip8-logo.4KiB")
  val pc = Reg(UInt(12 bits)) init 0x200
  val i = Reg(UInt(12 bits)) init 0
  val vs = Vec(Reg(Bits(8 bits)) init 0, 16)
  val dt = Reg(UInt(8 bits)) init 0
  val st = Reg(UInt(8 bits)) init 0

}
object Chip8 extends App {
  Config.spinal.generateVerilog(Chip8())
}
