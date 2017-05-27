package com.mrpowergamerbr.loritta.userdata

import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id
import org.mongodb.morphia.annotations.Indexed

/**
 * Perfil de um usuário que usa a Loritta
 */
@Entity(value = "users") class LorittaProfile {
    constructor()

    constructor(userId: String) : this() {
        this.userId = userId;
    }

    @Id
    @Indexed
    var userId: String? = null;
    var xp: Int = 0 // XP do usuário
    var aboutMe: String = "A Loritta é minha amiga!";
    var tempoOnline: Long = 0;
    var games = HashMap<String, Long>();

    fun getCurrentLevel(): Int {
        var lvl = 1;
        var expLeft = xp;
        var expToAdvance = getExpToAdvanceFrom(lvl);
        while (expLeft > expToAdvance && expLeft > 0) {
            lvl++;
            expLeft = expLeft - expToAdvance;
            expToAdvance = getExpToAdvanceFrom(lvl);
        }
        return lvl;
    }

    fun getExpToAdvanceFrom(lvl: Int): Int {
        return 100 * lvl;
    }
}
