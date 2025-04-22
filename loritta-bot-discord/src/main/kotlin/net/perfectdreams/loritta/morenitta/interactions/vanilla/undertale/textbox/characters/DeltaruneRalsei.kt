package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters

import net.dv8tion.jda.api.components.selects.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.UndertaleCommand

object DeltaruneRalsei : CharacterData() {
    override fun menuOptions(i18nContext: I18nContext, activePortrait: String, builder: StringSelectMenu.Builder) {
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral), "deltarune/ralsei/neutral", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Neutral2), "deltarune/ralsei/neutral2", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.NeutralLookingAway), "deltarune/ralsei/neutral_looking_away", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Angry), "deltarune/ralsei/angry", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Angry), "deltarune/ralsei/angry2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.AngryLookingAway), "deltarune/ralsei/angry_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.AngryUwU), "deltarune/ralsei/angry_uwu", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Astonished), "deltarune/ralsei/astonished", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.BlushingLookingDown), "deltarune/ralsei/blushing_looking_down", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.BlushingLookingDown), "deltarune/ralsei/blushing_looking_down2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Disappointed), "deltarune/ralsei/disappointed", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Fear), "deltarune/ralsei/fear", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Fear), "deltarune/ralsei/fear2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Flustered), "deltarune/ralsei/flustered", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Happy), "deltarune/ralsei/happy", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Happy), "deltarune/ralsei/happy2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "deltarune/ralsei/sad", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Sad), "deltarune/ralsei/sad2", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SadLookingDown), "deltarune/ralsei/sad_looking_down", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Screaming), "deltarune/ralsei/screaming", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Serious), "deltarune/ralsei/serious", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SeriousLookingAway), "deltarune/ralsei/serious_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Shocked), "deltarune/ralsei/shocked", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.ShockedLookingAway), "deltarune/ralsei/shocked_looking_away", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Smiling), "deltarune/ralsei/smiling", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SmilingBlushing), "deltarune/ralsei/smiling_blushing", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SmilingNoGlasses), "deltarune/ralsei/smiling_no_glasses", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SprFaceRNohat_11), "deltarune/ralsei/spr_face_r_nohat_11", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SprFaceRNohat_19), "deltarune/ralsei/spr_face_r_nohat_19", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SprFaceRNohat_3), "deltarune/ralsei/spr_face_r_nohat_3", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SprFaceRNohat_5), "deltarune/ralsei/spr_face_r_nohat_5", activePortrait)
        // builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.SprFaceRNohat_9), "deltarune/ralsei/spr_face_r_nohat_9", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Uhhh), "deltarune/ralsei/uhhh", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Uhhh), "deltarune/ralsei/uhhh2", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Uhhh), "deltarune/ralsei/uhhh3", activePortrait)
        builder.optionAndAutomaticallySetDefault(i18nContext.get(UndertaleCommand.I18N_TEXTBOX_PREFIX.Portraits.Wink), "deltarune/ralsei/wink", activePortrait)
    }
}