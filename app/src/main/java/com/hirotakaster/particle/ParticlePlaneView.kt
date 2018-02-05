package com.hirotakaster.particle

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.hirotakaster.particle.views.BaseView
import java.util.*
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock

/**
 * Created by niisato on 2017/10/21.
 */

class ParticlePlaneView : View, BaseView {


    internal var DEBUG_TAG: String = "Particle"
    internal var particlenum: Int = 1000
    internal var fillAlpha: Int = 64
    internal var effect: Int = 0
    internal var dispLayHeight: Float = 0.0f
    internal var displayWidth: Float = 0.0f
    internal var particles: Vector<Particle> = Vector()
    internal var mousePosition: Vector<Vector2D> = Vector()
    val lock = java.util.concurrent.locks.ReentrantLock()


    var now = System.currentTimeMillis()
    internal var offScreenBitmap: Bitmap? = null
    internal var offScreenCanvas: Canvas? = null
    internal var blackPaint = Paint()
    internal val paint = Paint()

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (context is Activity) {
            var display = context.getWindowManager().getDefaultDisplay()
            val point = Point(0, 0)
            display.getRealSize(point);
            dispLayHeight = point.y.toFloat()
            displayWidth = point.x.toFloat()
            offScreenBitmap = Bitmap.createBitmap(displayWidth.toInt(), dispLayHeight.toInt(), Bitmap.Config.ARGB_8888)
            offScreenCanvas = Canvas(offScreenBitmap)
        }
        // initialize
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {}


    override fun init(partcile: Int, effect: Int, alpha: Int) {
        this.particlenum = partcile
        this.effect = effect
        this.fillAlpha = alpha
    }

    override fun reset() {
        if (particles.size > 0)
            particles.clear()

        if (particlenum > 0) {
            for (i in 0..(particlenum - 1)) {
                particles.add(Particle())
                particles[i].height = dispLayHeight
                particles[i].width = displayWidth
                particles[i].effect = effect
                ParticleUtil.reset(particles[i])
            }
        }
        blackPaint.color = Color.argb(fillAlpha, 0, 0, 0)
        lock.withLock {
            mousePosition.clear()
        }
    }

    override fun getlock(): Lock {
        return this.lock
    }

    override fun mouseclear() {
        this.mousePosition.clear();
    }

    override  fun getmousepoint(): Vector<Vector2D> {
        return this.mousePosition
    }

    override fun mouseadd(position: Vector2D) {
        this.mousePosition.addElement(position)
    }

    override fun getval(): Triple<Int, Int, Int> {
        return Triple(particlenum, fillAlpha, effect)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var tmpmousePosition: Vector<Vector2D> = Vector()
        lock.withLock {
            for (p in mousePosition) {
                tmpmousePosition.add(Vector2D(p.x, p.y))
            }
        }

        try {
            if (canvas != null) {
                offScreenCanvas?.drawRect(0f, 0f, displayWidth.toFloat(), dispLayHeight.toFloat(), blackPaint)

                for (p in particles) {
                    paint.color = p.color
                    p.onUpdate(tmpmousePosition)
                    offScreenCanvas?.drawCircle(p.pos.x, p.pos.y, p.scale * 4.0f, paint)

                }
                canvas.drawBitmap(offScreenBitmap, 0.toFloat(), 0.toFloat(), null)
            }

        } catch (exp: Exception) {
            Log.d(DEBUG_TAG, exp.message)
        }

        val now_next = System.currentTimeMillis()
        val fps = 1000 / (now_next - now).toFloat()
        now = now_next
        Log.d(DEBUG_TAG, "fps:" + fps.toString())

        // redraw
        invalidate()
    }

}