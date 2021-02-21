package io.github.CoderInABarrel.tntrun;


import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.CoderInABarrel.tntrun.game.TNTTAGConfig;
import io.github.CoderInABarrel.tntrun.game.TNTTAGWaiting;

public class TNTTAG implements ModInitializer {

    public static final String ID = "tnttag";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<TNTTAGConfig> TYPE = GameType.register(
            new Identifier(ID, "tnttag"),
            TNTTAGWaiting::open,
            TNTTAGConfig.CODEC
    );

    @Override
    public void onInitialize() {
    }
}
