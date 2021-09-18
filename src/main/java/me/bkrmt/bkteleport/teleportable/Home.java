package me.bkrmt.bkteleport.teleportable;

import me.bkrmt.bkcore.PagedItem;
import me.bkrmt.bkcore.PagedList;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.event.ElementResponse;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkteleport.HomeType;
import me.bkrmt.bkteleport.PluginUtils;
import me.bkrmt.bkteleport.edit.options.home.DeleteHome;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Home extends Teleportable {
    private final String ownerName;
    private boolean isPublic;

    public Home(String homeName, Configuration config) {
        super("homes", "homes." + homeName, XMaterial.OAK_SIGN.parseMaterial(), homeName, config);

        this.ownerName = config.get("player") == null ? findOwnerName() : config.getString("player").equalsIgnoreCase("error") ? findOwnerName() : config.getString("player");
        isPublic = config.getBoolean(getConfigKey() + "is-public");
    }

    public void teleportToHome(Player player) {
        String title = getPlugin().getLangFile().get(player, "info.warped.home.title");
        String subTitle = getPlugin().getLangFile().get(player, "info.warped.home.subtitle");

        if (title == null) title = "§cError!";
        if (subTitle == null) subTitle = "§cError!";

        super.teleport(
                player,
                Utils.translateColor(title.replace("{home-name}", Utils.capitalize(getName().replace("_", " "))).replace("{owner}", ownerName).replace("{player}", player.getName())),
                Utils.translateColor(subTitle).replace("{home-name}", Utils.capitalize(getName().replace("_", " "))).replace("{owner}", ownerName).replace("{player}", player.getName())
        );
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public PagedList buildEditMenu(Player player, Page previousMenu) {
        List<PagedItem> extraOptions = new ArrayList<>();
//        extraOptions.add(new Visibility(this));
        extraOptions.add(new DeleteHome(this, player));
        return super.buildEditMenu(
                player,
                getPlugin().getLangFile().get(null, "homes-menu.edit-menu.title").replace("{home-name}", getName()),
                previousMenu,
                event -> {
                    PluginUtils.sendHomes(HomeType.Home, getConfig().getFile(), player, true);
                },
                extraOptions
        );
    }

    public void deleteHome() {
        getConfig().set("homes." + getName(), null);
        getConfig().saveToFile();
    }

    @Override
    public void saveValues() {
        super.saveValues();
        getConfig().set(getConfigKey() + "is-public", isPublic);
        getConfig().saveToFile();
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String getDisplayName(PagedList list, Page currentPage) {
        return getDisplayName() == null ? getPlugin().getLangFile().get(null, "homes-menu.default-home-name").replace("{home-name}", getName().replace("_", " ")) : getDisplayName().replace("_", " ");
    }

    @Override
    public ElementResponse getElementResponse(PagedList list, Page currentPage) {
        return event -> {
            PagedOptions options = ((PagedOptions) list.getCustomOptions());
            if (options.isEditMode()) {
                currentPage.setSwitchingPages(true);
                buildEditMenu((Player) event.getWhoClicked(), currentPage).openPage(0);
            } else {
                event.getWhoClicked().closeInventory();
                String command = getPlugin().getLangFile().get((OfflinePlayer) event.getWhoClicked(), "commands.home.command") + " ";
                if (options.isSpy()) command += getOwnerName() + ":" + getName().toLowerCase();
                else command += getName().toLowerCase();
                ((Player) event.getWhoClicked()).performCommand(command);
            }
        };
    }

    private String findOwnerName() {
        String returnValue = "error";
        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(getConfig().getFile().getName().replace(".yml", "")));
        if (owner != null) {
            try {
                UUID.fromString(owner.getName());
            } catch (IllegalArgumentException ignored) {
                returnValue = owner.getName();
                if (getConfig().get("player") == null) {
                    getConfig().set("player", returnValue);
                    getConfig().saveToFile();
                }
            }
        }
        return returnValue;
    }

}
