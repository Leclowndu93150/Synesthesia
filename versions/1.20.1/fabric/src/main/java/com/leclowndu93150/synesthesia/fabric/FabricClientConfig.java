package com.leclowndu93150.synesthesia.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricClientConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("synesthesia-client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Data data = new Data();

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                Data loaded = GSON.fromJson(json, Data.class);
                if (loaded != null) {
                    data = loaded;
                }
            } else {
                save();
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException ignored) {
        }
    }

    public static boolean renderThroughWalls() {
        return data.renderThroughWalls;
    }

    public static class Data {
        public boolean renderThroughWalls = true;
    }
}
