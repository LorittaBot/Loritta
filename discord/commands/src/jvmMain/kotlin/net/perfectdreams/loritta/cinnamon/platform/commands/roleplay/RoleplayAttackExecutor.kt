package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.declarations.RoleplayCommand
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayAttackExecutor(
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    client,
    RoleplayUtils.ATTACK_ATTRIBUTES
) {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val user = user("user", RoleplayCommand.I18N_PREFIX.Attack.Options.User.Text)
                .register()
        }

        override val options = Options
    }

    override val userOption: CommandOption<User> = Options.user
}