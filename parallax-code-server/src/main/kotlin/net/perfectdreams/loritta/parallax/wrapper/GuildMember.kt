package net.perfectdreams.loritta.parallax.wrapper

class GuildMember(
		val nickname: String,
		val joinedAt: Long?,
		val premiumSince: Long?,
		val roleIds: Array<Long>,
		val user: User
) {
	lateinit var guild: Guild

	val id: Long
		get() = user.id

	val roles: List<Role>
		get() = guild.roles.filter {
			it.id in roleIds
		}.toMutableList()

	fun addRole(role: Role) = guild.addRoleToMember(this, role)
	fun removeRole(role: Role) = guild.removeRoleFromMember(this, role)
}