package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox.characters

import dev.kord.rest.builder.component.SelectMenuBuilder
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.platform.commands.undertale.declarations.UndertaleCommand

object UndertaleAsriel : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: SelectMenuBuilder) {
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/asriel/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Couraged), "undertale/asriel/couraged", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.LookingAway), "undertale/asriel/looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Relieved), "undertale/asriel/relieved", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "undertale/asriel/sad", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadClosedEyes), "undertale/asriel/sad_closed_eyes", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadClosedEyes), "undertale/asriel/sad_closed_eyes2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingDown), "undertale/asriel/sad_looking_down", activePortrait)
    }
}