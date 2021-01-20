package io.github.CoderInABarrel.tntrun.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import io.github.CoderInABarrel.tntrun.game.map.TNTRUNMapConfig;

public class TNTRUNConfig {
    public static final Codec<TNTRUNConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            TNTRUNMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, TNTRUNConfig::new));

    public final PlayerConfig playerConfig;
    public final TNTRUNMapConfig mapConfig;
    public final int timeLimitSecs;

    public TNTRUNConfig(PlayerConfig players, TNTRUNMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
