package io.github.CoderInABarrel.tntrun;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.CoderInABarrel.tntrun.game.TNTRUNConfig;
import io.github.CoderInABarrel.tntrun.game.TNTRUNWaiting;

public class TNTRUN implements ModInitializer {

    public static final String ID = "tnttag";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<TNTRUNConfig> TYPE = GameType.register(
            new Identifier(ID, "tnttag"),
            TNTRUNWaiting::open,
            TNTRUNConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
