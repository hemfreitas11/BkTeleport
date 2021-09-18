package me.bkrmt.bkteleport.teleportable;

import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.BkTeleport;
import me.bkrmt.bkteleport.edit.options.DisplayItem;
import me.bkrmt.bkteleport.edit.options.DisplayName;
import me.bkrmt.bkteleport.edit.options.Lore;
import me.bkrmt.teleport.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.List;

public abstract class Teleportable implements PagedItem {
    private final BkTeleport plugin;
    private final String name;
    private long id = -1;
    private int page = -1;
    private int slot = -1;
    private boolean ignoreSlot;
    private boolean ignorePage;
    private final Configuration config;
    private List<String> lore;
    private final String identifier;
    private String displayName;
    private Location location;
    private ItemStack displayItem;
    private final String configKey;

    public Teleportable(String identifier, String key, Material defaultMateiral, String name, Configuration config) {
        this.plugin = BkTeleport.getInstance();
        this.name = name;
        this.identifier = identifier;
        this.configKey = key.isEmpty() ? "" : key + ".";
        this.config = config;
        if (config.get(configKey + "display-item") == null) this.displayItem = new ItemStack(defaultMateiral);
        else {
            try {
                this.displayItem = new ItemStack(Material.valueOf(config.getString(configKey + "display-item")));
            } catch (Exception ignored) {
                plugin.sendConsoleMessage("§7[§4BkTeleport§7] " + plugin.getLangFile().get(null, "error.material-not-found")
                        .replace("{item}", config.getString(configKey + "display-item"))
                        .replace("{file}", config.getFile().getName()));
                this.displayItem = new ItemStack(defaultMateiral);
            }
        }
        this.lore = config.get(configKey + "description") == null ? null : config.getStringList(configKey + "description");
        this.displayName = config.get(configKey + "display-name") == null ? null : config.getString(configKey + "display-name");

        if (config.get(configKey + "page") != null) this.page = config.getInt(configKey + "page");
        if (config.get(configKey + "slot") != null) this.slot = config.getInt(configKey + "slot");

        this.location = getLocation();
    }

    public Location getLocation() {
        if (location == null) {
            if (getConfig().get(configKey + "world") != null &&
                    getConfig().get(configKey + "x") != null &&
                    getConfig().get(configKey + "y") != null &&
                    getConfig().get(configKey + "z") != null) {
                return new Location(Bukkit.getWorld(getConfig().getString(configKey + "world")),
                        getConfig().getDouble(configKey + "x"),
                        getConfig().getDouble(configKey + "y"),
                        getConfig().getDouble(configKey + "z"),
                        getConfig().get(configKey + "yaw") == null ? 0f : (float) getConfig().getDouble(configKey + "yaw"),
                        getConfig().get(configKey + "pitch") == null ? 0f : (float) getConfig().getDouble(configKey + "pitch"));
            } else return null;
        } else return location;
    }

    public void saveValues() {
        if (lore != null) getConfig().set(configKey + "description", lore);
        if (displayName != null) getConfig().set(configKey + "display-name", displayName);
        if (displayItem != null) getConfig().set(configKey + "display-item", displayItem.getType().toString());
        if (location != null) getConfig().setLocation(configKey, location);
        if (!(slot < 0))getConfig().set(configKey + "slot", slot);
        if (!(page < 0))getConfig().set(configKey + "page", page);
    }

    protected void teleport(Player player, String title, String subTitle) {
        new Teleport(getPlugin(), player, getPlugin().getConfigManager().getConfig().getBoolean("teleport-countdown.cancel-on-move"))
                .setLocation(getName(), getLocation())
                .setTitle(title)
                .setSubtitle(subTitle)
                .setDuration(Utils.intFromPermission(player, 5, "bkteleport.countdown", new String[]{"bkteleport.countdown.0", "bkteleport.admin"}))
                .setIsCancellable(true)
                .startTeleport();
    }

    public BkTeleport getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public Configuration getConfig() {
        return config;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setLocation(Location location) {
        if (location != null) {
            this.location = location;
            getConfig().setLocation(configKey, location);
        }
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setIgnorePage(boolean ignorePage) {
        this.ignorePage = ignorePage;
    }

    @Override
    public void setIgnoreSlot(boolean ignoreSlot) {
        this.ignoreSlot = ignoreSlot;
    }

    @Override
    public boolean isIgnorePage() {
        return ignorePage;
    }

    @Override
    public boolean isIgnoreSlot() {
        return ignoreSlot;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return displayName;
    }

    @Override
    public List<String> getLore(PagedList list, Page currentPage) {
        return lore;
    }

    @Override
    public ItemStack getDisplayItem(PagedList list, Page currentPage) {
        return displayItem;
    }

    @Override
    public void assignID(long id) {
        this.id = id;
    }

    @Override
    public long getID() {
        return id;
    }

    protected PagedList buildEditMenu(Player player, String title, Page previousMenu, ElementResponse backResponse, List<PagedItem> extraOptions) {
        ArrayDeque<PagedItem> options = new ArrayDeque<>();
        options.add(new DisplayName(this, player));
        options.add(new Lore(this, player));
        options.add(new me.bkrmt.bkteleport.edit.options.Location(this, player));
        options.add(new DisplayItem(this, player));
        if (extraOptions != null) options.addAll(extraOptions);

        PagedList optionsList = new PagedList(BkTeleport.getInstance(), player, getIdentifier(player), options)
                .setGuiRows(Rows.THREE)
                .setListRows(1)
                .setStartingSlot(11)
                .setListRowSize(5)
                .setGuiTitle(title)
                .buildMenu();
        optionsList.getPages().forEach(page -> page.setButtonSlots(new int[]{9, 17}));
        if (previousMenu != null) {
            ItemBuilder backButton = new ItemBuilder(XMaterial.RED_WOOL)
                    .setName(plugin.getLangFile().get(player, "gui-buttons.previous-menu.name"))
                    .setLore(plugin.getLangFile().getStringList("gui-buttons.previous-menu.description"))
                    .hideTags();
            optionsList.getPages().get(0).addPreviousMenu(previousMenu).setBackMenuButton(9, backButton, getIdentifier(player) + "-back-menu-button", backResponse);
        }

        return optionsList;
    }

    private String getIdentifier(Player player) {
        return player.getName().toLowerCase() + "-paged-" + identifier;
    }
}
