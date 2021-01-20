package io.github.CoderInABarrel.tntrun.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class TNTRUNMapConfig {
    public static final Codec<TNTRUNMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, TNTRUNMapConfig::new));

    public final BlockState spawnBlock;

    public TNTRUNMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
