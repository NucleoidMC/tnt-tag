package io.github.CoderInABarrel.tntrun.game;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TeamManager {
    private static List<UUID> alive = new LinkedList<>();
    private static List<UUID> tagger = new LinkedList<>();
    private static List<UUID> runners = new LinkedList<>();

    public static boolean isTagger(UUID player) {
        if (tagger.contains(player)) {
            return true;
        }
        return false;
    }

    public static boolean isRunner(UUID player) {
        if (runners.contains(player)) {
            return true;
        }
        return false;
    }


    public static void addRunner(UUID player) {
        if (!isRunner(player))
            runners.add(player);
    }

    public static void removeRunner(UUID player) {
        runners.remove(player);
    }

    public static void addTagger(UUID player) {
        if (!isTagger(player))
            tagger.add(player);
    }

    public static void removeTagger(UUID player) {
        tagger.remove(player);
    }

    public static List<UUID> getTaggers() {
        return tagger;
    }

    public static List<UUID> getRunners() {
        return runners;
    }

    public static boolean isLiving(UUID player) {
        if (alive.contains(player)) {
            return true;
        }
        return false;
    }


    public static void addLiving(UUID player) {
        if (!isLiving(player))
            alive.add(player);
    }

    public static void removeLiving(UUID player) {
        alive.remove(player);
    }


    public static List<UUID> getLiving() {
        return alive;
    }

    public static UUID selectTagger() {
        Random rand = new Random();
        UUID firstTagger = TeamManager.getLiving().get(rand.nextInt(TeamManager.getLiving().size()));
        return firstTagger;
    }
}
