package org.soraworld.randshop;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Himmelt
 */
@SerializableAs("Good")
public class Good implements ConfigurationSerializable {

    private int price = 0;
    private float rate = 0F;
    private ItemStack item = null;

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("price", price);
        map.put("rate", rate);
        if (item != null) {
            map.put("item", item.serialize());
        }
        return map;
    }

    public static Good deserialize(Map<String, Object> args) {
        if (args != null && args.containsKey("price") && args.containsKey("rate") && args.containsKey("item")) {
            try {
                Good good = new Good();
                good.price = Integer.parseInt(args.get("price").toString());
                good.rate = Float.parseFloat(args.get("rate").toString());
                Object item = args.get("item");
                if (item instanceof Map) {
                    good.item = ItemStack.deserialize((Map) item);
                    return good;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void buy(Player player) {
        if (item != null && Eco.hasEco(player, price) && Eco.takeEco(player, price)) {
            player.getInventory().addItem(item.clone());
        }
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
