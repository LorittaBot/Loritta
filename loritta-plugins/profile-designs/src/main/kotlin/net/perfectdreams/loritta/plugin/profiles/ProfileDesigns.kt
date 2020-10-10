package net.perfectdreams.loritta.plugin.profiles

import com.mrpowergamerbr.loritta.profile.ProfileCreator
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.profiles.designs.*

class ProfileDesigns(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    val registeredProfiles = mutableListOf<ProfileCreator>()

    override fun onEnable() {
        registeredProfiles.add(DebugProfileCreator())
        registeredProfiles.add(DefaultProfileCreator())
        registeredProfiles.add(MSNProfileCreator())
        registeredProfiles.add(OrkutProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainWhiteProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainOrangeProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainPurpleProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainAquaProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainGreenProfileCreator())
        registeredProfiles.add(PlainProfileCreator.PlainGreenHeartsProfileCreator())
        registeredProfiles.add(CowboyProfileCreator())
        registeredProfiles.add(NextGenProfileCreator())
        registeredProfiles.add(MonicaAtaProfileCreator())
        registeredProfiles.add(UndertaleProfileCreator())
        registeredProfiles.add(LoriAtaProfileCreator())
        registeredProfiles.add(Halloween2019ProfileCreator())
        registeredProfiles.add(Christmas2019ProfileCreator())
        registeredProfiles.add(LorittaChristmas2019ProfileCreator())

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
