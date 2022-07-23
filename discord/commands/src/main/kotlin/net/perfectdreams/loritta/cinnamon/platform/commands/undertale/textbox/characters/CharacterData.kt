package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters

import dev.kord.rest.builder.component.SelectMenuBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import net.perfectdreams.i18nhelper.core.I18nContext

sealed class CharacterData {
    abstract fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: SelectMenuBuilder)

    fun SelectMenuBuilder.optionAndAutomaticallySetDefault(label: String, value: String, activePortrait: String, builder: SelectOptionBuilder.() -> Unit = {}) {
        option(label, value) {
            default = value == activePortrait
            apply(builder)
        }
    }
}