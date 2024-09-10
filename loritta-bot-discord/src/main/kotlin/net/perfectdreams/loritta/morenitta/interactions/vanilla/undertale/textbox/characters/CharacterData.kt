package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import dev.minn.jda.ktx.interactions.components.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext

sealed class CharacterData {
    abstract fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder)

    fun StringSelectMenu.Builder.optionAndAutomaticallySetDefault(label: String, value: String, activePortrait: String) {
        addOptions(SelectOption(label, value, null, null, value == activePortrait))
    }
}