package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.textbox.characters

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emote
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations.UndertaleCommand

enum class UniverseType(val universeName: StringI18nData, val emote: net.perfectdreams.loritta.cinnamon.emotes.Emote?) {
    NONE(UndertaleCommand.I18N_TEXTBOX_PREFIX.None.Name, null),
    UNDERTALE(UndertaleCommand.I18N_TEXTBOX_PREFIX.Undertale.Name, net.perfectdreams.loritta.cinnamon.emotes.Emotes.Undertale),
    DELTARUNE(UndertaleCommand.I18N_TEXTBOX_PREFIX.Deltarune.Name, net.perfectdreams.loritta.cinnamon.emotes.Emotes.Deltarune)
}