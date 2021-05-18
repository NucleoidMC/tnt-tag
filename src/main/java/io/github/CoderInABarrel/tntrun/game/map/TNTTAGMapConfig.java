package io.github.CoderInABarrel.tntrun.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class TNTTAGMapConfig {
    public static final Codec<TNTTAGMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id)
    ).apply(instance, TNTTAGMapConfig::new));

    public final Identifier id;

    public TNTTAGMapConfig(Identifier id) {
        this.id = id;
    }
}
