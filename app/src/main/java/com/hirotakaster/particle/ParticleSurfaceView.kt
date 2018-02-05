package com.hirotakaster.particle

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import java.util.*
import kotlin.concurrent.withLock
import android.graphics.Bitmap
import com.hirotakaster.particle.views.BaseView
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


/**
 * Created by niisato on 2017/10/21.
 */

class ParticleSurfaceView : SurfaceView, SurfaceHolder.Callback, Runnable, BaseView {
    internal var DEBUG_TAG: String = "Particle"
    internal var particlenum: Int = 1000
    internal var fillAlpha: Int = 64
    internal var effect: Int = 0
    internal var dispLayHeight: Float = 0.0f
    internal var displayWidth: Float = 0.0f
    internal var particles: Vector<Particle> = Vector()
    internal var mousePosition: Vector<Vector2D> = Vector()
    val lock = ReentrantLock()

    internal var paint = Paint()
    internal var blackPaint = Paint()
    internal var thread: Thread? = null
    internal lateinit var offScreenBitmap: Bitmap
    internal lateinit var offScreenCanvas: Canvas

    var now = System.currentTimeMillis()

    constructor(context: Context) : super(context) {
        if (context is Activity) {
            var display = context.getWindowManager().getDefaultDisplay()
            val point = Point(0, 0)
            display.getRealSize(point);
            dispLayHeight = point.y.toFloat()
            displayWidth = point.x.toFloat()
        }

        // initialize
        holder.addCallback(this)
    }
    constructor(context: Context, attrs:AttributeSet) : super(context, attrs) {
    }
    constructor(context: Context, attrs:AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {}


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        thread = null
        if (offScreenBitmap != null)
            offScreenBitmap?.recycle()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        offScreenBitmap = Bitmap.createBitmap(displayWidth.toInt(), dispLayHeight.toInt(), Bitmap.Config.ARGB_8888);
        offScreenCanvas = Canvas(offScreenBitmap)
        if(holder != null){
            doDraw(holder)
        }
    }

    override fun run() {
        while (thread != null){
            doDraw(holder)
        }
    }

    override fun init(partcile: Int, effect: Int, alpha: Int) {
        this.particlenum = partcile
        this.effect = effect
        this.fillAlpha = alpha
    }

    override fun reset() {
        var canvas: Canvas? = null
        canvas = holder?.lockCanvas()

        blackPaint.color = Color.argb(fillAlpha, 0, 0, 0)

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
        mousePosition.clear()

        if (canvas != null)
            holder?.unlockCanvasAndPost(canvas)
    }

    override fun getlock(): Lock {
        return this.lock
    }

    override  fun getmousepoint(): Vector<Vector2D> {
        return this.mousePosition
    }

    override fun mouseclear() {
        this.mousePosition.clear();
    }

    override fun mouseadd(position: Vector2D) {
        this.mousePosition.addElement(position)
    }

    override fun getval(): Triple<Int, Int, Int> {
        return Triple(particlenum, fillAlpha, effect)
    }


    private fun doDraw(holder: SurfaceHolder) {
        var canvas: Canvas
        var tmpmousePosition: Vector<Vector2D> = Vector()

        lock.withLock {
            for (p in mousePosition) {
                tmpmousePosition.add(Vector2D(p.x, p.y))
            }
        }

        try {
            canvas = holder.lockCanvas()
            offScreenCanvas.drawRect(0f, 0f, displayWidth, dispLayHeight, blackPaint)


            runBlocking {
                var drawthread: Vector<kotlinx.coroutines.experimental.Job> = Vector()
                for (i in 1..4) {
                    drawthread.add(async {
                        var paintA = Paint()
                        for (j in i * particles.size / 4..(i + 1) * particles.size / 4 - 1) {
                            paintA.color = particles[j].color
                            particles[j].onUpdate(tmpmousePosition)
                            offScreenCanvas.drawCircle(particles[j].pos.x, particles[j].pos.y, particles[j].scale * 4.0f, paintA)
                        }
                    })
                }
                for (p in drawthread) p.join()
            }

            if (canvas != null) {
                canvas.drawBitmap(offScreenBitmap, 0f, 0f, null)
                holder.unlockCanvasAndPost(canvas)
            }

        } catch (exp: Exception) {
            Log.d(DEBUG_TAG, exp.message)

        }

        val now_next = System.currentTimeMillis()
        val fps = 1000 / (now_next - now).toFloat()
        now = now_next
        // Log.d(DEBUG_TAG, "fps:" + fps.toString())
    }
}
