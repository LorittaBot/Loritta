package com.mrpowergamerbr.loritta.threads

class MutedUsersThread : Thread("Muted Users Thread") {
	override fun run() {
		while (true) {
			try {
				checkMuteStatus()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000)
		}
	}

	fun checkMuteStatus() {
		/* run {
			// MUTE
			val guilds = loritta.mongo.getDatabase("loritta")
					.getCollection("servers")
					.aggregate(listOf(
							Aggregates.match(Filters.exists("userData")),
							Aggregates.project(
									Projections.include("_id", "userData")
							),
							Document("\$unwind", "\$userData")
							// Aggregates.sort(Document("spinnerScores.forTime", -1))
							// limit(5)
					)
					)

			println("SIZE: ${guilds.count()}")
			println(guilds.first())
			for (guild in guilds) {
				val toBeUnmuted = guild.userData.filter { it.value.isMuted && it.value.temporaryMute && System.currentTimeMillis() > it.value.expiresIn }

				if (toBeUnmuted.isEmpty())
					continue

				val _guild = LORITTA_SHARDS.getGuildById(guild.guildId)

				if (_guild == null)
					continue

				if (!_guild.selfMember.hasPermission(Permission.MANAGE_ROLES))
					continue

				for ((key, userData) in toBeUnmuted) {
					try {
						userData.isMuted = false
						userData.temporaryMute = false

						val member = _guild.getMemberById(key)

						if (member == null)
							continue

						var mutedRoles = _guild.getRolesByName(loritta.getLocaleById(guild.localeId).MUTE_ROLE_NAME, false)

						if (mutedRoles.isEmpty())
							continue

						val role = mutedRoles.first()

						if (!_guild.selfMember.canInteract(role))
							continue

						println("Removing roles from member...")
						_guild.controller.removeRolesFromMember(member, role).complete()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
				loritta save guild
			}
		}

		run {
			// TEMP BANS
			val guilds = loritta.ds.find(ServerConfig::class.java).field("temporaryBans").exists()

			println("TEMP BAN SIZE: " + guilds.count())
			for (guild in guilds) {
				val _guild = LORITTA_SHARDS.getGuildById(guild.guildId) ?: continue

				if (!_guild.selfMember.hasPermission(Permission.BAN_MEMBERS))
					continue

				val temporaryBans = guild.temporaryBans
				val toRemove = mutableListOf<String>()

				for ((id, time) in temporaryBans) {
					try {
						if (System.currentTimeMillis() > time) {
							toRemove.add(id)

							_guild.getMemberById(id) ?: continue

							_guild.controller.unban(id).complete()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				toRemove.forEach {
					temporaryBans.remove(it)
				}

				loritta save guild
			}
		} */
	}
}