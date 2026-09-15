package com.example.androidtic_tac_toe

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button>
    private lateinit var mInfoTextView: TextView
    private lateinit var mHumanCountTextView: TextView
    private lateinit var mTieCountTextView: TextView
    private lateinit var mAndroidCountTextView: TextView
    private lateinit var mNewGameButton: Button

    private var mGameOver = false
    private var mHumanStarts = true

    private var mHumanWins = 0
    private var mComputerWins = 0
    private var mTies = 0

    // Handler para retrasar el movimiento de la máquina
    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBoardButtons = arrayOf(
            findViewById(R.id.one),
            findViewById(R.id.two),
            findViewById(R.id.three),
            findViewById(R.id.four),
            findViewById(R.id.five),
            findViewById(R.id.six),
            findViewById(R.id.seven),
            findViewById(R.id.eight),
            findViewById(R.id.nine)
        )

        mInfoTextView = findViewById(R.id.information)
        mHumanCountTextView = findViewById(R.id.human_count)
        mTieCountTextView = findViewById(R.id.tie_count)
        mAndroidCountTextView = findViewById(R.id.android_count)
        mNewGameButton = findViewById(R.id.new_game_button)

        mNewGameButton.setOnClickListener {
            startNewGame()
        }

        mGame = TicTacToeGame()
        startNewGame()
    }

    private fun startNewGame() {
        // Cancelar cualquier movimiento pendiente de la máquina
        mHandler.removeCallbacksAndMessages(null)

        mGame.clearBoard()
        mGameOver = false

        for (i in mBoardButtons.indices) {
            mBoardButtons[i].text = ""
            mBoardButtons[i].isEnabled = true
            mBoardButtons[i].setOnClickListener(ButtonClickListener(i))
        }

        updateScores()

        if (mHumanStarts) {
            mInfoTextView.setText(R.string.first_human)
            mHumanStarts = false
        } else {
            // La máquina empieza, con retraso para que se vea
            mInfoTextView.setText(R.string.turn_computer)
            mHandler.postDelayed({
                if (!mGameOver) {
                    val move = mGame.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    val winner = mGame.checkForWinner()
                    handleWinner(winner, isHumanTurn = true)
                }
            }, 600L)
            mHumanStarts = true
        }
    }

    private inner class ButtonClickListener(private val location: Int) : View.OnClickListener {
        override fun onClick(view: View?) {
            if (!mGameOver && mBoardButtons[location].isEnabled) {
                // Deshabilitar todos los botones mientras la máquina piensa
                setBoardEnabled(false)

                setMove(TicTacToeGame.HUMAN_PLAYER, location)

                var winner = mGame.checkForWinner()
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer)

                    // Retraso de 600 ms antes de que la máquina mueva
                    mHandler.postDelayed({
                        if (!mGameOver) {
                            val move = mGame.getComputerMove()
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                            winner = mGame.checkForWinner()

                            if (winner == 0) {
                                mInfoTextView.setText(R.string.turn_human)
                                setBoardEnabled(true)
                            } else {
                                handleWinner(winner, isHumanTurn = true)
                            }
                        }
                    }, 600L)
                } else {
                    handleWinner(winner, isHumanTurn = true)
                }
            }
        }
    }

    private fun handleWinner(winner: Int, isHumanTurn: Boolean) {
        when (winner) {
            0 -> {
                mInfoTextView.setText(R.string.turn_human)
                setBoardEnabled(true)
            }
            1 -> {
                mInfoTextView.setText(R.string.result_tie)
                mTies++
                mGameOver = true
            }
            2 -> {
                mInfoTextView.setText(R.string.result_human_win)
                mHumanWins++
                mGameOver = true
            }
            3 -> {
                mInfoTextView.setText(R.string.result_computer_win)
                mComputerWins++
                mGameOver = true
            }
        }
        updateScores()
    }

    private fun setBoardEnabled(enabled: Boolean) {
        for (i in mBoardButtons.indices) {
            // Solo habilitar los botones que aún estén vacíos
            if (mBoardButtons[i].text.isEmpty()) {
                mBoardButtons[i].isEnabled = enabled
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        mBoardButtons[location].isEnabled = false
        mBoardButtons[location].text = player.toString()
        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mBoardButtons[location].setTextColor(0xFF00CC66.toInt())
        } else {
            mBoardButtons[location].setTextColor(0xFFFF4444.toInt())
        }
    }

    private fun updateScores() {
        mHumanCountTextView.text = getString(R.string.human_score, mHumanWins)
        mTieCountTextView.text = getString(R.string.tie_score, mTies)
        mAndroidCountTextView.text = getString(R.string.android_score, mComputerWins)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(R.string.new_game)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startNewGame()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Evitar fugas de memoria
        mHandler.removeCallbacksAndMessages(null)
    }
}