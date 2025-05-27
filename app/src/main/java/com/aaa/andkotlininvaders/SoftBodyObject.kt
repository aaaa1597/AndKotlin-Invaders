package com.aaa.andkotlininvaders

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

abstract class SoftBodyObject {
}

interface OnCollisionCallBack {
    fun onCollision(softBodyObject: SoftBodyObjectData)
}

data class SoftBodyCoordinates(val x: Float, val y: Float)

data class SoftBodyObjectData(
    val objectId: UUID,
    val softBodyPosition: MutableStateFlow<SoftBodyCoordinates>,
//    val sender: BulletView.Sender,
    val objectType: SoftBodyObjectType,
)

sealed class SoftBodyObjectType {
    object BULLET : SoftBodyObjectType()
    data class DROP(val dropType: DropType) : SoftBodyObjectType()
}

sealed class DropType {
    data class Ammo(val ammoCount: Int) : DropType()
}
