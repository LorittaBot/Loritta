package com.mrpowergamerbr.loritta.userdata

import com.mrpowergamerbr.loritta.profile.ProfileType
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import java.util.*

/**
 * Perfil de um usuário que usa a Loritta
 */
class LorittaProfile @BsonCreator constructor(
        @BsonProperty("_id")
        @get:[BsonIgnore]
        val userId: String
) {
    var xp: Long = 0 // XP do usuário
    var aboutMe: String = "A Loritta é minha amiga!" // TODO: null
    var isBanned = false;
    var banReason: String? = null;
    var reminders: MutableList<Reminder> = arrayListOf();
    var receivedReputations: MutableList<String> = ArrayList<String>(); // Nós salvamos os usuários que deram reputação em vez de só salvar um número
    var lastReputationGiven: Long = 0;
    var lastMessageSent: Long = 0; // Última vez que o usuário enviou uma mensagem
	var lastMessageSentHash: Int = 0; // HashCode da última mensagem enviada
    var usernameChanges: MutableList<UsernameChange> = arrayListOf()
    var spinnerScores = mutableListOf<SpinnerScore>()
    var dreams: Double = 0.0
    var hidePreviousUsernames: Boolean = false
    var hideSharedServers: Boolean = false
    var isAfk = false
    var afkReason: String? = null
    var usedEmotes = mutableMapOf<String, Int>()
    var receivedDailyAt = 0L
    var ip: String? = null
    var activeDesign = ProfileType.DEFAULT
	var designsBought = mutableListOf<ProfileType>()
	var editedShipEffects = mutableListOf<ShipEffect>()

	var isDonator = false
	var donatorPaid = 0.0
	var donatedAt = 0L
	var donationExpiresIn = 0L

    @BsonIgnore
    fun getCurrentLevel(): LorittaProfile.XpWrapper {
        return LorittaProfile.XpWrapper((xp / 1000).toInt(), xp)
    }

    @BsonIgnore
    fun getExpToAdvanceFrom(lvl: Int): Int {
        return lvl * 1000
    }

    @BsonIgnore
    fun getReputation(): Int {
        return receivedReputations.size;
    }

    class XpWrapper @BsonCreator constructor(@BsonProperty("currentLevel") val currentLevel: Int, @BsonProperty("expLeft") val expLeft: Long)

    class UsernameChange @BsonCreator constructor(@BsonProperty("changedAt") val changedAt: Long = 0L, @BsonProperty("username") val username: String, @BsonProperty("discriminator") val discriminator: String)

    class SpinnerScore @BsonCreator constructor(@BsonProperty("emoji") val emoji: String, @BsonProperty("forTime") val forTime: Long)

	class ShipEffect @BsonCreator constructor(@BsonProperty("userId") val userId: String, @BsonProperty("editedTo") val editedTo: Int, @BsonProperty("createdAt") val createdAt: Long)
}