package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class AutoroleConfig {
	var isEnabled: Boolean = false
	var roles: MutableList<String> = mutableListOf<String>()
	var rolesVoteRewards = mutableListOf<RoleVoteReward>()

	class RoleVoteReward @BsonCreator constructor(
			@BsonProperty("voteCount")
			var voteCount: Int,
			@BsonProperty("roles")
			var roles: MutableList<String>
	)
}