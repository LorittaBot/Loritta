package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

enum class UniverseType(val universeName: StringI18nData, val emote: Emote?) {
    NONE(UndertaleCommand.I18N_TEXTBOX_PREFIX.None.Name, null),
    UNDERTALE(UndertaleCommand.I18N_TEXTBOX_PREFIX.Undertale.Name, Emotes.Undertale),
    DELTARUNE(UndertaleCommand.I18N_TEXTBOX_PREFIX.Deltarune.Name, Emotes.Deltarune)
}