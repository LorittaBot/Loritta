package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id
import org.mongodb.morphia.annotations.Indexed

/**
 * Perfil de um usuário que usa a Loritta
 */
@Entity(value = "users", noClassnameStored = true)
class LorittaProfile {
    constructor()

    constructor(userId: String) : this() {
        this.userId = userId;
    }

    @Id
    @Indexed
    var userId: String? = null;
    var xp: Int = 0 // XP do usuário
    var aboutMe: String = "A Loritta é minha amiga!";
    var isBanned = false;
    var banReason: String? = null;
    var reminders: MutableList<Reminder> = arrayListOf();
    var receivedReputations: MutableList<String> = ArrayList<String>(); // Nós salvamos os usuários que deram reputação em vez de só salvar um número
    var lastReputationGiven: Long = 0;
    var lastMessageSent: Long = 0; // Última vez que o usuário enviou uma mensagem
	var lastMessageSentHash: Int = 0; // HashCode da última mensagem enviada
    var usernameChanges: MutableList<UsernameChange> = arrayListOf()
    var dreams: Int = 0

    fun getCurrentLevel(): XpWrapper {
        var lvl = 1;
        var currentXp = xp;
        var expToAdvance = getExpToAdvanceFrom(lvl);
        while (currentXp > expToAdvance) {
            currentXp -= expToAdvance;
            lvl++;
            expToAdvance = getExpToAdvanceFrom(lvl);
        }
        return XpWrapper(lvl, currentXp);
    }

    fun getExpToAdvanceFrom(lvl: Int): Int {
        return 325 + lvl * (25 + lvl)
    }

    fun getReputation(): Int {
        return receivedReputations.size;
    }

    data class XpWrapper(val currentLevel: Int, val expLeft: Int)

    data class UsernameChange(val changedAt: Long = 0L, val username: String = "???", val discriminator: String = "0000") {
	}
}