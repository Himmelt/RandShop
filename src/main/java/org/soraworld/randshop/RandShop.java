package org.soraworld.randshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR;
import static org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;

/**
 * @author Himmelt
 */
public final class RandShop extends JavaPlugin implements Listener {

    private int shopSize = 3;
    private int refreshPrice = 100;
    private String shopTitle = "${player}'s Rand Shop";
    private long sumAmount = 0;
    private final HashMap<Integer, Button> buttons = new HashMap<>();
    private final HashMap<UUID, Shop> shops = new HashMap<>();
    private final HashMap<String, Good> goods = new HashMap<>();

    private File goodsFile, shopsFile;
    private final YamlConfiguration goodsYaml = new YamlConfiguration();
    private final YamlConfiguration shopsYaml = new YamlConfiguration();

    public static final String EMPTY_GOOD = "EMPTY_GOOD";

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(Shop.class, "Shop");
        ConfigurationSerialization.registerClass(Good.class, "Good");
        ConfigurationSerialization.registerClass(Button.class, "Button");
        goodsFile = new File(getDataFolder(), "goods.yml");
        shopsFile = new File(getDataFolder(), "shops.yml");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void reload() {
        reloadConfig();
        shopSize = getConfig().getInt("shopSize", 1);
        shopSize = shopSize < 1 ? 1 : Math.min(shopSize, 5);
        refreshPrice = getConfig().getInt("refreshPrice", 100);
        refreshPrice = Math.max(refreshPrice, 0);
        shopTitle = getConfig().getString("shopTitle", "${player}'s Rand Shop");
        buttons.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("buttons");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot = Integer.parseInt(key);
                try {
                    Button button = (Button) section.get(key);
                    if (slot >= 0 && slot <= 8 && button != null) {
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
                if (!EMPTY_GOOD.equals(key)) {
                    try {
                        Good good = (Good) goodsYaml.get(key);
                        if (good != null) {
                            goods.put(key, good);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        calcSumAmount();
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
        saveGoods();
        saveShops();
    }

    private void saveGoods() {
        try {
            goods.forEach(goodsYaml::set);
            goodsYaml.save(goodsFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void saveShops() {
        try {
            shops.forEach(((uuid, shop) -> shopsYaml.set(uuid.toString(), shop)));
            shopsYaml.save(shopsFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        getConfig().set("shopSize", shopSize);
        getConfig().set("refreshPrice", refreshPrice);
        getConfig().set("shopTitle", shopTitle);
        super.saveConfig();
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

    private void calcSumAmount() {
        sumAmount = 0;
        for (Good good : goods.values()) {
            sumAmount += good.getAmount();
        }
    }

    private void fillShop(final Player player, final Inventory inv) {
        inv.clear();
        List<String> list = shops.computeIfAbsent(player.getUniqueId(), uuid -> new Shop()).getGoods();
        for (int i = 0; i < list.size() && i < shopSize * 9; i++) {
            Good good = goods.get(list.get(i));
            if (good != null) {
                inv.setItem(i, good.getItem());
            } else {
                inv.setItem(i, null);
            }
        }
        for (int i = 0; i <= 8; i++) {
            Button button = buttons.get(i);
            if (button != null) {
                inv.setItem(shopSize * 9 + i, button.getIcon());
            }
        }
    }

    private void randShop(final Player player, boolean force) {
        Shop shop = shops.computeIfAbsent(player.getUniqueId(), uuid -> new Shop());
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (today != shop.getLastUpdate() || force) {
            Random random = new Random();
            final ArrayList<String> names = new ArrayList<>(goods.keySet());
            final ArrayList<String> list = new ArrayList<>();
            for (int times = 0; times < shopSize * 90 && list.size() < shopSize * 9; times++) {
                int num = random.nextInt(names.size());
                String name = names.get(num);
                Good good = goods.get(name);
                if (good != null) {
                    double rate = random.nextDouble();
                    if (rate <= good.getRate(sumAmount)) {
                        list.add(name);
                    }
                }
            }
            shop.setGoods(list);
            shop.setLastUpdate(today);
            saveShops();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (label) {
            case "rs":
            case "shop":
            case "rshop":
            case "randshop": {
                if (args.length == 0 && sender instanceof Player) {
                    Player player = (Player) sender;
                    Inventory inv = Bukkit.createInventory(new ShopHolder(player.getUniqueId()), shopSize * 9 + 9, shopTitle.replaceAll("\\$\\{player}", player.getName()));
                    randShop(player, false);
                    fillShop(player, inv);
                    player.openInventory(inv);
                } else if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
                    if (sender.hasPermission("randshop.admin")) {
                        reload();
                        sender.sendMessage("Config reloaded.");
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }
                } else if (args.length == 1 && "save".equalsIgnoreCase(args[0])) {
                    if (sender.hasPermission("randshop.admin")) {
                        save();
                        sender.sendMessage("Config saved.");
                    } else {
                        sender.sendMessage(command.getPermissionMessage());
                    }
                } else {
                    sender.sendMessage("Only in-game players can run this command without args.");
                }
                break;
            }
            case "addgood":
            case "agood": {
                if (sender.hasPermission("randshop.admin")) {
                    if (sender instanceof Player) {
                        if (args.length == 3) {
                            ItemStack stack = ((Player) sender).getItemInHand();
                            if (stack != null && stack.getType() != Material.AIR) {
                                if (!EMPTY_GOOD.equals(args[0])) {
                                    try {
                                        Good good = new Good(Integer.parseInt(args[1]), Integer.parseInt(args[2]), stack);
                                        goods.put(args[0], good);
                                        sender.sendMessage("Add good: " + args[0]);
                                        calcSumAmount();
                                        saveGoods();
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    sender.sendMessage("You can't set the reserved word [" + EMPTY_GOOD + "] as name for a good.");
                                }
                            } else {
                                sender.sendMessage("You must hold an item in hand.");
                            }
                        } else {
                            sender.sendMessage(command.getUsage());
                        }
                    } else {
                        sender.sendMessage("Only in-game players can run this command.");
                    }
                } else {
                    sender.sendMessage(command.getPermissionMessage());
                }
                break;
            }
            case "setbutton":
            case "sbutton": {
                if (sender.hasPermission("randshop.admin")) {
                    if (sender instanceof Player) {
                        if (args.length == 1) {
                            ItemStack stack = ((Player) sender).getItemInHand();
                            try {
                                int index = Integer.parseInt(args[0]);
                                if (index >= 0 && index <= 8) {
                                    Button button = buttons.computeIfAbsent(index, i -> new Button(stack));
                                    button.setIcon(stack);
                                    sender.sendMessage("Set button at " + index);
                                    saveConfig();
                                } else {
                                    sender.sendMessage("Button index must be in [ 0 , 8 ] slot");
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        } else {
                            sender.sendMessage(command.getUsage());
                        }
                    } else {
                        sender.sendMessage("Only in-game players can run this command.");
                    }
                } else {
                    sender.sendMessage(command.getPermissionMessage());
                }
                break;
            }
            case "rerand": {
                if (args.length == 0 && sender instanceof Player) {
                    Player player = (Player) sender;
                    if (Eco.hasEco(player, refreshPrice) && Eco.takeEco(player, refreshPrice)) {
                        randShop(player, true);
                        InventoryView view = player.getOpenInventory();
                        if (view != null) {
                            Inventory top = view.getTopInventory();
                            if (top != null && top.getHolder() instanceof ShopHolder) {
                                fillShop(player, top);
                            }
                        }
                    } else {
                        player.sendMessage("You have not enough money.");
                    }
                } else if (args.length == 1 && sender.hasPermission("randshop.admin")) {
                    Player player = Bukkit.getPlayerExact(args[0]);
                    if (player != null) {
                        randShop(player, true);
                        InventoryView view = player.getOpenInventory();
                        if (view != null) {
                            Inventory top = view.getTopInventory();
                            if (top != null && top.getHolder() instanceof ShopHolder) {
                                fillShop(player, top);
                            }
                        }
                    } else {
                        sender.sendMessage("The player doesn't exist.");
                    }
                } else {
                    sender.sendMessage(command.getPermissionMessage());
                }
                break;
            }
            default:
                sender.sendMessage(command.getUsage());
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        Inventory inv = event.getInventory();
        if (inv != null && inv.getHolder() instanceof ShopHolder) {
            InventoryAction action = event.getAction();
            if (action == COLLECT_TO_CURSOR || action == MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
        }
        Inventory click = event.getClickedInventory();
        if (click != null) {
            InventoryHolder holder = click.getHolder();
            if (holder instanceof ShopHolder) {
                event.setCancelled(true);
                if (uuid.equals(((ShopHolder) holder).getUuid()) && event.getClick() == ClickType.LEFT) {
                    int slot = event.getSlot();
                    if (slot >= 0 && slot < shopSize * 9) {
                        Shop shop = shops.get(uuid);
                        if (shop != null) {
                            Good good = goods.get(shop.getGood(slot));
                            if (good != null && good.sell(player)) {
                                click.setItem(slot, null);
                                shop.setGood(slot, EMPTY_GOOD);
                                saveShops();
                            }
                        }
                    } else {
                        int btn = slot - shopSize * 9;
                        if (btn >= 0 && btn <= 8) {
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

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShopHolder) {
            Set<Integer> rawSlots = event.getRawSlots();
            int min = Collections.min(rawSlots);
            if (min < shopSize * 9 + 9) {
                event.setCancelled(true);
            }
        }
    }
}
