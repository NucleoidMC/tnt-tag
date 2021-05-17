package io.github.CoderInABarrel.tntrun.game;

import io.github.CoderInABarrel.tntrun.TNTTAG;
import io.github.CoderInABarrel.tntrun.game.map.TNTTAGMap;
import io.github.CoderInABarrel.tntrun.game.powerup.PowerupManager;
import io.github.CoderInABarrel.tntrun.game.powerup.PowerupRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TNTTAGActive {
    public final GameSpace gameSpace;
    private final TNTTAGConfig config;
    private final TNTTAGMap gameMap;

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final Object2ObjectMap<PlayerRef, TNTTAGPlayer> participants;
    private final TNTTAGSpawnLogic spawnLogic;
    private final TNTTAGStageManager stageManager;
    private final boolean ignoreWinState;
    private final TeamManager teammanager;
    private long nextTime = 0;
    private PowerupManager powerupManager;
    //private final TNTRUNTimerBar timerBar;

    private TNTTAGActive(GameSpace gameSpace, TNTTAGMap map, GlobalWidgets widgets, TNTTAGConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.spawnLogic = new TNTTAGSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.teammanager = new TeamManager();
        LinkedList test = new LinkedList<BlockPos>();
        test.push(map.spawn);
        this.powerupManager = new PowerupManager(gameSpace.getServer(), gameSpace.getWorld(),test);
        for (PlayerRef player : participants) {
            this.participants.put(player, new TNTTAGPlayer());
        }

        for (PlayerRef ref : participants) {
            System.out.println(ref.getEntity(gameSpace.getServer()).getUuid());
            teammanager.addLiving(ref.getEntity(gameSpace.getServer()).getUuid());
            teammanager.addRunner(ref.getEntity(gameSpace.getServer()).getUuid());
            ref.getEntity(gameSpace.getServer()).addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,0, true, false, true));
        }
        System.out.println(teammanager.getLiving().size());
        UUID firstTagger = teammanager.selectTagger();
        teammanager.removeRunner(firstTagger);
        teammanager.addTagger(firstTagger);
        PlayerEntity taggerEntity = gameSpace.getServer().getPlayerManager().getPlayer(firstTagger);
        taggerEntity.sendMessage(new LiteralText("You are tagger").formatted(Formatting.RED), false);
        taggerEntity.inventory.armor.set(3, Items.TNT.getDefaultStack());
        taggerEntity.inventory.insertStack(Items.TNT.getDefaultStack());
        taggerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,1, true, false, true));

        this.stageManager = new TNTTAGStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.nextTime = gameSpace.getWorld().getTime() + (20 * 60);
        //this.timerBar = new TNTRUNTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, TNTTAGMap map, TNTTAGConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());


            GlobalWidgets widgets = new GlobalWidgets(game);
            TNTTAGActive active = new TNTTAGActive(gameSpace, map, widgets, config, participants);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        });

        
    }

    private void onOpen() {
        ServerWorld world = this.gameSpace.getWorld();
        for (PlayerRef ref : this.participants.keySet()) {
            ref.ifOnline(world, this::spawnParticipant);

        }
        this.stageManager.onOpen(world.getTime(), this.config);
        // TODO setup logic
    }

    private void onClose() {
        // TODO teardown logic
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player))) {
            this.spawnSpectator(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        spawnSpectator(player);
        teammanager.removeRunner(player.getUuid());
        teammanager.removeTagger(player.getUuid());
        teammanager.removeLiving(player.getUuid());
    }

    private void setTagger(UUID player) {
        ServerPlayerEntity playerEnt = gameSpace.getServer().getPlayerManager().getPlayer(player);
        playerEnt.sendMessage(new LiteralText("You are now tagger").formatted(Formatting.RED), false);
        playerEnt.inventory.armor.set(3, Items.TNT.getDefaultStack());
        playerEnt.setStackInHand(playerEnt.getActiveHand(),Items.TNT.getDefaultStack());
        playerEnt.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,1, true, false, true));
        teammanager.removeRunner(player);
        teammanager.addTagger(player);
    }

    private void setRunner(UUID player) {
        ServerPlayerEntity playerEnt = gameSpace.getServer().getPlayerManager().getPlayer(player);
        playerEnt.inventory.armor.set(3, Items.AIR.getDefaultStack());
        playerEnt.removeStatusEffect(StatusEffects.SPEED);
        playerEnt.inventory.clear();
        playerEnt.setStackInHand(playerEnt.getActiveHand(),Items.AIR.getDefaultStack());
        playerEnt.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,0, true, false, true));
        teammanager.removeTagger(player);
        teammanager.addRunner(player);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        // TODO handle damage
        if (source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) source.getAttacker();
            player.heal(amount);
            if (teammanager.isTagger(attacker.getUuid()) && teammanager.isRunner(player.getUuid())) {
                setTagger(player.getUuid());
                setRunner(attacker.getUuid());
            }
        } else if (!(source.isOutOfWorld())) {
            player.heal(amount);
        } else if (source.isOutOfWorld()) {
            removePlayer(player);
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        // TODO handle death
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();
        powerupManager.tick();

        for (UUID player : teammanager.getLiving()) {
            ServerPlayerEntity playerEnt = gameSpace.getServer().getPlayerManager().getPlayer(player);
            if (playerEnt != null) {
                playerEnt.setExperienceLevel(((int) (nextTime - time) / 20));
            }
        }

        if (time == nextTime && teammanager.getLiving().size() > 1) {
            for (UUID player : teammanager.getTaggers()) {
                ServerPlayerEntity tagger = gameSpace.getServer().getPlayerManager().getPlayer(player);
                for (PlayerRef ref : this.participants.keySet()) {
                    if (teammanager.getRunners().contains(ref.getEntity(world).getUuid()) && ref != null) {
                        ref.getEntity(world).sendMessage(new LiteralText(tagger.getName().asString() + " blew up").formatted(Formatting.YELLOW), false);
                    }
                }
                world.createExplosion(null, tagger.getPos().getX(), tagger.getPos().getY(), tagger.getPos().getZ(), 0.0f, Explosion.DestructionType.NONE);
                tagger.sendMessage(new LiteralText("You lost").formatted(Formatting.YELLOW), false);
                removePlayer(tagger);
                if (teammanager.getLiving().size() <= 1) {
                    for (UUID user : teammanager.getLiving()) {
                        this.broadcastWin(WinResult.win(this.gameSpace.getServer().getPlayerManager().getPlayer(user)));
                        this.gameSpace.close(GameCloseReason.FINISHED);
                    }
                } else {
                    UUID firstTagger = teammanager.selectTagger();
                    setTagger(firstTagger);
                }
            }
            nextTime = time + (20 * 30);
        } else if (teammanager.getLiving().size() <= 1) {
            for (UUID user : teammanager.getLiving()) {
                this.broadcastWin(WinResult.win(this.gameSpace.getServer().getPlayerManager().getPlayer(user)));
                this.gameSpace.close(GameCloseReason.FINISHED);
            }
        }

        TNTTAGStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close();
                return;
        }

        //this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20);

        // TODO tick logic
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerWorld world = this.gameSpace.getWorld();
        ServerPlayerEntity winningPlayer = null;

        // TODO win result logic
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
