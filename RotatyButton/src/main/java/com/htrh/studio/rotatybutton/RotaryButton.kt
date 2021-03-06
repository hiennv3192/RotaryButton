package com.htrh.studio.rotatybutton

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.ref.SoftReference
import kotlin.math.atan2
import kotlin.math.floor

class RotaryButton : View {

    companion object {
        private const val DEFAULT_MAX_VALUE = 100
        private const val DEFAULT_MAX_ROTATE_DEGREES = 270
        private const val DEFAULT_PROGRESS_START_DEGREES = 135f
        private const val DEFAULT_BUTTON_START_DEGREES = 225
        private const val DEFAULT_PROGRESS_PADDING = 0f
        private const val DEFAULT_BTN_BG_PADDING = 100f
        private const val DEFAULT_BTN_FG_PADDING = 180f
    }

    private var mButtonFgPadding: Float = 0f
    private var mButtonBgPadding: Float = 0f
    private var mProgressPadding: Float = 0f
    private var mMaxRotateDegrees: Int = 0
    private var mProgressStartDegrees: Float = 0f
    private var mButtonStartDegrees: Int = 0
    private var mRotateDegrees: Float = 0f
    private var mMax = 0f
    private var mIsEnable = true
    private var mProgressBgId = R.drawable.progress_bg
    private var mProgressFgId = R.drawable.progress_foreground
    private var mButtonBgId = R.drawable.btn_bg
    private var mButtonFgId = R.drawable.btn_foreground

    private var mSweepAngle: Float = 0f
    private var mCenterCanvasX = 0f
    private var mCenterCanvasY = 0f
    private var mDownDegrees = 0f
    private var mCurrDegrees = 0f
    private var mDegrees = 0f

    private lateinit var mRectF: RectF
    private lateinit var mPaint: Paint
    private lateinit var mPaintFlags: PaintFlagsDrawFilter
    private lateinit var mProgressBgBm: SoftReference<Bitmap>
    private lateinit var mProgressFgBm: SoftReference<Bitmap>
    private lateinit var mButtonFgBm: SoftReference<Bitmap>
    private lateinit var mButtonBgBm: SoftReference<Bitmap>

    private var mListener: OnCircleSeekBarChangeListener? = null

    private var mClickListener: OnClickListener? = null

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     */
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     *
     *
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply `R.attr.buttonStyle` for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     * reference to a style resource that supplies default values for
     * the view. Can be 0 to not look for defaults.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewWidthHeight = Math.min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(viewWidthHeight, viewWidthHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterCanvasX = width / 2f
        mCenterCanvasY = height / 2f
        setupPaintShaderProgress(w, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mListener != null) {
            mListener!!.onProgressChange(mDegrees.toInt())
        }
        if (mDegrees > mMax) {
            mDegrees = mMax
        }
        if (mDegrees < 0) {
            mDegrees = 0f
        }
        mSweepAngle = mMaxRotateDegrees * (mDegrees / mMax)

        canvas.save()
        drawProgress(canvas, mSweepAngle)
        drawButton(canvas, mSweepAngle)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mClickListener != null) {
            mClickListener!!.onClick(this)
        }
        if (!mIsEnable) {
            return super.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (mListener != null) {
                mListener!!.onStartTrackingTouch(this)
            }
            val dx = event.x - mCenterCanvasX
            val dy = event.y - mCenterCanvasY
            val radians = atan2(dy.toDouble(), dx.toDouble())
            mDownDegrees = (radians * 180 / Math.PI).toFloat()
            mDownDegrees -= 90f
            if (mDownDegrees < 0) {
                mDownDegrees += 360f
            }
            mDownDegrees = floor(mDownDegrees / 360 * (mMax + 5).toDouble()).toFloat()
            return true
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            val dx = event.x - mCenterCanvasX
            val dy = event.y - mCenterCanvasY
            val radians = Math.atan2(dy.toDouble(), dx.toDouble())
            mCurrDegrees = (radians * 180 / Math.PI).toFloat()
            mCurrDegrees -= 90f
            if (mCurrDegrees < 0) {
                mCurrDegrees += 360f
            }
            mCurrDegrees = Math.floor(mCurrDegrees / 360 * (mMax + 5).toDouble()).toFloat()
            if (mCurrDegrees / (mMax + 4) > 0.75f && (mDownDegrees - 0) / (mMax + 4) < 0.25f) {
                mDegrees--
                if (mDegrees < 0) {
                    mDegrees = 0f
                }
                mDownDegrees = mCurrDegrees
            } else if (mDownDegrees / (mMax + 4) > 0.75f
                && (mCurrDegrees - 0) / (mMax + 4) < 0.25f
            ) {
                mDegrees++
                if (mDegrees > mMax) {
                    mDegrees = mMax
                }
                mDownDegrees = mCurrDegrees
            } else {
                mDegrees += mCurrDegrees - mDownDegrees
                if (mDegrees > mMax) {
                    mDegrees = mMax
                }
                if (mDegrees < 0) {
                    mDegrees = 0f
                }
                mDownDegrees = mCurrDegrees
            }
            invalidate()
            return true
        }
        if (event.action == MotionEvent.ACTION_UP) {
            if (mListener != null) {
                mListener!!.onStopTrackingTouch(this)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun setEnabled(enabled: Boolean) {
        mIsEnable = enabled
    }

    override fun isEnabled(): Boolean {
        return mIsEnable
    }

    /**
     * Set the image for progress's background
     *
     * @param id image resource id
     */
    fun setProgressBgImgRes(id: Int) {
        mProgressBgId = id
        mProgressBgBm = getBitmap(mProgressBgId)
        invalidate()
    }

    /**
     * Set the image for progress's foreground
     *
     * @param id image resource id
     */
    fun setProgressFgImgRes(id: Int) {
        mProgressFgId = id
        mProgressFgBm = getBitmap(mProgressFgId)
        invalidate()
    }

    /**
     * Set the image for button's background
     *
     * @param id image resource id
     */
    fun setButtonBgImgRes(id: Int) {
        mButtonBgId = id
        mButtonBgBm = getBitmap(mButtonBgId)
        invalidate()
    }

    /**
     * Set the image for button's foreground
     *
     * @param id image resource id
     */
    fun setButtonFgImgRes(id: Int) {
        mButtonFgId = id
        mButtonFgBm = getBitmap(mButtonFgId)
        invalidate()
    }

    /**
     * Set max for progress
     *
     * @param max max value of progress
     */
    fun setProgressMax(max: Int) {
        this.mMax = max.toFloat()
    }

    /**
     * Get max progress
     *
     * @return progress max value
     */
    fun getProgressMax(): Int {
        return this.mMax.toInt()
    }

    /**
     * Set progress
     *
     * @param progress progress value
     */
    fun setProgress(progress: Int) {
        mDegrees = progress.toFloat()
        invalidate()
    }

    /**
     * Get current progress
     *
     * @return current progress value
     */
    fun getProgress(): Int {
        return mDegrees.toInt()
    }

    /**
     * Set the max rotation degrees of button
     *
     * @param degrees 0 to 360
     */
    fun setMaxRotateDegrees(degrees: Int) {
        mMaxRotateDegrees = when {
            degrees > 360 -> {
                360
            }
            degrees < 0 -> {
                0
            }
            else -> {
                degrees
            }
        }
    }

    /**
     * Starting angle (in degrees) where the progress begins
     *
     * @param degrees 0 to 360
     */
    fun setProgressStartDegrees(degrees: Int) {
        mProgressStartDegrees = when {
            degrees > 360 -> {
                360f
            }
            degrees < 0 -> {
                0f
            }
            else -> {
                degrees.toFloat()
            }
        }
    }

    /**
     * Set the start point in degrees of button foreground.
     *
     * @param degrees 0 to 360
     */
    fun setButtonStartDegrees(degrees: Int) {
        mButtonStartDegrees = when {
            degrees > 360 -> {
                360
            }
            degrees < 0 -> {
                0
            }
            else -> {
                degrees
            }
        }
    }

    /**
     * Set padding for progress foreground and progress background
     *
     * @param padding padding left, right, top, bottom
     */
    fun setProgressPadding(padding: Float) {
        mProgressPadding = padding
    }

    /**
     * Set padding for button background
     *
     * @param padding padding left, right, top, bottom
     */
    fun setButtonBgPadding(padding: Float) {
        mButtonBgPadding = padding
    }

    /**
     * Set padding for button foreground
     *
     * @param padding padding left, right, top, bottom
     */
    fun setButtonFgPadding(padding: Float) {
        mButtonFgPadding = padding
    }

    fun setOnSeekBarChangeListener(mListener: OnCircleSeekBarChangeListener?) {
        this.mListener = mListener
    }

    override fun setOnClickListener(mListener: OnClickListener?) {
        mClickListener = mListener
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mRectF = RectF()
        mPaint = Paint()
        mPaintFlags = PaintFlagsDrawFilter(0, 3)

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotaryButton,
            0, 0
        )

        try {
            // progress background
            mProgressBgId = typedArray.getResourceId(
                R.styleable.RotaryButton_rotary_progressBackgroundDrawable,
                R.drawable.progress_bg
            )
            mProgressBgBm = getBitmap(mProgressBgId)

            // progress foreground
            mProgressFgId = typedArray.getResourceId(
                R.styleable.RotaryButton_rotary_progressForegroundDrawable,
                R.drawable.progress_foreground
            )
            mProgressFgBm = getBitmap(mProgressFgId)

            // button background
            mButtonBgId = typedArray.getResourceId(
                R.styleable.RotaryButton_rotary_buttonBackgroundDrawable,
                R.drawable.btn_bg
            )
            mButtonBgBm = getBitmap(mButtonBgId)

            // button foreground
            mButtonFgId = typedArray.getResourceId(
                R.styleable.RotaryButton_rotary_buttonForegroundDrawable,
                R.drawable.btn_foreground
            )
            mButtonFgBm = getBitmap(mButtonFgId)

            // other
            mMax = typedArray.getInteger(
                R.styleable.RotaryButton_rotary_progressMax,
                DEFAULT_MAX_VALUE
            )
                .toFloat()
            mDegrees = typedArray.getInteger(R.styleable.RotaryButton_rotary_progress, 0)
                .toFloat()
            mMaxRotateDegrees = typedArray.getInteger(
                R.styleable.RotaryButton_rotary_maxRotateDegrees,
                DEFAULT_MAX_ROTATE_DEGREES
            )
            mProgressStartDegrees = typedArray.getInteger(
                R.styleable.RotaryButton_rotary_progressStartDegrees,
                DEFAULT_PROGRESS_START_DEGREES.toInt()
            ).toFloat()
            mButtonStartDegrees = typedArray.getInteger(
                R.styleable.RotaryButton_rotary_buttonStartDegrees,
                DEFAULT_BUTTON_START_DEGREES
            )
            mProgressPadding = typedArray.getFloat(
                R.styleable.RotaryButton_rotary_progressPadding,
                DEFAULT_PROGRESS_PADDING
            )
            mButtonBgPadding = typedArray.getFloat(
                R.styleable.RotaryButton_rotary_buttonBackgroundPadding,
                DEFAULT_BTN_BG_PADDING
            )
            mButtonFgPadding = typedArray.getFloat(
                R.styleable.RotaryButton_rotary_buttonForegroundPadding,
                DEFAULT_BTN_FG_PADDING
            )
            mIsEnable = typedArray.getBoolean(
                R.styleable.RotaryButton_android_enabled,
                true
            )
        } finally {
            typedArray.recycle()
        }
    }

    private fun getBitmap(resId: Int): SoftReference<Bitmap> {
        return SoftReference(
            BitmapFactory.decodeResource(
                resources,
                resId
            )
        )
    }

    private fun setupPaintShaderProgress(viewWidth: Int, viewHeight: Int) {
        val matrix = Matrix()
        // kích thước img sẽ được vẽ lên
        if (mProgressFgBm.get() == null) {
            mProgressFgBm = getBitmap(mProgressFgId)
        }
        val src = RectF(
            0f,
            0f,
            mProgressFgBm.get()?.width?.toFloat()!!,
            mProgressFgBm.get()?.width?.toFloat()!!
        )
        // kích thước canvas để đặt img
        val dst = RectF(
            0f + mProgressPadding,
            0f + mProgressPadding,
            viewWidth.toFloat() - mProgressPadding,
            viewHeight.toFloat() - mProgressPadding
        )
        val shader =
            BitmapShader(mProgressFgBm.get()!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        // scale ảnh được vẽ bởi src cho fit với dst
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
        shader.setLocalMatrix(matrix)
        mPaint.shader = shader
        matrix.mapRect(mRectF, src)
    }

    private fun drawProgress(canvas: Canvas, sweepAngle: Float) {
        // create a rectangle frame to contain a progress line
        mRectF.set(
            0 + mProgressPadding,
            0 + mProgressPadding,
            width - mProgressPadding,
            height - mProgressPadding
        )
        drawProgressBg(canvas)
        drawProgressFg(canvas, sweepAngle)
    }

    private fun drawProgressBg(canvas: Canvas) {
        if (mProgressBgBm.get() == null) {
            mProgressBgBm = getBitmap(mProgressBgId)
        }
        canvas.drawBitmap(mProgressBgBm.get()!!, null, mRectF, null)
    }

    private fun drawProgressFg(canvas: Canvas, sweepAngle: Float) {
        // set useCenter la true thì sẽ vẽ từ tâm ra, false thì chỉ vẽ viền ngoài
        // tham khảo https://thoughtbot.com/blog/android-canvas-drawarc-method-a-visual-guide

        // mProgressStartDegrees trừ 90 là để cho user dễ hình dung khi thiết định start degrees của
        // progress và start degrees của button
        canvas.drawArc(
            mRectF, mProgressStartDegrees - 90,
            sweepAngle, true, mPaint
        )
    }

    private fun drawButton(canvas: Canvas, sweepAngle: Float) {
        drawButtonBg(canvas)

        // rotate image, object be draw after call this method will take effect
        mRotateDegrees = mButtonStartDegrees + sweepAngle //rotation degree

        canvas.rotate(mRotateDegrees, mCenterCanvasX, mCenterCanvasY)
        drawButtonFg(canvas)
    }

    private fun drawButtonBg(canvas: Canvas) {
        // create a rectangle frame to contain a button background
        mRectF.set(
            0 + mButtonBgPadding,
            0 + mButtonBgPadding,
            width - mButtonBgPadding,
            height - mButtonBgPadding
        )
        if (mButtonBgBm.get() == null) {
            mButtonBgBm = getBitmap(mButtonBgId)
        }
        canvas.drawBitmap(mButtonBgBm.get()!!, null, mRectF, null)
    }

    private fun drawButtonFg(canvas: Canvas) {
        // create a rectangle frame to contain a button
        mRectF.set(
            0 + mButtonFgPadding,
            0 + mButtonFgPadding,
            width - mButtonFgPadding,
            height - mButtonFgPadding
        )
        if (mButtonFgBm.get() == null) {
            mButtonFgBm = getBitmap(mButtonFgId)
        }
        canvas.drawBitmap(mButtonFgBm.get()!!, null, mRectF, null)
    }

    interface OnCircleSeekBarChangeListener {
        fun onProgressChange(progress: Int)
        fun onStartTrackingTouch(rotaryButton: RotaryButton?)
        fun onStopTrackingTouch(rotaryButton: RotaryButton?)
    }
}