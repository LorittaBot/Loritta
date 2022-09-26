package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile

import dev.kord.common.entity.TextInputStyle
import io.github.netvl.ecoji.Ecoji
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.modals.CinnamonModalExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.modals.CinnamonModalExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.modals.ModalContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.ProfileCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.getOrCreateUserProfile
import net.perfectdreams.loritta.cinnamon.emotes.Emotes

class ChangeAboutMeModalExecutor(loritta: LorittaCinnamon) : CinnamonModalExecutor(loritta) {
    companion object : CinnamonModalExecutorDeclaration(ComponentExecutorIds.CHANGE_ABOUT_ME_MODAL_SUBMIT_EXECUTOR) {
        object Options : ModalComponents() {
            val aboutMe = textInput("about_me", TextInputStyle.Paragraph)
        }

        override val options = Options
    }

    override suspend fun onSubmit(context: ModalContext, args: ModalArguments) {
        val newAboutMe = args[options.aboutMe]

        val data = loritta.decodeDataFromComponentOnDatabase<ChangeAboutMeModalData>(context.data)

        val userSettings = context.loritta.pudding.users.getOrCreateUserProfile(context.user)
            .getProfileSettings()

        userSettings.setAboutMe(newAboutMe)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(ProfileCommand.ABOUT_ME_I18N_PREFIX.SuccessfullyChanged(newAboutMe)),
                Emotes.Tada
            )
        }

        val guild = context.interaKTionsModalContext.discordInteraction.guildId.value?.let { loritta.kord.getGuild(it) }

        val result = loritta.profileDesignManager.createProfile(
            loritta,
            context.i18nContext,
            context.user,
            context.user,
            guild
        )

        val message = ProfileExecutor.createMessage(loritta, context.i18nContext, context.user, context.user, result)

        loritta.rest.interaction.modifyInteractionResponse(
            loritta.interaKTions.applicationId,
            data.data!!.interactionToken,
            InteractionOrFollowupMessageModifyBuilder().apply {
                message()
            }.toInteractionMessageResponseModifyBuilder()
                .toRequest()
        )
    }
}