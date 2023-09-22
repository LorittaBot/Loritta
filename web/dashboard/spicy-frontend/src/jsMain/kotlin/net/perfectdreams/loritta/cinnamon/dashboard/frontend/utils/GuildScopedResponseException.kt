package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

class GuildScopedResponseException(val type: GuildScopedErrorType) : RuntimeException() {
    sealed class GuildScopedErrorType {
        data object InvalidDiscordAuthorization : GuildScopedErrorType()
        data object MissingPermission : GuildScopedErrorType()
        data object UnknownGuild : GuildScopedErrorType()
        data object UnknownMember : GuildScopedErrorType()
    }
}