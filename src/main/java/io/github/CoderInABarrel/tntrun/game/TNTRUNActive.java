package io.github.CoderInABarrel.tntrun.game;

import io.github.CoderInABarrel.tntrun.game.map.TNTRUNMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
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

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TNTRUNActive {
    public final GameSpace gameSpace;
    private final TNTRUNConfig config;
    private final TNTRUNMap gameMap;

    // TODO replace with ServerPlayerEntity if players are removed upon leaving
    private final Object2ObjectMap<PlayerRef, TNTRUNPlayer> participants;
    private final TNTRUNSpawnLogic spawnLogic;
    private final TNTRUNStageManager stageManager;
    private final boolean ignoreWinState;
    private long nextTime = 0;
    //private final TNTRUNTimerBar timerBar;

    private TNTRUNActive(GameSpace gameSpace, TNTRUNMap map, GlobalWidgets widgets, TNTRUNConfig config, Set<PlayerRef> participants) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.spawnLogic = new TNTRUNSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (PlayerRef player : participants) {
            this.participants.put(player, new TNTRUNPlayer());
        }

        this.stageManager = new TNTRUNStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.nextTime = gameSpace.getWorld().getTime() + (20 * 60);
        //this.timerBar = new TNTRUNTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, TNTRUNMap map, TNTRUNConfig config) {
        gameSpace.openGame(game -> {
            Set<PlayerRef> participants = gameSpace.getPlayers().stream()
                    .map(PlayerRef::of)
                    .collect(Collectors.toSet());

            for (PlayerRef ref : participants) {
                System.out.println(ref.getEntity(gameSpace.getServer()).getUuid());
                TeamManager.addLiving(ref.getEntity(gameSpace.getServer()).getUuid());
                TeamManager.addRunner(ref.getEntity(gameSpace.getServer()).getUuid());
                ref.getEntity(gameSpace.getServer()).addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,1, true, false, true));
            }
            System.out.println(TeamManager.getLiving().size());
            GlobalWidgets widgets = new GlobalWidgets(game);
            TNTRUNActive active = new TNTRUNActive(gameSpace, map, widgets, config, participants);

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
        UUID firstTagger = TeamManager.selectTagger();
        TeamManager.removeRunner(firstTagger);
        TeamManager.addTagger(firstTagger);
        PlayerEntity taggerEntity = gameSpace.getServer().getPlayerManager().getPlayer(firstTagger);
        taggerEntity.sendMessage(new LiteralText("You are tagger").formatted(Formatting.RED), false);
        taggerEntity.inventory.armor.set(3, Items.TNT.getDefaultStack());
        taggerEntity.inventory.insertStack(Items.TNT.getDefaultStack());
        taggerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,2, true, false, true));

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
        this.participants.remove(PlayerRef.of(player));
        TeamManager.removeRunner(player.getUuid());
        TeamManager.removeTagger(player.getUuid());
        TeamManager.removeLiving(player.getUuid());
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        // TODO handle damage
        if (source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) source.getAttacker();
            player.heal(amount);
            if (TeamManager.isTagger(attacker.getUuid()) && TeamManager.isRunner(player.getUuid())) {
                TeamManager.removeRunner(player.getUuid());
                TeamManager.removeTagger(attacker.getUuid());
                TeamManager.addRunner(attacker.getUuid());
                TeamManager.addTagger(player.getUuid());
                player.sendMessage(new LiteralText("You are now tagger").formatted(Formatting.RED), false);
                player.inventory.armor.set(3, Items.TNT.getDefaultStack());
                attacker.inventory.armor.set(3, Items.AIR.getDefaultStack());
                attacker.removeStatusEffect(StatusEffects.SPEED);
                attacker.inventory.clear();
                attacker.setStackInHand(player.getActiveHand(),Items.AIR.getDefaultStack());
                player.setStackInHand(player.getActiveHand(),Items.TNT.getDefaultStack());
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,1, true, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,2, true, false, true));
            }
        } else {
            player.heal(amount);
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

        for (UUID player : TeamManager.getLiving()) {
            ServerPlayerEntity playerEnt = gameSpace.getServer().getPlayerManager().getPlayer(player);
            if (playerEnt != null) {
                playerEnt.setExperienceLevel(((int) (nextTime - time) / 20));
            }

        }

        if (time == nextTime && TeamManager.getLiving().size() > 1) {
            for (UUID player : TeamManager.getTaggers()) {
                ServerPlayerEntity tagger = gameSpace.getServer().getPlayerManager().getPlayer(player);
                for (PlayerRef ref : this.participants.keySet()) {
                    if (TeamManager.getRunners().contains(ref.getEntity(world).getUuid()) && ref != null) {
                        ref.getEntity(world).sendMessage(new LiteralText(tagger.getName().asString() + " blew up").formatted(Formatting.YELLOW), false);
                    }
                }
                world.createExplosion(null, tagger.getPos().getX(), tagger.getPos().getY(), tagger.getPos().getZ(), 0.0f, Explosion.DestructionType.NONE);
                tagger.sendMessage(new LiteralText("You lost").formatted(Formatting.YELLOW), false);
                this.spawnSpectator(tagger);
                TeamManager.removeTagger(tagger.getUuid());
                TeamManager.removeLiving(tagger.getUuid());
                if (TeamManager.getLiving().size() <= 1) {
                    for (UUID user : TeamManager.getLiving()) {
                        this.broadcastWin(WinResult.win(this.gameSpace.getServer().getPlayerManager().getPlayer(user)));
                        this.gameSpace.close(GameCloseReason.FINISHED);
                    }
                } else {
                    UUID firstTagger = TeamManager.selectTagger();
                    TeamManager.removeRunner(firstTagger);
                    TeamManager.addTagger(firstTagger);
                    ServerPlayerEntity taggerEnt = gameSpace.getServer().getPlayerManager().getPlayer(firstTagger);
                    taggerEnt.inventory.armor.set(3, Items.TNT.getDefaultStack());
                    taggerEnt.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,99999,2, true, false, true));
                    taggerEnt.inventory.insertStack(Items.TNT.getDefaultStack());
                }
            }
            nextTime = time + (20 * 30);
        } else if (TeamManager.getLiving().size() <= 1) {
            for (UUID user : TeamManager.getLiving()) {
                this.broadcastWin(WinResult.win(this.gameSpace.getServer().getPlayerManager().getPlayer(user)));
                this.gameSpace.close(GameCloseReason.FINISHED);
            }
        }

        TNTRUNStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

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
