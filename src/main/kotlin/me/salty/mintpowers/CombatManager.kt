package me.salty.mintpowers

import io.papermc.paper.ban.BanListType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

class CombatManager(private val plugin: MintPowers): Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val victim = event.entity as? Player ?: return
        val damager = event.damageSource.causingEntity as? Player ?: return

        val victimInfo = plugin.playerManager.getPlayerInfo(victim.uniqueId) ?: return
        val damagerInfo = plugin.playerManager.getPlayerInfo(damager.uniqueId) ?: return

        val damagerKarmaLoss = event.damage.toInt()

        if (victimInfo.team != KarmaTeam.VILLAIN) {
            damagerInfo.changeKarma(damager, -damagerKarmaLoss)

            damager.sendActionBar(Component.text("You have assaulted an innocent player. -$damagerKarmaLoss karma.", NamedTextColor.RED))
        }

        if (!victimInfo.isKnockedOut) {

            if (event.finalDamage >= victim.health) {

                event.isCancelled = true

                victimInfo.isKnockedOut = true
                victim.isInvulnerable = true
                victim.health = 1.0

                victim.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 700, 3))

                victim.scheduler.runDelayed(plugin, { task ->
                    victimInfo.isKnockedOut = false
                }, null, 600)

                victim.scheduler.runDelayed(plugin, { task ->
                    victim.isInvulnerable = false
                }, null, 50)

                damager.sendActionBar(Component.text("You have knocked out ${victim.name}!", NamedTextColor.RED))
            }
        }

        if (victimInfo.team == KarmaTeam.VILLAIN && damagerInfo.team == KarmaTeam.HERO && victimInfo.isKnockedOut) {

            val killerKarmaGain = victimInfo.bounty.karmaReward + victimInfo.karma / 4

            damagerInfo.changeKarma(damager, killerKarmaGain)

            damager.sendMessage("You have successfully neutralized a villain! You have been awarded $killerKarmaGain!")

            val banList = Bukkit.getServer().getBanList(BanListType.PROFILE)

            val profile = victim.playerProfile

            if (victimInfo.bounty.order == BountyOrder.CAPTURE) {
                event.isCancelled = true

                val banDuration = Duration.ofHours(1)

                banList.addBan(profile, "You have been captured. Come back in an hour.", banDuration, "Server")

                victim.kick(Component.text("You have been captured and are currently being relocated. Standby."))
            }

            else if (victimInfo.bounty.order == BountyOrder.KILL) {
                val banDuration = Duration.ofHours(2)

                banList.addBan(profile, "Your villainous deeds have caught up to you. Sit and reflect for a few hours.", banDuration, "Server")

                victim.kick(Component.text("You have been killed."))
            }
        }

    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val killer = event.damageSource.causingEntity

        if (killer !is Player) return

        val playerInfo = plugin.playerManager.getPlayerInfo(event.player.uniqueId)
        val killerInfo = plugin.playerManager.getPlayerInfo(killer.uniqueId)

        playerInfo?.isKnockedOut = false

        if (event.damageSource.causingEntity is Player) {
            playerInfo?.lives -= 1

            var killerKarmaLoss = 50

            if (playerInfo?.team == KarmaTeam.HERO) {
                killerKarmaLoss = 100
            }

            killerInfo?.changeKarma(killer, -killerKarmaLoss)

            killer.sendActionBar(Component.text("You have killed a ${playerInfo?.team} player. -$killerKarmaLoss karma.", NamedTextColor.RED))
        }

        if ((playerInfo?.lives ?: 0) <= 0) {
            val banList = Bukkit.getServer().getBanList(BanListType.PROFILE)

            val profile = event.player.playerProfile
            val banDuration = Duration.ofHours(1).plusMinutes(30)

            playerInfo?.lives = 3

            banList.addBan(profile, "You ran out of lives. Check back in an hour or two.", banDuration, "Server")

            event.player.kick(Component.text("You ran out of lives and have been temporarily banned."))
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val playerInfo = plugin.playerManager.getPlayerInfo(event.player.uniqueId)

        event.player.sendActionBar(Component.text("You have died. (Current Lives: ${playerInfo?.lives})", NamedTextColor.RED))
    }
}