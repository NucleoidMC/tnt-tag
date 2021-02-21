package io.github.CoderInABarrel.tntrun.game;

import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import io.github.CoderInABarrel.tntrun.game.map.TNTTAGMap;
import io.github.CoderInABarrel.tntrun.game.map.TNTTAGMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class TNTTAGWaiting {
    private final GameSpace gameSpace;
    private final TNTTAGMap map;
    private final TNTTAGConfig config;
    private final TNTTAGSpawnLogic spawnLogic;
    private TNTTAGWaiting(GameSpace gameSpace, TNTTAGMap map, TNTTAGConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new TNTTAGSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<TNTTAGConfig> context) {
        TNTTAGConfig config = context.getConfig();
        TNTTAGMapGenerator generator = new TNTTAGMapGenerator(config.mapConfig);
        TNTTAGMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            TNTTAGWaiting waiting = new TNTTAGWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        TNTTAGActive.open(this.gameSpace, this.map, this.config);
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
