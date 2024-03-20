package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.UserUtils
import net.perfectdreams.loritta.morenitta.utils.images.userAvatarCollage
import net.perfectdreams.loritta.serializable.UserId
import java.awt.Color

class EveryGroupHasCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Everygrouphas
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        enableLegacyMessageSupport = true
        examples = I18N_PREFIX.Examples
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        executor = SadRealityExecutor()
    }

    inner class SadRealityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Popular.Male))
            val user2 = optionalUser("user2", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Quiet.Male))
            val user3 = optionalUser("user3", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Clown.Male))
            val user4 = optionalUser("user4", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Nerd.Male))
            val user5 = optionalUser("user5", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Fanboy.Male))
            val user6 = optionalUser("user6", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Cranky.Male))
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
                    styled(context.i18nContext.get(SadRealityCommand.I18N_PREFIX.NotEnoughUsers), Emotes.LoriSob)

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

            val result = userAvatarCollage(3, 2) {
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[0],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Popular.Male,
                    I18N_PREFIX.Slot.Popular.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Quiet.Male,
                    I18N_PREFIX.Slot.Quiet.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Clown.Male,
                    I18N_PREFIX.Slot.Clown.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Nerd.Male,
                    I18N_PREFIX.Slot.Nerd.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Fanboy.Male,
                    I18N_PREFIX.Slot.Fanboy.Female
                )
                localizedGenderedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    profileSettings,
                    I18N_PREFIX.Slot.Cranky.Male,
                    I18N_PREFIX.Slot.Cranky.Female
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "every_group_has.png")
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