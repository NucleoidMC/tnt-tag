package io.github.CoderInABarrel.tntrun.game.map;

import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.util.math.BlockPos;

public class TNTTAGMapGenerator {

    private final TNTTAGMapConfig config;

    public TNTTAGMapGenerator(TNTTAGMapConfig config) {
        this.config = config;
    }

    public TNTTAGMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        TNTTAGMap map = new TNTTAGMap(template, this.config);

        this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildSpawn(MapTemplate builder) {
        BlockPos min = new BlockPos(-50, 64, -50);
        BlockPos max = new BlockPos(50, 64, 50);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
