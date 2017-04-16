// See LICENSE for license details.
package sifive.blocks.devices.xilinxaximem

import Chisel._
import diplomacy.LazyModule
import rocketchip.{TopNetwork,TopNetworkBundle,TopNetworkModule}
import uncore.tilelink2.TLWidthWidget

trait PeripheryXilinxAXIMem extends TopNetwork{
  val xilinxaximem = LazyModule(new XilinxAXIMem)
  xilinxaximem.slave   := TLWidthWidget(socBusConfig.beatBytes)(socBus.node)
}

trait PeripheryXilinxAXIMemBundle extends TopNetworkBundle {
  val xilinxaximem = new XilinxAXIMemIO
}

trait PeripheryXilinxAXIMemModule extends TopNetworkModule {
  val outer: PeripheryXilinxAXIMem
  val io: PeripheryXilinxAXIMemBundle

  io.xilinxaximem <> outer.xilinxaximem.module.io.port
}
