package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class CommandCategory(val localizedName: StringI18nData, val localizedDescription: ListI18nData) {
    FUN(I18nKeysData.Commands.Category.Fun.Name, I18nKeysData.Commands.Category.Fun.Description),
    IMAGES(I18nKeysData.Commands.Category.Images.Name, I18nKeysData.Commands.Category.Images.Description),
    MINECRAFT(I18nKeysData.Commands.Category.Minecraft.Name, I18nKeysData.Commands.Category.Minecraft.Description),
    POKEMON(I18nKeysData.Commands.Category.Pokemon.Name, I18nKeysData.Commands.Category.Pokemon.Description),
    UNDERTALE(I18nKeysData.Commands.Category.Undertale.Name, I18nKeysData.Commands.Category.Undertale.Description),
    ROBLOX(I18nKeysData.Commands.Category.Roblox.Name, I18nKeysData.Commands.Category.Roblox.Description),
    ANIME(I18nKeysData.Commands.Category.Anime.Name, I18nKeysData.Commands.Category.Anime.Description),
    DISCORD(I18nKeysData.Commands.Category.Discord.Name, I18nKeysData.Commands.Category.Discord.Description),
    MISC(I18nKeysData.Commands.Category.Misc.Name, I18nKeysData.Commands.Category.Misc.Description),
    MODERATION(I18nKeysData.Commands.Category.Moderation.Name, I18nKeysData.Commands.Category.Moderation.Description),
    UTILS(I18nKeysData.Commands.Category.Utils.Name, I18nKeysData.Commands.Category.Utils.Description),
    SOCIAL(I18nKeysData.Commands.Category.Social.Name, I18nKeysData.Commands.Category.Social.Description),
    ROLEPLAY(I18nKeysData.Commands.Category.Roleplay.Name, I18nKeysData.Commands.Category.Roleplay.Description),
    ECONOMY(I18nKeysData.Commands.Category.Economy.Name, I18nKeysData.Commands.Category.Economy.Description),
    VIDEOS(I18nKeysData.Commands.Category.Videos.Name, I18nKeysData.Commands.Category.Videos.Description),
    FORTNITE(I18nKeysData.Commands.Category.Fortnite.Name, I18nKeysData.Commands.Category.Fortnite.Description),
    MAGIC(I18nKeysData.Commands.Category.Magic.Name, I18nKeysData.Commands.Category.Misc.Description); // Esta categoria é usada para comandos APENAS para o dono do bot

    fun getLocalizedName(i18nContext: I18nContext): String {
        return i18nContext.get(localizedName)
    }

    fun getLocalizedDescription(i18nContext: I18nContext): List<String> {
        return i18nContext.get(localizedDescription)
    }
}