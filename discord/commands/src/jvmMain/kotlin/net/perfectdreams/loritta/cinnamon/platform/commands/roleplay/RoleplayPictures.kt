package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

object RoleplayPictures {
    /* val hugPictures = pictures {
        picture("/assets/img/hug/male-x-female/hug-1.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-2.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-3.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-4.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-5.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-6.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-7.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-8.gif", MaleXFemaleGenderMatchType)

        picture("/assets/img/hug/male-x-male/hug-1.gif", MaleXMaleGenderMatchType)
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
    object BothMatchType : MatchType()
    sealed class GenderMatchType(val gender1: Gender, val gender2: Gender) : MatchType()
    object MaleXMaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.MALE)
    object FemaleXFemaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.FEMALE)
    // Yes, they are the same thing but opposite, because the user that initiated the action also matters
    object MaleXFemaleGenderMatchType : GenderMatchType(Gender.MALE, Gender.FEMALE)
    object FemaleXMaleGenderMatchType : GenderMatchType(Gender.FEMALE, Gender.MALE) */
}