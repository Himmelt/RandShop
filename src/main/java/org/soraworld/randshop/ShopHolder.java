package org.soraworld.randshop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * @author Himmelt
 */
public class ShopHolder implements InventoryHolder {

    private final UUID uuid;

    public ShopHolder(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
