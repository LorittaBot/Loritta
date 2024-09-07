package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.UserUtils
import net.perfectdreams.loritta.morenitta.utils.images.userAvatarCollage
import net.perfectdreams.loritta.serializable.UserId
import java.awt.Color
import java.util.*

class SadRealityCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Sadreality
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("0342dec6-209d-4435-9186-0e8503b84810")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = SadRealityExecutor()
    }

    inner class SadRealityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.TheGuyYouLike.Female))
            val user2 = optionalUser("user2", I18N_PREFIX.Options.User2.Text(I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale))
            val user3 = optionalUser("user3", I18N_PREFIX.Options.User3.Text(I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale))
            val user4 = optionalUser("user4", I18N_PREFIX.Options.User4.Text(I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale))
            val user5 = optionalUser("user5", I18N_PREFIX.Options.User5.Text(I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale))
            val user6 = optionalUser("user6", I18N_PREFIX.Options.User6.Text(I18N_PREFIX.Slot.You.Male))
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user1FromArguments = args[options.user1]
            val user2FromArguments = args[options.user2]
            val user3FromArguments = args[options.user3]
            val user4FromArguments = args[options.user4]
            val user5FromArguments = args[options.user5]
            val user6FromArguments = args[options.user6]

            val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
                context,
                listOf(
                    user1FromArguments?.user,
                    user2FromArguments?.user,
                    user3FromArguments?.user,
                    user4FromArguments?.user,
                    user5FromArguments?.user,
                    user6FromArguments?.user
                )
            )

            // Not enough users!
            if (!successfullyFilled) {
                context.fail(false) {
                    styled(context.i18nContext.get(I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

                    if (noPermissionToQuery) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersPermissionsTip), Emotes.LoriReading)
                    } else if (context.guildOrNull == null) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.UsersFill.NotEnoughUsersGuildTip), Emotes.LoriReading)
                    }
                }
            }

            val profileSettings = context.loritta.pudding.users.getProfileSettingsOfUsers(
                listOfUsers.map { UserId(it.idLong) }
            )

            val lovedGender = profileSettings[listOfUsers[0].id.toLong()]?.gender ?: Gender.FEMALE // The default is FEMALE for the loved gender
            val theFatherGender = profileSettings[listOfUsers[1].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theBrotherGender = profileSettings[listOfUsers[2].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theFirstLoverGender = profileSettings[listOfUsers[3].id.toLong()]?.gender ?: Gender.UNKNOWN
            val theBestFriendGender = profileSettings[listOfUsers[4].id.toLong()]?.gender ?: Gender.UNKNOWN
            val youGender = profileSettings[listOfUsers[5].id.toLong()]?.gender ?: Gender.UNKNOWN

            // This is more complicated than the others userAvatarCollage's uses because it depends on the "lovedGender" value
            val result = userAvatarCollage(3, 2) {
                if (listOfUsers[0].idLong == context.loritta.config.loritta.discord.applicationId.toLong()) {
                    // Easter Egg: Loritta
                    localizedSlot(
                        context.i18nContext,
                        listOfUsers[0],
                        Color.WHITE,
                        I18N_PREFIX.Slot.TheGuyYouLike.Loritta
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[0],
                        Color.WHITE,
                        lovedGender,
                        I18N_PREFIX.Slot.TheGuyYouLike.Male,
                        I18N_PREFIX.Slot.TheGuyYouLike.Female
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[1],
                        Color.WHITE,
                        theFatherGender,
                        I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale,
                        I18N_PREFIX.Slot.TheFather.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[1],
                        Color.WHITE,
                        theFatherGender,
                        I18N_PREFIX.Slot.TheFather.Male.LovedGenderMale,
                        I18N_PREFIX.Slot.TheFather.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[2],
                        Color.WHITE,
                        theBrotherGender,
                        I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale,
                        I18N_PREFIX.Slot.TheBrother.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[2],
                        Color.WHITE,
                        theBrotherGender,
                        I18N_PREFIX.Slot.TheBrother.Male.LovedGenderMale,
                        I18N_PREFIX.Slot.TheBrother.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[3],
                        Color.WHITE,
                        theFirstLoverGender,
                        I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale,
                        I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[3],
                        Color.WHITE,
                        theFirstLoverGender,
                        I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderMale,
                        I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderMale
                    )
                }

                if (lovedGender == Gender.FEMALE) {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[4],
                        Color.WHITE,
                        theBestFriendGender,
                        I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale,
                        I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderFemale
                    )
                } else {
                    localizedGenderedSlot(
                        context.i18nContext,
                        listOfUsers[4],
                        Color.WHITE,
                        theBestFriendGender,
                        I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderMale,
                        I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderMale
                    )
                }

                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    youGender,
                    I18N_PREFIX.Slot.You.Male,
                    I18N_PREFIX.Slot.You.Female
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "sad_reality.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember1 = context.getUserAndMember(0)
            val userAndMember2 = context.getUserAndMember(1)
            val userAndMember3 = context.getUserAndMember(2)
            val userAndMember4 = context.getUserAndMember(3)
            val userAndMember5 = context.getUserAndMember(4)
            val userAndMember6 = context.getUserAndMember(5)

            return mapOf(
                options.user1 to userAndMember1,
                options.user2 to userAndMember2,
                options.user3 to userAndMember3,
                options.user4 to userAndMember4,
                options.user5 to userAndMember5,
                options.user6 to userAndMember6,
            )
        }
    }
}