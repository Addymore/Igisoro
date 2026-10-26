package com.example.igisoro

import kotlin.random.Random

enum class Player {
    PLAYER_1, // Bottom Player (South)
    PLAYER_2  // Top Player (North / AI or Player 2)
}

enum class GameMode {
    PVP,       // Player vs Player (Pass & Play)
    AI_EASY,   // Easy AI (Aboroheye)
    AI_MEDIUM, // Medium AI (Iringaniye)
    AI_HARD    // Hard AI (Umuhanga)
}

enum class GamePhase {
    IDLE,
    SOWING,
    CAPTURE_AVAILABLE,
    NDAKUBYE_PROMPT,
    GAME_OVER
}

enum class SowingSpeed(val delayMs: Long, val label: String) {
    NORMAL(210L, "Normal"),
    FAST(110L, "Fast"),
    INSTANT(45L, "Turbo")
}

data class PlayerStats(
    val totalSeeds: Int,
    val activePits: Int,
    val vulnerableColumns: Int
)

data class BoardState(
    // 4 rows, 8 columns
    val board: Array<IntArray> = Array(4) { IntArray(8) },
    var currentPlayer: Player = Player.PLAYER_1,
    var phase: GamePhase = GamePhase.IDLE,
    var handCount: Int = 0,
    var currentPitIndex: Int = -1,
    var currentLapStart: Int = -1,
    var captureColumn: Int = -1, // Column index of capture if CAPTURE_AVAILABLE
    var p1CompletedTurns: Int = 0,
    var p2CompletedTurns: Int = 0,
    var totalCapturesP1: Int = 0,
    var totalCapturesP2: Int = 0,
    var lastActionBanner: String? = null,
    var winner: Player? = null
) : java.io.Serializable {

    fun copy(): BoardState {
        val newBoard = Array(4) { board[it].clone() }
        return BoardState(
            board = newBoard,
            currentPlayer = currentPlayer,
            phase = phase,
            handCount = handCount,
            currentPitIndex = currentPitIndex,
            currentLapStart = currentLapStart,
            captureColumn = captureColumn,
            p1CompletedTurns = p1CompletedTurns,
            p2CompletedTurns = p2CompletedTurns,
            totalCapturesP1 = totalCapturesP1,
            totalCapturesP2 = totalCapturesP2,
            lastActionBanner = lastActionBanner,
            winner = winner
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoardState
        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (phase != other.phase) return false
        if (handCount != other.handCount) return false
        if (currentPitIndex != other.currentPitIndex) return false
        if (currentLapStart != other.currentLapStart) return false
        if (captureColumn != other.captureColumn) return false
        if (p1CompletedTurns != other.p1CompletedTurns) return false
        if (p2CompletedTurns != other.p2CompletedTurns) return false
        if (totalCapturesP1 != other.totalCapturesP1) return false
        if (totalCapturesP2 != other.totalCapturesP2) return false
        if (lastActionBanner != other.lastActionBanner) return false
        if (winner != other.winner) return false
        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + phase.hashCode()
        result = 31 * result + handCount
        result = 31 * result + currentPitIndex
        result = 31 * result + currentLapStart
        result = 31 * result + captureColumn
        result = 31 * result + p1CompletedTurns
        result = 31 * result + p2CompletedTurns
        result = 31 * result + totalCapturesP1
        result = 31 * result + totalCapturesP2
        result = 31 * result + (lastActionBanner?.hashCode() ?: 0)
        result = 31 * result + (winner?.hashCode() ?: 0)
        return result
    }
}

object GameLogic {

    // Initialize board with 4 seeds in each outer-row pit (traditional 32 seeds per player = 64 Inka)
    fun createInitialState(): BoardState {
        val state = BoardState()
        for (c in 0..7) {
            state.board[0][c] = 4 // Player 2 Outer Row
            state.board[1][c] = 0 // Player 2 Inner Row
            state.board[2][c] = 0 // Player 1 Inner Row
            state.board[3][c] = 4 // Player 1 Outer Row
        }
        return state
    }

    // Convert 1D pit index (0..15) to 2D board coordinates (Row, Column)
    // Counter-clockwise flow for both players from their own perspective:
    // P1 (bottom): Inner row left-to-right (0..7 -> row 2, col 0..7), Outer row right-to-left (8..15 -> row 3, col 7..0)
    // P2 (top): Inner row right-to-left (0..7 -> row 1, col 7..0), Outer row left-to-right (8..15 -> row 0, col 0..7)
    fun getCoords(player: Player, index: Int): Pair<Int, Int> {
        return if (player == Player.PLAYER_1) {
            if (index < 8) {
                Pair(2, index)
            } else {
                Pair(3, 15 - index)
            }
        } else {
            if (index < 8) {
                Pair(1, 7 - index)
            } else {
                Pair(0, index - 8)
            }
        }
    }

    // Map 2D board coordinates back to 1D pit index (0..15) for the owning player
    fun get1DIndexForRowCol(row: Int, col: Int): Int {
        return when (row) {
            2 -> col          // P1 Inner Row (0..7)
            3 -> 15 - col     // P1 Outer Row (8..15)
            1 -> 7 - col      // P2 Inner Row (0..7)
            0 -> col + 8      // P2 Outer Row (8..15)
            else -> -1
        }
    }

    // Compute live player stats for HUD display
    fun getPlayerStats(state: BoardState, player: Player): PlayerStats {
        val rows = if (player == Player.PLAYER_1) listOf(2, 3) else listOf(0, 1)
        val innerRow = if (player == Player.PLAYER_1) 2 else 1
        val outerRow = if (player == Player.PLAYER_1) 3 else 0

        var seeds = 0
        var active = 0
        var vulnerable = 0

        for (r in rows) {
            for (c in 0..7) {
                val count = state.board[r][c]
                seeds += count
                if (count >= 2) active++
            }
        }
        // Include seeds currently in hand if this player is sowing
        if (state.currentPlayer == player && state.phase != GamePhase.IDLE) {
            seeds += state.handCount
        }
        for (c in 0..7) {
            if (state.board[innerRow][c] > 0 && state.board[outerRow][c] > 0) {
                vulnerable++
            }
        }
        return PlayerStats(totalSeeds = seeds, activePits = active, vulnerableColumns = vulnerable)
    }

    // Check if a move is valid to start a turn
    fun isValidStartMove(state: BoardState, player: Player, pitIndex: Int): Boolean {
        if (state.currentPlayer != player) return false
        if (state.phase != GamePhase.IDLE) return false
        if (pitIndex !in 0..15) return false

        val coords = getCoords(player, pitIndex)
        val seeds = state.board[coords.first][coords.second]
        return seeds >= 2
    }

    // Initiate sowing for a chosen pit
    fun startSowing(state: BoardState, pitIndex: Int): BoardState {
        val newState = state.copy()
        val player = newState.currentPlayer
        val coords = getCoords(player, pitIndex)

        newState.handCount = newState.board[coords.first][coords.second]
        newState.board[coords.first][coords.second] = 0
        newState.currentPitIndex = pitIndex
        newState.currentLapStart = pitIndex
        newState.phase = GamePhase.SOWING
        newState.lastActionBanner = null

        return newState
    }

    // Check if the current landing pit is a valid capture position
    fun checkCaptureAvailable(state: BoardState, pitIndex: Int): Boolean {
        val player = state.currentPlayer
        // Capture can only occur in the inner row (indices 0..7)
        if (pitIndex !in 0..7) return false

        // Landed pit must already have been occupied before the last seed dropped (so now >= 2)
        val coords = getCoords(player, pitIndex)
        if (state.board[coords.first][coords.second] < 2) return false

        // Opponent's two opposite pits in the same column must BOTH be occupied
        val col = coords.second
        val opponentInnerRow = if (player == Player.PLAYER_1) 1 else 2
        val opponentOuterRow = if (player == Player.PLAYER_1) 0 else 3

        return state.board[opponentInnerRow][col] > 0 && state.board[opponentOuterRow][col] > 0
    }

    // Perform capture (Kurasa)
    fun captureOpponentSeeds(state: BoardState, pitIndex: Int): BoardState {
        val newState = state.copy()
        val player = newState.currentPlayer
        val coords = getCoords(player, pitIndex)
        val col = coords.second

        val opponentInnerRow = if (player == Player.PLAYER_1) 1 else 2
        val opponentOuterRow = if (player == Player.PLAYER_1) 0 else 3

        val captured = newState.board[opponentInnerRow][col] + newState.board[opponentOuterRow][col]
        newState.board[opponentInnerRow][col] = 0
        newState.board[opponentOuterRow][col] = 0

        if (player == Player.PLAYER_1) {
            newState.totalCapturesP1 += captured
        } else {
            newState.totalCapturesP2 += captured
        }

        // Captured seeds are placed into hand and sown starting from the pit where the capturing lap began
        newState.handCount += captured
        newState.currentPitIndex = newState.currentLapStart
        newState.phase = GamePhase.SOWING
        newState.captureColumn = -1
        newState.lastActionBanner = "Kurasa! +$captured Inka captured"
        return newState
    }

    // Implement "Ndakubye" (Retreat) rule when player passes capture (Ndahise)
    fun retreatOpponentSeeds(state: BoardState, col: Int): BoardState {
        val newState = state.copy()
        val opponent = if (newState.currentPlayer == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1

        val innerRow = if (opponent == Player.PLAYER_1) 2 else 1
        val outerRow = if (opponent == Player.PLAYER_1) 3 else 0

        val innerSeeds = newState.board[innerRow][col]
        if (innerSeeds > 0) {
            newState.board[outerRow][col] += innerSeeds
            newState.board[innerRow][col] = 0
            newState.lastActionBanner = "Ndahise -> Ndakubye! Column ${col + 1} retreated"
        }
        return newState
    }

    // Advance sowing by 1 step (1 seed dropped)
    fun sowOneStep(state: BoardState): BoardState {
        if (state.phase != GamePhase.SOWING || state.handCount <= 0) return state

        val newState = state.copy()
        val player = newState.currentPlayer

        // Move to next pit counter-clockwise
        newState.currentPitIndex = (newState.currentPitIndex + 1) % 16
        val coords = getCoords(player, newState.currentPitIndex)

        // Drop seed
        newState.board[coords.first][coords.second] += 1
        newState.handCount -= 1

        // Check if hand is empty
        if (newState.handCount == 0) {
            val cellSeeds = newState.board[coords.first][coords.second]
            if (cellSeeds > 1) {
                // Landed in an occupied pit: check if capture is available
                if (checkCaptureAvailable(newState, newState.currentPitIndex)) {
                    newState.phase = GamePhase.CAPTURE_AVAILABLE
                    newState.captureColumn = coords.second
                } else {
                    // Relay sow (Gukomeza): pick up all seeds in this pit and continue
                    newState.handCount = cellSeeds
                    newState.board[coords.first][coords.second] = 0
                    newState.currentLapStart = newState.currentPitIndex
                }
            } else {
                // Landed in an empty pit (now has 1 seed): turn ends
                newState.phase = GamePhase.IDLE
                newState.currentPitIndex = -1
                newState.currentLapStart = -1
                newState.captureColumn = -1

                if (player == Player.PLAYER_1) {
                    newState.p1CompletedTurns += 1
                } else {
                    newState.p2CompletedTurns += 1
                }

                // Toggle player turn
                newState.currentPlayer = if (player == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1

                // Check if the game is over (next player has no legal moves)
                if (!hasLegalMoves(newState, newState.currentPlayer)) {
                    newState.phase = GamePhase.GAME_OVER
                    newState.winner = player
                }
            }
        }

        return newState
    }

    // Check if player has any moves (at least one pit with >= 2 seeds)
    fun hasLegalMoves(state: BoardState, player: Player): Boolean {
        for (i in 0..15) {
            val coords = getCoords(player, i)
            if (state.board[coords.first][coords.second] >= 2) {
                return true
            }
        }
        return false
    }

    // Simulation of a full turn (used by AI for minimax evaluation, with safety step limit for infinite loops)
    fun simulateFullTurn(state: BoardState, startPit: Int): BoardState {
        var tempState = startSowing(state, startPit)
        var steps = 0
        val maxSteps = 250 // Guard against rare infinite relay cycles

        while ((tempState.phase == GamePhase.SOWING || tempState.phase == GamePhase.CAPTURE_AVAILABLE) && steps < maxSteps) {
            steps++
            tempState = if (tempState.phase == GamePhase.CAPTURE_AVAILABLE) {
                captureOpponentSeeds(tempState, tempState.currentPitIndex)
            } else {
                sowOneStep(tempState)
            }
        }

        if (steps >= maxSteps && tempState.phase != GamePhase.GAME_OVER) {
            tempState.phase = GamePhase.IDLE
            tempState.currentPlayer = if (state.currentPlayer == Player.PLAYER_1) Player.PLAYER_2 else Player.PLAYER_1
        }
        return tempState
    }

    // Heuristic evaluation function for Minimax AI
    private fun evaluateBoard(state: BoardState, aiPlayer: Player): Int {
        var aiSeeds = 0
        var oppSeeds = 0
        var aiActivePits = 0
        var oppActivePits = 0
        var aiVulnerableCols = 0
        var oppVulnerableCols = 0

        for (r in 0..3) {
            for (c in 0..7) {
                val seeds = state.board[r][c]
                val belongsToAi = (aiPlayer == Player.PLAYER_1 && r in 2..3) || (aiPlayer == Player.PLAYER_2 && r in 0..1)
                if (belongsToAi) {
                    aiSeeds += seeds
                    if (seeds >= 2) aiActivePits++
                } else {
                    oppSeeds += seeds
                    if (seeds >= 2) oppActivePits++
                }
            }
        }

        val aiInner = if (aiPlayer == Player.PLAYER_1) 2 else 1
        val aiOuter = if (aiPlayer == Player.PLAYER_1) 3 else 0
        val oppInner = if (aiPlayer == Player.PLAYER_1) 1 else 2
        val oppOuter = if (aiPlayer == Player.PLAYER_1) 0 else 3

        for (c in 0..7) {
            if (state.board[aiInner][c] > 0 && state.board[aiOuter][c] > 0) {
                aiVulnerableCols += state.board[aiInner][c] + state.board[aiOuter][c]
            }
            if (state.board[oppInner][c] > 0 && state.board[oppOuter][c] > 0) {
                oppVulnerableCols += state.board[oppInner][c] + state.board[oppOuter][c]
            }
        }

        val seedDiff = aiSeeds - oppSeeds
        val pitDiff = aiActivePits - oppActivePits
        val vulnerabilityDiff = oppVulnerableCols - aiVulnerableCols

        return seedDiff * 12 + pitDiff * 18 + vulnerabilityDiff * 4
    }

    // Minimax AI with Alpha-Beta Pruning
    fun getBestMove(state: BoardState, difficulty: GameMode): Int {
        val aiPlayer = state.currentPlayer
        val validPits = (0..15).filter { isValidStartMove(state, aiPlayer, it) }

        if (validPits.isEmpty()) return -1

        val depth = when (difficulty) {
            GameMode.AI_EASY -> 1
            GameMode.AI_MEDIUM -> 2
            GameMode.AI_HARD -> 4
            else -> 2
        }

        // Easy AI occasionally makes a casual move
        if (difficulty == GameMode.AI_EASY && Random.nextFloat() < 0.35f) {
            return validPits.random()
        }

        var bestScore = Int.MIN_VALUE
        var bestPit = validPits.first()

        for (pit in validPits) {
            val simulatedState = simulateFullTurn(state, pit)
            val score = minimax(simulatedState, depth - 1, false, aiPlayer, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestPit = pit
            }
        }

        return bestPit
    }

    private fun minimax(
        state: BoardState,
        depth: Int,
        isMaximizing: Boolean,
        aiPlayer: Player,
        alpha: Int,
        beta: Int
    ): Int {
        if (depth == 0 || state.phase == GamePhase.GAME_OVER) {
            return evaluateBoard(state, aiPlayer)
        }

        val currentPlayer = state.currentPlayer
        val validPits = (0..15).filter { isValidStartMove(state, currentPlayer, it) }

        if (validPits.isEmpty()) {
            return if (currentPlayer == aiPlayer) Int.MIN_VALUE + 500 else Int.MAX_VALUE - 500
        }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (pit in validPits) {
                val nextState = simulateFullTurn(state, pit)
                val eval = minimax(nextState, depth - 1, false, aiPlayer, currentAlpha, currentBeta)
                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (pit in validPits) {
                val nextState = simulateFullTurn(state, pit)
                val eval = minimax(nextState, depth - 1, true, aiPlayer, currentAlpha, currentBeta)
                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }
}
