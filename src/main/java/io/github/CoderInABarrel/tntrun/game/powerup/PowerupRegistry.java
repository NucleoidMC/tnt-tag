package io.github.CoderInABarrel.tntrun.game.powerup;

import io.github.CoderInABarrel.tntrun.TNTTAG;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.Random;

public class PowerupRegistry {
    public static TinyRegistry<GenericPowerup> REGISTRY = TinyRegistry.newStable();


    public static void register (Identifier id, GenericPowerup powerup) {
        try {
            Identifier targetEffectIdentifier = new Identifier("minecraft", powerup.getEffect());
            StatusEffect targetEffect = Registry.STATUS_EFFECT.get(targetEffectIdentifier);

            Identifier targetBlockIdentifier = new Identifier("minecraft", powerup.getBlockId());
            Block targetBlock = Registry.BLOCK.get(targetBlockIdentifier);

            if (targetEffect == null) {
                Plasmid.LOGGER.error(powerup.getEffect() + " Is null");
                return;
            }

            if (targetBlock == null) {
                Plasmid.LOGGER.error(powerup.getBlockId() + " Is null");
                return;
            }

            if (powerup.getBuffType().equalsIgnoreCase("buff")) {
                powerup.setGeneratedBuffType(PowerupType.BUFF);
            }else if (powerup.getBuffType().equalsIgnoreCase("debuff")) {
                powerup.setGeneratedBuffType(PowerupType.DEBUFF);
            }else {
                Plasmid.LOGGER.error(powerup.getBuffType() + " Is malformed");
                return;
            }

            powerup.setGeneratedBlock(targetBlock);
            powerup.setGeneratedEffect(targetEffect);
            REGISTRY.register(id, powerup);

        } catch (Exception err) {
            Plasmid.LOGGER.error(err.getMessage());
        }
    }

    public static GenericPowerup get(String id) {
        if (REGISTRY.get(new Identifier(TNTTAG.ID, id)) == null) {
            Plasmid.LOGGER.error(new Identifier(TNTTAG.ID, id) + "Is null");
            return null;
        }
        return REGISTRY.get(new Identifier(TNTTAG.ID, id));
    }

    public static GenericPowerup getRandom() {
        return REGISTRY.values().stream()
                .skip((int) (REGISTRY.values().size() * Math.random()))
                .findFirst().get();
    }
}
