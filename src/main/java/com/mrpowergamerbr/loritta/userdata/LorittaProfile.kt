package com.mrpowergamerbr.loritta.userdata

import lombok.ToString
import lombok.experimental.Accessors
import org.mongodb.morphia.annotations.Entity

/**
 * Perfil de um usuário que usa a Loritta
 */
@Entity(value = "servers")
class LorittaProfile(val userId: String) {
    var profileBackground: String? = null // Background do +perfil, base64
}
