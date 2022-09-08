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
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.ThanksFriendsCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.User128AvatarText
import net.perfectdreams.loritta.cinnamon.discord.utils.images.withTextAntialiasing
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.Gender
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ThanksFriendsExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user1 = optionalUser("user1", ThanksFriendsCommand.I18N_PREFIX.Options.User1.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Thanks))
        val user2 = optionalUser("user2", ThanksFriendsCommand.I18N_PREFIX.Options.User2.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.For))
        val user3 = optionalUser("user3", ThanksFriendsCommand.I18N_PREFIX.Options.User3.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Being))
        val user4 = optionalUser("user4", ThanksFriendsCommand.I18N_PREFIX.Options.User4.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.The))
        val user5 = optionalUser("user5", ThanksFriendsCommand.I18N_PREFIX.Options.User5.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.NotYou))
        val user6 = optionalUser("user6", ThanksFriendsCommand.I18N_PREFIX.Options.User6.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Best))
        val user7 = optionalUser("user7", ThanksFriendsCommand.I18N_PREFIX.Options.User7.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Friends))
        val user8 = optionalUser("user8", ThanksFriendsCommand.I18N_PREFIX.Options.User8.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.Of))
        val user9 = optionalUser("user9", ThanksFriendsCommand.I18N_PREFIX.Options.User9.Text(ThanksFriendsCommand.I18N_PREFIX.Slot.All))
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
        val user7FromArguments = args[options.user7]
        val user8FromArguments = args[options.user8]
        val user9FromArguments = args[options.user9]

        val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
            context,
            listOf(
                user1FromArguments,
                user2FromArguments,
                user3FromArguments,
                user4FromArguments,
                user5FromArguments,
                user6FromArguments,
                user7FromArguments,
                user8FromArguments,
                user9FromArguments
            )
        )

        // Not enough users!
        if (!successfullyFilled) {
            context.fail {
                styled(context.i18nContext.get(SadRealityCommand.I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

                if (noPermissionToQuery) {
                    styled(context.i18nContext.get(SadRealityCommand.I18N_PREFIX.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                } else if (context !is GuildApplicationCommandContext) {
                    styled(context.i18nContext.get(SadRealityCommand.I18N_PREFIX.NotEnoughUsersGuildTip), Emotes.LoriReading)
                }
            }
        }

        val everyGroupHasUsers = listOf(
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[0],
                ThanksFriendsCommand.I18N_PREFIX.Slot.Thanks
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[1],
                ThanksFriendsCommand.I18N_PREFIX.Slot.For
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[2],
                ThanksFriendsCommand.I18N_PREFIX.Slot.Being
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[3],
                ThanksFriendsCommand.I18N_PREFIX.Slot.The
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[4],
                ThanksFriendsCommand.I18N_PREFIX.Slot.NotYou
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[5],
                ThanksFriendsCommand.I18N_PREFIX.Slot.Best
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[6],
                ThanksFriendsCommand.I18N_PREFIX.Slot.Friends
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[7],
                ThanksFriendsCommand.I18N_PREFIX.Slot.Of
            ),
            createUserWithBufferedImage(
                context.i18nContext,
                listOfUsers[8],
                ThanksFriendsCommand.I18N_PREFIX.Slot.All
            )
        )

        val image = generate(
            everyGroupHasUsers[0],
            everyGroupHasUsers[1],
            everyGroupHasUsers[2],
            everyGroupHasUsers[3],
            everyGroupHasUsers[4],
            everyGroupHasUsers[5],
            everyGroupHasUsers[6],
            everyGroupHasUsers[7],
            everyGroupHasUsers[8]
        )

        context.sendMessage {
            addFile("thanks_friends.png", image.toByteArray(ImageFormatType.PNG).inputStream())
        }
    }

    private suspend fun createUserWithBufferedImage(
        i18nContext: I18nContext,
        user: User,
        key: StringI18nData,
    ): UserWithBufferedImage {
        return UserWithBufferedImage(
            i18nContext.get(key),
            user,
            ImageUtils.downloadImage(
                user.effectiveAvatar.cdnUrl.toUrl {
                    format = Image.Format.PNG
                },
                overrideTimeoutsForSafeDomains = true
            ) ?: ImageUtils.DEFAULT_DISCORD_AVATAR
        )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun generate(
        user1: UserWithBufferedImage,
        user2: UserWithBufferedImage,
        user3: UserWithBufferedImage,
        user4: UserWithBufferedImage,
        user5: UserWithBufferedImage,
        user6: UserWithBufferedImage,
        user7: UserWithBufferedImage,
        user8: UserWithBufferedImage,
        user9: UserWithBufferedImage
    ): BufferedImage {
        var x = 0
        var y = 0

        val base = BufferedImage(384, 384, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x384 (tamanho do template)
        val results = listOf(
            user1,
            user2,
            user3,
            user4,
            user5,
            user6,
            user7,
            user8,
            user9
        )

        var i = 0
        val d = measureTime {
            for ((text, user, avatar) in results) {
                User128AvatarText.draw(
                    loritta,
                    base,
                    x,
                    y,
                    user,
                    avatar,
                    text,
                    if (i == 4) Color.RED else Color.WHITE
                )

                x += 128
                if (x > 256) {
                    x = 0
                    y += 128
                }
                i++
            }
        }
        println("Took $d!")

        return base
    }

    data class UserWithBufferedImage(
        val text: String,
        val user: User,
        val image: BufferedImage,
    )
}