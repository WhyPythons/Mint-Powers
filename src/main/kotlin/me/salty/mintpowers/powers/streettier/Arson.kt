package me.salty.mintpowers.powers.streettier

import me.salty.mintpowers.MintPowers
import me.salty.mintpowers.powers.PowerLogic
import me.salty.mintpowers.powers.AbstractPower
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockType
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity

class Arson(plugin: MintPowers) : AbstractPower(plugin) {

    override val id: String = "arson"
    override val name: String = "Arson"
    override val description: String = "Flames engulf you, and the infants you worship."

    override fun provideLogic(): PowerLogic {
        return PowerLogic(

            onPlayerAttack = { event ->
                val player = event.original.entity as? LivingEntity ?: return@PowerLogic

                player.fireTicks = 100
            },

            onPlayerHit = { event ->
                val attacker = event.original.damager as? LivingEntity ?: return@PowerLogic

                attacker.fireTicks = 80
            },

            onPlayerDamageBlock = { event ->

                val blockAbove = event.original.block.getRelative(BlockFace.UP)

                if (blockAbove.type == Material.AIR) {
                    blockAbove.setType(Material.FIRE, false)
                }

            },

            onDamageTaken = { event ->

                if (event.original.damageSource.damageType == DamageType.IN_FIRE || event.original.damageSource.damageType == DamageType.ON_FIRE ) {
                    event.original.isCancelled = true
                }

            },

            onPlayerMove = { event ->

                val blockBelow = event.original.player.location.block

                if (blockBelow == BlockType.AIR) {
                    blockBelow.type = Material.FIRE
                }

            }

        )
    }
}
