package io.github.CoderInABarrel.tntrun.game.powerup;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;

public class GenericPowerup {
    public static final Codec<GenericPowerup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1,Integer.MAX_VALUE).fieldOf("duration").forGetter(powerup -> powerup.duration),
            Codec.STRING.fieldOf("effect").forGetter(powerup -> powerup.effect),
            Codec.STRING.fieldOf("id").forGetter(powerup -> powerup.id),
            Codec.STRING.fieldOf("block_id").forGetter(powerup -> powerup.blockId),
            Codec.STRING.fieldOf("type").forGetter(powerup -> powerup.buffType)
    ).apply(instance, GenericPowerup::new));

    private final int duration;
    private final String id;
    private final String effect;
    private final String buffType;
    private String blockId;
    private StatusEffect generatedEffect;
    private Block generatedBlock;
    private PowerupType generatedBuffType;



    public GenericPowerup(int duration, String effect, String id, String block_id, String buffType) {
        this.duration = duration;
        this.effect = effect;
        this.id = id;
        this.blockId = block_id;
        this.buffType = buffType;
    }

    public int getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public String getEffect() {
        return effect;
    }

    public StatusEffect getGeneratedEffect() {
        return generatedEffect;
    }

    public void setGeneratedEffect(StatusEffect generatedEffect) {
        this.generatedEffect = generatedEffect;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }
    public Block getGeneratedBlock() {
        return generatedBlock;
    }

    public void setGeneratedBlock(Block generatedBlock) {
        this.generatedBlock = generatedBlock;
    }

    public PowerupType getGeneratedBuffType() {
        return generatedBuffType;
    }

    public void setGeneratedBuffType(PowerupType generatedBuffType) {
        this.generatedBuffType = generatedBuffType;
    }

    public String getBuffType() {
        return buffType;
    }
}
