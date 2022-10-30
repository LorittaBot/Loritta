package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.*
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData

class BRMemesCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
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

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.IMAGES, TodoFixThisData) {
        subcommandGroup(I18N_PREFIX.Bolsonaro.Label, I18N_PREFIX.Bolsonaro.Description) {
            subcommand(I18N_PREFIX.Bolsonaro.Tv.Label, I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = { BolsonaroExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Bolsonaro.Tv2.Label, I18N_PREFIX.Bolsonaro.Tv.Description) {
                executor = { Bolsonaro2Executor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Bolsonaro.Frame.Label, I18N_PREFIX.Bolsonaro.Frame.Description) {
                executor = { BolsoFrameExecutor(it, it.gabrielaImageServerClient) }
            }
        }

        subcommandGroup(I18N_PREFIX.Ata.Label, I18N_PREFIX.Ata.Description) {
            subcommand(I18N_PREFIX.Ata.Monica.Label, I18N_PREFIX.Ata.Monica.Description) {
                executor = { MonicaAtaExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Ata.Chico.Label, I18N_PREFIX.Ata.Chico.Description) {
                executor = { ChicoAtaExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Ata.Lori.Label, I18N_PREFIX.Ata.Lori.Description) {
                executor = { LoriAtaExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Ata.Gessy.Label, I18N_PREFIX.Ata.Gessy.Description) {
                executor = { GessyAtaExecutor(it, it.gabrielaImageServerClient) }
            }
        }

        subcommandGroup(I18N_PREFIX.Ednaldo.Label, TodoFixThisData) {
            subcommand(
                I18N_PREFIX.Ednaldo.Bandeira.Label,
                I18N_PREFIX.Ednaldo.Bandeira.Description
            ) {
                executor = { EdnaldoBandeiraExecutor(it, it.gabrielaImageServerClient) }
            }

            subcommand(I18N_PREFIX.Ednaldo.Tv.Label, I18N_PREFIX.Ednaldo.Tv.Description) {
                executor = { EdnaldoTvExecutor(it, it.gabrielaImageServerClient) }
            }
        }

        subcommand(I18N_PREFIX.Cortesflow.Label, I18N_PREFIX.Cortesflow.Description) {
            executor = { CortesFlowExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Sam.Label, I18N_PREFIX.Sam.Description) {
            executor = { SAMExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Canelladvd.Label, I18N_PREFIX.Canelladvd.Description) {
            executor = { CanellaDvdExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Cepo.Label, I18N_PREFIX.Cepo.Description) {
            executor = { CepoDeMadeiraExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Romerobritto.Label, I18N_PREFIX.Romerobritto.Description) {
            executor = { RomeroBrittoExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Briggscover.Label, I18N_PREFIX.Briggscover.Description) {
            executor = { BriggsCoverExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}