package com.hirotakaster.particle

import android.graphics.Color
import java.util.*

/**
 * Created by niisato on 2017/10/21.
 */

class Particle {

    internal var pos: Vector2D = Vector2D(0f, 0f)
    internal var vel: Vector2D = Vector2D(0f, 0f)
    internal var frc: Vector2D = Vector2D(0f, 0f)
    internal var drag: Float = 0.toFloat()
    internal var scale: Float = 0.toFloat()
    internal var height: Float = 0f
    internal var width: Float = 0f
    internal var color: Int = Color.GREEN
    internal var effect: Int = 0

    fun onUpdate(mousePosition: Vector<Vector2D>) {
        if (mousePosition.size == 1) {
            if (effect == 0) {
                var attractPt = Vector2D(mousePosition[0].x, mousePosition[0].y)
                frc = attractPt - pos
                frc.normalize()
                vel.mul(drag)
                vel += frc.mul(0.6f)

            } else if (effect == 1) {
                var attractPt = Vector2D(mousePosition[0].x, mousePosition[0].y)
                frc = attractPt - pos
                var dist: Float = frc.length()
                frc.normalize()
                vel.mul(drag)

                if( dist < 150 ){
                    vel = vel + frc.mul(-0.6f)
                }else{
                    frc.x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                    frc.y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                    vel += frc.mul(0.4f)
                }
            }

        } else if (mousePosition.size > 1) {
            if (effect == 0) {
                var closest: Int = -1;
                var closestDist: Int = 99999999

                for (i in 0..(mousePosition.size - 1)) {
                    var lenSq: Float = mousePosition[i].absolute(pos)
                    if (lenSq < closestDist) {
                        closestDist = lenSq.toInt()
                        closest = i
                    }
                }

                if (closest != -1) {
                    var closestPt = Vector2D(mousePosition[closest].x, mousePosition[closest].y)
                    var dist: Float = Math.sqrt(closestDist.toDouble()).toFloat()

                    frc = closestPt - pos
                    vel = vel.mul(drag)

                    if (dist < 300 && dist > 40) {
                        vel += frc.mul(0.03f)
                    } else if (dist <= 10) {
                        vel += frc.mul(-3.0f)
                    } else {
                        frc.x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        frc.y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        vel += frc.mul(0.4f)
                    }
                }
            } else if (effect == 1) {
                var closest: Int = -1;
                var closestDist: Int = 99999999

                for (i in 0..(mousePosition.size - 1)) {
                    var lenSq: Float = mousePosition[i].absolute(pos)
                    if (lenSq < closestDist) {
                        closestDist = lenSq.toInt()
                        closest = i
                    }
                }

                if (closest != -1) {
                    var closestPt = Vector2D(mousePosition[closest].x, mousePosition[closest].y)
                    var dist: Float = Math.sqrt(closestDist.toDouble()).toFloat()

                    frc = closestPt - pos
                    vel = vel.mul(drag)

                    if (dist < 150) {
                        vel += frc.mul(-0.006f)
                    } else {
                        frc.x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        frc.y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        vel += frc.mul(0.4f)
                    }
                }
            }
        }

        //2 - UPDATE OUR POSITION
        pos += vel

        //3 - (optional) LIMIT THE PARTICLES TO STAY ON SCREEN
        if( pos.x > width){
            pos.x = width
            vel.x *= -1.0.toFloat()
        }else if( pos.x < 0 ){
            pos.x = 0.toFloat();
            vel.x *= -1.0f
        }
        if( pos.y > height ){
            pos.y = height
            vel.y *= -1.0f
        }
        else if( pos.y < 0.0f ){
            pos.y = 0.toFloat()
            vel.y *= -1.0.toFloat()
        }
    }
}
