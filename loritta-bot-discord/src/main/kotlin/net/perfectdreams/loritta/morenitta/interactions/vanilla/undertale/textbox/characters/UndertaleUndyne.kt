package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.dv8tion.jda.api.components.selects.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

object UndertaleUndyne : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder) {
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/undyne/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Angry), "undertale/undyne/angry", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Annoyed), "undertale/undyne/annoyed", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.AnnoyedLookingAway), "undertale/undyne/annoyed_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Blushing), "undertale/undyne/blushing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Defeated), "undertale/undyne/defeated", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Disgusted), "undertale/undyne/disgusted", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Happy), "undertale/undyne/happy", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.HappyStained), "undertale/undyne/happy_stained", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing), "undertale/undyne/laughing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Pissed), "undertale/undyne/pissed", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingAway), "undertale/undyne/sad_looking_away", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingDown), "undertale/undyne/sad_looking_down", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Surprised), "undertale/undyne/surprised", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SurprisedStained), "undertale/undyne/surprised_stained", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sweating), "undertale/undyne/sweating", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.What), "undertale/undyne/what", activePortrait)

        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/undyne_the_undying/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Grin), "undertale/undyne_the_undying/grin", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Hurt), "undertale/undyne_the_undying/hurt", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing), "undertale/undyne_the_undying/laughing", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing2), "undertale/undyne_the_undying/laughing2", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing3), "undertale/undyne_the_undying/laughing3", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "undertale/undyne_the_undying/sad", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad2), "undertale/undyne_the_undying/sad2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sweating), "undertale/undyne_the_undying/sweating", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sweating2), "undertale/undyne_the_undying/sweating2", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SweatingLookingAway), "undertale/undyne_the_undying/sweating_looking_away", activePortrait)
    }
}