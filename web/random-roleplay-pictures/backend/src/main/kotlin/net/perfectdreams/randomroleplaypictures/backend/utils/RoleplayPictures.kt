package net.perfectdreams.randomroleplaypictures.backend.utils

import net.perfectdreams.loritta.cinnamon.common.utils.Gender

object RoleplayPictures {
    val hugPictures = pictures {
        picture("/assets/img/hug/male_x_male/hug_0.gif", MaleXMaleGenderMatchType)
    }

    private fun pictures(group: RoleplayPicturesBuilder.() -> (Unit)): RoleplayPicturesBuilder {
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
    sealed class GenderMatchType(val gender1: Gender, val gender2: Gender) : MatchType()
    object MaleXMaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.MALE)
    object FemaleXFemaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.FEMALE)
    // Yes, they are the same thing but opposite, because the user that initiated the action also matters
    object MaleXFemaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.FEMALE)
    object FemaleXMaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.MALE)
}