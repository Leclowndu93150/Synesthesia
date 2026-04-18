package com.leclowndu93150.synesthesia.client;

import java.util.function.BooleanSupplier;

public class ClientConfig {
    private static BooleanSupplier renderThroughWalls = () -> true;

    public static void setRenderThroughWallsSupplier(BooleanSupplier supplier) {
        renderThroughWalls = supplier;
    }

    public static boolean renderThroughWalls() {
        return renderThroughWalls.getAsBoolean();
    }
}
