package com.leclowndu93150.extreme_sound_visualizer.client;

public class VisualizerState {
    private static boolean enabled = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            SoundVisualizerData.clear();
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            SoundVisualizerData.clear();
        }
    }
}
