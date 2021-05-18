package io.github.CoderInABarrel.tntrun.game.map;

import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.Plasmid;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;

import java.io.IOException;
import java.util.LinkedList;

public class TNTTAGMapGenerator {

    private final TNTTAGMapConfig config;

    public TNTTAGMapGenerator(TNTTAGMapConfig config) {
        this.config = config;
    }

    public @NotNull TNTTAGMap build() {
        MapTemplate template = null;
        try  {
            template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.id);
        } catch (GameOpenException | IOException err) {
            Plasmid.LOGGER.error(err.getMessage());
        }
        LinkedList<BlockPos> spawns = new LinkedList<BlockPos>();
        TemplateRegion spawnRegion = template.getMetadata().getFirstRegion("Spawn");
        if (spawnRegion == null) {
            Plasmid.LOGGER.error("Spawn region for map " + this.config.id + " is null");
        }
        template.getMetadata().getRegions().forEach( region -> {
            if (region.getMarker().toLowerCase().contains("powerup")) {
                System.out.println(region.getBounds().getCenter());
                spawns.push(new BlockPos(region.getBounds().getCenter()));
            }
        });
        return new TNTTAGMap(template, config, spawns, spawnRegion.getBounds());
    }
}
