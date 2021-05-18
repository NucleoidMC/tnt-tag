package io.github.CoderInABarrel.tntrun.game.powerup;

import com.sun.tools.javac.jvm.Gen;
import jdk.internal.module.SystemModuleFinders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import xyz.nucleoid.plasmid.Plasmid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public class PowerupManager {
    private HashMap<UUID, GenericPowerup> powerUpEntities = new HashMap<>();
    private LinkedList<BlockPos> spawnPositions = new LinkedList<BlockPos>();
    private MinecraftServer server;
    private ServerWorld world;
    private int initTick;
    private int targetTick;

    public PowerupManager(MinecraftServer server, ServerWorld world, LinkedList<BlockPos> spawnPositions) {
        this.server = server;
        this.world = world;
        this.initTick = server.getTicks();
        this.spawnPositions = spawnPositions;
        this.targetTick = server.getTicks() + (20 * 20);
        respawn();
    }

    public void tick() {
        int curTick = server.getTicks();
        System.out.println(targetTick - curTick);

        for (Iterator<UUID> iterator = powerUpEntities.keySet().iterator(); iterator.hasNext();) {
            UUID id = iterator.next();
            ArmorStandEntity ent = (ArmorStandEntity) world.getEntity(id);
            // ent could be null do to them killing themselves on touched by player
            if (ent == null) {
                iterator.remove();
            }else {
                EulerAngle curAngle = ent.getHeadRotation();
                ent.setHeadRotation(new EulerAngle(0, 10 + curAngle.getYaw(), 0));
            }
        }
        if (curTick == targetTick) {
            respawn();
            targetTick = curTick + (20 * 20);
        }else {
            // TODO: 5/17/2021 do stuff on non spawn tick
        }
    }

    // respawn all the powerups
    public void respawn() {
        // clear the remaining ones
        for (Iterator<UUID> iterator = powerUpEntities.keySet().iterator(); iterator.hasNext();) {
            UUID id = iterator.next();
            world.getEntity(id).remove();
            iterator.remove();
        }
        // respawn them
        for (BlockPos pos : spawnPositions) {
            GenericPowerup powerup = PowerupRegistry.getRandom();

            PowerupEntity ent = new PowerupEntity(EntityType.ARMOR_STAND, world, powerup);
            ent.requestTeleport(pos.getX(), pos.getY() - 1, pos.getZ());
            world.spawnEntity(ent);
            System.out.println(ent.getPos());
            ent.equipStack(EquipmentSlot.HEAD, powerup.getGeneratedBlock().asItem().getDefaultStack());
            System.out.println(powerup.getGeneratedBlock().asItem().getDefaultStack());
            ent.setInvisible(true);
            if (powerup.getGeneratedBuffType() == PowerupType.BUFF) {
                ent.setCustomName(new LiteralText(powerup.getId()).formatted(Formatting.GREEN));
            }else {
                ent.setCustomName(new LiteralText(powerup.getId()).formatted(Formatting.RED));
            }

            ent.setCustomNameVisible(true);
            ent.setNoGravity(true);


            powerUpEntities.put(ent.getUuid(), powerup);
        }
    }
}
