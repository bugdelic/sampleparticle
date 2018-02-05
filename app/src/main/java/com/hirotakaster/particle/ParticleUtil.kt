package com.hirotakaster.particle

import android.graphics.Color
import java.util.Random

/**
 * Created by niisato on 2017/10/25.
 */

class ParticleUtil {
    companion object {
        val random = Random()
        fun reset(particle: Particle) {
            particle.pos.x = nextFloat(0.0f, particle.width.toFloat())
            particle.pos.y = nextFloat(0.0f, particle.height.toFloat())

            particle.vel.x = nextFloat(-3.9f, 3.9f)
            particle.vel.y = nextFloat(-3.9f, 3.9f)

            particle.frc = Vector2D(0f, 0f)
            particle.scale = nextFloat(0.5f, 1.0f)
            particle.drag = nextFloat(0.90f, 0.998f);

            particle.color = Color.argb(nextFloat(40.0f, 240.0f).toInt(),
                    nextFloat(20.0f, 245.0f).toInt(), nextFloat(20.0f, 245.0f).toInt(), nextFloat(20.0f, 245.0f).toInt())
        }

        fun nextFloat(min: Float?, max: Float?): Float {
            return min!! + random.nextFloat() * (max!! - min)
        }
    }
}
