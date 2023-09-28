package SPCH8.UART

import SPCH8.Config
import spinal.core._
import spinal.lib._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
case class UART_RX (CLOCK_SPEED:Int = 12 * 1000 * 1000,BAUD_RATE:Int=115200) extends Component{
  val DELAY_FRAMES:Int = CLOCK_SPEED/BAUD_RATE
  val HALF_DELAY_WAIT:Int = DELAY_FRAMES / 2
  val io = new Bundle {
    val uart_rx = in Bool()
    val writeToMem = master Flow UInt(8 bits)
  }
  val rxCounter = Reg(UInt(log2Up(DELAY_FRAMES * 10) bits)) init 0
  val rxBitNumber = Reg(UInt(3 bits)) init 0
  val dataIn = Reg(Bits(8 bits)) init 0
  val byteReady = Reg(Bool()) init False
  io.writeToMem.valid := byteReady
  io.writeToMem.payload := byteReady ? U(dataIn) | 0
  val rxFSM = new StateMachine{
    val RX_STATE_IDLE:State = new State with EntryPoint{
      whenIsActive{
        byteReady := False
        when(!io.uart_rx) {
          rxCounter := 1
          rxBitNumber := 0
          dataIn := 32
          goto(RX_STATE_START_BIT)
        }
      }
    }
    val RX_STATE_START_BIT:State = new State{
      whenIsActive {
        when(rxCounter === HALF_DELAY_WAIT) {
          rxCounter := 1
          goto(RX_STATE_READ_WAIT)
        } otherwise {
          rxCounter := rxCounter + 1
        }
      }
    }
    val RX_STATE_READ_WAIT:State = new State{
      whenIsActive {
        rxCounter := rxCounter + 1
        when(rxCounter + 1 === DELAY_FRAMES) {
          goto(RX_STATE_READ)
        }
      }
    }
    val RX_STATE_READ:State = new State{
      whenIsActive {
        rxCounter := 1
        dataIn := io.uart_rx ## dataIn(7 downto 1)
        rxBitNumber := rxBitNumber + 1
        when(rxBitNumber === 7) {
          goto(RX_STATE_STOP_BIT)
        } otherwise {
          goto(RX_STATE_READ_WAIT)
        }
      }
    }
    val RX_STATE_STOP_BIT:State = new State {
      whenIsActive {
        rxCounter := rxCounter + 1
        when((rxCounter + 1) === DELAY_FRAMES) {
          rxCounter := 0
          byteReady := True
          goto(RX_STATE_IDLE)
        }
      }
    }
  }
}
object UART_RX extends App {
  Config.spinal.generateVerilog(UART_RX())
}