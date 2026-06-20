package me.salty.mintpowers.powers

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import me.salty.mintpowers.MintPowers
import me.salty.mintpowers.PlayerInfo
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import java.util.UUID

data class PowerData (
    val id: String,
    val name: String,
    val description: String,
    val metadata: PowerMetadata,
    val logic: PowerLogic
)

data class PowerMetadata (
    var cooldowns: HashMap<String, Cooldown> = hashMapOf(),
    var toggles: HashMap<String, Boolean> = hashMapOf(),
    var counters: HashMap<String, Int> = hashMapOf(),
) {
    
    inline fun <reified T> setPlayerData(playerUUID: UUID, key: String, value: T) {
        val scopedKey = "$playerUUID:$key"

        when (T::class) {
            Cooldown::class -> cooldowns[scopedKey] = value as Cooldown
            Boolean::class -> toggles[scopedKey] = value as Boolean
            Int::class -> counters[scopedKey] = value as Int
            else -> throw IllegalArgumentException("Unsupported metadata type: ${T::class.simpleName}")
        }

    }

    inline fun <reified T> getPlayerData(playerUUID: UUID, key: String, defaultValue: T): T {
        val scopedKey = "$playerUUID:$key"

        return when (T::class) {
            Cooldown::class -> cooldowns.computeIfAbsent(scopedKey) { defaultValue as Cooldown } as T
            Boolean::class -> toggles.computeIfAbsent(scopedKey) { defaultValue as Boolean } as T
            Int::class -> counters.computeIfAbsent(scopedKey) {defaultValue as Int} as T
            else -> throw IllegalArgumentException("Unsupported metadata type: ${T::class.simpleName}")
        }
    }


}

data class Cooldown (
    var isOn: Boolean,
    var currentTicks: Long,
    val totalTicks: Long
) {

    fun start(player: Player, cooldownMessage: Pair<String, NamedTextColor>, plugin: MintPowers) {
        if (this.isOn) return

        this.show(player, Pair("=", cooldownMessage.second), plugin)

        this.isOn = true

        player.scheduler.runDelayed(plugin, {
            this.isOn = false
            player.sendActionBar(Component.text(cooldownMessage.first, cooldownMessage.second))
        }, null, this.totalTicks)

    }

    fun show(player: Player, barLook: Pair<String, NamedTextColor>, plugin: MintPowers) {
        if (this.isOn) return

        val cooldownBar = mutableListOf("")

        for (i in 0..this.totalTicks / 20) {
            cooldownBar.add("-")
        }

        player.scheduler.runAtFixedRate(plugin, { task ->
            this.currentTicks += 20

            for ((index, item) in cooldownBar.withIndex()) {
                if (item == "-") {
                    cooldownBar[index] = "="
                    break
                }

            }

            player.sendActionBar(Component.text(cooldownBar.joinToString(prefix = "[", postfix = "]", separator = ""), barLook.second))

            if (this.currentTicks >= this.totalTicks) {
                this.currentTicks = 0

                task.cancel()
            }

        },null, 1, 20)

    }

}

data class PowerEvent<T : Event> (
    val original: T,
    val causerInfo: PlayerInfo,
    val power: PowerData
)

data class PowerLogic (
    val onDamageTaken: ((PowerEvent<EntityDamageEvent>) -> Unit)? = null,
    val onPlayerMove: ((PowerEvent<PlayerMoveEvent>) -> Unit)? = null,
    val onPlayerAttack: ((PowerEvent<EntityDamageByEntityEvent>) -> Unit)? = null,
    val onPlayerHit: ((PowerEvent<EntityDamageByEntityEvent>) -> Unit)? = null,
    val onFoodItemConsumed: ((PowerEvent<PlayerItemConsumeEvent>) -> Unit)? = null,
    val onPlayerJump : ((PowerEvent<PlayerJumpEvent>) -> Unit)? = null,
    val onPlayerSneak: ((PowerEvent<PlayerToggleSneakEvent>) -> Unit)? = null,
    val onPlayerPostRespawn : ((PowerEvent<PlayerPostRespawnEvent>) -> Unit)? = null,
    val onPlayerJoin : ((PowerEvent<PlayerJoinEvent>) -> Unit)? = null,
    val onPlayerToggleGlide : ((PowerEvent<EntityToggleGlideEvent>) -> Unit)? = null,
    val onPlayerSwapHands: ((PowerEvent<PlayerSwapHandItemsEvent>) -> Unit)? = null,
    val onPlayerAnimation : ((PowerEvent<PlayerAnimationEvent>) -> Unit)? = null,
    val onPlayerHeldItem : ((PowerEvent<PlayerItemHeldEvent>) -> Unit)? = null,
    val onPlayerDamageBlock : ((PowerEvent<BlockDamageEvent>) -> Unit)? = null,
)


