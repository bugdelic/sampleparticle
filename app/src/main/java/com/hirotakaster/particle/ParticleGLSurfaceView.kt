package com.hirotakaster.particle

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.AttributeSet
import com.hirotakaster.particle.views.BaseView
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Vector
import java.util.concurrent.locks.Lock

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock


/**
 * Created by niisato on 2017/10/26.
 */

class ParticleGLSurfaceView : GLSurfaceView, BaseView {
    val particleRenderer: ParticleRenderer = ParticleRenderer()

    var DEBUG_TAG = "Particle"
    var MAX_FLOAT_BUFFER = 20000

    internal var particlenum: Int = 1000
    internal var fillAlpha: Int = 64
    internal var effect: Int = 0
    internal var dispLayHeight: Float = 0.0f
    internal var displayWidth: Float = 0.0f
    internal var mousePosition: Vector<Vector2D> = Vector()
    val lock = java.util.concurrent.locks.ReentrantLock()

    internal var vel: Array<Vector2D> = Array<Vector2D>(MAX_FLOAT_BUFFER, {Vector2D(0.0f, 0.0f)})
    internal var frc: Array<Vector2D> = Array<Vector2D>(MAX_FLOAT_BUFFER, {Vector2D(0.0f, 0.0f)})
    internal var drag: Array<Float> = Array<Float>(MAX_FLOAT_BUFFER, {0.0f})
    internal var scale: Float = 0.0f

    private var mVertexBuffer: FloatBuffer = makeFloatBuffer(MAX_FLOAT_BUFFER * 2)
    private var mColorBuffer: FloatBuffer = makeFloatBuffer(MAX_FLOAT_BUFFER * 4)
    var now = System.currentTimeMillis()


    constructor(context: Context) : super(context) {
        setRenderer(particleRenderer)

        if (context is Activity) {
            var display = context.getWindowManager().getDefaultDisplay()
            val point = Point(0, 0)
            display.getRealSize(point);
            dispLayHeight = point.y.toFloat()
            displayWidth = point.x.toFloat()
        }

        for (i in 0..MAX_FLOAT_BUFFER - 1) {
            vel[i] = Vector2D(0f, 0f)
            frc[i] = Vector2D(0f, 0f)
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    override fun init(partcile: Int, effect: Int, alpha: Int) {
        this.particlenum = partcile
        this.effect = effect
        this.fillAlpha = alpha
    }

    override fun reset() {
        scale = ParticleUtil.nextFloat(0.5f, 1.0f) * 3.0f
        if (particlenum > 0) {
            for (i in 0..particlenum - 1) {
                mVertexBuffer.put(ParticleUtil.nextFloat(0.0f, displayWidth))
                mVertexBuffer.put(ParticleUtil.nextFloat(0.0f, dispLayHeight))

                vel[i].x = ParticleUtil.nextFloat(-3.9f, 3.9f)
                vel[i].y = ParticleUtil.nextFloat(-3.9f, 3.9f)

                frc[i] = Vector2D(0f, 0f)
                drag[i] = ParticleUtil.nextFloat(0.90f, 0.998f);

                mColorBuffer.put(ParticleUtil.nextFloat(0.1f, 1.0f))
                mColorBuffer.put(ParticleUtil.nextFloat(0.1f, 1.0f))
                mColorBuffer.put(ParticleUtil.nextFloat(0.1f, 1.0f))
                mColorBuffer.put(ParticleUtil.nextFloat(0.1f, 1.0f))
            }
        }
        mVertexBuffer.position(0);
        mColorBuffer.position(0)

        lock.withLock {
            mousePosition.clear()
        }
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

    fun onUpdate() {
        var tmpmousePosition: Vector<Vector2D> = Vector()

        lock.withLock {
            for (p in mousePosition) {
                tmpmousePosition.add(Vector2D(p.x, dispLayHeight - p.y))
            }
        }

        for (i in 0..(particlenum - 1)) {
            var pos: Vector2D = Vector2D(0f, 0f )
            mVertexBuffer.position(i * 2)
            pos.x = mVertexBuffer.get()
            pos.y = mVertexBuffer.get()

            if (tmpmousePosition.size == 1) {
                if (effect == 0) {
                    var attractPt = Vector2D(tmpmousePosition[0].x, tmpmousePosition[0].y)
                    frc[i] = attractPt - pos
                    var dist: Float = frc[i].length()
                    frc[i].normalize()
                    vel[i].mul(drag[i])

                    if (dist < 10)
                        vel[i] = vel[i] + frc[i].mul(-0.6f)
                    else
                        vel[i] += frc[i].mul(0.4f)

                } else if (effect == 1) {
                    var attractPt = Vector2D(tmpmousePosition[0].x, tmpmousePosition[0].y)
                    frc[i] = attractPt - pos
                    var dist: Float = frc[i].length()
                    frc[i].normalize()
                    vel[i].mul(drag[i])

                    if (dist < 120) {
                        vel[i] = vel[i] + frc[i].mul(-0.4f)
                    } else {
                        frc[i].x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        frc[i].y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                        vel[i] += frc[i].mul(0.4f)
                    }
                }

            } else if (tmpmousePosition.size > 1) {
                if (effect == 0) {
                    var closest: Int = -1;
                    var closestDist: Int = 99999999

                    for (j in 0..(tmpmousePosition.size - 1)) {
                        var lenSq: Float = tmpmousePosition[j].absolute(pos)
                        if (lenSq < closestDist) {
                            closestDist = lenSq.toInt()
                            closest = j
                        }
                    }

                    if (closest != -1) {
                        var closestPt = Vector2D(tmpmousePosition[closest].x, tmpmousePosition[closest].y)
                        var dist: Float = Math.sqrt(closestDist.toDouble()).toFloat()

                        frc[i] = closestPt - pos
                        vel[i] = vel[i].mul(drag[i])

                        if (dist < 300 && dist > 15) {
                            vel[i] += frc[i].mul(0.03f)
                        } else if (dist <= 10) {
                            vel[i] += frc[i].mul(-1.0f)
                        } else {
                            frc[i].x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                            frc[i].y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                            vel[i] += frc[i].mul(0.8f)
                        }
                    }
                } else if (effect == 1) {
                    var closest: Int = -1;
                    var closestDist: Int = 99999999

                    for (j in 0..(tmpmousePosition.size - 1)) {
                        var lenSq: Float = tmpmousePosition[j].absolute(pos)
                        if (lenSq < closestDist) {
                            closestDist = lenSq.toInt()
                            closest = j
                        }
                    }

                    if (closest != -1) {
                        var closestPt = Vector2D(tmpmousePosition[closest].x, tmpmousePosition[closest].y)
                        var dist: Float = Math.sqrt(closestDist.toDouble()).toFloat()

                        frc[i] = closestPt - pos
                        vel[i] = vel[i].mul(drag[i])

                        if (dist < 120) {
                            vel[i] += frc[i].mul(-0.4f)
                        } else {
                            frc[i].x = ParticleUtil.nextFloat(-1.0f, 1.0f)
                            frc[i].y = ParticleUtil.nextFloat(-1.0f, 1.0f)
                            vel[i] += frc[i].mul(0.4f)
                        }
                    }
                }
            }

            //2 - UPDATE OUR POSITION
            pos += vel[i]

            //3 - (optional) LIMIT THE PARTICLES TO STAY ON SCREEN
            if (pos.x > displayWidth) {
                pos.x = displayWidth
                vel[i].x *= -1.0.toFloat()
            } else if (pos.x < 0) {
                pos.x = 0.toFloat();
                vel[i].x *= -1.0f
            }
            if (pos.y > dispLayHeight) {
                pos.y = dispLayHeight
                vel[i].y *= -1.0f
            } else if (pos.y < 0.0f) {
                pos.y = 0.toFloat()
                vel[i].y *= -1.0.toFloat()
            }
            mVertexBuffer.position(i * 2)
            mVertexBuffer.put(pos.x)
            mVertexBuffer.put(pos.y)
        }
        mVertexBuffer.position(0)
    }

    protected fun makeFloatBuffer(count: Int): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(count * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.position(0)
        return fb
    }

    inner class ParticleRenderer : Renderer {
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            gl.glViewport(0, 0, width, height)
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            GLU.gluOrtho2D(gl, 0.0f, width.toFloat(), 0.0f, height.toFloat())
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        }

        override fun onDrawFrame(gl: GL10) {
            onUpdate();
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
            gl.glClearColor(0f, 0f, 0f, 0.01f)
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glLoadIdentity()

            gl.glPointSize(scale)
            gl.glVertexPointer(1 * 2, GL10.GL_FLOAT, 0, mVertexBuffer)
            gl.glColorPointer(1 * 4, GL10.GL_FLOAT, 0, mColorBuffer)
            gl.glDrawArrays(GL10.GL_POINTS, 0,  particlenum)

            val now_next = System.currentTimeMillis()
            val fps = 1000 / (now_next - now).toFloat()
            now = now_next
            // Log.d(DEBUG_TAG, "fps:" + fps.toString())
        }
    }
}
