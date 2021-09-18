package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.BkGUI;
import me.bkrmt.bkcore.command.CommandMapper;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.command.MainCommand;
import me.bkrmt.bkcore.config.ConfigType;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.guiconfig.GUIConfig;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkteleport.commands.BackCmd;
import me.bkrmt.bkteleport.commands.CommandHandler;
import me.bkrmt.bkteleport.commands.home.DelHomeCmd;
import me.bkrmt.bkteleport.commands.home.HomeCmd;
import me.bkrmt.bkteleport.commands.home.SetHomeCmd;
import me.bkrmt.bkteleport.commands.spawn.SetSpawnCmd;
import me.bkrmt.bkteleport.commands.spawn.SpawnCmd;
import me.bkrmt.bkteleport.commands.tp.TpHereCmd;
import me.bkrmt.bkteleport.commands.tp.TpaAcceptCmd;
import me.bkrmt.bkteleport.commands.tp.TpaCmd;
import me.bkrmt.bkteleport.commands.tp.TpaDenyCmd;
import me.bkrmt.bkteleport.commands.warp.DelWarpCmd;
import me.bkrmt.bkteleport.commands.warp.SetWarpCmd;
import me.bkrmt.bkteleport.commands.warp.WarpCmd;
import me.bkrmt.teleport.TeleportCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.StringUtil;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public final class BkTeleport extends BkPlugin {
    private static BkTeleport plugin;
    private HomesManager homesManager;
    private Hashtable<String, List<String>> commands;
    private AnimatorManager animatorManager;

    @Override
    public void onEnable() {
        plugin = this;
        start(true);
        setRunning(true);

        sendConsoleMessage("§6__________ __   §e_________     __                               __");
        sendConsoleMessage("§6\\______   \\  | _§e\\__  ___/___ |  |   ____ ______   ____________/  |_");
        sendConsoleMessage("§6 |    |  _/  |/ / §e|  |_/ __ \\|  | _/ __ \\|   _ \\ /  _ \\_  __ \\   __\\");
        sendConsoleMessage("§6 |    |   \\    <  §e|  |\\  ___/|  |_\\  ___/|  |_| >  |_| )  | \\/|  |");
        sendConsoleMessage("§6 |______  /__|_ \\ §e|__| \\___  >____/\\___  >   __/ \\____/|__|   |__|");
        sendConsoleMessage("§6        \\/     \\/§e          \\/          \\/|__|");
        sendConsoleMessage("");
        sendConsoleMessage("                 §6© BkPlugins | discord.io/bkplugins");
        sendConsoleMessage("");
        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.PLUGIN_STARTING.getMessage(this));

        File warpsFolder = getFile("", "warps");
        if (!warpsFolder.exists()) warpsFolder.mkdir();
        File homesFolder = getFile("", "userdata");
        if (!homesFolder.exists()) homesFolder.mkdir();

        if (getConfigManager().getConfig().getBoolean("essentials.import-from-essentials")) {
            copyFromEss("warps");
            copyFromEss("userdata");
        }

        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.LOADING_WARPS.getMessage(this));
        getConfigManager().loadAllFromFolder(warpsFolder, ConfigType.CONFIG);
        int warpsSize = warpsFolder.listFiles() == null ? 0 : warpsFolder.listFiles().length;
        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.LOADED_WARPS.getMessage(this).replace("{0}", String.valueOf(warpsSize)));/*
        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.LOADING_HOMES.getMessage(this));
        getConfigManager().loadAllFromFolder(homesFolder, ConfigType.PLAYER_DATA);
        int homesSize = homesFolder.listFiles() == null ? 0 : homesFolder.listFiles().length;
        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.LOADED_HOMES.getMessage(this).replace("{0}", String.valueOf(homesSize)));*/

        homesManager = new HomesManager();

        BkGUI.INSTANCE.register(this);
        animatorManager = new AnimatorManager(this);
        getCommandMapper()
                .addCommand(new CommandModule(new MainCommand(plugin, "bkteleport.admin", GUIConfig::openMenu), null))
                .addCommand(new CommandModule(new SpawnCmd(plugin, "spawn", "bkteleport.spawn"), null))
                .addCommand(new CommandModule(new SetSpawnCmd(plugin, "setspawn", "bkteleport.setspawn"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new BackCmd(plugin, "back", "bkteleport.back"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new TpaCmd(plugin, "tpa", "bkteleport.tpa"), (sender, b, c, args) -> tpaCompleter(args, sender)))
                .addCommand(new CommandModule(new TpHereCmd(plugin, "tpahere", "bkteleport.tpahere"), (sender, b, c, args) -> tpaCompleter(args, sender)))
                .addCommand(new CommandModule(new TpaAcceptCmd(plugin, "tpaccept", "bkteleport.tpaccept"), (sender, b, c, args) -> tpaCompleter(args, sender)))
                .addCommand(new CommandModule(new TpaDenyCmd(plugin, "tpdeny", "bkteleport.tpdeny"), (sender, b, c, args) -> tpaCompleter(args, sender)))
                .addCommand(new CommandModule(new SetHomeCmd(plugin, "sethome", "bkteleport.sethome"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new DelHomeCmd(plugin, "delhome", "bkteleport.delhome"), (sender, b, c, args) -> homesTabCompleter(args, sender)))
                .addCommand(new CommandModule(new HomeCmd(plugin, "home", "bkteleport.home"), (sender, b, c, args) -> homesTabCompleter(args, sender)))
                .addCommand(new CommandModule(new HomeCmd(plugin, "homes", "bkteleport.home"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new WarpCmd(plugin, "warp", "bkteleport.warp"), (sender, b, c, args) -> warpsTabCompleter("warp", args, sender)))
                .addCommand(new CommandModule(new WarpCmd(plugin, "warps", "bkteleport.warps"), (sender, b, c, args) -> warpsTabCompleter("warps", args, sender)))
                .addCommand(new CommandModule(new SetWarpCmd(plugin, "setwarp", "bkteleport.setwarp"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new DelWarpCmd(plugin, "delwarp", "bkteleport.delwarp"), (sender, b, c, args) -> warpsTabCompleter("warp", args, sender)));
        if (getLangFile().getLanguage().equalsIgnoreCase("pt_BR")) {
            addExtraCommand("home", (sender, b, c, args) -> homesTabCompleter(args, sender));
            addExtraCommand("back", (a, b, c, d) -> Collections.singletonList(""));
            addExtraCommand("tpahere", (sender, b, c, args) -> tpaCompleter(args, sender));
            addExtraCommand("tpaccept", (sender, b, c, args) -> tpaCompleter(args, sender));
            addExtraCommand("tpdeny", (sender, b, c, args) -> tpaCompleter(args, sender));
            addExtraCommand("sethome", (a, b, c, d) -> Collections.singletonList(""));
            addExtraCommand("delhome", (sender, b, c, args) -> homesTabCompleter(args, sender));
        }

        getCommandMapper().registerAll();

        commands = new Hashtable<>();
        addCommand("spawn");
        addCommand("setspawn");
        addCommand("back");
        addCommand("tpa");
        addCommand("tpahere");
        addCommand("tpaccept");
        addCommand("tpdeny");
        addCommand("home");
        addCommand("homes");
        addCommand("sethome");
        addCommand("delhome");
        addCommand("warp");
        addCommand("warps");
        addCommand("setwarp");
        addCommand("delwarp");

        getServer().getPluginManager().registerEvents(new CommandHandler(), this);

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onJoin(PlayerSpawnLocationEvent event) {
                if (getConfigManager().getConfig().getBoolean("spawn.spawn-on-join")) {
                    Player player = event.getPlayer();
                    File spawnFile = plugin.getFile("warps", "spawn.yml");
                    if (!spawnFile.exists()) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(BkTeleport.getInstance(), () -> {
                                    if (player.isOnline()) {
                                        player.sendMessage(plugin.getLangFile().get(player, "error.invalid-spawn"));
                                    }
                                }, 20
                        );
                    } else {
                        Configuration spawnConfig = new Configuration(plugin, spawnFile);
                        Location spawnLocation = spawnConfig.getLocation("");
                        if (spawnLocation != null) {
                            event.setSpawnLocation(spawnLocation);
                        } else {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(BkTeleport.getInstance(), () -> {
                                        if (player.isOnline()) {
                                            player.sendMessage(plugin.getLangFile().get(player, "error.invalid-spawn"));
                                        }
                                    }, 20
                            );
                        }
                    }
                }
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onJoin(PlayerRespawnEvent event) {
                if (getConfigManager().getConfig().getBoolean("spawn.spawn-on-respawn")) {
                    Player player = event.getPlayer();
                    File spawnFile = plugin.getFile("warps", "spawn.yml");
                    if (!spawnFile.exists()) {
                        player.sendMessage(plugin.getLangFile().get(player, "error.invalid-spawn"));
                    } else {
                        Configuration spawnConfig = new Configuration(plugin, spawnFile);
                        Location spawnLocation = spawnConfig.getLocation("");
                        if (spawnLocation != null) {
                            event.setRespawnLocation(spawnLocation);
                        } else {
                            player.sendMessage(plugin.getLangFile().get(player, "error.invalid-spawn"));
                        }
                    }
                }
            }
        }, this);

        if (TeleportCore.INSTANCE.getPlayersInCooldown().get("Core-Started") == null)
            TeleportCore.INSTANCE.start(this);

        if (getConfigManager().getConfig().getBoolean("essentials.save-warps-to-essentials")) copyToEss();

        sendConsoleMessage(me.bkrmt.bkteleport.InternalMessages.PLUGIN_STARTED.getMessage(this));
    }

    private CommandMapper addExtraCommand(String key, TabCompleter completer) {
        return getCommandMapper()
                .addCommand(
                        new CommandModule(
                                new Executor(this, key + ".comando-ingles-nao-mudar", "bkteleport." + key) {
                                    @Override
                                    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                                        ((Player) sender).performCommand("bkteleport:" + getLangFile().get("commands." + key + ".command") + " " + Utils.joinStringArray(args));
                                        return true;
                                    }
                                }, completer
                        )
                );
    }

    private void addCommand(String key) {
        List<String> tempCommands = new ArrayList<>();
        for (String command : getConfigManager().getConfig().getStringList("commands.spawn")) {
            tempCommands.add(command.toLowerCase());
        }
        commands.put(key, tempCommands);
    }

    @Override
    public void onDisable() {
        getConfigManager().saveConfigs();
    }

    private void copyToEss() {
        File essFolder = new File("plugins" + File.separatorChar + "Essentials");
        if (essFolder.exists()) {
            File warpsFolder = getFile("warps", "");
            if (warpsFolder.exists()) {
                File[] files = warpsFolder.listFiles();
                if (files != null && files.length > 0) {
                    for (File warpFile : files) {
                        try {
                            File destFile = new File("plugins" + File.separatorChar + "Essentials" + File.separator + "warps" + File.separator + warpFile.getName());
                            if (!destFile.exists()) {
                                Files.copy(warpFile.toPath(), destFile.toPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void copyFromEss(String essFolderName) {
        File essFolder = new File("plugins" + File.separator + "Essentials" + File.separator + essFolderName);
        if (essFolder.exists()) {
            if (essFolder.listFiles().length > 0) {
                File[] files = essFolder.listFiles();
                boolean warned = false;
                for (File file : files) {
                    try {
                        File destFile = (new File(getDataFolder().getPath() + File.separator + essFolderName + File.separator + file.getName()));
                        if (!destFile.exists()) {
                            if (!warned) {
                                String infoMessage = destFile.getPath().contains("userdata") ? me.bkrmt.bkteleport.InternalMessages.ESS_COPY_HOME.getMessage(this).replace("{0}", getName()) : me.bkrmt.bkteleport.InternalMessages.ESS_COPY_WARPS.getMessage(this).replace("{0}", getName());
                                getServer().getLogger().log(Level.INFO, infoMessage);
                                warned = true;
                            }
                            Files.copy(file.toPath(), destFile.toPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (warned)
                    getServer().getLogger().log(Level.INFO, me.bkrmt.bkteleport.InternalMessages.ESS_COPY_DONE.getMessage(this).replace("{0}", getName()));
            }
        }
    }

    public HomesManager getHomesManager() {
        return homesManager;
    }

    @Override
    public AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    public static BkTeleport getInstance() {
        return plugin;
    }

    public Hashtable<String, List<String>> getCommands() {
        return commands;
    }

    private List<String> warpsTabCompleter(String command, String[] args, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialCommand = args[0];

            List<String> warps = new ArrayList<>();
            if (command.equalsIgnoreCase("warp")) warps.addAll(Arrays.asList(PluginUtils.getWarps()));
            warps.add(plugin.getLangFile().get((OfflinePlayer) sender, "commands.warp.subcommands.edit.command"));
            StringUtil.copyPartialMatches(partialCommand, warps, completions);
        } else if (command.equalsIgnoreCase("warp") && args.length == 2 && (sender.hasPermission("bkteleport.warp.others") || sender.hasPermission("bkteleport.admin"))) {
            String partialPlayer = args[1];
            List<String> playerList = new ArrayList<>();
            for (Player player : plugin.getHandler().getMethodManager().getOnlinePlayers()) {
                playerList.add(player.getName());
            }
            StringUtil.copyPartialMatches(partialPlayer, playerList, completions);
        }
        if (getCommands().get("warp").contains("/" + command)) {
            completions.removeIf(completion -> (!sender.hasPermission("bkteleport.warp.*") && !sender.hasPermission("bkteleport.admin")) && !sender.hasPermission("bkteleport.warp." + completion));
            Collections.sort(completions);
        }
        return completions;

    }

    private List<String> homesTabCompleter(String[] args, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("bkteleport.home") || sender.hasPermission("bkteleport.player")) {
            if (args.length == 1) {
                String partialCommand = args[0];
                List<String> homes = Arrays.asList(PluginUtils.getHomes(((Player) sender)));
                StringUtil.copyPartialMatches(partialCommand, homes, completions);
            }
        }
        Collections.sort(completions);

        return completions;
    }

    private List<String> tpaCompleter(String[] args, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("bkteleport.home") || sender.hasPermission("bkteleport.player")) {
            if (args.length == 1) {
                String partialCommand = args[0];

                List<String> validPlayers = new ArrayList<>();

                for (Player player : getHandler().getMethodManager().getOnlinePlayers()) {
                    if (!player.getName().equalsIgnoreCase(sender.getName())) validPlayers.add(player.getName());
                }
                StringUtil.copyPartialMatches(partialCommand, validPlayers, completions);
            }
        }
        Collections.sort(completions);

        return completions;
    }
}