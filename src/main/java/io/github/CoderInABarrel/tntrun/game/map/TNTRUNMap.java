package io.github.CoderInABarrel.tntrun.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class TNTRUNMap {
    private final MapTemplate template;
    private final TNTRUNMapConfig config;
    public BlockPos spawn;

    public TNTRUNMap(MapTemplate template, TNTRUNMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
