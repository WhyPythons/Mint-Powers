package me.salty.mintpowers

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

@Serializable
data class PlayerInfo (
    var lives: Int = 3,
    var karma: Int = 0,
    var team: KarmaTeam = KarmaTeam.CIVILIAN,
    var group: String = "" ,
    var isKnockedOut: Boolean = false,
    val powers: HashSet<String> = hashSetOf()
) {

    fun changeKarma(player: Player, karma: Int) {
        this.karma += karma
        validateTeam(player, this.karma)
    }

    fun updateKarma(player: Player, karma: Int) {
        this.karma = karma
        validateTeam(player, this.karma)
    }

    fun validateTeam(player: Player, karma: Int) {
        var messageColor = NamedTextColor.WHITE

        if (karma >= 100) {
            this.team = KarmaTeam.HERO
            messageColor = NamedTextColor.GOLD
        }
        else if (karma <= -100) {
            this.team = KarmaTeam.VILLAIN
            messageColor = NamedTextColor.RED
        }
        else {
            this.team = KarmaTeam.CIVILIAN
        }

        player.sendActionBar(Component.text("Your karma has changed. You are a $team.", messageColor))
    }
}

@Serializable
data class Group (
    val id: String = "",
    val name: String = "",
    val teamRestriction: KarmaTeam = KarmaTeam.CIVILIAN,
    val karmaFreedom: Boolean = false,
    val teamMembers: HashSet<String> = hashSetOf(), //Store UUIDS as strings
    val waypoints: HashSet<SerializableVector> = hashSetOf(),
)

@Serializable
data class SerializableVector(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0)

enum class KarmaTeam {
    CIVILIAN,
    VILLAIN,
    HERO;

    fun isEnemy(otherPlayerTeam: KarmaTeam): Boolean {
        return if (this == HERO && otherPlayerTeam == VILLAIN) {
            true
        } else if (this == VILLAIN && otherPlayerTeam == HERO ) {
            true
        } else {
            false
        }
    }
}