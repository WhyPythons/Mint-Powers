package me.salty.mintpowers.powers.lowtier

import me.salty.mintpowers.MintPowers
import me.salty.mintpowers.powers.AbstractPower
import me.salty.mintpowers.powers.Cooldown
import me.salty.mintpowers.powers.PowerLogic
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.inventory.ItemStack

class Unnoticed(plugin: MintPowers) : AbstractPower(plugin) {

    override val id: String = "unnoticed"
    override val name: String = "Unnoticed"
    override val description: String = "Placeholder desc"

    override fun provideLogic(): PowerLogic {
        return PowerLogic(

            onPlayerSwapHands = { event ->

                val player = event.original.player

                val invisCooldown = event.power.metadata.getPlayerData(
                    player.uniqueId, "invis_cooldown",
                    Cooldown(false, 0, 3000)
                )

                val abilitySlot = player.inventory.heldItemSlot

                if (abilitySlot == 0 && !invisCooldown.isOn) {

                    event.original.isCancelled = true

                    event.original.player.addPotionEffect(
                        PotionEffect(PotionEffectType.INVISIBILITY, 150, 1, true, false)
                    )

                    var currentHelmet = player.equipment.helmet
                    var currentChestplate = player.equipment.chestplate
                    var currentLeggings = player.equipment.leggings
                    var currentBoots = player.equipment.boots

                    if (player.equipment.helmet.getType().getKey().Astring().startsWith("diamond_"))




                    player.equipment.setHelmet(null,false)
                    player.equipment.setChestplate(null,false)
                    player.equipment.setLeggings(null,false)
                    player.equipment.setBoots(null,false)




                    invisCooldown.isOn = true

                    player.scheduler.runDelayed(plugin, { task ->
                        invisCooldown.isOn = false
                    }, null, invisCooldown.totalTicks)


                }

            },

            onPlayerHit = { event ->

                val target = event.original.entity as? LivingEntity ?: return@PowerLogic
                val targetXDirection = event.original.entity.facing.direction.x //grabs attackers X direction
                val targetZDirection = event.original.entity.facing.direction.z //grabs attackers Z direction

                target.knockback(1.0,(targetXDirection*-1),(targetZDirection*-1))
            },

















}