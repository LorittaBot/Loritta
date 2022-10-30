package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.morenitta.LorittaBot

class UnlockCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "unlock",
    listOf("destrancar"),
    net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.unlock.description")

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.ManageGuild)
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun getBotPermissions(): List<Permission> {
        return listOf(Permission.ManageChannels, Permission.ManageRoles)
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val channel = getTextChannel(context, context.args.getOrNull(0))
            ?: context.event.channel // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo

        val publicRole = context.guild.publicRole
        val override = channel.getPermissionOverride(publicRole)

        if (override != null) {
            if (Permission.SendMessages in override.deny) {
                override.edit {
                    // Undeny SendMessages
                    denied = override.deny.minus(Permission.SendMessages)
                }

                context.reply(
                    LorittaReply(
                        locale["commands.command.unlock.allowed", context.config.commandPrefix],
                        "\uD83C\uDF89"
                    )
                )
            } else {
                context.reply(
                    LorittaReply(
                        locale["commands.command.unlock.channelAlreadyIsUnlocked", context.config.commandPrefix],
                        Emotes.LORI_CRYING
                    )
                )
            }
        } else { // Bem, na verdade não seria totalmente necessário este else, mas vamos supor que o cara usou o "+unlock" com o chat destravado sem ter travado antes :rolling_eyes:
            context.reply(
                LorittaReply(
                    locale["commands.command.unlock.allowed", context.config.commandPrefix],
                    "\uD83C\uDF89"
                )
            )
        }
    }

    fun getTextChannel(context: CommandContext, input: String?): Channel? {
        if (input == null)
            return null

        val guild = context.guild

        val channels = guild.getTextChannelsByName(input, false)
        if (channels.isNotEmpty()) {
            return channels[0]
        }

        val id = input
            .replace("<", "")
            .replace("#", "")
            .replace(">", "")

        if (!id.isValidSnowflake())
            return null

        val channel = guild.getTextChannelById(id)
        if (channel != null) {
            return channel
        }

        return null
    }
}