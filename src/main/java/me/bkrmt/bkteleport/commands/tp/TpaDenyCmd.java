package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.bkcore.xlibs.XSound;
import me.bkrmt.bkteleport.api.events.PlayerBkTeleportReplyEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TpaDenyCmd extends Executor {

    public TpaDenyCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender) && !sender.hasPermission("bkteleport.player")) {
            sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-permission"));
        } else {
            Player requestTarget = (Player) sender;
            if (args.length == 0) {
                for (Player inviter : getPlugin().getHandler().getMethodManager().getOnlinePlayers()) {
                    ClickableRequest tpHereRequest = ClickableRequest.getInteraction(TpHereCmd.TPHERE_IDENTIFIER, inviter.getUniqueId());
                    ClickableRequest tpaRequest = ClickableRequest.getInteraction(TpaCmd.TPA_IDENTIFIER, inviter.getUniqueId());

                    if ((tpHereRequest != null && tpHereRequest.getTarget().getUniqueId().equals(requestTarget.getUniqueId())) ||
                            (tpaRequest != null && tpaRequest.getTarget().getUniqueId().equals(requestTarget.getUniqueId()))) {
                        args = new String[]{inviter.getName()};
                        break;
                    }
                }
            }

            if (args.length == 0) {
                sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-pending-invite"));
                return true;
            }

            Player requestSender = Utils.getPlayer(args[0]);

            if (requestSender == null) {
                requestTarget.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.not-online").replace("{player}", args[0]));
            } else {
                PlayerBkTeleportReplyEvent replyEvent = new PlayerBkTeleportReplyEvent((Player) sender, requestSender);
                getPlugin().getServer().getPluginManager().callEvent(replyEvent);

                if (!replyEvent.isCancelled()) {
                    ClickableRequest tpHereRequest = ClickableRequest.getInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestTarget.getUniqueId());
                    ClickableRequest tpaRequest = ClickableRequest.getInteraction(TpaCmd.TPA_IDENTIFIER, requestTarget.getUniqueId());

                    if ((tpHereRequest != null && tpHereRequest.getSender().getUniqueId().equals(requestSender.getUniqueId())) ||
                            (tpaRequest != null && tpaRequest.getSender().getUniqueId().equals(requestSender.getUniqueId()))) {
                        if (tpHereRequest == null) {
                            ClickableRequest.removeInteraction(TpaCmd.TPA_IDENTIFIER, requestTarget.getUniqueId());
                        } else {
                            ClickableRequest.removeInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestTarget.getUniqueId());
                        }

                        XSound.BLOCK_NOTE_BLOCK_PLING.play(requestSender, 15, 0.5f);
                        requestSender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.invite-denied").replace("{player}", requestTarget.getName()));
                        requestTarget.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "info.invite-denied").replace("{player}", requestSender.getName()));
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get((OfflinePlayer) sender, "error.no-pending-invite"));
                    }
                }
            }
        }
        return true;
    }
}