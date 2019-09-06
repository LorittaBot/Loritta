package net.perfectdreams.loritta.plugin.profiles

import com.mrpowergamerbr.loritta.plugin.LorittaPlugin
import com.mrpowergamerbr.loritta.profile.ProfileDesign
import net.perfectdreams.loritta.plugin.profiles.designs.CowboyProfileCreator
import net.perfectdreams.loritta.plugin.profiles.designs.DebugProfileCreator
import net.perfectdreams.loritta.plugin.profiles.designs.MonicaAtaProfileCreator

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
                        MonicaAtaProfileCreator::class.java,
                        "monica_ata",
                        10000.0,
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
