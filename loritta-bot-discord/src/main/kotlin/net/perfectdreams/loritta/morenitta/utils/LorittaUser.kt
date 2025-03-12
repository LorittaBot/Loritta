package net.perfectdreams.loritta.morenitta.utils

import com.google.common.collect.Sets
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser.Companion.convertRolePermissionsMapToMemberPermissionList
import net.perfectdreams.loritta.morenitta.utils.LorittaUser.Companion.loadMemberLorittaPermissions
import net.perfectdreams.loritta.morenitta.utils.LorittaUser.Companion.loadMemberRolesLorittaPermissions
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

/**
 * Um usuário que está comunicando com a Loritta
 */
open class LorittaUser(val loritta: LorittaBot, val user: User, val permissions: EnumSet<LorittaPermission>, val _profile: Profile?) {
	companion object {
		/**
		 * Loads the guild's roles Loritta Permissions
		 *
		 * @return a map containing all the loritta permissions of the roles in [guild]
		 *
		 * @see convertRolePermissionsMapToMemberPermissionList
		 * @see loadMemberLorittaPermissions
		 */
		suspend fun loadGuildRolesLorittaPermissions(loritta: LorittaBot, serverConfig: ServerConfig, guild: Guild) = loritta.newSuspendedTransaction {
			val permissions = ServerRolePermissions.selectAll().where {
				ServerRolePermissions.guild eq serverConfig.id
			}

			permissions
					.asSequence()
					.map { it[ServerRolePermissions.roleId] to it[ServerRolePermissions.permission] }
					.groupBy { it.first }
					.map { it.key to it.value.map { it.second }}
					.map { it.first to Sets.newEnumSet(it.second, LorittaPermission::class.java) }
					.toMap()
		}

		/**
		 * Loads the member's roles Loritta Permissions from the guild they are in
		 *
		 * @return a map containing all the loritta permissions the user has
		 *
		 * @see convertRolePermissionsMapToMemberPermissionList
		 * @see loadMemberLorittaPermissions
		 */
		suspend fun loadMemberRolesLorittaPermissions(loritta: LorittaBot, serverConfig: ServerConfig, member: Member) = loritta.newSuspendedTransaction {
			_loadMemberRolesLorittaPermissions(serverConfig, member)
		}

		private fun _loadMemberRolesLorittaPermissions(serverConfig: ServerConfig, member: Member): Map<Long, EnumSet<LorittaPermission>> {
			val permissions = ServerRolePermissions.selectAll().where {
				ServerRolePermissions.guild eq serverConfig.id and (ServerRolePermissions.roleId inList member.roles.map { it.idLong })
			}

			return permissions
					.asSequence()
					.map { it[ServerRolePermissions.roleId] to it[ServerRolePermissions.permission] }
					.groupBy { it.first }
					.map { it.key to it.value.map { it.second }}
					.map { it.first to Sets.newEnumSet(it.second, LorittaPermission::class.java) }
					.toMap()
		}

		/**
		 * Converts a map retrieved by [loadMemberRolesLorittaPermissions] to a EnumSet containing only the valid permissions
		 * for the [member]
		 *
		 * This is similar to [loadMemberRolesLorittaPermissions], but uses your own map instead of retrieving from the database,
		 * this should be used to avoid unnecessary database retrievals
		 *
		 * @return the converted [EnumSet] containing all the valid loritta permissions for the user
		 *
		 * @see loadMemberRolesLorittaPermissions
		 * @see loadMemberLorittaPermissions
		 */
		fun convertRolePermissionsMapToMemberPermissionList(member: Member, rolePermissions: Map<Long, EnumSet<LorittaPermission>>): EnumSet<LorittaPermission> {
			val enumSet = EnumSet.noneOf(LorittaPermission::class.java)

			val roles = member.roles.toMutableList()

			roles.add(member.guild.publicRole)

			roles.sortByDescending { it.position }

			enumSet.addAll(
					// We need to filter the permissions to only add the permissions from the roles that the user *does* have.
					// loadMemberRolesLorittaPermissions(...) does filter that for us, but loadGuildRolesLorittaPermissions(...) doesn't!
					rolePermissions.filterKeys { roleId ->
						roles.any { it.idLong == roleId }
					}.values.flatten()
			)

			// The "IGNORE_COMMANDS" permission check is kinda... tricky
			// We check if all the roles the user has the IGNORE_COMMANDS, if any of them doesn't have it, we ignore it.
			if (enumSet.contains(LorittaPermission.IGNORE_COMMANDS)) {
				// We are only going to check if the set already contains the IGNORE_COMMANDS permission,
				// no need to check if the user doesn't have it, right?
				for (role in roles) {
					val permissions = rolePermissions[role.idLong]

					if (permissions == null || !permissions.contains(LorittaPermission.IGNORE_COMMANDS)) {
						enumSet.remove(LorittaPermission.IGNORE_COMMANDS)
						break
					}
				}
			}

			return enumSet
		}

		/**
		 * Loads the user's Loritta Permissions from the guild they are in
		 *
		 * @return a set containing all the loritta permissions the user has
		 *
		 * @see convertRolePermissionsMapToMemberPermissionList
		 * @see loadMemberRolesLorittaPermissions
		 */
		suspend fun loadMemberLorittaPermissions(loritta: LorittaBot, serverConfig: ServerConfig, member: Member) = convertRolePermissionsMapToMemberPermissionList(
				member,
				loadMemberRolesLorittaPermissions(loritta, serverConfig, member)
		)
	}

	val asMention: String
		get() = getAsMention(false)
	val profile by lazy { _profile ?: runBlocking { loritta.getOrCreateLorittaProfile(user.idLong) } }

	fun getAsMention(addSpace: Boolean): String {
		return user.asMention + (if (addSpace) " " else "")
	}

	open fun hasPermission(lorittaPermission: LorittaPermission): Boolean = permissions.contains(lorittaPermission)

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	open fun canUseCommand(context: CommandContext): Boolean {
		// A coisa mais importante a se verificar é se o comando só pode ser executado pelo dono (para não causar problemas)
		if (context.cmd.onlyOwner && !loritta.isOwner(context.userHandle.id))
			return false

		if (!context.cmd.canHandle(context))
			return false

		return true
	}
}

/**
 * Um usuário que está comunicando com a Loritta em canais de texto
 */
class GuildLorittaUser(loritta: LorittaBot, val member: Member, permissions: EnumSet<LorittaPermission>, _profile: Profile?) : LorittaUser(loritta, member.user, permissions, _profile) {
	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	override fun canUseCommand(context: CommandContext): Boolean {
		if (!super.canUseCommand(context))
			return false

		// E, finalmente, iremos verificar as permissões do usuário
		if (member.hasPermission((context.event.channel as GuildChannel), context.cmd.getDiscordPermissions())) {
			return true
		}

		return false
	}
}