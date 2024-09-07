package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.AdminUtils
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.UnbanCommand
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.util.*

class BanInfoCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Baninfo
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("58e7d211-a33b-489f-b4e1-3a332dd264aa")) {
        enableLegacyMessageSupport = true
        isGuildOnly = true

        alternativeLegacyAbsoluteCommandPaths.apply {
            add("checkban")
            add("infoban")
        }

        defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)

        examples = I18N_PREFIX.Examples

        executor = BanInfoExecutor()
    }

    inner class BanInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", I18N_PREFIX.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val bannedUser = args[options.user]
            val locale = context.locale
            val i18nContext = context.i18nContext
            val guild = context.guild
            val loritta = context.loritta

            try {
                val banInformation = context.guild.retrieveBan(bannedUser.user).await()
                val banReason = banInformation.reason ?: locale["commands.command.baninfo.noReasonSpecified"]
                val embed = EmbedBuilder()
                    .setTitle("${Emotes.LORI_COFFEE} ${locale["commands.command.baninfo.title"]}")
                    .setThumbnail(banInformation.user.avatarUrl)
                    .addField("${Emotes.LORI_TEMMIE} ${locale["commands.command.baninfo.user"]}", "`${banInformation.user.asTag}`", false)
                    .addField("${Emotes.LORI_BAN_HAMMER} ${locale["commands.command.baninfo.reason"]}", "`${banReason}`", false)
                    .setColor(Constants.DISCORD_BLURPLE)
                    .setFooter(i18nContext.get(I18N_PREFIX.IfYouWantToUnbanThisUser("⚒️")))

                context.reply(false) {
                    embeds += embed.build()

                    actionRow(loritta.interactivityManager.buttonForUser(context.user, ButtonStyle.DANGER, i18nContext.get(I18N_PREFIX.UnbanUser), { emoji = Emoji.fromUnicode("⚒️") }) {
                        val deferredChannelMessage = it.deferChannelMessage(true)

                        val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)

                        UnbanCommand.unban(
                            loritta,
                            context.i18nContext,
                            settings,
                            guild,
                            context.user,
                            locale,
                            bannedUser.user,
                            "",
                            false
                        )

                        deferredChannelMessage.editOriginal(
                            MessageEdit {
                                styled(
                                    locale["commands.command.unban.successfullyUnbanned"],
                                    Emotes.LORI_BAN_HAMMER.asMention
                                )
                            }
                        )
                    })
                }
            } catch (e: ErrorResponseException) {
                if (e.errorResponse == ErrorResponse.UNKNOWN_BAN) {
                    context.reply(true) {
                        styled(
                            locale["commands.command.baninfo.banDoesNotExist"],
                            Emotes.LORI_CRYING.asMention
                        )
                    }
                    return
                }
                throw e
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val userAndMember = context.getUserAndMember(0)
            if (userAndMember == null) {
                context.explain()
                return null
            }

            return mapOf(options.user to userAndMember)
        }
    }
}
