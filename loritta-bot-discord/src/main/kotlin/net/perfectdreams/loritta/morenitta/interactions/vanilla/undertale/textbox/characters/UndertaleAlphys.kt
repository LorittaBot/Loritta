package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.dv8tion.jda.api.components.selects.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

object UndertaleAlphys : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder) {
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/alphys/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Blushing), "undertale/alphys/blushing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Concerned), "undertale/alphys/concerned", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Confident), "undertale/alphys/confident", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Excited), "undertale/alphys/excited", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.HeartEyes), "undertale/alphys/heart_eyes", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.LookingUp), "undertale/alphys/looking_up", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Nervous), "undertale/alphys/nervous", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Nervous2), "undertale/alphys/nervous2", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.NervousLookingUp), "undertale/alphys/nervous_looking_up", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Pain), "undertale/alphys/pain", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.PainLookingAway), "undertale/alphys/pain_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Relieved), "undertale/alphys/relieved", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.RelievedLookingAway), "undertale/alphys/relieved_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "undertale/alphys/sad", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingAway), "undertale/alphys/sad_looking_away", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingDown), "undertale/alphys/sad_looking_down", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadSurprised), "undertale/alphys/sad_surprised", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Serious), "undertale/alphys/serious", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SeriousLookingAway), "undertale/alphys/serious_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Surprised), "undertale/alphys/surprised", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sweating), "undertale/alphys/sweating", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SweatingBlushing), "undertale/alphys/sweating_blushing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SweatingCouraged), "undertale/alphys/sweating_couraged", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SweatingLookingAway), "undertale/alphys/sweating_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SweatingSmiling), "undertale/alphys/sweating_smiling", activePortrait)
    }
}