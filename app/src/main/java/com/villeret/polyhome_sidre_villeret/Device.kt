package com.villeret.polyhome_sidre_villeret

data class Device(
    val id: String,
    val type: String,
    val availableCommands: List<String>,
    val opening: Int? = null,
    val power: Int? = null
)

data class DevicesContainer(
    val devices: List<Device>
)
