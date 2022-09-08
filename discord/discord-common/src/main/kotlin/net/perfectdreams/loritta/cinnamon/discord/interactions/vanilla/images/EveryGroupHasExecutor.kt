package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import dev.kord.core.entity.User
import dev.kord.rest.Image
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.EveryGroupHasCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.SadRealityCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.User128AvatarText
import net.perfectdreams.loritta.cinnamon.discord.utils.images.withTextAntialiasing
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.utils.Gender
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage

class EveryGroupHasExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user1 = optionalUser("user1", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Popular.Male))
        val user2 = optionalUser("user2", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Quiet.Male))
        val user3 = optionalUser("user3", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Clown.Male))
        val user4 = optionalUser("user4", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Nerd.Male))
        val user5 = optionalUser("user5", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Fanboy.Male))
        val user6 = optionalUser("user6", EveryGroupHasCommand.I18N_PREFIX.Options.User1.Text(EveryGroupHasCommand.I18N_PREFIX.Slot.Cranky.Male))
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val user1FromArguments = args[options.user1]
        val user2FromArguments = args[options.user2]
        val user3FromArguments = args[options.user3]
        val user4FromArguments = args[options.user4]
        val user5FromArguments = args[options.user5]
        val user6FromArguments = args[options.user6]

        val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
            context,
            listOf(
                user1FromArguments,
                user2FromArguments,
                user3FromArguments,
                user4FromArguments,
                user5FromArguments,
                user6FromArguments
            )
        )

        // Not enough users!
        if (!successfullyFilled) {
            context.fail {
                styled(context.i18nContext.get(EveryGroupHasCommand.I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

                if (noPermissionToQuery) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                } else if (context !is GuildApplicationCommandContext) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                }
            }
        }

        val everyGroupHasUsers = listOf(
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[0],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Popular.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Popular.Female,
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[1],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Quiet.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Quiet.Female,
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[2],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Clown.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Clown.Female,
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[3],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Nerd.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Nerd.Female,
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[4],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Fanboy.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Fanboy.Female,
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[5],
                EveryGroupHasCommand.I18N_PREFIX.Slot.Cranky.Male,
                EveryGroupHasCommand.I18N_PREFIX.Slot.Cranky.Female,
            )
        )

        val image = generate(
            everyGroupHasUsers[0],
            everyGroupHasUsers[1],
            everyGroupHasUsers[2],
            everyGroupHasUsers[3],
            everyGroupHasUsers[4],
            everyGroupHasUsers[5]
        )

        context.sendMessage {
            addFile("every_group_has.png", image.toByteArray(ImageFormatType.PNG).inputStream())
        }
    }

    private suspend fun createUserWithBufferedImage(
        i18nContext: I18nContext,
        user: User,
        maleKey: StringI18nData,
        femaleKey: StringI18nData
    ): UserWithBufferedImage {
        val profile = loritta.services.users.getUserProfile(UserId(user.id))
            ?.getProfileSettings()
            ?.gender ?: Gender.UNKNOWN

        return UserWithBufferedImage(
            when (profile) {
                Gender.FEMALE -> i18nContext.get(femaleKey)
                else -> i18nContext.get(maleKey)
            },
            user,
            ImageUtils.downloadImage(
                user.effectiveAvatar.cdnUrl.toUrl {
                    format = Image.Format.PNG
                },
                overrideTimeoutsForSafeDomains = true
            ) ?: ImageUtils.DEFAULT_DISCORD_AVATAR
        )
    }

    private suspend fun generate(
        user1: UserWithBufferedImage,
        user2: UserWithBufferedImage,
        user3: UserWithBufferedImage,
        user4: UserWithBufferedImage,
        user5: UserWithBufferedImage,
        user6: UserWithBufferedImage
    ): BufferedImage {
        var x = 0
        var y = 0

        val base = BufferedImage(384, 256, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x256 (tamanho do template)
        val results = listOf(
            user1,
            user2,
            user3,
            user4,
            user5,
            user6
        )

        for ((text, user, avatar) in results) {
            User128AvatarText.draw(
                loritta,
                base,
                x,
                y,
                user,
                avatar,
                text,
                Color.WHITE
            )

            x += 128
            if (x > 256) {
                x = 0
                y += 128
            }
        }

        return base
    }

    data class UserWithBufferedImage(
        val text: String,
        val user: User,
        val image: BufferedImage,
    )
}