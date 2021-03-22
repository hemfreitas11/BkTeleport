package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkteleport.RequestType;
import me.bkrmt.bkteleport.TpaUtils;
import me.bkrmt.bkteleport.events.PlayerBkTeleportReplyEvent;
import me.bkrmt.teleport.TeleportType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TpaDenyCmd extends Executor {

    public TpaDenyCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
            if (args.length == 0) {
                for (Player inviter : getPlugin().getHandler().getMethodManager().getOnlinePlayers()) {
                    if (TpaUtils.playerExpiredChecker.containsKey(inviter.getName().toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + TeleportType.Tpa.toString()) ||
                            TpaUtils.playerExpiredChecker.containsKey(inviter.getName().toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + TeleportType.TpaHere.toString())) {
                        args = new String[]{inviter.getName()};
                    }
                }
            }
            if (args.length == 0) {
                sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                return true;
            }
            Player inviter = getPlugin().getServer().getPlayer(args[0]);
            String name = inviter == null ? args[0] : inviter.getName();

            PlayerBkTeleportReplyEvent replyEvent = new PlayerBkTeleportReplyEvent((Player) sender, inviter);
            getPlugin().getServer().getPluginManager().callEvent(replyEvent);

            RequestType type = TpaUtils.isExpiring(name.toLowerCase(), sender.getName().toLowerCase());

            if (!replyEvent.isCancelled()) {
                if (type != null) {
                    if (inviter != null) {
                        inviter.playSound(inviter.getLocation(), getPlugin().getHandler().getSoundManager().getPling(), 15, 0.5f);
                        inviter.sendMessage(getPlugin().getLangFile().get("error.invite-denied").replace("{player}", sender.getName()));
                        sender.sendMessage(getPlugin().getLangFile().get("info.invite-denied").replace("{player}", inviter.getName()));
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.not-online").replace("{player}", name));
                    }

                    TpaUtils.playerExpiredChecker.get(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString()).cancel();
                    TpaUtils.playerExpiredChecker.remove(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString());
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                }
            } else {
                if (type != null) {
                    TpaUtils.playerExpiredChecker.get(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString()).cancel();
                    TpaUtils.playerExpiredChecker.remove(name.toLowerCase() + "-" + sender.getName().toLowerCase() + "-" + type.toString());
                } else {
                    sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                }
            }
        }
        return true;
    }
}
