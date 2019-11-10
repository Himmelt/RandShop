package org.soraworld.randshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Himmelt
 */
@SerializableAs("Button")
public class Button implements ConfigurationSerializable {

    private String command = "say ${player} pressed button test!";
    private ItemStack icon = new ItemStack(Material.MAP, 1);

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("command", command);
        if (icon != null) {
            map.put("icon", icon.serialize());
        }
        return map;
    }

    public static Button deserialize(Map<String, Object> args) {
        if (args != null && args.containsKey("command") && args.containsKey("icon")) {
            try {
                Button button = new Button();
                button.command = args.getOrDefault("command", "").toString();
                Object item = args.get("icon");
                if (item instanceof Map) {
                    button.icon = ItemStack.deserialize((Map) item);
                    return button;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void click(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("\\$\\{player}", player.getName()));
    }
}
