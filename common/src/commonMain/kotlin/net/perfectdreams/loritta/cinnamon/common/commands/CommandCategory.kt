package net.perfectdreams.loritta.cinnamon.common.commands

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.ListI18nKey
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

enum class CommandCategory(val localizedName: StringI18nData, val localizedDescription: ListI18nKey) {
    FUN(I18nKeysData.Commands.Category.Fun.Name, ListI18nKey("TODO_FIX_THIS")),
    IMAGES(I18nKeysData.Commands.Category.Images.Name, ListI18nKey("TODO_FIX_THIS")),
    MINECRAFT(I18nKeysData.Commands.Category.Minecraft.Name, ListI18nKey("TODO_FIX_THIS")),
    POKEMON(I18nKeysData.Commands.Category.Pokemon.Name, ListI18nKey("TODO_FIX_THIS")),
    UNDERTALE(I18nKeysData.Commands.Category.Undertale.Name, ListI18nKey("TODO_FIX_THIS")),
    ROBLOX(I18nKeysData.Commands.Category.Roblox.Name, ListI18nKey("TODO_FIX_THIS")),
    ANIME(I18nKeysData.Commands.Category.Anime.Name, ListI18nKey("TODO_FIX_THIS")),
    DISCORD(I18nKeysData.Commands.Category.Discord.Name, ListI18nKey("TODO_FIX_THIS")),
    MISC(I18nKeysData.Commands.Category.Misc.Name, ListI18nKey("TODO_FIX_THIS")),
    MODERATION(I18nKeysData.Commands.Category.Moderation.Name, ListI18nKey("TODO_FIX_THIS")),
    UTILS(I18nKeysData.Commands.Category.Utils.Name, ListI18nKey("TODO_FIX_THIS")),
    SOCIAL(I18nKeysData.Commands.Category.Social.Name, ListI18nKey("TODO_FIX_THIS")),
    ACTION(I18nKeysData.Commands.Category.Action.Name, ListI18nKey("TODO_FIX_THIS")),
    ECONOMY(I18nKeysData.Commands.Category.Economy.Name, ListI18nKey("TODO_FIX_THIS")),
    VIDEOS(I18nKeysData.Commands.Category.Videos.Name, ListI18nKey("TODO_FIX_THIS")),
    FORTNITE(I18nKeysData.Commands.Category.Fortnite.Name, ListI18nKey("TODO_FIX_THIS")),
    MAGIC(I18nKeysData.Commands.Category.Magic.Name, ListI18nKey("TODO_FIX_THIS")); // Esta categoria é usada para comandos APENAS para o dono do bot

    fun getLocalizedName(i18nContext: I18nContext): String {
        return i18nContext.get(localizedName)
    }

    /* fun getLocalizedDescription(i18nContext: I18nContext): List<String> {
        return i18nContext.get(localizedDescription)
    } */
}