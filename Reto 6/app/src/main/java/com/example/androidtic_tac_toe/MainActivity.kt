package com.example.androidtic_tac_toe

import android.app.AlertDialog
import android.content.SharedPreferences
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
    private lateinit var mPrefs: SharedPreferences
    private lateinit var mBoardView: BoardView
    private lateinit var mInfoTextView: TextView
    private lateinit var mHumanCountTextView: TextView
    private lateinit var mTieCountTextView: TextView
    private lateinit var mAndroidCountTextView: TextView
    private lateinit var mNewGameButton: Button

    private var mGameOver = false
    private var mHumanStarts = true
    private var mIsComputerTurn = false   // ← NUEVA VARIABLE

    private var mHumanWins = 0
    private var mComputerWins = 0
    private var mTies = 0

    private val mHandler = Handler(Looper.getMainLooper())

    private var mHumanMediaPlayer: MediaPlayer? = null
    private var mComputerMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Inicializar SharedPreferences SIEMPRE
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE)

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

        // Restaurar marcadores desde SharedPreferences (persistente)
        mHumanWins = mPrefs.getInt("mHumanWins", 0)
        mComputerWins = mPrefs.getInt("mComputerWins", 0)
        mTies = mPrefs.getInt("mTies", 0)

        // Restaurar dificultad desde SharedPreferences
        val diffPref = mPrefs.getInt("mDifficulty", TicTacToeGame.DifficultyLevel.Expert.ordinal)
        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[diffPref])

        if (savedInstanceState == null) {
            // Primera vez
            startNewGame()
        } else {
            // Restaurar estado tras rotación
            mGame.setBoardState(savedInstanceState.getCharArray("board") ?: CharArray(9) { ' ' })
            mGameOver = savedInstanceState.getBoolean("mGameOver")
            mHumanStarts = savedInstanceState.getBoolean("mHumanStarts")
            mIsComputerTurn = savedInstanceState.getBoolean("mIsComputerTurn")
            mInfoTextView.text = savedInstanceState.getCharSequence("info")

            val diff = savedInstanceState.getInt("mDifficulty")
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[diff])

            mBoardView.invalidate()
            updateScores()

            // Si es turno de la máquina, relanzar su movimiento
            if (mIsComputerTurn && !mGameOver) {
                mHandler.postDelayed({
                    if (!mGameOver) {
                        val move = mGame.getComputerMove()
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                        val winner = mGame.checkForWinner()
                        handleWinner(winner)
                        mIsComputerTurn = false
                    }
                }, 1000L)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCharArray("board", mGame.getBoardState())
        outState.putBoolean("mGameOver", mGameOver)
        outState.putInt("mHumanWins", mHumanWins)
        outState.putInt("mComputerWins", mComputerWins)
        outState.putInt("mTies", mTies)
        outState.putCharSequence("info", mInfoTextView.text)
        outState.putBoolean("mHumanStarts", mHumanStarts)
        outState.putBoolean("mIsComputerTurn", mIsComputerTurn)   // ← NUEVO
        outState.putInt("mDifficulty", mGame.getDifficultyLevel().ordinal)
    }

    override fun onStop() {
        super.onStop()
        // Guardar marcadores y dificultad de forma persistente
        val ed = mPrefs.edit()
        ed.putInt("mHumanWins", mHumanWins)
        ed.putInt("mComputerWins", mComputerWins)
        ed.putInt("mTies", mTies)
        ed.putInt("mDifficulty", mGame.getDifficultyLevel().ordinal)
        ed.apply()
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
            mIsComputerTurn = false
        } else {
            mInfoTextView.setText(R.string.turn_computer)
            mIsComputerTurn = true
            mHandler.postDelayed({
                if (!mGameOver) {
                    val move = mGame.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    val winner = mGame.checkForWinner()
                    handleWinner(winner)
                    mIsComputerTurn = false
                }
            }, 1000L)
            mHumanStarts = true
        }
    }

    private val mTouchListener = View.OnTouchListener { v, event ->
        if (event.action != MotionEvent.ACTION_DOWN) return@OnTouchListener false

        v.performClick()
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col

        if (!mGameOver && !mIsComputerTurn && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
            mIsComputerTurn = true

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
                        mIsComputerTurn = false
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
        mBoardView.invalidate()

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

    // --- CICLO DE VIDA ---

    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.sword)
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.swish)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)   // ← Cancelar pendientes
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
            R.id.reset_scores -> {
                mHumanWins = 0
                mComputerWins = 0
                mTies = 0
                updateScores()
                val ed = mPrefs.edit()
                ed.putInt("mHumanWins", 0)
                ed.putInt("mComputerWins", 0)
                ed.putInt("mTies", 0)
                ed.apply()
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

    private fun showAboutDialog() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.about_dialog, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(layout)
            .setPositiveButton("OK", null)
        builder.create().show()
    }
}