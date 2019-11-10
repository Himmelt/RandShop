package org.soraworld.randshop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Himmelt
 */
public final class RandShop extends JavaPlugin implements Listener {

    private int shopSize = 1;
    private String shopTitle = "${player}'s Rand Shop";
    private final HashMap<Integer, Button> buttons = new HashMap<>();
    private final HashMap<UUID, Shop> shops = new HashMap<>();
    private final HashMap<String, Good> goods = new HashMap<>();
    private final HashMap<UUID, WeakReference<Inventory>> inventories = new HashMap<>();

    private File goodsFile, shopsFile;
    private final YamlConfiguration goodsYaml = new YamlConfiguration();
    private final YamlConfiguration shopsYaml = new YamlConfiguration();

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(Shop.class, "Shop");
        ConfigurationSerialization.registerClass(Good.class, "Good");
        ConfigurationSerialization.registerClass(Button.class, "Button");
        goodsFile = new File(getDataFolder(), "goods.yml");
        shopsFile = new File(getDataFolder(), "shops.yml");
    }

    @Override
    public void onDisable() {
        save();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
    }

    private void reload() {
        reloadConfig();
        shopSize = getConfig().getInt("shopSize", 1);
        shopSize = shopSize < 1 ? 1 : Math.min(shopSize, 5);
        shopTitle = getConfig().getString("shopTitle", "${player}'s Rand Shop");
        buttons.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("buttons");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot = Integer.parseInt(key);
                try {
                    Button button = (Button) section.get(key);
                    if (slot >= 1 && slot <= 9 && button != null) {
                        buttons.put(slot, button);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        /* Goods */
        try {
            goodsYaml.load(goodsFile);
            goods.clear();
            for (String key : goodsYaml.getKeys(false)) {
                try {
                    Good good = (Good) goodsYaml.get(key);
                    if (good != null) {
                        goods.put(key, good);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        /* Shops */
        try {
            shopsYaml.load(shopsFile);
            shops.clear();
            for (String key : shopsYaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    Shop good = (Shop) shopsYaml.get(key);
                    if (good != null) {
                        shops.put(uuid, good);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void save() {
        saveConfig();
        try {
            goods.forEach(goodsYaml::set);
            goodsYaml.save(goodsFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            shops.forEach(((uuid, shop) -> shopsYaml.set(uuid.toString(), shop)));
            shopsYaml.save(shopsFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        if (!goodsFile.exists()) {
            saveResource("goods.yml", false);
        }
        if (!shopsFile.exists()) {
            try {
                shopsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillShop(final Player player, final Inventory inv) {
        Shop shop = shops.computeIfAbsent(player.getUniqueId(), uuid -> new Shop());
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (today != shop.getLastUpdate()) {
            float sum = 0F;
            for (Good good : goods.values()) {
                sum += good.getRate();
            }
            float avgRate = sum / goods.size();

            Random random = new Random();
            ArrayList<String> names = new ArrayList<>(goods.keySet());
            ArrayList<String> list = new ArrayList<>();
            while (list.size() < shopSize * 9) {
                int num = random.nextInt(names.size());
                String name = names.get(num);
                Good good = goods.get(name);
                if (good != null) {
                    float f = random.nextFloat();
                    if (f < good.getRate() / avgRate) {
                        list.add(names.remove(num));
                    }
                }
            }

            shop.setLastUpdate(today);
        }
        inv.clear();
        List<String> list = shop.getGoods();
        for (int i = 0; i < list.size() && i < shopSize * 9; i++) {
            Good good = goods.get(list.get(i));
            if (good != null) {
                inv.setItem(i + 1, good.getItem());
            }
        }
        for (int i = 0; i < 9; i++) {
            Button button = buttons.get(i);
            if (button != null) {
                inv.setItem(shopSize * 9 + i + 1, button.getIcon());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ("openshop".equalsIgnoreCase(label) || "oshop".equalsIgnoreCase(label)) {
                Inventory inv = Bukkit.createInventory(player, shopSize * 9 + 9, shopTitle.replaceAll("\\$\\{player}", player.getName()));
                fillShop(player, inv);
                inventories.put(player.getUniqueId(), new WeakReference<>(inv));
                player.openInventory(inv);
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (inventories.get(player.getUniqueId()) == event.getClickedInventory()) {
            event.setCancelled(true);
            if (event.getClick() == ClickType.LEFT) {
                int slot = event.getSlot();
                if (slot >= 1 && slot <= shopSize * 9) {
                    Shop shop = shops.get(player.getUniqueId());
                    if (shop != null) {
                        Good good = goods.get(shop.getGood(slot));
                        if (good != null) {
                            good.buy(player);
                        }
                    }
                } else {
                    int btn = slot - shopSize * 9;
                    if (btn >= 1 && btn <= 9) {
                        Button button = buttons.get(btn);
                        if (button != null) {
                            button.click(player);
                        }
                    }
                }
            }
        }
    }
}
