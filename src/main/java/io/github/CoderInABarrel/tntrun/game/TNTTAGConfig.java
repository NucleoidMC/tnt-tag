package io.github.CoderInABarrel.tntrun.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import io.github.CoderInABarrel.tntrun.game.map.TNTTAGMapConfig;

public class TNTTAGConfig {
    public static final Codec<TNTTAGConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            TNTTAGMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, TNTTAGConfig::new));

    public final PlayerConfig playerConfig;
    public final TNTTAGMapConfig mapConfig;
    public final int timeLimitSecs;

    public TNTTAGConfig(PlayerConfig players, TNTTAGMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
