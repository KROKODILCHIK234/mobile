package com.example.memoris

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val cardFaces = mutableListOf(
        R.drawable.cat,
        R.drawable.dog,
        R.drawable.lion,
        R.drawable.fox,
        R.drawable.monkey,
        R.drawable.panda,
        R.drawable.raccoon,
        R.drawable.tiger
    )

    private val cardBackResource = R.drawable.card_back

    private val gameCards = mutableListOf<Int>()
    private val flippedCards = mutableListOf<ImageView>()
    private var isChecking = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            restartGame()
        }

        setupGame()
    }

    private fun setupGame() {
        prepareCards()
        displayCards()
    }

    private fun prepareCards() {
        gameCards.clear()
        gameCards.addAll(cardFaces)
        gameCards.addAll(cardFaces)
        gameCards.shuffle()
    }

    private fun displayCards() {
        val gridLayout = findViewById<GridLayout>(R.id.cards_grid)
        gridLayout.removeAllViews()

        gridLayout.columnCount = 4
        gridLayout.rowCount = gameCards.size / gridLayout.columnCount

        for (cardResId in gameCards) {
            val cardImageView = ImageView(this)
            cardImageView.setImageResource(cardBackResource)
            cardImageView.tag = cardResId

            val layoutParams = GridLayout.LayoutParams().apply {
                width = 200
                height = 280
                setMargins(10, 10, 10, 10)
            }
            cardImageView.layoutParams = layoutParams

            cardImageView.setOnClickListener {
                if (!isChecking && flippedCards.size < 2 && it !in flippedCards) {
                    flipCard(it as ImageView)
                }
            }

            gridLayout.addView(cardImageView)
        }
    }

    private fun flipCard(imageView: ImageView) {
        val faceImageResId = imageView.tag as Int
        imageView.setImageResource(faceImageResId)
        flippedCards.add(imageView)

        if (flippedCards.size == 2) {
            isChecking = true
            CoroutineScope(Dispatchers.Main).launch {
                checkForMatch()
            }
        }
    }

    private suspend fun checkForMatch() {
        val card1 = flippedCards[0]
        val card2 = flippedCards[1]

        if (card1.tag == card2.tag) {
            // Match
            card1.isVisible = false
            card2.isVisible = false
            checkWinCondition()
        } else {

            delay(1000)
            card1.setImageResource(cardBackResource)
            card2.setImageResource(cardBackResource)
        }
        flippedCards.clear()
        isChecking = false
    }

    private fun checkWinCondition() {
        val gridLayout = findViewById<GridLayout>(R.id.cards_grid)
        var allInvisible = true
        for (i in 0 until gridLayout.childCount) {
            if (gridLayout.getChildAt(i).isVisible) {
                allInvisible = false
                break
            }
        }
        if (allInvisible) {
            showVictory()
        }
    }

    private fun showVictory() {
        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
        lottieAnimationView.isVisible = true
        lottieAnimationView.playAnimation()

        AlertDialog.Builder(this)
            .setTitle("Поздравляем!")
            .setMessage("Вы победили!")
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
                findViewById<Button>(R.id.restart_button).isVisible = true
            }
            .setCancelable(false)
            .show()
    }

    private fun restartGame() {
        findViewById<LottieAnimationView>(R.id.lottie_animation_view).isVisible = false
        findViewById<Button>(R.id.restart_button).isVisible = false
        setupGame()
    }
}