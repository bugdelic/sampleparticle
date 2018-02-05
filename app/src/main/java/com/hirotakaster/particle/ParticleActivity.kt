package com.hirotakaster.particle

import android.app.Activity
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Point
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_particle.*
import kotlinx.android.synthetic.main.setting_menu.view.*
import android.widget.LinearLayout
import com.google.android.gms.analytics.HitBuilders
import com.hirotakaster.particle.views.BaseView
import kotlin.concurrent.withLock


class ParticleActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener  {
    internal var DEBUG_TAG: String = "Particle"
    private var MAX_SURFACE_PARTICLE_NUM: Int = 2000
    private var MAX_SURFACE_GL_PARTICLE_NUM: Int = 10000
    internal lateinit var sharedPreferences: SharedPreferences
    private var gestureDetector: GestureDetector? = null
    // viewtype : 0 PlaneView
    //            1 Surfaceview
    //            2 GLSurfaceview
    private var viewType: Int = 1

    private lateinit var activity: Activity
    private lateinit var mainview: BaseView

    // hold state
    var now = System.currentTimeMillis()
    private var multitouch: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_particle)

        // tracking
        val application = application as ParticleApp
        var mTracker = application.defaultTracker
        mTracker.setScreenName("ParticleActivity")
        mTracker.send(HitBuilders.ScreenViewBuilder().build());

        //
        // mainview = particleview
        activity = this
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        viewType = sharedPreferences.getInt("viewtype", 1)

        // select view type
        if (viewType == 1) {
            mainview = ParticleSurfaceView(this)
            mainConstraintLayout.addView(mainview as SurfaceView)
        } else if (viewType == 2) {
            mainview = ParticleGLSurfaceView(this)
            mainConstraintLayout.addView(mainview as GLSurfaceView)
        }

        // default settings
        mainview.init(sharedPreferences.getInt("particles", 800),
                sharedPreferences.getInt("effect", 0),
                sharedPreferences.getInt("fillalpha", 40))

        // start main view particle
        mainview.reset()

        // default position
        var display = getWindowManager().getDefaultDisplay()
        val point = Point(0, 0)
        display.getRealSize(point);
        mainview.mouseadd(Vector2D(point.x.toFloat() / 2, point.y.toFloat() / 3 + 80 ))
        mainview.mouseadd(Vector2D(point.x.toFloat() / 3, point.y.toFloat() * 2/ 3 - 80))
        mainview.mouseadd(Vector2D(point.x.toFloat() * 2/ 3, point.y.toFloat() * 2/ 3 - 80))
        gestureDetector = GestureDetector(this, this)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector?.onTouchEvent(event)

        val count:Int = event!!.pointerCount
        val now_next = System.currentTimeMillis()
        val elapstime:Float = (now_next - now).toFloat()

        if (multitouch && mainview.getmousepoint().size <= count) {
            mainview.getlock().withLock {
                Log.d(DEBUG_TAG, "count:" + mainview.getmousepoint().size + " total point: " + count)
                mainview.mouseclear()
                for (i in 0..(count!! - 1)) {
                    mainview.mouseadd(Vector2D(event.getX(i), event.getY(i)))
                }
            }
            return true
        } else if (multitouch && elapstime > 1500.0f) {
            return true
        }
        now = now_next

        mainview.getlock().withLock {
            Log.d(DEBUG_TAG, "count:" + mainview.getmousepoint().size + " total point: " + count)
            mainview.mouseclear()
            for (i in 0..(count!! - 1)) {
                mainview.mouseadd(Vector2D(event.getX(i), event.getY(i)))
            }
        }

        if (count > 1)
            multitouch = true
        else
            multitouch = false

        return true
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.d(DEBUG_TAG, "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp")
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onDown")
        mainview.getlock().withLock {
            multitouch = false
            mainview.mouseclear()
            mainview.mouseadd(Vector2D(e!!.x, e!!.y))
        }
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        Log.d(DEBUG_TAG, "onFling")
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.d(DEBUG_TAG, "onScroll")
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(DEBUG_TAG, "onLongPress")
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap")
        mainview.reset()

        val view = LayoutInflater.from(this).inflate(R.layout.setting_menu, null)
        val (particlenum, fillAlpha, effect) = mainview.getval()

        // view type
        if (viewType == 1)
            view.surfaceRadioButton.isChecked = true
        else if (viewType == 2)
            view.glRadioButton.isChecked = true

        // effect
        if (effect == 0)
            view.effectMeetradioButton.isChecked = true
        else if (effect == 1)
            view.effectLeaveRadioButton.isChecked = true

        // seekbar
        if (viewType == 1)
            view.particleSeekBar.max = MAX_SURFACE_PARTICLE_NUM - 1
        else if (viewType == 2)
            view.particleSeekBar.max = MAX_SURFACE_GL_PARTICLE_NUM - 1
        view.particleSeekBar.progress = particlenum - 1
        view.particleSeekTextView?.text = particlenum.toString()
        view.particleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.particleSeekTextView?.text = (progress + 1).toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                view.particleSeekTextView?.text = (seekBar.progress + 1).toString()
            }
        });

        view.fillAlphaSeekBar.progress = fillAlpha - 1
        view.fillAlphaTextView?.text = fillAlpha.toString()
        view.fillAlphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                view.fillAlphaTextView?.text = (progress + 1).toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                view.fillAlphaTextView?.text = (seekBar.progress + 1).toString()
            }
        });

        // create dialog
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth)
        builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog : DialogInterface?, which : Int) {
                        // save particle num, effect
                        val editor = sharedPreferences.edit()
                        var particlenum = (view.particleSeekBar.progress + 1)
                        if (view.effectMeetradioButton.isChecked)
                            editor.putInt("effect", 0)
                        else if (view.effectLeaveRadioButton.isChecked)
                            editor.putInt("effect", 1)
                        editor.putInt("fillalpha", (view.fillAlphaSeekBar.progress + 1))

                        if (view.surfaceRadioButton.isChecked) {
                            if (viewType == 2) {
                                mainConstraintLayout.removeView(mainview as GLSurfaceView)
                                mainview = ParticleSurfaceView(activity)
                                mainConstraintLayout.addView(mainview as SurfaceView)
                            }
                            viewType = 1
                            editor.putInt("viewtype", 1)
                        } else if (view.glRadioButton.isChecked) {
                            if (viewType == 1) {
                                mainConstraintLayout.removeView(mainview as SurfaceView)
                                mainview = ParticleGLSurfaceView(activity)
                                mainConstraintLayout.addView(mainview as GLSurfaceView)
                            }
                            viewType = 2
                            editor.putInt("viewtype", 2)
                        }
                        if (viewType == 1 && particlenum >= MAX_SURFACE_PARTICLE_NUM)
                            particlenum = MAX_SURFACE_PARTICLE_NUM
                        editor.putInt("particles", particlenum)
                        editor.commit()

                        // reset particle
                        mainview.init(particlenum,
                                (if (view.effectMeetradioButton.isChecked) 0 else 1),
                                            view.fillAlphaSeekBar.progress + 1)
                        mainview.reset()

                    }
                });
        builder.setView(view)
        builder.setCancelable(true)
        var alert = builder.create()
        alert.show()

        // change positive button position
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        var parentLayout        = positiveButton.getParent() as LinearLayout
        parentLayout.setGravity(Gravity.CENTER)
        parentLayout.getChildAt(1).setVisibility(View.GONE)

        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTapEvent")
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed")
        mainview.getlock().withLock {
            multitouch = false
            mainview.mouseclear()
            mainview.reset()
        }
        return true
    }
}
