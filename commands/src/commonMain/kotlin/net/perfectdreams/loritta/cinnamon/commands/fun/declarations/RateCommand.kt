package net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateHusbandoExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateLoliExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object RateCommand : CommandDeclaration {
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

    override fun declaration() = command(listOf("rate", "avaliar"), CommandCategory.FUN, I18N_PREFIX.Description) {
        subcommand(listOf("waifu"), I18N_PREFIX.WaifuHusbando.Description(WAIFU_SINGULAR)) {
            executor = RateWaifuExecutor
        }

        subcommand(listOf("husbando"), I18N_PREFIX.WaifuHusbando.Description(HUSBANDO_SINGULAR)) {
            executor = RateHusbandoExecutor
        }

        subcommand(listOf("loli"), I18N_PREFIX.Loli.Description) {
            executor = RateLoliExecutor
        }
    }
}