package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureMemberCounterRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/member-counter", "member_counter", "configure_member_counter.html")