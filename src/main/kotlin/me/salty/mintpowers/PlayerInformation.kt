package me.salty.mintpowers

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.checkerframework.checker.units.qual.s

@Serializable
data class PlayerInfo (
    var lives: Int = 3,
    var karma: Int = 0,
    var team: KarmaTeam = KarmaTeam.CIVILIAN,
    var group: String = "" ,
    var bounty: Bounty = Bounty(),
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
            this.bounty = Bounty()
            messageColor = NamedTextColor.GOLD
        }
        else if (karma <= -100) {
            this.team = KarmaTeam.VILLAIN

            if (karma <= -1000) {
                this.bounty = Bounty(BountyOrder.KILL, this.karma)
            }
            else {
                this.bounty = Bounty(BountyOrder.CAPTURE, this.karma / 2)
            }

            messageColor = NamedTextColor.RED
        }
        else {
            this.team = KarmaTeam.CIVILIAN
            this.bounty = Bounty()
        }

        player.sendActionBar(Component.text("Your karma has changed. You are a $team.", messageColor))
    }
}

@Serializable
data class Bounty (
    val order: BountyOrder = BountyOrder.NONE,
    val karmaReward: Int = 0
)

data class Group (
    val id: String = "",
    val name: String = "",
    val teamRestriction: KarmaTeam = KarmaTeam.CIVILIAN,
    val karmaFreedom: Boolean = false,
    val teamMembers: HashSet<String> = hashSetOf(), //Store UUIDS as strings
    val waypoints: HashSet<SerializableVector> = hashSetOf(),
)

data class SerializableVector(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0)

enum class BountyOrder {
    NONE,
    CAPTURE,
    KILL
}

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