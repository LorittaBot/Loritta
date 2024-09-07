package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
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
import java.awt.Color
import java.util.*

class ThanksFriendsCommand : SlashCommandDeclarationWrapper  {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Thanksfriends
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("a55edb85-0877-4e8a-8970-79eb73aa71a4")) {
        enableLegacyMessageSupport = true
        examples = I18N_PREFIX.Examples
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = SadRealityExecutor()
    }

    inner class SadRealityExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val user1 = optionalUser("user1", I18N_PREFIX.Options.User1.Text(I18N_PREFIX.Slot.Thanks))
            val user2 = optionalUser("user2", I18N_PREFIX.Options.User2.Text(I18N_PREFIX.Slot.For))
            val user3 = optionalUser("user3", I18N_PREFIX.Options.User3.Text(I18N_PREFIX.Slot.Being))
            val user4 = optionalUser("user4", I18N_PREFIX.Options.User4.Text(I18N_PREFIX.Slot.The))
            val user5 = optionalUser("user5", I18N_PREFIX.Options.User5.Text(I18N_PREFIX.Slot.NotYou))
            val user6 = optionalUser("user6", I18N_PREFIX.Options.User6.Text(I18N_PREFIX.Slot.Best))
            val user7 = optionalUser("user7", I18N_PREFIX.Options.User7.Text(I18N_PREFIX.Slot.Friends))
            val user8 = optionalUser("user8", I18N_PREFIX.Options.User8.Text(I18N_PREFIX.Slot.Of))
            val user9 = optionalUser("user9", I18N_PREFIX.Options.User9.Text(I18N_PREFIX.Slot.All))
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
            val user7FromArguments = args[options.user7]
            val user8FromArguments = args[options.user8]
            val user9FromArguments = args[options.user9]

            val (listOfUsers, successfullyFilled, noPermissionToQuery) = UserUtils.fillUsersFromRecentMessages(
                context,
                listOf(
                    user1FromArguments?.user,
                    user2FromArguments?.user,
                    user3FromArguments?.user,
                    user4FromArguments?.user,
                    user5FromArguments?.user,
                    user6FromArguments?.user,
                    user7FromArguments?.user,
                    user8FromArguments?.user,
                    user9FromArguments?.user
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

            val result = userAvatarCollage(3, 3) {
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[0],
                    Color.WHITE,
                    I18N_PREFIX.Slot.Thanks
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[1],
                    Color.WHITE,
                    I18N_PREFIX.Slot.For
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[2],
                    Color.WHITE,
                    I18N_PREFIX.Slot.Being
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[3],
                    Color.WHITE,
                    I18N_PREFIX.Slot.The
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[4],
                    Color.RED,
                    I18N_PREFIX.Slot.NotYou
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[5],
                    Color.WHITE,
                    I18N_PREFIX.Slot.Best
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[6],
                    Color.WHITE,
                    I18N_PREFIX.Slot.Friends
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[7],
                    Color.WHITE,
                    I18N_PREFIX.Slot.Of
                )
                localizedSlot(
                    context.i18nContext,
                    listOfUsers[8],
                    Color.WHITE,
                    I18N_PREFIX.Slot.All
                )
            }.generate(context.loritta)

            context.reply(false) {
                files += AttachedFile.fromData(result.toByteArray(ImageFormatType.PNG).inputStream(), "thanks_friends.png")
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