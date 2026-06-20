package me.salty.mintpowers.powers.streettier

import me.salty.mintpowers.MintPowers
import me.salty.mintpowers.powers.AbstractPower
import me.salty.mintpowers.powers.PowerLogic

class Courier(plugin: MintPowers) : AbstractPower(plugin) {

    override val id: String = "courier"
    override val name: String = "Courier"
    override val description: String = "Delivery, any time, anywhere."

    override fun provideLogic(): PowerLogic {
        return PowerLogic()
    }

}