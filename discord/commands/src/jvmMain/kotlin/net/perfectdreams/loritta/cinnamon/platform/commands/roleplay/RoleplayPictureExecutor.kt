package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand
import net.perfectdreams.loritta.cinnamon.platform.utils.getOrCreateUserProfile

abstract class RoleplayPictureExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(RoleplayHugExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val user = user("user", AnagramCommand.I18N_PREFIX.Options.Text)
                .register()
        }

        override val options = Options
    }

    abstract val pictures: RoleplayPicturesBuilder

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val receiver = args[options.user]

        val gender1 = context.loritta.services.users.getOrCreateUserProfile(context.user).getProfileSettings().gender
        val gender2 = context.loritta.services.users.getOrCreateUserProfile(receiver).getProfileSettings().gender

        // Time to filter the pictures!
        val filteredPictures = pictures.pictures
            .filter {
                // TODO: Decrease the chance of selecting a "generic" picture if both users have their genders set
                (it.matchType is GenderMatchType && (it.matchType.gender1 == gender1 || it.matchType.gender2 == gender2)) || (it.matchType is GenericMatchType || it.matchType is BothMatchType)
            }

        // If the filteredPictures list is empty, select the full list
        val pictures = filteredPictures.ifEmpty { pictures.pictures }

        val picture = pictures.random()

        context.sendMessage {
            embed {
                description = "owo"

                image = "URL: ${context.loritta.config.website} ${picture.path.removePrefix("/")}"
            }
        }
    }

    fun pictures(group: RoleplayPicturesBuilder.() -> (Unit)): RoleplayPicturesBuilder {
        return RoleplayPicturesBuilder().apply(group)
    }

    class RoleplayPicturesBuilder {
        val pictures = mutableListOf<RoleplayPictureBuilder>()

        fun picture(path: String, matchType: MatchType, builder: RoleplayPictureBuilder.() -> (Unit) = {}) {
            pictures.add(RoleplayPictureBuilder(path, matchType).apply(builder))
        }
    }

    class RoleplayPictureBuilder(val path: String, val matchType: MatchType)

    sealed class MatchType
    object GenericMatchType : MatchType()
    object BothMatchType : MatchType()
    sealed class GenderMatchType(val gender1: Gender, val gender2: Gender) : MatchType()
    object MaleXMaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.MALE)
    object FemaleXFemaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.FEMALE)
    // Yes, they are the same thing but opposite, because the user that initiated the action also matters
    object MaleXFemaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.FEMALE)
    object FemaleXMaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.MALE)
}