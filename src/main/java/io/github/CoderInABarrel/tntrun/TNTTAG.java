package io.github.CoderInABarrel.tntrun;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.CoderInABarrel.tntrun.game.powerup.GenericPowerup;
import io.github.CoderInABarrel.tntrun.game.powerup.PowerupRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.CoderInABarrel.tntrun.game.TNTTAGConfig;
import io.github.CoderInABarrel.tntrun.game.TNTTAGWaiting;

import java.io.*;
import java.util.Collection;

public class TNTTAG implements ModInitializer {

    public static final String ID = "tnttag";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final boolean THROW_EXAMPLE = true;
    public static final GameType<TNTTAGConfig> TYPE = GameType.register(
            new Identifier(ID, "tnttag"),
            TNTTAGWaiting::open,
            TNTTAGConfig.CODEC
    );

    @Override
    public void onInitialize() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        helper.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(ID);
            }

            @Override
            public void apply(ResourceManager manager) {
                Collection<Identifier> resources = manager.findResources("powerups", path -> path.endsWith(".json"));

                for (Identifier source : resources) {
                    try {
                        Resource resource = manager.getResource(source);

                        Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

                        JsonElement json = new JsonParser().parse(reader);

                        Identifier identifier = identifierFromPath(source);

                        DataResult<GenericPowerup> result = GenericPowerup.CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);

                        result.result().ifPresent(powerup -> PowerupRegistry.register(identifier, powerup));
                        result.error().ifPresent(error -> Plasmid.LOGGER.error("Failed to decode module at {}: {}", source, error.toString()));


                    } catch (IOException e) {
                        Plasmid.LOGGER.error(e.getMessage());
                    }
                }

            }
        });
    }
    private static Identifier identifierFromPath(Identifier location) {
        String path = location.getPath();
        path = path.substring("powerups/".length(), path.length() - ".json".length());
        return new Identifier(location.getNamespace(), path);
    }
}
