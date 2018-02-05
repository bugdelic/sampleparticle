package com.hirotakaster.particle

/**
 * Created by niisato on 2017/10/21.
 */

class Vector2D {
    internal var x: Float = 0.toFloat()
    internal var y: Float = 0.toFloat()

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    constructor(v: Vector2D) {
        this.x = v.x
        this.y = v.y
    }

    operator fun set(x: Float, y: Float): Vector2D {
        this.x = x
        this.y = y
        return this
    }

    operator fun minus(v: Vector2D) : Vector2D {
        return Vector2D(this.x - v.x, this.y - v.y)
    }

    operator fun plus(v: Vector2D) : Vector2D {
        return Vector2D(this.x + v.x, this.y + v.y)
    }

    operator fun times(v: Vector2D) : Vector2D {
        return Vector2D(this.x * v.x, this.y * v.y)
    }

    operator fun div(v:Vector2D) : Vector2D {
        return Vector2D(this.x/v.x, this.y/v.y)
    }

    fun length(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    fun normalize(): Vector2D {
        val len = length()
        if (len != 0f) {
            x /= len
            y /= len
        }
        return this
    }

    fun mul(scalar: Float): Vector2D {
        x *= scalar
        y *= scalar
        return this
    }

    fun absolute(v: Vector2D): Float {
        val x_d = v.x - x
        val y_d = v.y - y
        return x_d * x_d + y_d * y_d
    }
}
