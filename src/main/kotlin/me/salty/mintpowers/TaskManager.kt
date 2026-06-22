package me.salty.mintpowers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class TaskManage(private val plugin: MintPowers) {

    fun Player.runPersistentDelayedTask(taskKey: String, ticks: Long, block: (Player) -> Unit) {
        var expiry = System.currentTimeMillis() + (ticks * 50L)

        val key = NamespacedKey(plugin, "task_$taskKey")

        if (!this.persistentDataContainer.has(key)) {
            this.persistentDataContainer.set(key, PersistentDataType.LONG, expiry)
        }
        else {
            expiry = this.persistentDataContainer.get(key, PersistentDataType.LONG) ?: expiry
        }

        this.scheduler.runDelayed(plugin, { task ->
            if (this.isOnline) {
                block(this)
            }
        }, null, ticks)
    }

}