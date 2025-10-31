package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.social.ProfileCommand.Companion.createMessage
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import java.util.*

class ProfileViewUserCommand(val loritta: LorittaBot) : UserCommandDeclarationWrapper {
    override fun command() = userCommand(I18nKeysData.Commands.Command.Profileview.ViewUserProfile, CommandCategory.DISCORD, UUID.fromString("5b2666e5-981c-49ef-977a-0d1416e2ddfc"), ProfileViewUserExecutor(loritta)) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    class ProfileViewUserExecutor(val loritta: LorittaBot) : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            val userToBeViewed = user

            if (AccountUtils.checkAndSendMessageIfUserIsBanned(loritta, context, userToBeViewed))
                return

            context.deferChannelMessage(true)

            val guild = context.guildOrNull

            val userProfile = loritta.getOrCreateLorittaProfile(userToBeViewed.id.toLong())
            val profileSettings = loritta.newSuspendedTransaction { userProfile.settings }

            val profileCreator = loritta.profileDesignManager.designs.firstOrNull {
                it.internalName == (profileSettings.activeProfileDesignInternalName?.value ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)
            } ?: loritta.profileDesignManager.defaultProfileDesign

            val result = loritta.profileDesignManager.createProfile(
                loritta,
                context.i18nContext,
                context.locale,
                loritta.profileDesignManager.transformUserToProfileUserInfoData(context.user),
                loritta.profileDesignManager.transformUserToProfileUserInfoData(userToBeViewed),
                guild?.let { loritta.profileDesignManager.transformGuildToProfileGuildInfoData(it) },
                profileCreator
            )

            val message = createMessage(
                loritta,
                context,
                context.i18nContext,
                context.user,
                userToBeViewed,
                profileCreator,
                result
            )

            context.reply(false) {
                message()
            }
        }
    }
}