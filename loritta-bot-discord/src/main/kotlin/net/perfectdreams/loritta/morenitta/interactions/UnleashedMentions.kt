package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor

/**
 * Mentions related to a context
 */
class UnleashedMentions(
    users: List<User>
) {
    val users: List<User>
        get() = mutableUsers

    private val mutableUsers = users.toMutableList()

    /**
     * Injects a new user into the [users]' mentions list
     *
     * Used in [LorittaLegacyMessageCommandExecutor.convertToInteractionsArguments] methods, to inject mentions into the mentions list if needed
     */
    fun injectUser(user: User) {
        mutableUsers.add(user)
    }
}