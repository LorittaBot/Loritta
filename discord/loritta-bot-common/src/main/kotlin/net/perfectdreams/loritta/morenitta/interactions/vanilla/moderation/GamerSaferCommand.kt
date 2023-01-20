package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.time.Duration.Companion.minutes


class GamerSaferCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Gamersafer

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        subcommand(I18N_PREFIX.Verify.Label, I18N_PREFIX.Verify.Description) {
            executor = GamerSaferVerifyExecutor()
        }
    }

    inner class GamerSaferVerifyExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val guildId = context.guildId!! // This command cannot be used in DMs anyway

            val requiresVerificationUserData = loritta.transaction {
                GamerSaferRequiresVerificationUsers.select {
                    GamerSaferRequiresVerificationUsers.guild eq guildId and (GamerSaferRequiresVerificationUsers.user eq context.user.idLong)
                }.firstOrNull()
            }

            if (requiresVerificationUserData == null) {
                context.reply(true) {
                    styled("Você não precisa verificar algo!")
                }
                return
            }

            val verifyId = requiresVerificationUserData[GamerSaferRequiresVerificationUsers.id].value

            val provider = loritta.config.loritta.gamerSafer.provider

            val jws = createJWTToken(buildJsonObject { put("alg", "HS256") }.toString(),"$provider|${context.guildId}")

            val response = loritta.http.post("${loritta.config.loritta.gamerSafer.endpointUrl}/guilds") {
                bearerAuth(jws)

                setBody(
                    TextContent(
                        buildJsonObject {
                            put("provider", provider)
                            put("providerId", context.guildId!!.toString())
                            put("name", context.guild.name)
                        }.toString(),
                        ContentType.Application.Json
                    )
                )
            }

            println("Status Code: ${response.status}")
            println("Payload: ${response.bodyAsText()}")

            val response2 = loritta.http.post("${loritta.config.loritta.gamerSafer.endpointUrl}/guilds/invite") {
                bearerAuth(jws)

                setBody(
                    TextContent(
                        buildJsonObject {
                            put("provider", provider)
                            put("providerId", context.guildId!!.toString())
                            put("providerLinkBack", "TODO?token=abc&verifyId=${requiresVerificationUserData[GamerSaferRequiresVerificationUsers.id]}")
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
                    "Verificação do cargo <@&${requiresVerificationUserData[GamerSaferRequiresVerificationUsers.role]}>",
                    Emotes.LoriHi
                )
                content = "Verificação do cargo <@&${requiresVerificationUserData[GamerSaferRequiresVerificationUsers.role]}>"

                this.files += FileUpload.fromData(base64QrCode, "qr_code.png")
            }

            // Wait until we receive the verification callback
            val ch = Channel<Unit>()
            loritta.gamerSaferWaitingForCallbacks[requiresVerificationUserData[GamerSaferRequiresVerificationUsers.id].value] = ch

            try {
                withTimeout(5.minutes) {
                    ch.receive()

                    context.reply(true) {
                        styled(
                            "Verificado com sucesso!",
                            Emotes.LoriYay
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                loritta.gamerSaferWaitingForCallbacks.remove(verifyId)
            }
        }
    }

    fun createJWTToken(header: String, data: String): String {
        val base64WithoutPadding = Base64.getUrlEncoder().withoutPadding()

        val secretKeyAsBase64 = loritta.config.loritta.gamerSafer.secretKey

        val decodedKey: ByteArray = secretKeyAsBase64.toByteArray(Charsets.UTF_8)
        val secretKey: SecretKey = SecretKeySpec(
            decodedKey,
            0,
            decodedKey.size,
            "HS256"
        )

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)

        val headerAsBase64 = base64WithoutPadding.encodeToString(header.toByteArray(Charsets.UTF_8))
        val dataAsBase64 = base64WithoutPadding.encodeToString(data.toByteArray(Charsets.UTF_8))
        val doneFinal = mac.doFinal("${headerAsBase64}.${dataAsBase64}".toByteArray(Charsets.UTF_8))
        val doneFinalAsBase64 = base64WithoutPadding.encodeToString(doneFinal)

        return "$headerAsBase64.$dataAsBase64.$doneFinalAsBase64"
    }
}