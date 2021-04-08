package io.github.CoderInABarrel.tntrun.game;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TeamManager {
    private static List<UUID> alive = new LinkedList<>();
    private static List<UUID> tagger = new LinkedList<>();
    private static List<UUID> runners = new LinkedList<>();

    public boolean isTagger(UUID player) {
        if (tagger.contains(player)) {
            return true;
        }
        return false;
    }

    public boolean isRunner(UUID player) {
        if (runners.contains(player)) {
            return true;
        }
        return false;
    }


    public void addRunner(UUID player) {
        if (!isRunner(player))
            runners.add(player);
    }

    public void removeRunner(UUID player) {
        runners.remove(player);
    }

    public void addTagger(UUID player) {
        if (!isTagger(player))
            tagger.add(player);
    }

    public void removeTagger(UUID player) {
        tagger.remove(player);
    }

    public List<UUID> getTaggers() {
        return tagger;
    }

    public List<UUID> getRunners() {
        return runners;
    }

    public boolean isLiving(UUID player) {
        if (alive.contains(player)) {
            return true;
        }
        return false;
    }


    public void addLiving(UUID player) {
        if (!isLiving(player))
            alive.add(player);
    }

    public void removeLiving(UUID player) {
        alive.remove(player);
    }


    public List<UUID> getLiving() {
        return alive;
    }

    public UUID selectTagger() {
        Random rand = new Random();
        UUID firstTagger = this.getLiving().get(rand.nextInt(this.getLiving().size()));
        return firstTagger;
    }
}
