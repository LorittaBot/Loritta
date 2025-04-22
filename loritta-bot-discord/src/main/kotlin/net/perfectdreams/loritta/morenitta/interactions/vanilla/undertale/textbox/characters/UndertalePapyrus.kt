package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.dv8tion.jda.api.components.selects.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

object UndertalePapyrus : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder) {
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/papyrus/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Crying), "undertale/papyrus/crying", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Evil), "undertale/papyrus/evil", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.GooglyEyes), "undertale/papyrus/googly_eyes", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing), "undertale/papyrus/laughing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.LookingAway), "undertale/papyrus/looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Mad), "undertale/papyrus/mad", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sunglasses), "undertale/papyrus/sunglasses", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sweating), "undertale/papyrus/sweating", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Worried), "undertale/papyrus/worried", activePortrait)
    }
}