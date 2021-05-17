package io.github.CoderInABarrel.tntrun.game.powerup;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;

public class PowerupEntity extends ArmorStandEntity {
    private GenericPowerup powerup;


    public PowerupEntity(EntityType<? extends ArmorStandEntity> entityType, World world, GenericPowerup powerup) {
        super(entityType, world);
        this.powerup = powerup;
    }

    public PowerupEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public GenericPowerup getPowerup() {
        return powerup;
    }

    public void setPowerup(GenericPowerup powerup) {
        this.powerup = powerup;
    }


    @Override
    public void onPlayerCollision(PlayerEntity player) {

        if (!player.world.isClient) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) world.getPlayerByUuid(player.getUuid());
            serverPlayer.sendMessage(new LiteralText("You picked up a " + powerup.getId()), false);
            StatusEffectInstance instance = new StatusEffectInstance(powerup.getGeneratedEffect(), powerup.getDuration() * 20, 1, true, true, true);
            serverPlayer.addStatusEffect(instance);
            this.kill();
            this.remove();
        }
    }
}
