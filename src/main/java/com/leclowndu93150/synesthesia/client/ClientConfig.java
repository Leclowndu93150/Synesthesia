package com.leclowndu93150.synesthesia.client;


import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue RENDER_THROUGH_WALLS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Synesthesia: Sound Visualizer Client Configuration");

        RENDER_THROUGH_WALLS = builder
                .comment("If true, sound visualizations will render through walls (disables depth testing)")
                .define("renderThroughWalls", true);

        SPEC = builder.build();
    }
}
