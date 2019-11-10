package org.soraworld.randshop;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Himmelt
 */
public final class Eco {

    private static PlayerPointsAPI api;

    private static PlayerPointsAPI getApi() {
        if (api == null) {
            try {
                PlayerPoints plugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
                api = plugin.getAPI();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return api;
    }

    public static boolean hasEco(Player player, int amount) {
        return api != null && api.look(player.getUniqueId()) >= amount;
    }

    public static boolean takeEco(Player player, int amount) {
        return api != null && api.take(player.getUniqueId(), amount);
    }
}
