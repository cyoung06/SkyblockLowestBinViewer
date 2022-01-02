package kr.syeyoung.priceviewer;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = ZombiesAddon.MODID, version = ZombiesAddon.VERSION)
public class ZombiesAddon
{
    public static final String MODID = "hypixel_view_price";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(new EventListener());

        Prices.registerTimer();
    }


}
