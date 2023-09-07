package com.plr.pttorch

import com.plr.pttorch.PreserveThatTorch.MODID
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumHand
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod(modid = MODID, useMetadata = true, modLanguage = "scala", acceptableRemoteVersions = "*")
object PreserveThatTorch {
  final val MODID = "pttorch"

  @EventHandler
  def preInit(e: FMLPreInitializationEvent): Unit = {
    CommonConfig.init(e)
    MinecraftForge.EVENT_BUS.register(new ForgeEventHandler)
  }

  class ForgeEventHandler {
    @SubscribeEvent
    def onRightClick(e: PlayerInteractEvent.RightClickBlock): Unit = {
      if (e.getWorld.isRemote) return
      if (!e.getHand.equals(EnumHand.OFF_HAND)) return
      val off = e.getItemStack
      if (off.isEmpty) return
      val rlOff = off.getItem.getRegistryName
      if (rlOff == null) return
      if (!CommonConfig.torches.contains(rlOff.toString)) return
      if (CommonConfig.doNotUseLastTorch && off.getCount == 1) {
        e.setCanceled(true)
        return
      }
      val main = e.getEntityPlayer.getHeldItemMainhand
      if (CommonConfig.noOffhandTorchWithEmptyHand && main.isEmpty) {
        e.setCanceled(true)
        return
      }
      if (CommonConfig.noOffhandTorchWithBlock && main.getItem.isInstanceOf[ItemBlock]) {
        e.setCanceled(true)
        return
      }
      if (!CommonConfig.offhandTorchWithToolOnly) return
      val rlMain = main.getItem.getRegistryName
      if (rlMain == null || !CommonConfig.torchTools.contains(rlMain.toString)) e.setCanceled(true)
    }
  }


  object CommonConfig {
    private var cfg: Configuration = _

    var torches: Array[String] = _

    var torchTools: Array[String] = _

    var doNotUseLastTorch: Boolean = _

    var noOffhandTorchWithBlock: Boolean = _

    var noOffhandTorchWithEmptyHand: Boolean = _

    var offhandTorchWithToolOnly: Boolean = _

    def init(e: FMLPreInitializationEvent): Unit = {
      cfg = new Configuration(e.getSuggestedConfigurationFile)
      sync()
    }

    def sync(): Unit = {
      cfg.load()
      torches = cfg.getStringList("torches", Configuration.CATEGORY_GENERAL,
        Array("minecraft:torch", "tconstruct:stone_torch"), "Items that count as torches for the offhand-torch tweak options.")
      torchTools = cfg.getStringList("torchTools", Configuration.CATEGORY_GENERAL,
        Array("minecraft:wooden_pickaxe", "minecraft:stone_pickaxe", "minecraft:iron_pickaxe",
          "minecraft:golden_pickaxe", "minecraft:diamond_pickaxe", "tconstruct:pickaxe", "tconstruct:hammer"),
        "Items that will place torches from your hotbar on right-click if \"offhandTorchWithToolOnly\" is enabled.")
      doNotUseLastTorch = cfg.getBoolean("doNotUseLastTorch", Configuration.CATEGORY_GENERAL,
        false, "Do Not Use Last Torch")
      noOffhandTorchWithBlock = cfg.getBoolean("noOffhandTorchWithBlock", Configuration.CATEGORY_GENERAL,
        true, "No Offhand Torch With Block")
      noOffhandTorchWithEmptyHand = cfg.getBoolean("noOffhandTorchWithEmptyHand", Configuration.CATEGORY_GENERAL,
        false, "No Offhand Torch With Empty Hand")
      offhandTorchWithToolOnly = cfg.getBoolean("offhandTorchWithToolOnly", Configuration.CATEGORY_GENERAL,
        false, "Offhand Torch With Tool Only")
      cfg.save()
    }
  }
}
