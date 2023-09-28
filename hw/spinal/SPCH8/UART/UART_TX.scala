package SPCH8.UART

import SPCH8.Config
import spinal.core._
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
import spinal.lib._
case class UART_TX (CLOCK_SPEED:Int = 12 * 1000 * 1000,BAUD_RATE:Int=115200) extends Component {
  val DELAY_FRAMES: Int = CLOCK_SPEED / BAUD_RATE
  val HALF_DELAY_WAIT: Int = DELAY_FRAMES / 2
  val io = new Bundle{
    val uart_tx = out Bool()
    val writeFromMem = slave Stream Bits(8 bits)
  }
  val dataOut = Reg(Bits(8 bits)) init 0
  val txPinRegister = Reg(Bool()) init True
  val txCounter = Reg(UInt(log2Up(DELAY_FRAMES * 10) bits)) init 0
  val recvByteCounter = Reg(UInt(7 bits)) init 0
  val txByteCounter = Reg(UInt(7 bits)) init 0
  //when(io.writeFromMem.fire){
  //  recvByteCounter := recvByteCounter + 1
  //}
  val txBitNumber = Reg(UInt(3 bits)) init 0
  val frameMemory = Mem(Bits(8 bits), 128)
  io.uart_tx := txPinRegister
  io.writeFromMem.ready := False
  val txFSM = new StateMachine{
    val TX_STATE_IDLE:State = new State with EntryPoint{
      whenIsActive{
        when(io.writeFromMem.fire){
          txCounter := 0
          txByteCounter := 0
          goto(TX_STATE_START_BIT)
        }otherwise{
          io.writeFromMem.ready := True
          txPinRegister := True
        }
      }
    }
    val TX_STATE_START_BIT:State = new State{
      whenIsActive{
        txPinRegister := False
        when((txCounter+1)===DELAY_FRAMES){
          //io.writeFromMem.ready := True
          dataOut := io.writeFromMem.payload
          txCounter := 0
          txBitNumber := 0
          goto(TX_STATE_WRITE)
        }otherwise{
          txCounter := txCounter + 1
        }
      }
    }
    val TX_STATE_WRITE:State = new State{
      whenIsActive{
        txPinRegister := dataOut(txBitNumber)
        when((txCounter+1)===DELAY_FRAMES){
          when(txBitNumber === 7){
            goto(TX_STATE_STOP_BIT)
          }otherwise{
            txBitNumber := txBitNumber + 1
          }
          txCounter := 0
        }otherwise{
          txCounter := txCounter + 1
        }
      }
    }
    val TX_STATE_STOP_BIT:State = new State {
      whenIsActive {
        txPinRegister := True
        when((txCounter + 1) === DELAY_FRAMES) {
          goto(TX_STATE_IDLE)
          txCounter := 0
        } otherwise {
          txCounter := txCounter + 1
        }
      }
    }
  }
}
object UART_TX extends App {
  Config.spinal.generateVerilog(UART_TX())
}