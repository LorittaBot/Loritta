package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButtonWithDatabaseData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.ProfileCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.ProfileDesignManager
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.ProfileUserInfoData
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.StaticProfileCreator
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordEmojis
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DonationConfigs
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.utils.UserPremiumPlans
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import kotlin.time.Duration.Companion.hours

class ProfileExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", ProfileCommand.PROFILE_VIEW_I18N_PREFIX.Options.User.Text)
    }

    companion object {
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            sender: User,
            userToBeViewed: User,
            result: ProfileDesignManager.ProfileCreationResult
        ): suspend MessageBuilder.() -> (Unit) = {
            content = "O comando ainda não está pronto! Use `+perfil` para ver o seu perfil com todos os frufrus dele!"

            addFile("profile.png", result.image.inputStream())

            if (userToBeViewed == sender) {
                actionRow {
                    interactiveButtonWithDatabaseData(
                        loritta,
                        ButtonStyle.Secondary,
                        ChangeAboutMeButtonExecutor,
                        ChangeAboutMeButtonData(
                            userToBeViewed.id,
                            result.aboutMe
                        )
                    ) {
                        label = i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeAboutMe)
                        loriEmoji = Emotes.LoriReading
                    }
                }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        // TODO: Check if the user is banned
        val userToBeViewed = args[options.user] ?: context.user

        context.deferChannelMessage()

        val guild = if (context is GuildApplicationCommandContext) loritta.kord.getGuild(context.guildId) else null

        val result = loritta.profileDesignManager.createProfile(
            loritta,
            context.i18nContext,
            context.user,
            userToBeViewed,
            guild
        )

        val message = createMessage(loritta, context.i18nContext, context.user, userToBeViewed, result)

        context.sendMessage {
            message()
        }
    }
}
