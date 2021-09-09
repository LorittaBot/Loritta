package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.commands.images.SAMExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object BRMemesCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Brmemes
    const val I18N_CORTESFLOW_KEY_PREFIX = "commands.command.brmemes.cortesflow"
    val cortesFlowThumbnails = listOf(
        "arthur-benozzati-smile",
        "douglas-laughing",
        "douglas-pointing",
        "douglas-pray",
        "gaules-sad",
        "igor-angry",
        "igor-naked",
        "igor-pointing",
        "julio-cocielo-eyes",
        "lucas-inutilismo-exalted",
        "metaforando-badge",
        "metaforando-surprised",
        "mitico-succ",
        "monark-discussion",
        "monark-smoking",
        "monark-stop",
        "peter-jordan-action-figure",
        "poladoful-discussion",
        "rato-borrachudo-disappointed",
        "rato-borrachudo-no-glasses"
    )

    override fun declaration() = command(listOf("brmemes"), CommandCategory.IMAGES, TodoFixThisData) {
        subcommandGroup(listOf("bolsonaro"), I18N_PREFIX.Bolsonaro.Description) {
            subcommand(listOf("tv"), I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = BolsonaroExecutor
            }

            subcommand(listOf("tv2"), I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = Bolsonaro2Executor
            }

            subcommand(listOf("frame"), I18N_PREFIX.Bolsonaro.Frame.Description) {
                executor = BolsoFrameExecutor
            }
        }

        subcommandGroup(listOf("ata"), I18N_PREFIX.Ata.Description) {
            subcommand(listOf("monica"), I18N_PREFIX.Ata.Monica.Description) {
                executor = MonicaAtaExecutor
            }

            subcommand(listOf("chico"), I18N_PREFIX.Ata.Chico.Description) {
                executor = ChicoAtaExecutor
            }

            subcommand(listOf("lori"), I18N_PREFIX.Ata.Lori.Description) {
                executor = LoriAtaExecutor
            }

            subcommand(listOf("gessy"), I18N_PREFIX.Ata.Gessy.Description) {
                executor = GessyAtaExecutor
            }
        }

        subcommandGroup(listOf("ednaldo"), TodoFixThisData) {
            subcommand(
                listOf("flag", "bandeira"),
                I18N_PREFIX.Ednaldo.Bandeira.Description
            ) {
                executor = EdnaldoBandeiraExecutor
            }

            subcommand(listOf("tv"), I18N_PREFIX.Ednaldo.Tv.Description) {
                executor = EdnaldoTvExecutor
            }
        }

        subcommand(listOf("cortesflow"), I18N_PREFIX.Cortesflow.Description) {
            executor = CortesFlowExecutor
        }

        subcommand(listOf("sam"), I18N_PREFIX.Sam.Description) {
            executor = SAMExecutor
        }

        subcommand(listOf("canelladvd"), I18N_PREFIX.Canelladvd.Description) {
            executor = CanellaDvdExecutor
        }

        subcommand(listOf("cepo"), I18N_PREFIX.Cepo.Description) {
            executor = CepoDeMadeiraExecutor
        }

        subcommand(listOf("romerobritto"), I18N_PREFIX.Romerobritto.Description) {
            executor = RomeroBrittoExecutor
        }

        subcommand(listOf("briggscover"), I18N_PREFIX.Briggscover.Description) {
            executor = BriggsCoverExecutor
        }
    }
}