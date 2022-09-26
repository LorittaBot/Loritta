package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SadRealityRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.Gender
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.SadRealityCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.EveryGroupHasCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.images.userAvatarCollage
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors
import java.awt.Color

class SadRealityExecutor(loritta: LorittaCinnamon, val client: GabrielaImageServerClient) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user1 = optionalUser("user1", SadRealityCommand.I18N_PREFIX.Options.User1.Text(SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Female))
        val user2 = optionalUser("user2", SadRealityCommand.I18N_PREFIX.Options.User2.Text(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale))
        val user3 = optionalUser("user3", SadRealityCommand.I18N_PREFIX.Options.User3.Text(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale))
        val user4 = optionalUser("user4", SadRealityCommand.I18N_PREFIX.Options.User4.Text(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale))
        val user5 = optionalUser("user5", SadRealityCommand.I18N_PREFIX.Options.User5.Text(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale))
        val user6 = optionalUser("user6", SadRealityCommand.I18N_PREFIX.Options.User6.Text(SadRealityCommand.I18N_PREFIX.Slot.You.Male))
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
                styled(context.i18nContext.get(SadRealityCommand.I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

                if (noPermissionToQuery) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                } else if (context !is GuildApplicationCommandContext) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                }
            }
        }

        val profileSettings = loritta.services.users.getProfileSettingsOfUsers(
            listOfUsers.map { net.perfectdreams.loritta.cinnamon.discord.utils.UserId(it.id) }
        )

        val lovedGender = profileSettings[listOfUsers[0].id.toLong()]?.gender ?: Gender.FEMALE // The default is FEMALE for the loved gender
        val theFatherGender = profileSettings[listOfUsers[1].id.toLong()]?.gender ?: Gender.UNKNOWN
        val theBrotherGender = profileSettings[listOfUsers[2].id.toLong()]?.gender ?: Gender.UNKNOWN
        val theFirstLoverGender = profileSettings[listOfUsers[3].id.toLong()]?.gender ?: Gender.UNKNOWN
        val theBestFriendGender = profileSettings[listOfUsers[4].id.toLong()]?.gender ?: Gender.UNKNOWN
        val youGender = profileSettings[listOfUsers[5].id.toLong()]?.gender ?: Gender.UNKNOWN

        // This is more complicated than the others userAvatarCollage's uses because it depends on the "lovedGender" value
        val result = userAvatarCollage(3, 2) {
            if (listOfUsers[0].id == applicationId) {
                // Easter Egg: Loritta
                localizedSlot(context.i18nContext, listOfUsers[0], LorittaColors.LorittaAqua.toJavaColor(), SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Loritta)
            } else {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[0],
                    Color.WHITE,
                    lovedGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Male,
                    SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Female
                )
            }

            if (lovedGender == Gender.FEMALE) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    theFatherGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFather.Female.LovedGenderFemale
                )
            } else {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    theFatherGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderMale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFather.Female.LovedGenderMale
                )
            }

            if (lovedGender == Gender.FEMALE) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    theBrotherGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Female.LovedGenderFemale
                )
            } else {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    theBrotherGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderMale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Female.LovedGenderMale
                )
            }

            if (lovedGender == Gender.FEMALE) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    theFirstLoverGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderFemale
                )
            } else {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    theFirstLoverGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderMale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderMale
                )
            }

            if (lovedGender == Gender.FEMALE) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.WHITE,
                    theBestFriendGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderFemale
                )
            } else {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.WHITE,
                    theBestFriendGender,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderMale,
                    SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderMale
                )
            }

            localizedGenderedSlot(
                context.i18nContext,
                listOfUsers[5],
                Color.WHITE,
                youGender,
                SadRealityCommand.I18N_PREFIX.Slot.You.Male,
                SadRealityCommand.I18N_PREFIX.Slot.You.Female
            )
        }.generate(loritta)

        context.sendMessage {
            addFile("sad_reality.png", result.toByteArray(ImageFormatType.PNG).inputStream())
        }
    }
}