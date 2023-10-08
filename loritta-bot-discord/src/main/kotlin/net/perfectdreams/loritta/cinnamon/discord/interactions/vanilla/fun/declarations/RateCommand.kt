package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.RateHusbandoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.RateLoliExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.RateWaifuExecutor

class RateCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Rate
        const val WAIFU_SINGULAR = "Waifu"
        const val WAIFU_PLURAL = "Waifus"

        const val HUSBANDO_SINGULAR = "Husbando"
        const val HUSBANDO_PLURAL = "Husbandos"

        fun waifuHusbandoScores(type: String, typePlural: String) = listOf(
            I18N_PREFIX.WaifuHusbando.Score0(type = type),
            I18N_PREFIX.WaifuHusbando.Score1(type = type),
            I18N_PREFIX.WaifuHusbando.Score2(type = type),
            I18N_PREFIX.WaifuHusbando.Score3(typePlural = typePlural),
            I18N_PREFIX.WaifuHusbando.Score4(type = type),
            I18N_PREFIX.WaifuHusbando.Score5,
            I18N_PREFIX.WaifuHusbando.Score6(type = type),
            I18N_PREFIX.WaifuHusbando.Score7(typePlural = typePlural),
            I18N_PREFIX.WaifuHusbando.Score8(type = type),
            I18N_PREFIX.WaifuHusbando.Score9(type = type),
            I18N_PREFIX.WaifuHusbando.Score10(type = type)
        )
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.FUN, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.WaifuHusbando.WaifuLabel, I18N_PREFIX.WaifuHusbando.Description(WAIFU_SINGULAR)) {
            executor = { RateWaifuExecutor(it) }
        }

        subcommand(I18N_PREFIX.WaifuHusbando.HusbandoLabel, I18N_PREFIX.WaifuHusbando.Description(HUSBANDO_SINGULAR)) {
            executor = { RateHusbandoExecutor(it) }
        }

        subcommand(I18N_PREFIX.Loli.Label, I18N_PREFIX.Loli.Description) {
            executor = { RateLoliExecutor(it) }
        }
    }
}