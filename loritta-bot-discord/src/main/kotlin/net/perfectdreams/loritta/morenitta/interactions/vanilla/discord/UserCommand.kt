package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class UserCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.User
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Avatar.Label, I18N_PREFIX.Avatar.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("avatar")
            }

            examples = I18N_PREFIX.Avatar.Examples

            executor = UserAvatarSlashExecutor()
        }

        subcommand(I18N_PREFIX.Banner.Label, I18N_PREFIX.Banner.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("banner")
            }

            executor = UserBannerSlashExecutor()
        }

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("userinfo")
                add("memberinfo")
            }

            executor = UserInfoSlashExecutor(loritta)
        }
    }

    class UserAvatarSlashExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.Avatar.Options.User)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userAndMember = args[options.user] ?: UserAndMember(context.user, context.memberOrNull)

            context.reply(false) {
                apply(
                    UserAvatarExecutor.createAvatarMessage(
                        context,
                        userAndMember,
                        UserAvatarExecutor.AvatarTarget.GLOBAL_AVATAR
                    )
                )
            }

            if (userAndMember.user.id == context.user.id)
                context.giveAchievementAndNotify(AchievementType.IS_THAT_AN_UNDERTALE_REFERENCE, ephemeral = true)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = mapOf(
            options.user to context.getUserAndMember(0)
        )
    }

    class UserBannerSlashExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.Banner.Options.User)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userAndMember = args[options.user] ?: UserAndMember(context.user, context.memberOrNull)

            val profile = userAndMember.user.retrieveProfile().await()

            val bannerUrlWithSize = profile.banner?.getUrl(512) ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.Command.User.Banner.UserDoesNotHaveAnBanner(userAndMember.user.asMention)
                    ),
                    prefix = Emotes.Error
                )
            }

            context.reply(false) {
                embed {
                    title = "\uD83D\uDDBC ${userAndMember.user.name}"

                    image = bannerUrlWithSize

                    // Easter Egg: Looking up yourself
                    if (context.user.id == userAndMember.user.id)
                        footer(context.i18nContext.get(I18N_PREFIX.Banner.YourselfEasterEgg))

                    val accentColor = profile.accentColor
                    color = accentColor?.rgb ?: LorittaColors.DiscordBlurple.rgb
                }

                actionRow(
                    Button.link(
                        bannerUrlWithSize,
                        context.i18nContext.get(I18N_PREFIX.Banner.OpenBannerInBrowser)
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = mapOf(
            options.user to context.getUserAndMember(0)
        )
    }

    class UserInfoSlashExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user = optionalUser("user", I18N_PREFIX.Info.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userAndMember = args[options.user] ?: UserAndMember(
                context.user,
                context.memberOrNull
            )

            context.reply(false) {
                apply(
                    UserInfoExecutor.createUserInfoMessage(
                        context,
                        userAndMember
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = mapOf(
            options.user to context.getUserAndMember(0)
        )
    }
}