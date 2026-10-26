package com.example.igisoro.ui.main

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igisoro.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    LOBBY,
    GAME,
    RULES,
    SETTINGS
}

class MainScreenViewModel : ViewModel() {

    private val _screen = MutableStateFlow(AppScreen.LOBBY)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _gameState = MutableStateFlow(GameLogic.createInitialState())
    val gameState: StateFlow<BoardState> = _gameState.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    // Settings
    val gameMode = MutableStateFlow(GameMode.AI_MEDIUM)
    val soundEnabled = MutableStateFlow(true)
    val rotateBoardEnabled = MutableStateFlow(true)
    val traditionalRulesEnabled = MutableStateFlow(true)
    val sowingSpeed = MutableStateFlow(SowingSpeed.NORMAL)

    // Undo history
    private val history = mutableListOf<BoardState>()

    // Active animation job so starting a new game or undoing cancels any in-flight coroutine cleanly
    private var activeTurnJob: Job? = null

    // Audio tone generator for synthesized wood & seed clicks
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (_: Exception) {
            // Audio unavailable in headless environments
        }
    }

    private fun refreshUndoState() {
        _canUndo.value = history.isNotEmpty() && _gameState.value.phase == GamePhase.IDLE
    }

    fun playClickSound() {
        if (soundEnabled.value) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 32)
            } catch (_: Exception) {}
        }
    }

    fun playCaptureSound() {
        if (soundEnabled.value) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 110)
            } catch (_: Exception) {}
        }
    }

    fun navigateTo(appScreen: AppScreen) {
        _screen.value = appScreen
    }

    fun startNewGame() {
        activeTurnJob?.cancel()
        _gameState.value = GameLogic.createInitialState()
        history.clear()
        refreshUndoState()
        _screen.value = AppScreen.GAME
        checkAiTurn()
    }

    private fun saveToHistory() {
        history.add(_gameState.value.copy())
        if (history.size > 30) {
            history.removeAt(0)
        }
        refreshUndoState()
    }

    fun undoLastMove() {
        if (history.isNotEmpty() && _gameState.value.phase == GamePhase.IDLE) {
            activeTurnJob?.cancel()
            _gameState.value = history.removeAt(history.size - 1)
            refreshUndoState()
        }
    }

    fun selectPit(index: Int) {
        val state = _gameState.value
        if (state.phase != GamePhase.IDLE) return
        if (state.currentPlayer == Player.PLAYER_2 && gameMode.value != GameMode.PVP) return
        if (!GameLogic.isValidStartMove(state, state.currentPlayer, index)) return

        saveToHistory()

        activeTurnJob?.cancel()
        activeTurnJob = viewModelScope.launch {
            animateTurn(index)
        }
    }

    private suspend fun animateTurn(startPit: Int) {
        val stepDelay = sowingSpeed.value.delayMs
        var state = GameLogic.startSowing(_gameState.value, startPit)
        _gameState.value = state
        refreshUndoState()
        playClickSound()
        delay(stepDelay)

        while (state.phase == GamePhase.SOWING || state.phase == GamePhase.CAPTURE_AVAILABLE) {
            if (state.phase == GamePhase.CAPTURE_AVAILABLE) {
                val isAiTurn = state.currentPlayer == Player.PLAYER_2 && gameMode.value != GameMode.PVP
                if (isAiTurn || !traditionalRulesEnabled.value) {
                    // AI or standard mandatory-capture mode captures automatically
                    delay(stepDelay + 90L)
                    state = GameLogic.captureOpponentSeeds(state, state.currentPitIndex)
                    _gameState.value = state
                    playCaptureSound()
                    delay(stepDelay + 60L)
                } else {
                    // Wait for human player to choose Kurasa (Capture) or Ndahise (Pass)
                    return
                }
            } else {
                state = GameLogic.sowOneStep(state)
                _gameState.value = state
                playClickSound()
                delay(sowingSpeed.value.delayMs)
            }
        }

        refreshUndoState()
        if (state.phase == GamePhase.IDLE) {
            checkAiTurn()
        }
    }

    fun chooseCapture() {
        val state = _gameState.value
        if (state.phase != GamePhase.CAPTURE_AVAILABLE) return

        activeTurnJob?.cancel()
        activeTurnJob = viewModelScope.launch {
            val nextState = GameLogic.captureOpponentSeeds(state, state.currentPitIndex)
            _gameState.value = nextState
            playCaptureSound()
            delay(sowingSpeed.value.delayMs + 60L)
            resumeSowingLoop()
        }
    }

    fun choosePass() {
        var state = _gameState.value
        if (state.phase != GamePhase.CAPTURE_AVAILABLE) return

        activeTurnJob?.cancel()
        activeTurnJob = viewModelScope.launch {
            if (traditionalRulesEnabled.value) {
                val col = state.captureColumn
                if (col != -1) {
                    state = GameLogic.retreatOpponentSeeds(state, col)
                    _gameState.value = state
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 95)
                    } catch (_: Exception) {}
                    delay(360)
                }
            }

            // Continue relay sowing from the pit where the player landed
            val nextState = state.copy()
            val coords = GameLogic.getCoords(nextState.currentPlayer, nextState.currentPitIndex)
            val cellSeeds = nextState.board[coords.first][coords.second]

            nextState.handCount = cellSeeds
            nextState.board[coords.first][coords.second] = 0
            nextState.currentLapStart = nextState.currentPitIndex
            nextState.phase = GamePhase.SOWING
            nextState.captureColumn = -1

            _gameState.value = nextState
            delay(sowingSpeed.value.delayMs)
            resumeSowingLoop()
        }
    }

    private suspend fun resumeSowingLoop() {
        var state = _gameState.value
        while (state.phase == GamePhase.SOWING || state.phase == GamePhase.CAPTURE_AVAILABLE) {
            if (state.phase == GamePhase.CAPTURE_AVAILABLE) {
                val isAiTurn = state.currentPlayer == Player.PLAYER_2 && gameMode.value != GameMode.PVP
                if (isAiTurn || !traditionalRulesEnabled.value) {
                    delay(sowingSpeed.value.delayMs + 90L)
                    state = GameLogic.captureOpponentSeeds(state, state.currentPitIndex)
                    _gameState.value = state
                    playCaptureSound()
                    delay(sowingSpeed.value.delayMs + 60L)
                } else {
                    return
                }
            } else {
                state = GameLogic.sowOneStep(state)
                _gameState.value = state
                playClickSound()
                delay(sowingSpeed.value.delayMs)
            }
        }

        refreshUndoState()
        if (state.phase == GamePhase.IDLE) {
            checkAiTurn()
        }
    }

    private fun checkAiTurn() {
        val state = _gameState.value
        if (state.phase == GamePhase.IDLE && state.currentPlayer == Player.PLAYER_2 && gameMode.value != GameMode.PVP) {
            activeTurnJob?.cancel()
            activeTurnJob = viewModelScope.launch {
                delay(650)
                val bestMove = GameLogic.getBestMove(state, gameMode.value)
                if (bestMove != -1) {
                    animateTurn(bestMove)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeTurnJob?.cancel()
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
