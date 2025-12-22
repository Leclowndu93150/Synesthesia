package com.leclowndu93150.synesthesia;

import com.leclowndu93150.synesthesia.client.ClientConfig;
import com.leclowndu93150.synesthesia.client.KeyBindings;
import com.leclowndu93150.synesthesia.client.SoundRenderer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value = Synesthesia.MODID)
public class Synesthesia {
    public static final String MODID = "synesthesia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Synesthesia() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(SoundRenderer.class);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
        modEventBus.addListener(KeyBindings::registerKeyBindings);
    }
}
