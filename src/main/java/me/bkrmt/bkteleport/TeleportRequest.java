package me.bkrmt.bkteleport;

import me.bkrmt.bkcore.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.bkrmt.bkteleport.BkTeleport.plugin;

public class TeleportRequest {
    Player senderPlayer;
    Player targetPlayer;
    TextComponent tpaMessage;
    RequestType type;

    public TeleportRequest(Player senderPlayer, Player targetPlayer, RequestType type) {
        this.senderPlayer = senderPlayer;
        this.targetPlayer = targetPlayer;
        this.type = type;
        tpaMessage = new TextComponent("");
        buildMessage();
    }

    public void sendMessage() {
        targetPlayer.spigot().sendMessage(tpaMessage);
    }

    private void buildMessage() {
        String sectionName = type.equals(RequestType.Tpa) ? "info.tpa-message" : "info.tpahere-message";
        String[] configSection = Utils.objectToString(plugin.getLangFile().getConfig().getConfigurationSection(sectionName).getKeys(false).toArray());
        TextComponent buttonAccept = null;
        TextComponent buttonDeny = null;

        for (int x = 0; x < configSection.length; x++) {
            String tpaReply = "/";
            String hover;
            if (configSection[x].equals("accept-button")) {
                buttonAccept = new TextComponent(plugin.getLangFile().get(sectionName + "." + configSection[x]));
                tpaReply += "tpaccept" + " " + senderPlayer.getName();
                hover = plugin.getLangFile().get(sectionName + ".accept-hover");
                buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpaReply));
                buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
            } else if (configSection[x].equals("deny-button")) {
                buttonDeny = new TextComponent(plugin.getLangFile().get(sectionName + "." + configSection[x]));
                tpaReply += "tpdeny" + " " + senderPlayer.getName();
                hover = plugin.getLangFile().get(sectionName + ".deny-hover");
                buttonDeny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpaReply));
                buttonDeny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
            } else if (!configSection[x].equals("accept-hover") && !configSection[x].equals("deny-hover")) {
                switch (configSection[x]) {
                    case "message1":
                        tpaMessage.addExtra(plugin.getLangFile().get(sectionName + "." + configSection[x]).replace("{player}", senderPlayer.getName()));
                        tpaMessage.addExtra("\n");
                        break;
                    case "message2":
                        String[] temp1 = plugin.getLangFile().get(sectionName + "." + configSection[x]).split(" ");
                        StringBuilder temp2 = new StringBuilder();
                        for (int c = 0; c < temp1.length; c++) {
                            if (temp1[c].equals("{accept-button}")) {
                                tpaMessage.addExtra(temp2.toString());
                                tpaMessage.addExtra(buttonAccept);
                                tpaMessage.addExtra(" ");
                                temp2 = new StringBuilder();
                            } else if (temp1[c].equals("{deny-button}")) {
                                tpaMessage.addExtra(temp2.toString());
                                tpaMessage.addExtra(buttonDeny);
                                tpaMessage.addExtra(" ");
                                temp2 = new StringBuilder();
                            } else {
                                temp2.append(ChatColor.translateAlternateColorCodes('&', temp1[c] + " "));
                            }

                            if (c == temp1.length - 1) {
                                tpaMessage.addExtra(temp2.toString());
                                tpaMessage.addExtra(" ");
                            }
                        }
                        tpaMessage.addExtra("\n");
                        break;
                    case "message3":
                        tpaMessage.addExtra(plugin.getLangFile().get(sectionName + "." + configSection[x]).replace("{seconds}",
                                String.valueOf(plugin.getConfigManager().getConfig().getInt("tpa-expiration"))));
                        if (x != configSection.length - 1) tpaMessage.addExtra("\n");
                        break;
                    default:
                        String message = plugin.getLangFile().get(sectionName + "." + configSection[x]);
                        if (!message.isEmpty()) {
                            tpaMessage.addExtra(message);
                            if (x != configSection.length - 1) tpaMessage.addExtra("\n");
                        }
                        break;
                }
            }
        }
    }
}
