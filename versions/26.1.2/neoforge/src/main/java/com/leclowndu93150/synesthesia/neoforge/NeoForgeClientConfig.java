package com.leclowndu93150.synesthesia.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue RENDER_THROUGH_WALLS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Synesthesia: Sound Visualizer Client Configuration");

        RENDER_THROUGH_WALLS = builder
                .comment("If true, sound visualizations will render through walls (disables depth testing)")
                .define("renderThroughWalls", true);

        SPEC = builder.build();
    }
}
