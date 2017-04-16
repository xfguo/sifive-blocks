// See LICENSE for license details.
package sifive.blocks.ip.xilinx.xilinx_axi_mem

import Chisel._
import config._
import diplomacy._
import uncore.axi4._
import junctions._

// IP VLNV: xilinx.com:customize_ip:vc707pcietoaxi:1.0
// Black Box
// Signals named _exactly_ as per Vivado generated verilog
// s : -{lock, cache, prot, qos}

trait AXIMemIOClocksReset extends Bundle {
  //clock, reset, control
  val s_aresetn           = Bool(INPUT)
  val s_aclk              = Clock(INPUT)
}

//scalastyle:off
//turn off linter: blackbox name must match verilog module 
class xilinx_axi_mem() extends BlackBox
{
  val io = new Bundle with AXIMemIOClocksReset {
    //axi slave
    //-{lock, cache, prot, qos}
    //slave interface write address
    val s_axi_awid            = Bits(INPUT,4)
    val s_axi_awaddr          = Bits(INPUT,32)
    //val s_axi_awregion        = Bits(INPUT,4)
    val s_axi_awlen           = Bits(INPUT,8)
    val s_axi_awsize          = Bits(INPUT,3)
    val s_axi_awburst         = Bits(INPUT,2)
    //val s_axi_awlock        = Bool(INPUT)
    //val s_axi_awcache       = Bits(INPUT,4)
    //val s_axi_awprot        = Bits(INPUT,3)
    //val s_axi_awqos         = Bits(INPUT,4)
    val s_axi_awvalid         = Bool(INPUT)
    val s_axi_awready         = Bool(OUTPUT)
    //slave interface write data
    val s_axi_wdata           = Bits(INPUT,32)
    val s_axi_wstrb           = Bits(INPUT,4)
    val s_axi_wlast           = Bool(INPUT)
    val s_axi_wvalid          = Bool(INPUT)
    val s_axi_wready          = Bool(OUTPUT)
    //slave interface write response
    val s_axi_bready          = Bool(INPUT)
    val s_axi_bid             = Bits(OUTPUT,4)
    val s_axi_bresp           = Bits(OUTPUT,2)
    val s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address
    val s_axi_arid            = Bits(INPUT,4)
    val s_axi_araddr          = Bits(INPUT,32)
    //val s_axi_arregion        = Bits(INPUT,4)
    val s_axi_arlen           = Bits(INPUT,8)
    val s_axi_arsize          = Bits(INPUT,3)
    val s_axi_arburst         = Bits(INPUT,2)
    //val s_axi_arlock        = Bits(INPUT,1)
    //val s_axi_arcache       = Bits(INPUT,4)
    //val s_axi_arprot        = Bits(INPUT,3)
    //val s_axi_arqos         = Bits(INPUT,4)
    val s_axi_arvalid         = Bool(INPUT)
    val s_axi_arready         = Bool(OUTPUT)
    //slave interface read data
    val s_axi_rready          = Bool(INPUT)
    val s_axi_rid             = Bits(OUTPUT,4)
    val s_axi_rdata           = Bits(OUTPUT,32)
    val s_axi_rresp           = Bits(OUTPUT,2)
    val s_axi_rlast           = Bool(OUTPUT)
    val s_axi_rvalid          = Bool(OUTPUT)
 }
}
//scalastyle:off

//wrap vc707_axi_to_pcie_x1 black box in Nasti Bundles

class AXIMem(implicit p:Parameters) extends LazyModule
{
  val slave = AXI4SlaveNode(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = List(AddressSet(0x60000000L, 0xfff)),
      executable    = true,
      supportsWrite = TransferSizes(1, 256),
      supportsRead  = TransferSizes(1, 256),
      interleavedId = Some(0))), // the Xilinx IP is friendly
    beatBytes = 8))

  lazy val module = new LazyModuleImp(this) {
    // Must have exactly the right number of idBits
    require (slave.edgesIn(0).bundle.idBits == 4)

    class AXIMemIOBundle extends Bundle with AXIMemIOClocksReset;

    val io = new Bundle {
      val port = new AXIMemIOBundle
      val slave_in = slave.bundleIn
      val interrupt_out = Bool(OUTPUT)
    }

    val blackbox = Module(new xilinx_axi_mem)

    val s = io.slave_in(0)

    //to top level
    blackbox.io.s_aclk            := io.port.s_aclk
    blackbox.io.s_aresetn         := io.port.s_aresetn

    //s
    //AXI4 signals ordered as per AXI4 Specification (Release D) Section A.2
    //-{lock, cache, prot, qos} 
    //-{aclk, aresetn, awuser, wid, wuser, buser, ruser}
    //global signals
    //aclk                          :=
    //aresetn                       :=
    //slave interface write address
    blackbox.io.s_axi_awid          := s.aw.bits.id
    blackbox.io.s_axi_awaddr        := s.aw.bits.addr
    blackbox.io.s_axi_awlen         := s.aw.bits.len
    blackbox.io.s_axi_awsize        := s.aw.bits.size
    blackbox.io.s_axi_awburst       := s.aw.bits.burst
    //blackbox.io.s_axi_awlock      := s.aw.bits.lock
    //blackbox.io.s_axi_awcache     := s.aw.bits.cache
    //blackbox.io.s_axi_awprot      := s.aw.bits.prot
    //blackbox.io.s_axi_awqos       := s.aw.bits.qos
    //blackbox.io.s_axi_awregion      := UInt(0)
    //blackbox.io.awuser            := s.aw.bits.user
    blackbox.io.s_axi_awvalid       := s.aw.valid
    s.aw.ready                   := blackbox.io.s_axi_awready
    //slave interface write data ports
    //blackbox.io.s_axi_wid         := s.w.bits.id
    blackbox.io.s_axi_wdata         := s.w.bits.data
    blackbox.io.s_axi_wstrb         := s.w.bits.strb
    blackbox.io.s_axi_wlast         := s.w.bits.last
    //blackbox.io.s_axi_wuser       := s.w.bits.user
    blackbox.io.s_axi_wvalid        := s.w.valid
    s.w.ready                    := blackbox.io.s_axi_wready
    //slave interface write response
    s.b.bits.id                  := blackbox.io.s_axi_bid
    s.b.bits.resp                := blackbox.io.s_axi_bresp
    //s.b.bits.user              := blackbox.io.s_axi_buser
    s.b.valid                    := blackbox.io.s_axi_bvalid
    blackbox.io.s_axi_bready        := s.b.ready
    //slave AXI interface read address ports
    blackbox.io.s_axi_arid          := s.ar.bits.id
    blackbox.io.s_axi_araddr        := s.ar.bits.addr
    blackbox.io.s_axi_arlen         := s.ar.bits.len
    blackbox.io.s_axi_arsize        := s.ar.bits.size
    blackbox.io.s_axi_arburst       := s.ar.bits.burst
    //blackbox.io.s_axi_arlock      := s.ar.bits.lock
    //blackbox.io.s_axi_arcache     := s.ar.bits.cache
    //blackbox.io.s_axi_arprot      := s.ar.bits.prot
    //blackbox.io.s_axi_arqos       := s.ar.bits.qos
    //blackbox.io.s_axi_arregion      := UInt(0)
    //blackbox.io.s_axi_aruser      := s.ar.bits.user
    blackbox.io.s_axi_arvalid       := s.ar.valid
    s.ar.ready                   := blackbox.io.s_axi_arready
    //slave AXI interface read data ports
    s.r.bits.id                  := blackbox.io.s_axi_rid
    s.r.bits.data                := blackbox.io.s_axi_rdata
    s.r.bits.resp                := blackbox.io.s_axi_rresp
    s.r.bits.last                := blackbox.io.s_axi_rlast
    //s.r.bits.ruser             := blackbox.io.s_axi_ruser
    s.r.valid                    := blackbox.io.s_axi_rvalid
    blackbox.io.s_axi_rready        := s.r.ready

  }
}
