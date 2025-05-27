package com.aaa.andkotlininvaders

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.util.UUID

interface RigidBodyObject {
    val collisionDetector: CollisionDetector?
    fun removeSoftBodyEntry(bullet: UUID)
    fun checkCollision(softBodyObjectData: SoftBodyObjectData)
}

class CollisionDetector(private val rigidBodyView: View) {
    var onCollisionCallBack: OnCollisionCallBack? = null
    private val softBodyPositionList: MutableList<SoftBodyObjectData> = mutableListOf()

    fun checkCollision( softBodyObjectData: SoftBodyObjectData,
                        newPositionCollected: (SoftBodyCoordinates, SoftBodyObjectData) -> Unit,) {
        softBodyPositionList.add(softBodyObjectData)

        rigidBodyView.findViewTreeLifecycleOwner()?.apply {
            lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.RESUMED) {
                softBodyPositionList.forEach { softBodyData ->
                    launch {
                        softBodyData.softBodyPosition.collect { softBodyCoordinates ->
                            newPositionCollected(softBodyCoordinates, softBodyData)
                        }
                    }
                }
            }}
        }

    }

    fun onHitRigidBody(softBodyObject: SoftBodyObjectData) {
        softBodyPositionList.forEach { softBodyObj ->
            if (softBodyObject.objectId == softBodyObj.objectId) {
                onCollisionCallBack?.onCollision(softBodyObject)
            }
        }
        removeSoftBodyEntry(softBodyObject.objectId)
    }


    fun removeSoftBodyEntry(bullet: UUID) {
        val iterator = softBodyPositionList.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            if (enemy.objectId == bullet) {
                iterator.remove()
            }
        }
    }
}
