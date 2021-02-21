package io.github.CoderInABarrel.tntrun.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class TNTTAGMapConfig {
    public static final Codec<TNTTAGMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, TNTTAGMapConfig::new));

    public final BlockState spawnBlock;

    public TNTTAGMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
