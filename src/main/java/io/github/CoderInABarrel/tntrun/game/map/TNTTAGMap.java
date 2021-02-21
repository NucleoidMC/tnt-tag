package io.github.CoderInABarrel.tntrun.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class TNTTAGMap {
    private final MapTemplate template;
    private final TNTTAGMapConfig config;
    public BlockPos spawn;

    public TNTTAGMap(MapTemplate template, TNTTAGMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
