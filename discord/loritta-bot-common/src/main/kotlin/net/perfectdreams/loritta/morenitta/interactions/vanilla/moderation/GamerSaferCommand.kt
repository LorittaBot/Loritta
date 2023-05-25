package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuilds
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferGuildInviteAdditionalData
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferPlayerVerificationAdditionalData
import net.perfectdreams.loritta.morenitta.utils.gamersafer.GamerSaferUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class GamerSaferCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Gamersafer

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        subcommand(I18N_PREFIX.Join.Label, I18N_PREFIX.Join.Description) {
            executor = GamerSaferJoinExecutor()
        }

        subcommand(I18N_PREFIX.Verify.Label, I18N_PREFIX.Verify.Description) {
            executor = GamerSaferVerifyExecutor()
        }
    }

    inner class GamerSaferJoinExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val guildId = context.guildId!! // This command cannot be used in DMs anyway

            // Check if the GamerSafer module is enabled and if the user has already joined the GamerSafer guild
            val mjs = loritta.transaction {
                val gsGuildConfig = ServerConfigs.innerJoin(GamerSaferConfigs).select {
                    ServerConfigs.id eq guildId
                }
                    .limit(1)
                    .firstOrNull()

                if (gsGuildConfig == null || !gsGuildConfig[GamerSaferConfigs.enabled])
                    return@transaction null
                
                if (GamerSaferGuildMembers.select { GamerSaferGuildMembers.guild eq guildId and (GamerSaferGuildMembers.discordUser eq context.user.idLong) }.count() == 0L) {
                    // Didn't join the guild yet
                    GamerSaferGuildMemberJoinStatus.DIDNT_JOIN_THE_GUILD_YET
                } else {
                    // Has joined the guild
                    GamerSaferGuildMemberJoinStatus.HAS_JOINED_THE_GUILD
                }
            }

            if (mjs == null) {
                context.reply(true) {
                    content = "GamerSafer não está ativada neste servidor"
                }
                return
            }

            val provider = loritta.config.loritta.gamerSafer.provider
            val jws = GamerSaferUtils.createJWTTokenForGuild(loritta, guildId)

            when (mjs) {
                GamerSaferGuildMemberJoinStatus.HAS_JOINED_THE_GUILD -> context.reply(true) {
                    content = "Você já entrou na guild da GamerSafer!"
                }
                GamerSaferGuildMemberJoinStatus.DIDNT_JOIN_THE_GUILD_YET -> {
                    // If the user haven't joined the GamerSafer guild yet, send a message to them about it
                    val response2 = loritta.http.post("${loritta.config.loritta.gamerSafer.endpointUrl}/guilds/invite") {
                        bearerAuth(jws)

                        setBody(
                            TextContent(
                                buildJsonObject {
                                    put("provider", provider)
                                    put("providerId", context.guildId!!.toString())
                                    put("providerLinkBack", loritta.lorittaCluster.websiteUrl + "api/v1/callbacks/gamersafer")
                                    put("internalId",
                                        Base64.getEncoder().encodeToString(
                                            Json.encodeToString(
                                                GamerSaferGuildInviteAdditionalData(
                                                    context.user.idLong,
                                                    loritta.config.loritta.gamerSafer.webhookSecret
                                                )
                                            ).toByteArray(Charsets.UTF_8)
                                        )
                                    )
                                }.toString(),
                                ContentType.Application.Json
                            )
                        )
                    }

                    println("Status Code: ${response2.status}")
                    val bodyAsText = response2.bodyAsText()
                    println("Payload: ${bodyAsText}")

                    val qrCodeAsByteArray = Json.parseToJsonElement(bodyAsText).jsonObject["qrCode"]!!.jsonPrimitive.content.substringAfter("base64,")

                    val base64QrCode = Base64.getDecoder().decode(qrCodeAsByteArray.toByteArray(Charsets.UTF_8))

                    context.reply(true) {
                        styled(
                            "Acesse o QR code no app da GamerSafer para entrar na guilda!",
                            Emotes.LoriHi
                        )

                        this.files += FileUpload.fromData(base64QrCode, "qr_code.png")
                    }
                }
            }
        }
    }

    inner class GamerSaferVerifyExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val guildId = context.guildId!! // This command cannot be used in DMs anyway

            // TODO: Check if the member has any matching roles that requires joining the GS guild

            // Check if the user has already joined the GamerSafer guild
            val mjs = loritta.transaction {
                GamerSaferGuildMembers.select { GamerSaferGuildMembers.guild eq guildId and (GamerSaferGuildMembers.discordUser eq context.user.idLong) }.firstOrNull()
            }

            val provider = loritta.config.loritta.gamerSafer.provider
            val jws = GamerSaferUtils.createJWTTokenForGuild(loritta, guildId)

            when {
                mjs == null -> context.reply(true) {
                    content = "Você ainda não entrou na guild da GamerSafer!"
                }
                mjs != null -> {
                    // If the user haven't joined the GamerSafer guild yet, send a message to them about it
                    val response2 = loritta.http.post("${loritta.config.loritta.gamerSafer.endpointUrl}/guilds/verifications") {
                        bearerAuth(jws)

                        setBody(
                            TextContent(
                                buildJsonObject {
                                    put("provider", provider)
                                    put("providerId", context.guildId!!.toString())
                                    put("providerLinkBack", "https://phoenix-whistler.tail2f90.ts.net/api/v1/callbacks/gamersafer")
                                    put("guildMemberId", mjs[GamerSaferGuildMembers.gamerSaferUser])
                                    put("discordMessage",
                                        Base64.getEncoder().encodeToString(
                                            Json.encodeToString(
                                                GamerSaferPlayerVerificationAdditionalData(
                                                    context.guildId!!,
                                                    context.user.idLong,
                                                    loritta.config.loritta.gamerSafer.webhookSecret
                                                )
                                            ).toByteArray(Charsets.UTF_8)
                                        )
                                    )
                                }.toString(),
                                ContentType.Application.Json
                            )
                        )
                    }

                    context.reply(true) {
                        styled(
                            "Você recebeu uma notificação no seu celular da GamerSafer para realizar a validação!",
                            Emotes.LoriHi
                        )
                    }
                }
            }
        }
    }

    enum class GamerSaferGuildMemberJoinStatus {
        HAS_JOINED_THE_GUILD,
        DIDNT_JOIN_THE_GUILD_YET
    }
}