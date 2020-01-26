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

    private int price = Integer.MAX_VALUE;
    private int amount = 1;
    private ItemStack item = null;

    public Good() {
    }

    public Good(int price, int amount, ItemStack item) {
        price = Math.max(0, price);
        amount = Math.max(0, amount);
        this.price = price;
        this.amount = amount;
        this.item = item.clone();
    }

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("price", price);
        map.put("amount", amount);
        if (item != null) {
            map.put("item", item.serialize());
        }
        return map;
    }

    public static Good deserialize(Map<String, Object> args) {
        if (args != null && args.containsKey("price") && args.containsKey("amount") && args.containsKey("item")) {
            try {
                Good good = new Good();
                good.price = Integer.parseInt(args.get("price").toString());
                good.amount = Integer.parseInt(args.get("amount").toString());
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

    public boolean sell(Player player) {
        if (item != null && Eco.hasEco(player, price) && Eco.takeEco(player, price)) {
            player.getInventory().addItem(item.clone());
            player.sendMessage("You buy this good.");
            return true;
        } else {
            player.sendMessage("You have not enough money.");
            return false;
        }
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public int getAmount() {
        return amount;
    }

    public double getRate(long sum) {
        return amount * 1.0D / sum;
    }
}
