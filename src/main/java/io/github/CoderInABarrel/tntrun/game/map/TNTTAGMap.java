package io.github.CoderInABarrel.tntrun.game.map;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.LinkedList;

public class TNTTAGMap {
    private final MapTemplate template;
    private final TNTTAGMapConfig config;
    public LinkedList<BlockPos> powerupSpawns;
    public BlockBounds playerSpawn;
    public TNTTAGMap(MapTemplate template, TNTTAGMapConfig config, LinkedList<BlockPos> powerupSpawns, @Nullable BlockBounds playerSpawn) {
        this.template = template;
        this.config = config;
        this.powerupSpawns = powerupSpawns;
        this.playerSpawn = playerSpawn;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
