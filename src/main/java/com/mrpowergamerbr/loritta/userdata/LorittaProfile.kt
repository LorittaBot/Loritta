package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty

/**
 * Perfil de um usuário que usa a Loritta
 */
class LorittaProfile @BsonCreator constructor(@BsonProperty("_id") _userId: String) {
    @BsonProperty("_id")
    val userId = _userId
    var xp: Long = 0 // XP do usuário
    var aboutMe: String = "A Loritta é minha amiga!";
    var isBanned = false;
    var banReason: String? = null;
    var reminders: MutableList<Reminder> = arrayListOf();
    var receivedReputations: MutableList<String> = ArrayList<String>(); // Nós salvamos os usuários que deram reputação em vez de só salvar um número
    var lastReputationGiven: Long = 0;
    var lastMessageSent: Long = 0; // Última vez que o usuário enviou uma mensagem
	var lastMessageSentHash: Int = 0; // HashCode da última mensagem enviada
    var usernameChanges: MutableList<UsernameChange> = arrayListOf()
    var spinnerScores = mutableListOf<SpinnerScore>()
    var dreams: Long = 0
    // var tamagotchi: TamagotchiPet? = null

	@BsonIgnore
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

	@BsonIgnore
    fun getExpToAdvanceFrom(lvl: Int): Int {
        return 325 + lvl * (25 + lvl)
    }

    fun getReputation(): Int {
        return receivedReputations.size;
    }

    class XpWrapper @BsonCreator constructor(@BsonProperty("currentLevel") val currentLevel: Int, @BsonProperty("expLeft") val expLeft: Long)

    class UsernameChange @BsonCreator constructor(@BsonProperty("changedAt") val changedAt: Long = 0L, @BsonProperty("username") val username: String, @BsonProperty("discriminator") val discriminator: String)

    class SpinnerScore @BsonCreator constructor(@BsonProperty("emoji") val emoji: String, @BsonProperty("forTime") val forTime: Long)
}