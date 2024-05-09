package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.CortesFlowRequest
import net.perfectdreams.gabrielaimageserver.data.SAMLogoRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.TextUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase
import java.awt.Color

class BRMemesCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        const val I18N_CORTESFLOW_KEY_PREFIX = "commands.command.brmemes.cortesflow"
        val I18N_PREFIX = I18nKeysData.Commands.Command.Brmemes

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

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.IMAGES) {
        enableLegacyMessageSupport = true

        subcommandGroup(I18N_PREFIX.Bolsonaro.Label, I18N_PREFIX.Bolsonaro.Description) {
            subcommand(I18N_PREFIX.Bolsonaro.Tv.Label, I18N_PREFIX.Bolsonaro.Tv.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("bolsonaro")
                    add("bolsonarotv")
                }

                executor = BolsonaroTvExecutor()
            }

            subcommand(I18N_PREFIX.Bolsonaro.Tv2.Label, I18N_PREFIX.Bolsonaro.Tv.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("bolsonarotv2")
                    add("bolsonaro2")
                }

                executor = BolsonaroTv2Executor()
            }

            subcommand(I18N_PREFIX.Bolsonaro.Frame.Label, I18N_PREFIX.Bolsonaro.Frame.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("bolsoframe")
                }

                executor = BolsoFrameExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Ata.Label, I18N_PREFIX.Ata.Description) {
            subcommand(I18N_PREFIX.Ata.Monica.Label, I18N_PREFIX.Ata.Monica.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ata")
                    add("monicaata")
                }

                executor = MonicaAtaExecutor()
            }

            subcommand(I18N_PREFIX.Ata.Chico.Label, I18N_PREFIX.Ata.Chico.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("chicoata")
                }

                executor = ChicoAtaExecutor()
            }

            subcommand(I18N_PREFIX.Ata.Lori.Label, I18N_PREFIX.Ata.Lori.Label) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("loriata")
                }

                executor = LoriAtaExecutor()
            }

            subcommand(I18N_PREFIX.Ata.Gessy.Label, I18N_PREFIX.Ata.Gessy.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("gessyata")
                }

                executor = GessyAtaExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Ednaldo.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Ednaldo.Bandeira.Label, I18N_PREFIX.Ednaldo.Bandeira.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ednaldobandeira")
                }

                executor = EdnaldoBandeiraExecutor()

            }

            subcommand(I18N_PREFIX.Ednaldo.Tv.Label, I18N_PREFIX.Ednaldo.Tv.Description) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ednaldotv")
                }

                executor = EdnaldoTvExecutor()
            }
        }

        subcommand(I18N_PREFIX.Cortesflow.Label, I18N_PREFIX.Cortesflow.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("cortesflow")
            }

            executor = CortesFlowExecutor()
        }

        subcommand(I18N_PREFIX.Sam.Label, I18N_PREFIX.Sam.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("sam")
            }

            executor = SAMExecutor()
        }

        subcommand(I18N_PREFIX.Canelladvd.Label, I18N_PREFIX.Canelladvd.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("canelladvd")
            }

            executor = CanellaDvdExecutor()
        }

        subcommand(I18N_PREFIX.Cepo.Label, I18N_PREFIX.Cepo.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("cepo")
            }

            executor = CepoDeMadeiraExecutor()
        }

        subcommand(I18N_PREFIX.Romerobritto.Label, I18N_PREFIX.Romerobritto.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("romerobritto")
            }

            executor = RomeroBrittoExecutor()
        }

        subcommand(I18N_PREFIX.Briggscover.Label, I18N_PREFIX.Briggscover.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("briggscover")
            }

            executor = BriggsCoverExecutor()
        }
    }

    inner class BolsonaroTvExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.bolsonaro(it) },
        "bolsonaro_tv.png"
    )

    inner class BolsonaroTv2Executor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.bolsonaro2(it) },
        "bolsonaro_tv2.png"
    )

    inner class BolsoFrameExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.bolsoFrame(it) },
        "bolsonaro_frame.png"
    )

    inner class MonicaAtaExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.monicaAta(it) },
        "monica_ata.png"
    )

    inner class ChicoAtaExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.chicoAta(it) },
        "chico_ata.png"
    )

    inner class LoriAtaExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.loriAta(it) },
        "lori_ata.png"
    )

    inner class GessyAtaExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.gessyAta(it) },
        "gessy_ata.png"
    )

    inner class EdnaldoBandeiraExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.ednaldoBandeira(it) },
        "ednaldo_bandeira.png"
    )

    inner class EdnaldoTvExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.ednaldoTv(it) },
        "ednaldo_tv.png"
    )

    inner class CortesFlowExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val type = string("thumbnail", I18N_PREFIX.Cortesflow.Options.Thumbnail) {
                cortesFlowThumbnails.forEach {
                    choice(
                        StringI18nData(
                            StringI18nKey(
                                "$I18N_CORTESFLOW_KEY_PREFIX.thumbnails.${
                                    TextUtils.kebabToLowerCamelCase(
                                        it
                                    )
                                }"
                            ),
                            emptyMap()
                        ),
                        it
                    )
                }
            }

            val text = string("text", I18N_PREFIX.Cortesflow.Options.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val type = args[options.type]
            val text = args[options.text]

            val result = client.handleExceptions(context) {
                client.images.cortesFlow(type, CortesFlowRequest(text))
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "cortes_flow.jpg"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val type = args.getOrNull(0)

            if (type == null || !cortesFlowThumbnails.contains(type)) {
                val result = context.loritta.http.get("https://gabriela.loritta.website/api/v1/images/cortes-flow")
                    .bodyAsText()

                val elements = Json.parseToJsonElement(result)
                    .jsonArray

                val serverConfig = context.loritta.cachedServerConfigs.asMap()[context.guild.idLong]

                val availableGroupedBy = elements.groupBy { it.jsonObject["participant"]!!.jsonPrimitive.content }
                    .entries
                    .sortedByDescending { it.value.size }

                context.reply(false) {
                    embed {
                        title = "${Emotes.FLOW_PODCAST} ${context.locale["commands.command.cortesflow.embedTitle"]}"
                        description = context.locale.getList(
                            "commands.command.cortesflow.embedDescription",
                            context.locale["commands.command.cortesflow.howToUseExample", serverConfig?.commandPrefix ?: "+"],
                            context.locale["commands.command.cortesflow.commandExample", serverConfig?.commandPrefix ?: "+"]
                        ).joinToString("\n")
                        color = Color.BLACK.rgb
                        footer {
                            name = context.locale["commands.command.cortesflow.findOutThumbnailSource"]
                            url = "https://yt3.ggpht.com/a/AATXAJwhhX5JXoYvdDwDI56fQfTDinfs21vzivC-DBW6=s88-c-k-c0x00ffffff-no-rj"
                        }

                        for ((_, value) in availableGroupedBy) {
                            field {
                                name = value.first().jsonObject["participantDisplayName"]!!.jsonPrimitive.content
                                this.value = value.joinToString {
                                    context.locale[
                                        "commands.command.cortesflow.thumbnailSelection",
                                        it.jsonObject["path"]!!.jsonPrimitive.content.removePrefix("/api/v1/images/cortes-flow/"),
                                        it.jsonObject["source"]!!.jsonPrimitive.content
                                    ]
                                }
                            }
                        }
                    }
                }

                return null
            }

            val text = args.drop(1).joinToString(" ")

            return mapOf(
                options.type to type,
                options.text to text
            )
        }
    }

    inner class SAMExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val type = string("type", I18N_PREFIX.Sam.Options.Type) {
                choice(I18N_PREFIX.Sam.Options.Choice.Sam1, "1")
                choice(I18N_PREFIX.Sam.Options.Choice.Sam2, "2")
                choice(I18N_PREFIX.Sam.Options.Choice.Sam3, "3")
            }

            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val type = args[options.type]
            val imageReference = args[options.imageReference].get(context)

            val result = client.handleExceptions(context) {
                client.images.samLogo(
                    SAMLogoRequest(
                        URLImageData(imageReference),
                        when (type) {
                            "1" -> SAMLogoRequest.LogoType.SAM_1
                            "2" -> SAMLogoRequest.LogoType.SAM_2
                            "3" -> SAMLogoRequest.LogoType.SAM_3
                            else -> error("Unsupported Logo Type!")
                        }
                    )
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "sam_logo.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val type = args.getOrNull(0)?.toInt()
            val choices = 1..3

            if (!choices.contains(type)) {
                context.explain()

                return null
            }

            val data = args.drop(1).getOrNull(0)
            val firstMention = context.mentions.users.firstOrNull()

            if (data == firstMention?.asMention) {
                return mapOf(
                    options.type to type.toString(),
                    options.imageReference to ImageReference(
                        firstMention?.effectiveAvatarUrl,
                        context.getImage(0)
                    )
                )
            }

            return mapOf(
                options.imageReference to ImageReference(
                    data,
                    context.getImage(0)
                )
            )
        }
    }

    inner class CanellaDvdExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.canellaDvd(it) },
        "canella_dvd.png"
    )

    inner class CepoDeMadeiraExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.cepoDeMadeira(it) },
        "cepo_de_madeira.gif"
    )

    inner class RomeroBrittoExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.romeroBritto(it) },
        "romero_britto.png"
    )

    inner class BriggsCoverExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.briggsCover(it) },
        "briggs_cover.png"
    )
}