package com.hirotakaster.particle.views

import android.content.Context
import android.util.AttributeSet
import android.view.View

import com.hirotakaster.particle.Vector2D

import java.util.Vector
import java.util.concurrent.locks.Lock

/**
 * Created by niisato on 2017/10/29.
 */

interface BaseView {
    fun init(partcile: Int, effect: Int, alpha: Int)
    fun reset()
    fun getlock(): Lock

    fun mouseclear()
    fun getmousepoint(): Vector<Vector2D>
    fun mouseadd(position: Vector2D)
    fun getval(): Triple<Int, Int, Int>
}
