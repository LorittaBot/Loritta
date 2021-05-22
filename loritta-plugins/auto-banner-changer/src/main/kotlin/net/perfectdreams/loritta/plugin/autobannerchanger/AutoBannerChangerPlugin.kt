package net.perfectdreams.loritta.plugin.autobannerchanger

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import java.io.File

class AutoBannerChangerPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    var bannerChanger: BannerChanger? = null

    override fun onEnable() {
        super.onEnable()

        val config = Constants.HOCON.decodeFromFile<AutoBannerChangerConfig>(File(dataFolder, "config.conf"))
        if (config.enabled) {
            bannerChanger = BannerChanger(loritta as Loritta, this, config).also {
                launch(it.start())
            }
        }

        loriToolsExecutors.add(
                object: LoriToolsCommand.LoriToolsExecutor {
                    override val args = "banner cycle"

                    override fun executes(): suspend CommandContext.() -> Boolean = task@{
                        if (this.args.getOrNull(0) != "banner")
                            return@task false
                        if (this.args.getOrNull(1) != "cycle")
                            return@task false

                        if (bannerChanger == null) {
                            reply(
                                    LorittaReply(
                                            "Banner Changer está desativado!"
                                    )
                            )
                            return@task true
                        }
                        bannerChanger?.changeBanner()
                        return@task true
                    }
                }
        )
    }
}
