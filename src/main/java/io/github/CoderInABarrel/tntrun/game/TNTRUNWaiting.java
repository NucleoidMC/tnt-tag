package io.github.CoderInABarrel.tntrun.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import io.github.CoderInABarrel.tntrun.game.map.TNTRUNMap;
import io.github.CoderInABarrel.tntrun.game.map.TNTRUNMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class TNTRUNWaiting {
    private final GameSpace gameSpace;
    private final TNTRUNMap map;
    private final TNTRUNConfig config;
    private final TNTRUNSpawnLogic spawnLogic;
    private TNTRUNWaiting(GameSpace gameSpace, TNTRUNMap map, TNTRUNConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new TNTRUNSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<TNTRUNConfig> context) {
        TNTRUNConfig config = context.getConfig();
        TNTRUNMapGenerator generator = new TNTRUNMapGenerator(config.mapConfig);
        TNTRUNMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            TNTRUNWaiting waiting = new TNTRUNWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        TNTRUNActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);

    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
