package me.bkrmt.bkteleport.commands.tp;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.request.ClickableRequest;
import me.bkrmt.bkteleport.events.PlayerBkTeleportReplyEvent;
import me.bkrmt.teleport.Teleport;
import me.bkrmt.teleport.TeleportCore;
import me.bkrmt.teleport.TeleportType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaAcceptCmd extends Executor {
    public TpaAcceptCmd(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player requestTarget = (Player) sender;
        if (!hasPermission(sender)) {
            sender.sendMessage(getPlugin().getLangFile().get("error.no-permission"));
        } else {
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
                sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                return true;
            }

            Player requestSender = Utils.getPlayer(args[0]);

            if (requestSender == null) {
                requestTarget.sendMessage(getPlugin().getLangFile().get("error.not-online").replace("{player}", args[0]));
            } else {
                PlayerBkTeleportReplyEvent replyEvent = new PlayerBkTeleportReplyEvent((Player) sender, requestSender);
                getPlugin().getServer().getPluginManager().callEvent(replyEvent);

                if (!replyEvent.isCancelled()) {
                    ClickableRequest tpHereRequest = ClickableRequest.getInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestTarget.getUniqueId());
                    ClickableRequest tpaRequest = ClickableRequest.getInteraction(TpaCmd.TPA_IDENTIFIER, requestTarget.getUniqueId());

                    if ((tpHereRequest != null && tpHereRequest.getSender().getUniqueId().equals(requestSender.getUniqueId())) ||
                            (tpaRequest != null && tpaRequest.getSender().getUniqueId().equals(requestSender.getUniqueId()))) {
                        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(requestSender.getName()) == null) {
                            if (TeleportCore.INSTANCE.getPlayersInCooldown().get(requestTarget.getName()) == null) {
                                requestSender.sendMessage(getPlugin().getLangFile().get("info.invite-accepted.self").replace("{player}", requestTarget.getName()));
                                requestTarget.sendMessage(getPlugin().getLangFile().get("info.invite-accepted.others").replace("{player}", requestSender.getName()));

                                if (tpHereRequest == null) {
                                    ClickableRequest.removeInteraction(TpaCmd.TPA_IDENTIFIER, requestTarget.getUniqueId());
                                    new Teleport(getPlugin(), requestTarget, requestSender.getName(), TeleportType.Tpa);
                                } else {
                                    ClickableRequest.removeInteraction(TpHereCmd.TPHERE_IDENTIFIER, requestTarget.getUniqueId());
                                    new Teleport(getPlugin(), requestSender, requestTarget.getName(), TeleportType.Tpa);
                                }
                            } else {
                                requestSender.sendMessage(getPlugin().getLangFile().get("error.already-waiting"));
                                cancelRequest(tpHereRequest, requestTarget);
                            }
                        } else {
                            requestSender.sendMessage(getPlugin().getLangFile().get("error.other-already-waiting").replace("{player}", requestSender.getName()));
                            cancelRequest(tpHereRequest, requestTarget);
                        }
                    } else {
                        sender.sendMessage(getPlugin().getLangFile().get("error.no-pending-invite"));
                    }
                }
            }
        }
        return true;
    }

    private void cancelRequest(ClickableRequest tpHere, Player player) {
        if (tpHere == null) {
            ClickableRequest.removeInteraction(TpaCmd.TPA_IDENTIFIER, player.getUniqueId());
        } else {
            ClickableRequest.removeInteraction(TpHereCmd.TPHERE_IDENTIFIER, player.getUniqueId());
        }
    }
}