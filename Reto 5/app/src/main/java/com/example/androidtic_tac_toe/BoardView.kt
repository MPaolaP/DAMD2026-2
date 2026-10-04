package com.example.androidtic_tac_toe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Ancho de las líneas del tablero
    private val GRID_WIDTH = 6

    // Bitmaps para X y O
    private lateinit var mHumanBitmap: Bitmap
    private lateinit var mComputerBitmap: Bitmap

    // Paint para dibujar
    private lateinit var mPaint: Paint

    // Referencia al juego
    private var mGame: TicTacToeGame? = null

    init {
        initialize()
    }

    private fun initialize() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHumanBitmap = BitmapFactory.decodeResource(resources, R.drawable.x_img)
        mComputerBitmap = BitmapFactory.decodeResource(resources, R.drawable.o_img)
    }

    fun setGame(game: TicTacToeGame) {
        mGame = game
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val boardWidth = width
        val boardHeight = height

        // Ancho de cada celda
        val cellWidth = boardWidth / 3
        val cellHeight = boardHeight / 3

        // Dibujar líneas grises
        mPaint.color = Color.LTGRAY
        mPaint.strokeWidth = GRID_WIDTH.toFloat()

        // Líneas verticales
        canvas.drawLine(cellWidth.toFloat(), 0f, cellWidth.toFloat(), boardHeight.toFloat(), mPaint)
        canvas.drawLine((cellWidth * 2).toFloat(), 0f, (cellWidth * 2).toFloat(), boardHeight.toFloat(), mPaint)

        // Líneas horizontales
        canvas.drawLine(0f, cellHeight.toFloat(), boardWidth.toFloat(), cellHeight.toFloat(), mPaint)
        canvas.drawLine(0f, (cellHeight * 2).toFloat(), boardWidth.toFloat(), (cellHeight * 2).toFloat(), mPaint)

        // Dibujar las X y O
        val game = mGame ?: return
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val col = i % 3
            val row = i / 3

            // Calcular el rectángulo destino
            val left = col * cellWidth + GRID_WIDTH
            val top = row * cellHeight + GRID_WIDTH
            val right = (col + 1) * cellWidth - GRID_WIDTH
            val bottom = (row + 1) * cellHeight - GRID_WIDTH

            val destRect = Rect(left, top, right, bottom)

            when (game.getBoardOccupant(i)) {
                TicTacToeGame.HUMAN_PLAYER -> {
                    canvas.drawBitmap(mHumanBitmap, null, destRect, null)
                }
                TicTacToeGame.COMPUTER_PLAYER -> {
                    canvas.drawBitmap(mComputerBitmap, null, destRect, null)
                }
            }
        }
    }

    // Devuelve el ancho de una celda (para detectar toques)
    fun getBoardCellWidth(): Int = width / 3
    fun getBoardCellHeight(): Int = height / 3

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}