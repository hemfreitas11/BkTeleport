package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkteleport.teleportable.Home;

import java.util.Enumeration;
import java.util.UUID;

public class HomesManager {
    public Home getHome(String playerName, String homeName) {
        Configuration homeConfig = null;

        Enumeration<String> configPaths = BkTeleport.getInstance().getConfigManager().getConfigs().keys();
        while (configPaths.hasMoreElements()) {
            String configPath = configPaths.nextElement();
            if (configPath.contains("userdata")) {
                Configuration tempConfig = BkTeleport.getInstance().getConfigManager().getConfigs().get(configPath);
                if (tempConfig != null) {
                    if (tempConfig.get("player") != null) {
                        if (tempConfig.getString("player").equalsIgnoreCase(playerName)) {
                            homeConfig = tempConfig;
                            break;
                        }
                    }
                }
            }
        }

        return homeConfig == null ? null : new Home(homeName, homeConfig);
    }

    public Home getHome(UUID uuid, String homeName) {
        Configuration homeConfig = null;
        if (uuid == null) {
            Enumeration<String> configPaths = BkTeleport.getInstance().getConfigManager().getConfigs().keys();
            while (configPaths.hasMoreElements()) {
                String configPath = configPaths.nextElement();
                if (configPath.contains("userdata")) {
                    Configuration tempConfig = BkTeleport.getInstance().getConfigManager().getConfigs().get(configPath);
                    if (tempConfig != null) {
                        if (tempConfig.get("homes") != null) {
                            for (String home : tempConfig.getConfigurationSection("homes").getKeys(false)) {
                                if (home.equalsIgnoreCase(homeName)) {
                                    homeConfig = tempConfig;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            homeConfig = BkTeleport.getInstance().getConfigManager().getConfig("userdata", uuid + ".yml");
        }
        return homeConfig == null ? null : new Home(homeName, homeConfig);
    }
}
