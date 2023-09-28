package SPCH8.SSD1306

import SPCH8.Config
import spinal.core._
import spinal.lib._

case class FrameBuffer() extends Component{
  val io = new Bundle{
    val addr = slave Flow UInt(10 bits)
    val RorW = in Bool()
    val inputData = slave Flow Bits(8 bits)
    val readData = master Flow Bits(8 bits)
    val spiOutData = master Stream Bits(8 bits)
  }
  val buffer = Mem(Bits(8 bits), 1024)
  val addr = io.addr.toReg()
  val iData = io.inputData.toReg()
  val dataCounter = Reg(UInt(10 bits)) init 0
  io.spiOutData.valid := False
  io.spiOutData.payload := 0
  when(io.spiOutData.ready){
    io.spiOutData.payload := buffer.readSync(dataCounter)
    dataCounter := dataCounter + 1
    io.spiOutData.valid := True
  }
  when(io.addr.fire){
    when(io.RorW){
      io.readData.payload := buffer.readSync(io.addr.payload)
      io.readData.valid := True
    }otherwise{
      io.readData.payload := 0
      io.readData.valid := False
      buffer.write(addr, iData)
    }
  }otherwise{
    io.readData.payload := 0
    io.readData.valid := False
  }
}

object FrameBuffer extends App {
  Config.spinal.generateVerilog(FrameBuffer())
}