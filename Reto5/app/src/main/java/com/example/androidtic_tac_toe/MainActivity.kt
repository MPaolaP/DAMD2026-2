package com.example.androidtic_tac_toe

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardView: BoardView
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

    // Sonidos
    private var mHumanMediaPlayer: MediaPlayer? = null
    private var mComputerMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mInfoTextView = findViewById(R.id.information)
        mHumanCountTextView = findViewById(R.id.human_count)
        mTieCountTextView = findViewById(R.id.tie_count)
        mAndroidCountTextView = findViewById(R.id.android_count)
        mNewGameButton = findViewById(R.id.new_game_button)
        mBoardView = findViewById(R.id.board)

        mNewGameButton.setOnClickListener { startNewGame() }

        mGame = TicTacToeGame()
        mBoardView.setGame(mGame)
        mBoardView.setOnTouchListener(mTouchListener)

        startNewGame()
    }

    private fun startNewGame() {
        mHandler.removeCallbacksAndMessages(null)
        mGame.clearBoard()
        mGameOver = false
        mBoardView.invalidate()
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
            }, 1000L)
            mHumanStarts = true
        }
    }

    // Detectar toques en el BoardView
    private val mTouchListener = View.OnTouchListener { v, event ->
        if (event.action != MotionEvent.ACTION_DOWN) return@OnTouchListener false

        v.performClick()
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col

        if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
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
                        } else {
                            handleWinner(winner)
                        }
                    }
                }, 1000L)
            } else {
                handleWinner(winner)
            }
        }
        false
    }

    private fun handleWinner(winner: Int) {
        when (winner) {
            0 -> mInfoTextView.setText(R.string.turn_human)
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

    private fun setMove(player: Char, location: Int): Boolean {
        if (!mGame.setMove(player, location)) return false

        mBoardView.invalidate()  // Redibujar

        if (player == TicTacToeGame.HUMAN_PLAYER) {
            mHumanMediaPlayer?.start()
        } else {
            mComputerMediaPlayer?.start()
        }
        return true
    }

    private fun updateScores() {
        mHumanCountTextView.text = getString(R.string.human_score, mHumanWins)
        mTieCountTextView.text = getString(R.string.tie_score, mTies)
        mAndroidCountTextView.text = getString(R.string.android_score, mComputerWins)
    }

    // --- CICLO DE VIDA PARA LOS SONIDOS ---

    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.sword)
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.swish)
    }

    override fun onPause() {
        super.onPause()
        mHumanMediaPlayer?.release()
        mComputerMediaPlayer?.release()
        mHumanMediaPlayer = null
        mComputerMediaPlayer = null
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
                showDifficultyDialog()
                return true
            }
            R.id.quit -> {
                showQuitDialog()
                return true
            }
            R.id.about -> {
                showAboutDialog()
                return true
            }
        }
        return false
    }

    private fun showDifficultyDialog() {
        val builder = AlertDialog.Builder(this)
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
        builder.create().show()
    }

    private fun showQuitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.quit_question)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ -> finish() }
            .setNegativeButton(R.string.no, null)
        builder.create().show()
    }

    private fun showAboutDialog() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.about_dialog, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(layout)
            .setPositiveButton("OK", null)
        builder.create().show()
    }
}