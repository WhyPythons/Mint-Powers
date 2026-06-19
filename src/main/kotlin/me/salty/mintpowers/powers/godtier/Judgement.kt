package me.salty.mintpowers.powers.godtier

import me.salty.mintpowers.powers.KarmaTeam
import me.salty.mintpowers.MintPowers
import me.salty.mintpowers.powers.PowerLogic
import me.salty.mintpowers.powers.AbstractPower
import org.bukkit.entity.Player
import java.util.HashSet
import java.util.UUID

class Judgement(plugin: MintPowers) : AbstractPower(plugin) {

    override val id: String = "judgement"
    override val name: String = "Judgement"
    override val description: String = "Anybody who commits evil is doomed to fall before their sins."

    //This power currently only works for one player.
    val storedPlayers = HashMap<UUID, HashSet<String>>()

    override fun provideLogic(): PowerLogic {
        return PowerLogic(

            onPlayerAttack = { event ->

                val attacker = event.original.damager as? Player ?: return@PowerLogic
                val attackerInfo = plugin.playerManager.getPlayerInfo(attacker.uniqueId) ?: return@PowerLogic

                val victim = event.original.entity as? Player ?: return@PowerLogic
                val victimInfo = plugin.playerManager.getPlayerInfo(victim.uniqueId) ?: return@PowerLogic

                val metadata = event.power.metadata

                val attackerHasPower = plugin.playerManager.hasPower(attacker.uniqueId, id)
                val victimHasPower = plugin.playerManager.hasPower(victim.uniqueId, id)

                if (attackerHasPower) {

                    val hits = metadata.getPlayerData(attacker.uniqueId, "hits", 0) + 1
                    metadata.setPlayerData(attacker.uniqueId, "hits", hits)

                    if (event.info.team.isEnemy(victimInfo.team)) {
                        if (!victimHasPower) {
                            event.original.damage *= 2
                        }
                    }
                    else {
                        event.original.isCancelled = true
                        return@PowerLogic
                    }

                    if (hits == 1) {
                        if (event.info.team.isEnemy(victimInfo.team)) {
                            val targetPowers = victimInfo.powers.toHashSet()

                            if (targetPowers.isNotEmpty()) {
                                for (power in targetPowers) {
                                    plugin.playerManager.revokePower(victim.uniqueId, power, false)
                                }
                                storedPlayers[victim.uniqueId] = targetPowers
                            }
                        }

                        victim.scheduler.runDelayed(plugin, {
                            metadata.setPlayerData(attacker.uniqueId, "hits", 0)

                            if (victim.uniqueId in storedPlayers) {
                                if (storedPlayers[victim.uniqueId] != null) {
                                    for (power in storedPlayers[victim.uniqueId]!!) {
                                        plugin.playerManager.grantPower(victim.uniqueId, power)
                                    }
                                }
                            }

                            storedPlayers.remove(victim.uniqueId)
                        }, null, 300)

                    }
                    if (hits == 2) {
                        metadata.setPlayerData(attacker.uniqueId, "hits", 0)

                        if (storedPlayers[victim.uniqueId] != null) {
                            for (power in storedPlayers[victim.uniqueId]!!) {
                                plugin.playerManager.grantPower(victim.uniqueId, power)
                            }
                            storedPlayers.remove(victim.uniqueId)
                        }
                    }
                }

                if (victimHasPower) {
                    if (event.info.team.isEnemy(attackerInfo.team)) {
                        event.original.damage /= 2
                    }
                }

            }
        )
    }
}
