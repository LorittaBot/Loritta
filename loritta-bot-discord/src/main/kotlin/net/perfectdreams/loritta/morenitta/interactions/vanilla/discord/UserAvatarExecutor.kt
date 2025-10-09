package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.utils.NotableUserIds
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.utils.extensions.await

object UserAvatarExecutor {
    fun createAvatarMessage(
        context: UnleashedContext,
        userAndMember: UserAndMember,
        avatarTarget: AvatarTarget
    ): InlineMessage<*>.() -> (Unit) = {
        val user = userAndMember.user
        val member = userAndMember.member
        val userId = user.idLong

        embed {
            title = "\uD83D\uDDBC ${userAndMember.user.name}"

            // Specific User Avatar Easter Egg Texts
            val easterEggFooterTextKey = when (userId) {
                // Easter Egg: Looking up yourself
                context.user.idLong -> UserCommand.I18N_PREFIX.Avatar.YourselfEasterEgg

                // Easter Egg: Loritta/Application ID
                // TODO: Show who made the fan art during the Fan Art Extravaganza
                context.loritta.config.loritta.discord.applicationId.toLong() -> UserCommand.I18N_PREFIX.Avatar.LorittaEasterEgg

                // Easter Egg: Pantufa
                NotableUserIds.PANTUFA.toLong() -> UserCommand.I18N_PREFIX.Avatar.PantufaEasterEgg

                // Easter Egg: Gabriela
                NotableUserIds.GABRIELA.toLong() -> UserCommand.I18N_PREFIX.Avatar.GabrielaEasterEgg

                // Easter Egg: Carl-bot
                NotableUserIds.CARLBOT.toLong() -> UserCommand.I18N_PREFIX.Avatar.CarlbotEasterEgg

                // Easter Egg: Dank Memer
                NotableUserIds.DANK_MEMER.toLong() -> UserCommand.I18N_PREFIX.Avatar.DankMemerEasterEgg

                // Easter Egg: Mantaro
                NotableUserIds.MANTARO.toLong() -> UserCommand.I18N_PREFIX.Avatar.MantaroEasterEgg

                // Easter Egg: Erisly
                NotableUserIds.ERISLY.toLong() -> UserCommand.I18N_PREFIX.Avatar.ErislyEasterEgg

                // Easter Egg: Kuraminha
                NotableUserIds.KURAMINHA.toLong() -> UserCommand.I18N_PREFIX.Avatar.KuraminhaEasterEgg

                // Easter Egg: Foxy
                NotableUserIds.FOXY.toLong() -> UserCommand.I18N_PREFIX.Avatar.FoxyEasterEgg

                // Nothing else, just use null
                else -> null
            }

            // If the text is present, set it as the footer!
            if (easterEggFooterTextKey != null)
                footer(context.i18nContext.get(easterEggFooterTextKey))

            color = LorittaColors.DiscordBlurple.rgb

            // This should NEVER be null at this point!
            val imageUrl = when (avatarTarget) {
                AvatarTarget.GLOBAL_AVATAR -> userAndMember.user.avatar?.getUrl(2048) ?: userAndMember.user.defaultAvatar.url
                AvatarTarget.GUILD_AVATAR -> userAndMember.member?.avatar?.getUrl(2048) ?: userAndMember.user.defaultAvatar.url
            }

            image = imageUrl

            val components = mutableListOf(
                // "Open Avatar in Browser" button
                Button.link(
                    imageUrl,
                    context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.OpenAvatarInBrowser)
                )
            )

            if (avatarTarget == AvatarTarget.GUILD_AVATAR) {
                components.add(
                    context.loritta.interactivityManager
                        .buttonForUser(
                            context.user.idLong,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGlobalAvatar)
                        ) {
                            it.event.editMessage(
                                MessageEdit {
                                    apply(createAvatarMessage(context, userAndMember, AvatarTarget.GLOBAL_AVATAR))
                                }
                            ).await()
                        }
                )
            } else {
                if (member?.avatarUrl != null)
                    components.add(
                        context.loritta.interactivityManager
                            .buttonForUser(
                                context.user.idLong,
                                context.alwaysEphemeral,
                                ButtonStyle.PRIMARY,
                                context.i18nContext.get(UserCommand.I18N_PREFIX.Avatar.ViewUserGuildProfileAvatar)
                            ) {
                                it.event.editMessage(
                                    MessageEdit {
                                        apply(createAvatarMessage(context, userAndMember, AvatarTarget.GUILD_AVATAR))
                                    }
                                ).await()
                            }
                    )
            }

            actionRow(
                *components.toTypedArray()
            )
        }
    }

    enum class AvatarTarget {
        GLOBAL_AVATAR,
        GUILD_AVATAR
    }
}