package me.bkrmt.bkteleport;


import me.bkrmt.bkcore.BkPlugin;

public enum InternalMessages {
    LOADING_HOMES("§7[§6BkTeleport§7] §6Loading homes...",
    "§7[§6BkTeleporte§7] §6Carregando casas..."),
    LOADED_HOMES("§7[§6BkTeleport§7] §6Loaded §e{0} §6home files.",
    "§7[§6BkTeleporte§7] §6Foram carregados §e{0} §6arquivos de casas."),
    LOADING_WARPS("§7[§6BkTeleport§7] §6Loading warps...",
    "§7[§6BkTeleporte§7] §6Carregando warps..."),
    LOADED_WARPS("§7[§6BkTeleport§7] §6Loaded §e{0} §6warps.",
    "§7[§6BkTeleporte§7] §6Foram carregadas §e{0} §6warps."),
    OWN_PLUGINS_FOUND("§7[§6BkTeleport§7] §6One or more of my plugins found, enabling support...",
    "§7[§6BkTeleporte§7] §6Um ou mais dos meus plugins encontrados, habilitando suporte..."),
    PLUGIN_STARTING("§7[§6BkTeleport§7] §6Plugin starting...",
    "§7[§6BkTeleporte§7] §6Plugin iniciando..."),
    PLUGIN_STARTED("§7[§6BkTeleport§7] §6Plugin started!",
    "§7[§6BkTeleporte§7] §6Plugin iniciado!"),
    INVALID_HOME("§7[§4BkTeleport§7] §cThe home {0} is invalid and could not be loaded!",
    "§7[§4BkTeleporte§7] §cA home {0} esta invalida e nao pode ser carregada!"),
    ESS_COPY_HOME("§7[§6BkTeleport§7] §6New homes in Essentials detected, importing...",
        "§7[§6BkTeleporte§7] §6Novas casas detectadas no Essentials, importando..."),
    ESS_COPY_WARPS("§7[§6BkTeleport§7] §6New warps in Essentials detected, importing...",
        "§7[§6BkTeleporte§7] §6Novas warps detectadas no essentials, importando..."),
    ESS_COPY_DONE("§7[§6BkTeleport§7] §6Finished importing.", "§7[§6BkTeleporte§7] §6Importacao concluida."),
    PLACEHOLDER_FOUND("§7[§6BkTeleport§7] §6PlaceholderAPI found, enabling support...",
    "§7[§6BkTeleporte§7] §6PlaceholderAPI encontrado, habilitando suporte...");

    private final String[] message;

    InternalMessages(String enMessage, String brMessage) {
        message = new String[2];
        this.message[0] = enMessage;
        this.message[1] = brMessage;
    }

    public String getMessage(BkPlugin plugin) {
        if (plugin.getLangFile().getLanguage().equalsIgnoreCase("pt_br"))  return message[1];
        else return message[0];
    }

}
