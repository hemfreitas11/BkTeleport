package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.command.HelpCmd;
import me.bkrmt.bkcore.command.ReloadCmd;
import me.bkrmt.bkcore.message.InternalMessages;
import me.bkrmt.bkteleport.commands.CommandHandler;
import me.bkrmt.bkteleport.commands.home.DelHomeCmd;
import me.bkrmt.bkteleport.commands.home.HomeCmd;
import me.bkrmt.bkteleport.commands.home.SetHomeCmd;
import me.bkrmt.bkteleport.commands.tp.TpaAcceptCmd;
import me.bkrmt.bkteleport.commands.tp.TpaCmd;
import me.bkrmt.bkteleport.commands.tp.TpaDenyCmd;
import me.bkrmt.bkteleport.commands.tp.TpaHereCmd;
import me.bkrmt.bkteleport.commands.warp.DelWarpCmd;
import me.bkrmt.bkteleport.commands.warp.SetWarpCmd;
import me.bkrmt.bkteleport.commands.warp.WarpCmd;
import me.bkrmt.teleport.TeleportCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public final class BkTeleport extends BkPlugin {
    public static BkPlugin plugin;
    public static Hashtable<String, List<String>> commands;

    @Override
    public void onEnable() {
        plugin = this;
        start(true);
        setRunning(true);
        getCommandMapper()
                .addCommand(new CommandModule(new HelpCmd(plugin, "bkteleport", ""), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new ReloadCmd(plugin, "tpreload", "bkteleport.reload"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new TpaCmd(plugin, "tpa", "bkteleport.tpa"), null))
                .addCommand(new CommandModule(new TpaHereCmd(plugin, "tpahere", "bkteleport.tpahere"), null))
                .addCommand(new CommandModule(new TpaAcceptCmd(plugin, "tpaccept", "bkteleport.tpaccept"), null))
                .addCommand(new CommandModule(new TpaDenyCmd(plugin, "tpdeny", "bkteleport.tpdeny"), null))
                .addCommand(new CommandModule(new SetHomeCmd(plugin, "sethome", "bkteleport.sethome"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new DelHomeCmd(plugin, "delhome", "bkteleport.delhome"), (sender, b, c, args) -> homesTabCompleter(args, sender)))
                .addCommand(new CommandModule(new HomeCmd(plugin, "home", "bkteleport.home"), (sender, b, c, args) -> homesTabCompleter(args, sender)))
                .addCommand(new CommandModule(new HomeCmd(plugin, "homes", "bkteleport.home"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new WarpCmd(plugin, "warp", "bkteleport.warp"), (sender, b, c, args) -> warpsTabCompleter(args, sender)))
                .addCommand(new CommandModule(new WarpCmd(plugin, "warps", "bkteleport.warps"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new SetWarpCmd(plugin, "setwarp", "bkteleport.setwarp"), (a, b, c, d) -> Collections.singletonList("")))
                .addCommand(new CommandModule(new DelWarpCmd(plugin, "delwarp", "bkteleport.delwarp"), (sender, b, c, args) -> warpsTabCompleter(args, sender)))
                .registerAll();

        commands = new Hashtable<>();
        commands.put("tpa", PluginUtils.getList("commands.tpa"));
        commands.put("tpahere", PluginUtils.getList("commands.tpahere"));
        commands.put("tpaccept", PluginUtils.getList("commands.tpaccept"));
        commands.put("tpdeny", PluginUtils.getList("commands.tpdeny"));
        commands.put("home", PluginUtils.getList("commands.home"));
        commands.put("homes", PluginUtils.getList("commands.homes"));
        commands.put("sethome", PluginUtils.getList("commands.sethome"));
        commands.put("delhome", PluginUtils.getList("commands.delhome"));
        commands.put("warp", PluginUtils.getList("commands.warp"));
        commands.put("warps", PluginUtils.getList("commands.warps"));
        commands.put("setwarp", PluginUtils.getList("commands.setwarp"));
        commands.put("delwarp", PluginUtils.getList("commands.delwarp"));

        getServer().getPluginManager().registerEvents(new ButtonFunctions(), this);
        getServer().getPluginManager().registerEvents(new CommandHandler(), this);

        File warpsFolder = new File(getDataFolder().getPath() + File.separator + "warps");
        if (!warpsFolder.exists()) warpsFolder.mkdir();
        File homesFolder = new File(getDataFolder().getPath() + File.separator + "userdata");
        if (!homesFolder.exists()) homesFolder.mkdir();

        new TpaUtils();

        if (TeleportCore.INSTANCE.getPlayersInCooldown().get("Core-Started") == null)
            TeleportCore.INSTANCE.start(this);

        if (getConfigManager().getConfig().getBoolean("import-from-essentials")) {
            copyFromEss("warps");
            copyFromEss("userdata");
        }

    }

    @Override
    public void onDisable() {
        getConfigManager().saveConfigs();
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
                                String infoMessage = destFile.getPath().contains("userdata") ? InternalMessages.ESS_COPY_HOME.getMessage().replace("{0}", getName()) : InternalMessages.ESS_COPY_WARPS.getMessage().replace("{0}", getName());
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
                    getServer().getLogger().log(Level.INFO, InternalMessages.ESS_COPY_DONE.getMessage().replace("{0}", getName()));
            }
        }
    }

    private List<String> warpsTabCompleter(String[] args, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialCommand = args[0];
            List<String> warps = Arrays.asList(PluginUtils.getWarps());
            StringUtil.copyPartialMatches(partialCommand, warps, completions);
        } else if (args.length == 2 && sender.hasPermission("bkteleport.warp.others")) {
            String partialPlayer = args[1];
            List<String> playerList = new ArrayList<>();
            for (Player player : plugin.getHandler().getMethodManager().getOnlinePlayers()) {
                playerList.add(player.getName());
            }
            StringUtil.copyPartialMatches(partialPlayer, playerList, completions);
        }
        completions.removeIf(completion -> !sender.hasPermission("bkteleport.warp.*") && !sender.hasPermission("bkteleport.warp." + completion));
        Collections.sort(completions);
        return completions;

    }

    private List<String> homesTabCompleter(String[] args, CommandSender sender) {
        List<String> completions = new ArrayList<>();
        if (sender.hasPermission("bkteleport.home")) {
            if (args.length == 1) {
                String partialCommand = args[0];
                List<String> homes = Arrays.asList(PluginUtils.getHomes(((Player) sender)));
                StringUtil.copyPartialMatches(partialCommand, homes, completions);
            }
        }
        Collections.sort(completions);

        return completions;
    }
}