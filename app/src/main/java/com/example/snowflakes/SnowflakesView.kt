package com.example.snowflakes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*
import java.util.Random as JavaRandom
import kotlin.random.Random
import kotlin.math.sin

class SnowflakesView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private data class Snowflake(
        var x: Float,
        var y: Float,
        var radius: Float,
        var speed: Float,
        var paint: Paint,
        var horizontalDrift: Float
    )

    private val fallingSnowflakes = mutableListOf<Snowflake>()
    private val numSnowflakes = 600
    private val javaRandom = JavaRandom()

    private lateinit var snowdriftHeights: FloatArray
    private val snowdriftPaint = Paint().apply {
        color = Color.rgb(240, 240, 255)
        style = Paint.Style.FILL
    }
    private val snowdriftPath = Path()
    private var peakSnowHeight = 0f

    private val scope = CoroutineScope(Dispatchers.Main)
    private var animationJob: Job? = null


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!::snowdriftHeights.isInitialized) {
            snowdriftHeights = FloatArray(w) { h.toFloat() }
        }
        if (fallingSnowflakes.isEmpty()) {
            createSnowflakes()
        }
        if (animationJob == null) {
            startAnimation()
        }
    }

    private fun startAnimation() {
        animationJob = scope.launch {
            while (isActive) {
                updateSnowflakesAndMound()
                postInvalidate()
                delay(16)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationJob?.cancel()
        animationJob = null
    }

    private fun createSnowflakes() {
        fallingSnowflakes.clear()
        for (i in 0 until numSnowflakes) {
            fallingSnowflakes.add(createSingleSnowflake(true))
        }
    }

    private fun getBiasedRandomX(): Float {
        if (width == 0) return 0f
        val centerX = width / 2f
        val standardDeviation = width / 5f
        val randomGaussian = javaRandom.nextGaussian().toFloat()
        val newX = randomGaussian * standardDeviation + centerX
        return newX.coerceIn(0f, width - 1f)
    }

    private fun createSingleSnowflake(isInitial: Boolean = false): Snowflake {
        return Snowflake(
            x = getBiasedRandomX(),
            y = if (isInitial) Random.nextFloat() * height else -10f,
            radius = Random.nextFloat() * 4 + 2,
            speed = Random.nextFloat() * 8 + 4,
            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                val shade = 220 + Random.nextInt(36)
                color = Color.rgb(shade, shade, 255)
            },
            horizontalDrift = Random.nextFloat() * 2 - 1
        )
    }

    private fun recalculateSnowdriftShape() {
        if (width == 0) return
        val centerX = width / 2f
        val peakY = height - peakSnowHeight

        for (x in snowdriftHeights.indices) {
            val distanceFromCenter = x - centerX
            val yOffset = (distanceFromCenter * distanceFromCenter) / (width * 1.5f)
            val newHeight = peakY + yOffset
            snowdriftHeights[x] = newHeight.coerceAtMost(height.toFloat())
        }
    }

    private suspend fun updateSnowflakesAndMound() = withContext(Dispatchers.Default) {
        recalculateSnowdriftShape()

        val iterator = fallingSnowflakes.iterator()
        while (iterator.hasNext()) {
            val snowflake = iterator.next()

            val slowdownFactor = 1.0f - (snowflake.y / height).coerceAtMost(0.9f)
            snowflake.y += snowflake.speed * slowdownFactor
            // Теперь компилятор знает, что такое 'sin'
            snowflake.x += sin(snowflake.y / 50) * snowflake.horizontalDrift * 2

            if (snowflake.x < 0) snowflake.x = width.toFloat()
            if (snowflake.x > width) snowflake.x = 0f

            val xIndex = snowflake.x.toInt()
            if (xIndex in snowdriftHeights.indices) {
                if (snowflake.y >= snowdriftHeights[xIndex]) {
                    peakSnowHeight += snowflake.radius * 0.1f
                    resetSnowflake(snowflake)
                }
            } else if (snowflake.y > height) {
                resetSnowflake(snowflake)
            }
        }
    }

    private fun resetSnowflake(snowflake: Snowflake) {
        snowflake.y = -10f
        snowflake.x = getBiasedRandomX()
        snowflake.speed = Random.nextFloat() * 8 + 4
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        for (snowflake in fallingSnowflakes) {
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.radius, snowflake.paint)
        }

        snowdriftPath.reset()
        snowdriftPath.moveTo(0f, height.toFloat())
        for (i in snowdriftHeights.indices) {
            snowdriftPath.lineTo(i.toFloat(), snowdriftHeights[i])
        }
        snowdriftPath.lineTo(width.toFloat(), height.toFloat())
        snowdriftPath.close()

        canvas.drawPath(snowdriftPath, snowdriftPaint)
    }
}