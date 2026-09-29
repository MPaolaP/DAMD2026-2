package com.example.androidtic_tac_toe

import java.util.Random

class TicTacToeGame {

    // Niveles de dificultad
    enum class DifficultyLevel { Easy, Harder, Expert }

    // Nivel actual (por defecto Expert)
    private var mDifficultyLevel = DifficultyLevel.Expert

    private val mBoard = CharArray(BOARD_SIZE)
    private val mRand = Random()

    init {
        clearBoard()
    }

    fun clearBoard() {
        for (i in 0 until BOARD_SIZE) {
            mBoard[i] = OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int) {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player
        }
    }

    // Getters y setters para el nivel de dificultad
    fun getDifficultyLevel(): DifficultyLevel = mDifficultyLevel

    fun setDifficultyLevel(difficultyLevel: DifficultyLevel) {
        mDifficultyLevel = difficultyLevel
    }

    /**
     * Devuelve el mejor movimiento según el nivel de dificultad.
     * NO modifica el tablero (los métodos auxiliares lo restauran).
     */
    fun getComputerMove(): Int {
        var move = -1
        when (mDifficultyLevel) {
            DifficultyLevel.Easy -> {
                move = getRandomMove()
            }
            DifficultyLevel.Harder -> {
                move = getWinningMove()
                if (move == -1) move = getRandomMove()
            }
            DifficultyLevel.Expert -> {
                move = getWinningMove()
                if (move == -1) move = getBlockingMove()
                if (move == -1) move = getRandomMove()
            }
        }
        return move
    }

    /** Devuelve una casilla vacía aleatoria (0-8). */
    private fun getRandomMove(): Int {
        var move: Int
        do {
            move = mRand.nextInt(BOARD_SIZE)
        } while (mBoard[move] != OPEN_SPOT)
        return move
    }

    /**
     * Si la máquina puede ganar en un movimiento, devuelve esa casilla.
     * Si no, devuelve -1.
     */
    private fun getWinningMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                val curr = mBoard[i]
                mBoard[i] = COMPUTER_PLAYER
                if (checkForWinner() == 3) {
                    mBoard[i] = curr  // Restaurar
                    return i
                } else {
                    mBoard[i] = curr  // Restaurar
                }
            }
        }
        return -1
    }

    /**
     * Si el humano puede ganar en un movimiento, la máquina lo bloquea.
     * Si no, devuelve -1.
     */
    private fun getBlockingMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                val curr = mBoard[i]
                mBoard[i] = HUMAN_PLAYER
                if (checkForWinner() == 2) {
                    mBoard[i] = curr  // Restaurar
                    return i
                } else {
                    mBoard[i] = curr  // Restaurar
                }
            }
        }
        return -1
    }

    fun checkForWinner(): Int {
        // Horizontales
        for (i in 0..6 step 3) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 1] == HUMAN_PLAYER && mBoard[i + 2] == HUMAN_PLAYER) return 2
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i + 1] == COMPUTER_PLAYER && mBoard[i + 2] == COMPUTER_PLAYER) return 3
        }
        // Verticales
        for (i in 0..2) {
            if (mBoard[i] == HUMAN_PLAYER && mBoard[i + 3] == HUMAN_PLAYER && mBoard[i + 6] == HUMAN_PLAYER) return 2
            if (mBoard[i] == COMPUTER_PLAYER && mBoard[i + 3] == COMPUTER_PLAYER && mBoard[i + 6] == COMPUTER_PLAYER) return 3
        }
        // Diagonales
        if ((mBoard[0] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[8] == HUMAN_PLAYER) ||
            (mBoard[2] == HUMAN_PLAYER && mBoard[4] == HUMAN_PLAYER && mBoard[6] == HUMAN_PLAYER)) return 2
        if ((mBoard[0] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[8] == COMPUTER_PLAYER) ||
            (mBoard[2] == COMPUTER_PLAYER && mBoard[4] == COMPUTER_PLAYER && mBoard[6] == COMPUTER_PLAYER)) return 3
        // Empate
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) return 0
        }
        return 1
    }

    companion object {
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }
}