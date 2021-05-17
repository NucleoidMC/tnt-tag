package io.github.CoderInABarrel.tntrun.game.powerup;

import com.sun.tools.javac.jvm.Gen;
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
import java.util.LinkedList;
import java.util.UUID;

public class PowerupManager {
    private HashMap<UUID, GenericPowerup> powerUpEntities = new HashMap<>();
    private LinkedList<BlockPos> spawnPositions = new LinkedList<BlockPos>();
    private MinecraftServer server;
    private ServerWorld world;
    private int initTick;
    private int targetTick = initTick + (10 * 20);

    public PowerupManager(MinecraftServer server, ServerWorld world, LinkedList<BlockPos> spawnPositions) {
        this.server = server;
        world.getEntitiesByType(EntityType.ARMOR_STAND, Entity::isOnGround).forEach(entity -> entity.kill());
        this.world = world;
        this.initTick = server.getTicks();
        this.spawnPositions = spawnPositions;
        respawn();
    }

    public void tick() {
        int curTick = server.getTicks();
        for (UUID id : powerUpEntities.keySet()) {
            ArmorStandEntity ent = (ArmorStandEntity) world.getEntity(id);
            // check if ent is null, then remove anyway
            if (ent == null) {
                powerUpEntities.remove(id);
                continue;
            }

            EulerAngle curAngle = ent.getHeadRotation();
            ent.setHeadRotation(new EulerAngle(0, 10 + curAngle.getYaw(), 0));
        }
        if (curTick == targetTick) {
            respawn();
            targetTick = curTick + (10 * 20);
        }else {
            // TODO: 5/17/2021 do stuff on non spawn tick
        }
    }

    // respawn all the powerups
    public void respawn() {
        // clear the remaining ones
        for (UUID powerUpEntity : powerUpEntities.keySet()) {
            try {
                world.getEntity(powerUpEntity).kill();
                powerUpEntities.remove(powerUpEntity);
            } catch (Exception err) {
                Plasmid.LOGGER.warn("Error clearing spawned powerup entities, may be first spawn, or is null");
            }

        }
        // respawn them
        for (BlockPos pos : spawnPositions) {
            GenericPowerup powerup = PowerupRegistry.getRandom();

            PowerupEntity ent = new PowerupEntity(EntityType.ARMOR_STAND, world, powerup);
            ent.requestTeleport(pos.getX(), pos.getY() - 1, pos.getZ());
            world.spawnEntity(ent);
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
