package com.example.androidtic_tac_toe

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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

    private val mHandler = Handler(Looper.getMainLooper())

    // IDs de los diálogos
    private val DIALOG_DIFFICULTY_ID = 0
    private val DIALOG_QUIT_ID = 1

    private val DIALOG_ABOUT_ID = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar la Toolbar (si tienes una en el XML)
        // Si no tienes Toolbar en el XML, esta línea puede omitirse.
        // setSupportActionBar(findViewById(R.id.toolbar))

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
            mInfoTextView.setText(R.string.turn_computer)
            mHandler.postDelayed({
                if (!mGameOver) {
                    val move = mGame.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    val winner = mGame.checkForWinner()
                    handleWinner(winner)
                }
            }, 600L)
            mHumanStarts = true
        }
    }

    private inner class ButtonClickListener(private val location: Int) : View.OnClickListener {
        override fun onClick(view: View?) {
            if (!mGameOver && mBoardButtons[location].isEnabled) {
                setBoardEnabled(false)
                setMove(TicTacToeGame.HUMAN_PLAYER, location)

                var winner = mGame.checkForWinner()
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer)
                    mHandler.postDelayed({
                        if (!mGameOver) {
                            val move = mGame.getComputerMove()
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                            winner = mGame.checkForWinner()
                            if (winner == 0) {
                                mInfoTextView.setText(R.string.turn_human)
                                setBoardEnabled(true)
                            } else {
                                handleWinner(winner)
                            }
                        }
                    }, 600L)
                } else {
                    handleWinner(winner)
                }
            }
        }
    }

    private fun handleWinner(winner: Int) {
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

    // --- MENÚ ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> {
                startNewGame()
                return true
            }
            R.id.ai_difficulty -> {
                showDialog(DIALOG_DIFFICULTY_ID)
                return true
            }
            R.id.quit -> {
                showDialog(DIALOG_QUIT_ID)
                return true
            }
            R.id.about -> {
                showDialog(DIALOG_ABOUT_ID)
                return true
            }
        }
        return false
    }

    // --- DIÁLOGOS ---

    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null
        val builder = AlertDialog.Builder(this)

        when (id) {
            DIALOG_DIFFICULTY_ID -> {
                builder.setTitle(R.string.difficulty_choose)

                val levels = arrayOf(
                    getString(R.string.difficulty_easy),
                    getString(R.string.difficulty_harder),
                    getString(R.string.difficulty_expert)
                )

                val selected = when (mGame.getDifficultyLevel()) {
                    TicTacToeGame.DifficultyLevel.Easy -> 0
                    TicTacToeGame.DifficultyLevel.Harder -> 1
                    TicTacToeGame.DifficultyLevel.Expert -> 2
                }

                builder.setSingleChoiceItems(levels, selected) { dialogInterface, item ->
                    dialogInterface.dismiss()

                    val newLevel = when (item) {
                        0 -> TicTacToeGame.DifficultyLevel.Easy
                        1 -> TicTacToeGame.DifficultyLevel.Harder
                        else -> TicTacToeGame.DifficultyLevel.Expert
                    }
                    mGame.setDifficultyLevel(newLevel)

                    Toast.makeText(applicationContext, levels[item], Toast.LENGTH_SHORT).show()
                }

                dialog = builder.create()
            }

            DIALOG_QUIT_ID -> {
                builder.setMessage(R.string.quit_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        finish()
                    }
                    .setNegativeButton(R.string.no, null)

                dialog = builder.create()
            }
            DIALOG_ABOUT_ID -> {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.about_dialog, null)
                builder.setView(layout)
                    .setPositiveButton("OK", null)
                dialog = builder.create()
            }
        }

        return dialog
    }
}