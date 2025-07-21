package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

object UndertaleMettaton : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder) {
        // TODO: All of these that use "addOptions" are borked, they were already borked before migrating to JDA so idk why they are in here...
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/mettaton_ex/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Angry), "undertale/mettaton_ex/angry", activePortrait)
        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.AngryClosedEyes),
                "mettaton_ex/angry_closed_eyes"
            )
        )
        builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Apathetic),
                "mettaton_ex/apathetic"
            )
        )
        builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.CatMouth),
                "mettaton_ex/cat_mouth"
            )
        ) */
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Evil), "undertale/mettaton_ex/evil", activePortrait)
        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.EvilClosedEyes),
                "mettaton_ex/evil_closed_eyes"
            )
        ) */
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Funny), "undertale/mettaton_ex/funny", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Happy), "undertale/mettaton_ex/happy", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Hurt), "undertale/mettaton_ex/hurt", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Laughing), "undertale/mettaton_ex/laughing", activePortrait)
        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.LookingAway),
                "mettaton_ex/looking_away"
            )
        )
        builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.OpenMouth),
                "mettaton_ex/open_mouth"
            )
        ) */
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.OpenMouth2), "undertale/mettaton_ex/open_mouth2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "undertale/mettaton_ex/sad", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad2), "undertale/mettaton_ex/sad2", activePortrait)
        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadSmile),
                "mettaton_ex/sad_smile"
            )
        ) */
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sassy), "undertale/mettaton_ex/sassy", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Tilted), "undertale/mettaton_ex/tilted", activePortrait)
        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.TongueOut),
                "mettaton_ex/tongue_out"
            )
        ) */
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.TongueOut2), "undertale/mettaton_ex/tongue_out2", activePortrait)

        /* builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.ClosedEyes),
                "mettaton_neo/closed_eyes"
            )
        )
        builder.addOptions(
            SelectOption(
                i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Confident),
                "mettaton_neo/confident"
            )
        ) */
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Insane), "undertale/mettaton_neo/insane", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "undertale/mettaton_neo/neutral", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Pissed), "undertale/mettaton_neo/pissed", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "undertale/mettaton_neo/sad", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Serious), "undertale/mettaton_neo/serious", activePortrait)
    }
}