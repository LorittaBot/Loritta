package net.perfectdreams.loritta.plugin.profiles

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.profile.ProfileDesign
import net.perfectdreams.loritta.plugin.profiles.designs.*

class ProfileDesigns : LorittaPlugin() {
    val registeredProfiles = mutableListOf<ProfileDesign>()

    override fun onEnable() {
        registeredProfiles.add(
                ProfileDesign(
                        false,
                        DebugProfileCreator::class.java,
                        "debug",
                        0.0,
                        listOf()
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainWhiteProfileCreator::class.java,
                        "plain_white",
                        2500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainOrangeProfileCreator::class.java,
                        "plain_orange",
                        2500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainPurpleProfileCreator::class.java,
                        "plain_purple",
                        2500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainAquaProfileCreator::class.java,
                        "plain_aqua",
                        2500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        PlainProfileCreator.PlainGreenProfileCreator::class.java,
                        "plain_green",
                        2500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        CowboyProfileCreator::class.java,
                        "cowboy",
                        3500.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        NextGenProfileCreator::class.java,
                        "next_gen",
                        3500.0,
                        listOf(361977144445763585L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        MonicaAtaProfileCreator::class.java,
                        "monica_ata",
                        10000.0,
                        listOf(321821618177310721L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        UndertaleProfileCreator::class.java,
                        "undertale",
                        12500.0,
                        listOf(419125869475397633L)
                )
        )
        registeredProfiles.add(
                ProfileDesign(
                        true,
                        LoriAtaProfileCreator::class.java,
                        "lori_ata",
                        15000.0,
                        listOf(321821618177310721L, 419125869475397633L)
                )
        )

        registeredProfiles.add(
                ProfileDesign(true, Halloween2019ProfileCreator::class.java, "halloween_2019", -1.0, listOf())
        )

        loritta.profileDesignManager.designs.removeAll(loritta.profileDesignManager.designs.filter { it.internalType == "halloween_2019" })

        registeredProfiles.forEach {
            loritta.profileDesignManager.registerDesign(it)
        }
    }

    override fun onDisable() {
        super.onDisable()

        registeredProfiles.forEach {
            loritta.profileDesignManager.unregisterDesign(it)
        }
    }
}
