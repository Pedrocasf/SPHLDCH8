package SPCH8.SSD1306

import SPCH8.Config
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
case class SSD1306 (CLOCK:Int = 12 * 1000 * 1000) extends Component{
  val STARTUP_WAIT:Int = CLOCK / 3
  val io = new Bundle{
    val sclk = out Bool()
    val sdin = out Bool()
    val cs = out Bool()
    val dc = out Bool()
    val reset = out Bool()
    val din = slave Stream Bits(8 bits)
  }
  io.din.ready := False
  val counter = Reg(UInt( log2Up(CLOCK)bits)) init 0
  val dc = Reg(Bool()) init True
  val sclk = Reg(Bool()) init True
  val sdin = Reg(Bool()) init False
  val reset = Reg(Bool()) init True
  val cs = Reg(Bool()) init False
  val dataToSend = Reg(Bits(8 bits)) init 0
  val bitNumber = Reg(UInt(3 bits)) init 0
  val pixelCounter = Reg(UInt(10 bits)) init 0
  val SETUP_INSTRUCTIONS:Int = 23
  val startupCommnads = Mem(Bits(8 bits),SETUP_INSTRUCTIONS) init Seq(
    0xAE,// display off
    0x81,// contast value to 0x7F according to datasheet
    0x7F,
    0xA6,// normal screen mode (not inverted)
    0x20,// horizontal addressing mode
    0x00,
    0xC8,// normal scan direction
    0x40,// first line to start scanning from
    0xA1,// address 0 is segment 0
    0xA8,// mux ratio
    0x3F,// 63 (64 -1)
    0xD3,// display offset
    0x00,// no offset
    0xD5,// clock divide ratio
    0x80,// set to default ratio/osc frequency
    0xD9,// set precharge
    0x22,// switch precharge to 0x22 default
    0xDB,// vcom deselect level
    0x20,// 0x20
    0x8D,// charge pump config
    0x14,// enable charge pump
    0xA4,// resume RAM content
    0xAF // display on
  )
  val commandIndex = Reg(UInt(5 bits)) init 0
  io.sclk := sclk
  io.sdin := sdin
  io.dc := dc
  io.reset := reset
  io.cs := cs
  val SSD1306FSM = new StateMachine{
    val STATE_INIT_POWER:State = new State with EntryPoint{
      whenIsActive{
        counter := counter + 1
        when(counter < STARTUP_WAIT){
          reset := True
        }elsewhen(counter < (STARTUP_WAIT*2)){
          reset := False
        }elsewhen(counter < (STARTUP_WAIT*3)){
          reset := True
        }otherwise{
          counter := 0
          goto(STATE_LOAD_INIT_CMD)
        }
      }
    }
    val STATE_LOAD_INIT_CMD:State = new State{
      whenIsActive{
        dc := False
        dataToSend := startupCommnads.readSync(commandIndex)
        bitNumber := 7
        cs := False
        commandIndex := commandIndex + 1
        goto(STATE_SEND)
      }
    }
    val STATE_SEND:State = new State{
      whenIsActive{
        when(counter === 0){
          sclk := False
          sdin := dataToSend(bitNumber)
          counter := 1
        }otherwise{
          counter := 0
          sclk := True
          when(bitNumber === 0){
            goto(STATE_CHECK_FINISHED_INIT)
          }otherwise{
            bitNumber := bitNumber - 1
          }
        }
      }
    }
    val STATE_CHECK_FINISHED_INIT:State = new State {
      whenIsActive{
        cs := True
        when(commandIndex === SETUP_INSTRUCTIONS){
          goto(STATE_LOAD_DATA)
        }otherwise{
          goto(STATE_LOAD_INIT_CMD)
        }
      }
    }
    val STATE_LOAD_DATA:State = new State{
      whenIsActive{
        pixelCounter := pixelCounter + 1
        io.din.ready := True
        cs := False
        dc := True
        when(io.din.fire){
          bitNumber := 7
          dataToSend := io.din.fire ? io.din.payload | 0
          goto(STATE_SEND)
        }
      }
    }
  }
}
object SSD1306 extends App {
  Config.spinal.generateVerilog(SSD1306())
}