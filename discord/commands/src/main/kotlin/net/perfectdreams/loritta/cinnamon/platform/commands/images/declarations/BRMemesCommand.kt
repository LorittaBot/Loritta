package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.Bolsonaro2Executor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BolsonaroExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CepoDeMadeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.CortesFlowExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.SAMExecutor

class BRMemesCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
   companion object {
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
   }

    override fun declaration() = slashCommand("brmemes", CommandCategory.IMAGES, TodoFixThisData) {
        subcommandGroup("bolsonaro", I18N_PREFIX.Bolsonaro.Description) {
            subcommand("tv", I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = BolsonaroExecutor(loritta, gabiClient)
            }

            subcommand("tv2", I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = Bolsonaro2Executor(loritta, gabiClient)
            }

            subcommand("frame", I18N_PREFIX.Bolsonaro.Frame.Description) {
                executor = BolsoFrameExecutor(loritta, gabiClient)
            }
        }

        subcommandGroup("ata", I18N_PREFIX.Ata.Description) {
            subcommand("monica", I18N_PREFIX.Ata.Monica.Description) {
                executor = MonicaAtaExecutor(loritta, gabiClient)
            }

            subcommand("chico", I18N_PREFIX.Ata.Chico.Description) {
                executor = ChicoAtaExecutor(loritta, gabiClient)
            }

            subcommand("lori", I18N_PREFIX.Ata.Lori.Description) {
                executor = LoriAtaExecutor(loritta, gabiClient)
            }

            subcommand("gessy", I18N_PREFIX.Ata.Gessy.Description) {
                executor = GessyAtaExecutor(loritta, gabiClient)
            }
        }

        subcommandGroup("ednaldo", TodoFixThisData) {
            subcommand(
                "flag",
                I18N_PREFIX.Ednaldo.Bandeira.Description
            ) {
                executor = EdnaldoBandeiraExecutor(loritta, gabiClient)
            }

            subcommand("tv", I18N_PREFIX.Ednaldo.Tv.Description) {
                executor = EdnaldoTvExecutor(loritta, gabiClient)
            }
        }

        subcommand("cortesflow", I18N_PREFIX.Cortesflow.Description) {
            executor = CortesFlowExecutor(loritta, gabiClient)
        }

        subcommand("sam", I18N_PREFIX.Sam.Description) {
            executor = SAMExecutor(loritta, gabiClient)
        }

        subcommand("canelladvd", I18N_PREFIX.Canelladvd.Description) {
            executor = CanellaDvdExecutor(loritta, gabiClient)
        }

        subcommand("cepo", I18N_PREFIX.Cepo.Description) {
            executor = CepoDeMadeiraExecutor(loritta, gabiClient)
        }

        subcommand("romerobritto", I18N_PREFIX.Romerobritto.Description) {
            executor = RomeroBrittoExecutor(loritta, gabiClient)
        }

        subcommand("briggscover", I18N_PREFIX.Briggscover.Description) {
            executor = BriggsCoverExecutor(loritta, gabiClient)
        }
    }
}