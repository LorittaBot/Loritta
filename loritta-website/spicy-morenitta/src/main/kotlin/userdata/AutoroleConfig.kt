package userdata

class AutoroleConfig {
	var isEnabled: Boolean = false
	var roles = arrayOf<String>()
	var giveRolesAfter: Long? = null
	var rolesVoteRewards = arrayOf<RoleVoteReward>()

	class RoleVoteReward constructor(
			var voteCount: Int,
			var roles: Array<String>
	)
}