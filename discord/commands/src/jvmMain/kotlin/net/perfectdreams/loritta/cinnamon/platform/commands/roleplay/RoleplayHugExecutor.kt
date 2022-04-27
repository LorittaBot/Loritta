package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand

class RoleplayHugExecutor : RoleplayPictureExecutor() {
    companion object : SlashCommandExecutorDeclaration(RoleplayHugExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val user = user("user", AnagramCommand.I18N_PREFIX.Options.Text)
                .register()
        }

        override val options = Options
    }

    override val pictures = pictures {
        picture("/assets/img/hug/both/hug-1.gif", BothMatchType)
        picture("/assets/img/hug/both/hug-2.gif", BothMatchType)
        picture("/assets/img/hug/both/hug-3.gif", BothMatchType)
        picture("/assets/img/hug/both/hug-4.gif", BothMatchType)

        picture("/assets/img/hug/both/hug-1.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-2.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-3.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-4.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-5.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-6.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-7.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-8.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-9.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-10.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-11.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-12.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-13.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-14.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-15.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-16.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-17.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-18.gif", GenericMatchType)
        picture("/assets/img/hug/both/hug-19.gif", GenericMatchType)

        picture("/assets/img/hug/male-x-female/hug-1.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-2.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-3.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-4.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-5.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-6.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-7.gif", MaleXFemaleGenderMatchType)
        picture("/assets/img/hug/male-x-female/hug-8.gif", MaleXFemaleGenderMatchType)

        picture("/assets/img/hug/male-x-male/hug-1.gif", MaleXMaleGenderMatchType)
        picture("/assets/img/hug/male-x-male/hug-2.gif", MaleXMaleGenderMatchType)
        picture("/assets/img/hug/male-x-male/hug-3.gif", MaleXMaleGenderMatchType)

        picture("/assets/img/hug/female-x-female/hug-1.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-2.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-3.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-4.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-5.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-6.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-7.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-8.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-9.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-10.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-11.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-12.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-13.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-14.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-15.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-16.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-17.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-18.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-19.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-20.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-21.gif", FemaleXFemaleGenderMatchType)
        picture("/assets/img/hug/female-x-female/hug-22.gif", FemaleXFemaleGenderMatchType)

        picture("/assets/img/hug/female-x-male/hug-1.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-2.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-3.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-4.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-5.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-6.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-7.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-8.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-9.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-10.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-11.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-12.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-13.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-14.gif", FemaleXMaleGenderMatchType)
        picture("/assets/img/hug/female-x-male/hug-15.gif", FemaleXMaleGenderMatchType)
    }
}