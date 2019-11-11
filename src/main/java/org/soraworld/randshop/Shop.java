package org.soraworld.randshop;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Himmelt
 */
@SerializableAs("Shop")
public class Shop implements ConfigurationSerializable {

    private int lastUpdate = 0;
    private ArrayList<String> goods = new ArrayList<>();

    @Override
    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("lastUpdate", lastUpdate);
        map.put("goods", goods);
        return map;
    }

    public static Shop deserialize(Map<String, Object> args) {
        if (args != null && args.containsKey("lastUpdate") && args.containsKey("goods")) {
            try {
                Shop shop = new Shop();
                shop.lastUpdate = Integer.parseInt(args.get("lastUpdate").toString());
                Object goods = args.get("goods");
                if (goods instanceof List) {
                    shop.goods.addAll((List) goods);
                    return shop;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getGood(int slot) {
        if (slot >= 0 && slot < goods.size()) {
            return goods.get(slot);
        }
        return "";
    }

    public int getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(int lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setGoods(List<String> goods) {
        this.goods.clear();
        this.goods.addAll(goods);
    }

    public ArrayList<String> getGoods() {
        return goods;
    }
}
