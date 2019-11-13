package org.soraworld.randshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Himmelt
 */
@SerializableAs("Button")
public class Button implements ConfigurationSerializable {

    private List<String> commands = new ArrayList<>();
    private ItemStack icon = new ItemStack(Material.MAP, 1);

    public Button() {
    }

    public Button(ItemStack icon, String command) {
        this.icon = icon.clone();
        this.commands.add(command);
    }

    public Button(ItemStack stack) {
        this.icon = stack;
    }

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("commands", commands);
        if (icon != null) {
            map.put("icon", icon.serialize());
        }
        return map;
    }

    public static Button deserialize(Map<String, Object> args) {
        if (args != null && args.containsKey("commands") && args.containsKey("icon")) {
            try {
                Button button = new Button();
                button.commands = (List<String>) args.getOrDefault("commands", new ArrayList<String>());
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
        for (String command : commands) {
            if (command.startsWith("server|")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.trim().substring(7).trim()
                        .replaceAll("\\$\\{player}", player.getName()));
            } else {
                player.performCommand(command.trim().replaceAll("\\$\\{player}", player.getName()));
            }
        }
    }

    public ItemStack getIcon() {
        return icon.clone();
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon.clone();
    }
}
