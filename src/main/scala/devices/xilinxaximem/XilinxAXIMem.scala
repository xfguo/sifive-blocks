// See LICENSE for license details.
package sifive.blocks.devices.xilinxaximem

import Chisel._
import config._
import diplomacy._
import uncore.tilelink2._
import uncore.axi4._
import rocketchip._
import sifive.blocks.ip.xilinx.xilinx_axi_mem.{AXIMem, AXIMemIOClocksReset}

class XilinxAXIMemIO extends Bundle with AXIMemIOClocksReset

class XilinxAXIMem(implicit p: Parameters) extends LazyModule {
  val slave = TLInputNode()

  val axi_to_mem = LazyModule(new AXIMem)
  axi_to_mem.slave   := TLToAXI4(idBits=4)(slave)

  lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val port = new XilinxAXIMemIO
      val slave_in = slave.bundleIn
    }

    io.port <> axi_to_mem.module.io.port
  }
}
