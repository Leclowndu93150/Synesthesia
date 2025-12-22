package com.leclowndu93150.synesthesia;

import com.leclowndu93150.synesthesia.client.ClientConfig;
import com.leclowndu93150.synesthesia.client.KeyBindings;
import com.leclowndu93150.synesthesia.client.SoundRenderer;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = Synesthesia.MODID, dist = Dist.CLIENT)
public class Synesthesia {
    public static final String MODID = "synesthesia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Synesthesia(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        NeoForge.EVENT_BUS.register(SoundRenderer.class);
        NeoForge.EVENT_BUS.register(KeyBindings.class);
        modEventBus.addListener(KeyBindings::registerKeyBindings);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
