package SPCH8.SSD1306

import SPCH8.Config
import spinal.core._
import spinal.lib._
case class WithBuffer() extends Component{
  val io = new Bundle {
    val addr = slave Flow UInt(10 bits)
    val inputData = slave Flow Bits(8 bits)
    val sclk = out Bool()
    val sdin = out Bool()
    val cs = out Bool()
    val dc = out Bool()
    val reset = out Bool()
  }
  val ssd1306 = SSD1306()
  ssd1306.io.sclk <> io.sclk
  ssd1306.io.sdin <> io.sdin
  ssd1306.io.cs <> io.cs
  ssd1306.io.dc <> io.dc
  ssd1306.io.reset <> io.reset
  val fb = FrameBuffer()
  ssd1306.io.din << fb.io.spiOutData
  fb.io.addr <> io.addr
  fb.io.inputData <> io.inputData
}

object WithBuffer extends App {
  Config.spinal.generateVerilog(WithBuffer())
}