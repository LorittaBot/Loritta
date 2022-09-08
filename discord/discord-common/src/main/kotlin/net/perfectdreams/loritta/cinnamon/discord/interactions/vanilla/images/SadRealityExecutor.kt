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
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

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

        val sadRealityUsers = listOfUsers.map {
            SadRealityUser(
                it.id,
                "${it.username}#${it.discriminator}",
                it.effectiveAvatar.cdnUrl.toUrl {
                    this.size = Image.Size.Size128
                    this.format = Image.Format.PNG
                }
            )
        }

        val user1 = sadRealityUsers[0]
        val user2 = sadRealityUsers[1]
        val user3 = sadRealityUsers[2]
        val user4 = sadRealityUsers[3]
        val user5 = sadRealityUsers[4]
        val user6 = sadRealityUsers[5]

        val user1ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user1.id.value))
            ?.getProfileSettings()
        val user2ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user2.id.value))
            ?.getProfileSettings()
        val user3ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user3.id.value))
            ?.getProfileSettings()
        val user4ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user4.id.value))
            ?.getProfileSettings()
        val user5ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user5.id.value))
            ?.getProfileSettings()
        val user6ProfileSettings = context.loritta.services.users.getUserProfile(UserId(user6.id.value))
            ?.getProfileSettings()

        val lovedGender = user1ProfileSettings?.gender ?: Gender.UNKNOWN
        val theFatherGender = user2ProfileSettings?.gender ?: Gender.UNKNOWN
        val theBrotherGender = user3ProfileSettings?.gender ?: Gender.UNKNOWN
        val theFirstLoverGender = user4ProfileSettings?.gender ?: Gender.UNKNOWN
        val theBestFriendGender = user5ProfileSettings?.gender ?: Gender.UNKNOWN
        val youGender = user6ProfileSettings?.gender ?: Gender.UNKNOWN

        val result = client.images.sadReality(
            SadRealityRequest(
                SadRealityRequest.SadRealityUser(
                    if (user1.id == applicationId) {
                        context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Loritta)
                    } else {
                        when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Male)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Female)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheGuyYouLike.Female)
                        }
                    },
                    user1.tag,
                    URLImageData(user1.avatarUrl)
                ),
                SadRealityRequest.SadRealityUser(
                    when (theFatherGender) {
                        Gender.MALE, Gender.UNKNOWN -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Male.LovedGenderFemale)
                        }
                        Gender.FEMALE -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Female.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Female.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFather.Female.LovedGenderFemale)
                        }
                    },
                    user2.tag,
                    URLImageData(user2.avatarUrl),
                ),
                SadRealityRequest.SadRealityUser(
                    when (theBrotherGender) {
                        Gender.MALE, Gender.UNKNOWN -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Male.LovedGenderFemale)
                        }
                        Gender.FEMALE -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Female.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Female.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBrother.Female.LovedGenderFemale)
                        }
                    },
                    user3.tag,
                    URLImageData(user3.avatarUrl),
                ),
                SadRealityRequest.SadRealityUser(
                    when (theFirstLoverGender) {
                        Gender.MALE, Gender.UNKNOWN -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Male.LovedGenderFemale)
                        }
                        Gender.FEMALE -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheFirstLover.Female.LovedGenderFemale)
                        }
                    },
                    user4.tag,
                    URLImageData(user4.avatarUrl),
                ),
                SadRealityRequest.SadRealityUser(
                    when (theBestFriendGender) {
                        Gender.MALE, Gender.UNKNOWN -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Male.LovedGenderFemale)
                        }
                        Gender.FEMALE -> when (lovedGender) {
                            Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderMale)
                            Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderFemale)
                            Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.TheBestFriend.Female.LovedGenderFemale)
                        }
                    },
                    user5.tag,
                    URLImageData(user5.avatarUrl),
                ),
                SadRealityRequest.SadRealityUser(
                    when (youGender) {
                        Gender.MALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.You.Male)
                        Gender.FEMALE -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.You.Female)
                        Gender.UNKNOWN -> context.i18nContext.get(SadRealityCommand.I18N_PREFIX.Slot.You.Male)
                    },
                    user6.tag,
                    URLImageData(user6.avatarUrl),
                )
            )
        )

        context.sendMessage {
            addFile("sad_reality.png", result.inputStream())
        }
    }

    data class SadRealityUser(
        val id: Snowflake,
        val tag: String,
        val avatarUrl: String
    )
}