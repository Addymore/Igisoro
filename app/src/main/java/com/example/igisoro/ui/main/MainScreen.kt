package com.example.igisoro.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.igisoro.*
import com.example.igisoro.R
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    onItemClick: (androidx.navigation3.runtime.NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val currentScreen by viewModel.screen.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val gameMode by viewModel.gameMode.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val rotateBoardEnabled by viewModel.rotateBoardEnabled.collectAsStateWithLifecycle()
    val traditionalRulesEnabled by viewModel.traditionalRulesEnabled.collectAsStateWithLifecycle()
    val sowingSpeed by viewModel.sowingSpeed.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ImigongoColors.AbsoluteBlack)
    ) {
        // Subtle traditional Imigongo geometric background across all screens
        ImigongoBackground(modifier = Modifier.fillMaxSize())

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.LOBBY -> LobbyScreen(
                    viewModel = viewModel,
                    gameMode = gameMode
                )
                AppScreen.GAME -> GameScreen(
                    viewModel = viewModel,
                    gameState = gameState,
                    canUndo = canUndo,
                    rotateBoardEnabled = rotateBoardEnabled,
                    gameMode = gameMode
                )
                AppScreen.RULES -> RulesScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    soundEnabled = soundEnabled,
                    rotateBoardEnabled = rotateBoardEnabled,
                    traditionalRulesEnabled = traditionalRulesEnabled,
                    sowingSpeed = sowingSpeed
                )
            }
        }
    }
}

@Composable
fun LobbyScreen(
    viewModel: MainScreenViewModel,
    gameMode: GameMode
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Hero Section with Imigongo Shield Emblem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            ImigongoEmblem(
                modifier = Modifier
                    .size(104.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.displayLarge,
                color = ImigongoColors.MustardGold,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = ImigongoColors.MutedIvory,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                ImigongoBorder(modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Rwandan Proverb Showcase Card
        ProverbCarouselCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Mode & Difficulty Selection Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ImigongoColors.SurfaceDark.copy(alpha = 0.92f)
            ),
            border = BorderStroke(1.dp, ImigongoColors.MustardGold.copy(alpha = 0.28f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "CHOOSE GAME MODE",
                    style = MaterialTheme.typography.labelLarge,
                    color = ImigongoColors.MustardGold,
                    letterSpacing = 1.5.sp
                )

                // Segmented Mode Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isAiMode = gameMode != GameMode.PVP
                    ModeOptionCard(
                        title = "Vs Umuhanga",
                        subtitle = "Single Player AI",
                        selected = isAiMode,
                        onClick = {
                            if (!isAiMode) viewModel.gameMode.value = GameMode.AI_MEDIUM
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeOptionCard(
                        title = "Pass & Play",
                        subtitle = "2 Players Local",
                        selected = !isAiMode,
                        onClick = { viewModel.gameMode.value = GameMode.PVP },
                        modifier = Modifier.weight(1f)
                    )
                }

                // AI Difficulty Selector Pills
                AnimatedVisibility(
                    visible = gameMode != GameMode.PVP,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(
                            color = ImigongoColors.MustardGold.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = stringResource(R.string.difficulty_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ImigongoColors.MutedIvory,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(GameMode.AI_EASY, "Easy", "Aboroheye"),
                                Triple(GameMode.AI_MEDIUM, "Medium", "Iringaniye"),
                                Triple(GameMode.AI_HARD, "Master", "Umuhanga")
                            ).forEach { (mode, eng, kin) ->
                                val selected = gameMode == mode
                                Surface(
                                    onClick = { viewModel.gameMode.value = mode },
                                    color = if (selected) ImigongoColors.RedOchre else Color(0xFF231D19),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) ImigongoColors.MustardGoldLight else Color.White.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = eng,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ImigongoColors.CreamWhite
                                        )
                                        Text(
                                            text = kin,
                                            fontSize = 10.sp,
                                            color = if (selected) ImigongoColors.MustardGoldLight else ImigongoColors.MutedIvory
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary CTA Button
                Button(
                    onClick = { viewModel.startNewGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImigongoColors.MustardGold,
                        contentColor = ImigongoColors.AbsoluteBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_play),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Secondary Actions (Rules & Settings)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.RULES) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        border = BorderStroke(1.dp, ImigongoColors.MustardGold.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImigongoColors.CreamWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ImigongoColors.MustardGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Amategeko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        border = BorderStroke(1.dp, ImigongoColors.MustardGold.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImigongoColors.CreamWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ImigongoColors.MustardGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Rwandan Cultural Heritage • Imigongo Art Edition",
            fontSize = 11.sp,
            color = ImigongoColors.MutedIvory.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) ImigongoColors.RedOchre.copy(alpha = 0.22f) else Color(0xFF1F1915),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) ImigongoColors.MustardGold else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) ImigongoColors.MustardGoldLight else ImigongoColors.CreamWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ImigongoColors.MutedIvory
            )
        }
    }
}

@Composable
private fun ProverbCarouselCard() {
    val proverbs = listOf(
        Pair("“Ubwenge burarahurwa.”", "Wisdom is kindled from one mind to another."),
        Pair("“Uhagaraye amaguru abiri abona kure.”", "One who plans with foresight sees far ahead."),
        Pair("“Umutwe umwe ntiwigira inama.”", "A single head alone cannot take full counsel.")
    )
    var idx by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5500)
            idx = (idx + 1) % proverbs.size
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1410).copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, ImigongoColors.MustardGold.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = proverbs[idx].first,
                color = ImigongoColors.MustardGoldLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = proverbs[idx].second,
                color = ImigongoColors.CreamWhite.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                proverbs.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(width = if (i == idx) 16.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == idx) ImigongoColors.MustardGold else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    viewModel: MainScreenViewModel,
    gameState: BoardState,
    canUndo: Boolean,
    rotateBoardEnabled: Boolean,
    gameMode: GameMode
) {
    val p1Stats = remember(gameState) { GameLogic.getPlayerStats(gameState, Player.PLAYER_1) }
    val p2Stats = remember(gameState) { GameLogic.getPlayerStats(gameState, Player.PLAYER_2) }

    val shouldRotate = rotateBoardEnabled && gameMode == GameMode.PVP && gameState.currentPlayer == Player.PLAYER_2
    val rotationAngle by animateFloatAsState(
        targetValue = if (shouldRotate) 180f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "BoardRotation"
    )

    val opponentName = when (gameMode) {
        GameMode.PVP -> "Umukinnyi wa 2 (North)"
        GameMode.AI_EASY -> "AI • Aboroheye (Easy)"
        GameMode.AI_MEDIUM -> "AI • Iringaniye (Medium)"
        GameMode.AI_HARD -> "AI • Umuhanga (Master)"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD BAR: Navigation & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { viewModel.navigateTo(AppScreen.LOBBY) },
                color = ImigongoColors.SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ImigongoColors.MustardGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Lobby",
                        tint = ImigongoColors.MustardGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lobby",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImigongoColors.CreamWhite
                    )
                }
            }

            // Territory Seed Ratio Pill
            TerritoryBalanceBar(
                p1Seeds = p1Stats.totalSeeds,
                p2Seeds = p2Stats.totalSeeds,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.RULES) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ImigongoColors.SurfaceDark)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rules",
                        tint = ImigongoColors.CreamWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.startNewGame() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ImigongoColors.RedOchre.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Match",
                        tint = ImigongoColors.CreamWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // TOP PLAYER HUD CARD (Player 2 / AI)
        PlayerHudCard(
            name = opponentName,
            stats = p2Stats,
            isCurrentTurn = gameState.currentPlayer == Player.PLAYER_2,
            isThinkingAi = gameState.currentPlayer == Player.PLAYER_2 && gameMode != GameMode.PVP && gameState.phase == GamePhase.IDLE,
            accentColor = ImigongoColors.RedOchre
        )

        // STATUS / SOWING TELEMETRY BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            contentAlignment = Alignment.Center
        ) {
            val statusText = when {
                gameState.phase == GamePhase.SOWING ->
                    "Kubiba (Sowing) • ${stringResource(R.string.seeds_in_hand, gameState.handCount)}"
                !gameState.lastActionBanner.isNullOrBlank() ->
                    gameState.lastActionBanner!!
                else ->
                    "Select any glowing pit with 2+ Inka to sow counter-clockwise"
            }
            val isSowing = gameState.phase == GamePhase.SOWING
            Surface(
                color = if (isSowing) ImigongoColors.RedOchre else ImigongoColors.SurfaceDark.copy(alpha = 0.9f),
                shape = RoundedCornerShape(19.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSowing) ImigongoColors.MustardGoldLight else ImigongoColors.MustardGold.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = if (isSowing) FontWeight.Bold else FontWeight.Medium,
                    color = ImigongoColors.CreamWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // CENTER: Carved Wooden Igisoro Board (4 rows x 8 columns)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(rotationAngle),
            contentAlignment = Alignment.Center
        ) {
            CarvedIgisoroBoard(
                viewModel = viewModel,
                gameState = gameState,
                gameMode = gameMode
            )
        }

        // BOTTOM PLAYER HUD CARD (Player 1)
        PlayerHudCard(
            name = stringResource(R.string.turn_p1),
            stats = p1Stats,
            isCurrentTurn = gameState.currentPlayer == Player.PLAYER_1,
            isThinkingAi = false,
            accentColor = ImigongoColors.MustardGold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM ACTION BAR: Undo & Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.undoLastMove() },
                enabled = canUndo,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (canUndo) ImigongoColors.MustardGold else Color.White.copy(alpha = 0.15f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ImigongoColors.MustardGoldLight,
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = stringResource(R.string.btn_undo),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ImigongoColors.CreamWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ImigongoColors.MustardGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Settings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Capture Opportunity Dialog (Kurasa vs Ndahise)
    if (gameState.phase == GamePhase.CAPTURE_AVAILABLE) {
        CaptureOpportunityDialog(
            gameState = gameState,
            onCapture = { viewModel.chooseCapture() },
            onPass = { viewModel.choosePass() }
        )
    }

    // Victory / Game Over Modal
    if (gameState.phase == GamePhase.GAME_OVER) {
        VictoryDialog(
            gameState = gameState,
            gameMode = gameMode,
            p1Stats = p1Stats,
            p2Stats = p2Stats,
            onPlayAgain = { viewModel.startNewGame() },
            onReturnLobby = { viewModel.navigateTo(AppScreen.LOBBY) }
        )
    }
}

@Composable
private fun TerritoryBalanceBar(
    p1Seeds: Int,
    p2Seeds: Int,
    modifier: Modifier = Modifier
) {
    val total = (p1Seeds + p2Seeds).coerceAtLeast(1)
    val p1Fraction by animateFloatAsState(
        targetValue = (p1Seeds.toFloat() / total).coerceIn(0.08f, 0.92f),
        animationSpec = tween(350),
        label = "SeedBalance"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "P1: $p1Seeds Inka",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImigongoColors.MustardGoldLight
            )
            Text(
                text = "P2: $p2Seeds Inka",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImigongoColors.RedOchre
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ImigongoColors.RedOchre)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(p1Fraction)
                    .background(ImigongoColors.MustardGold)
            )
        }
    }
}

@Composable
private fun PlayerHudCard(
    name: String,
    stats: PlayerStats,
    isCurrentTurn: Boolean,
    isThinkingAi: Boolean,
    accentColor: Color
) {
    Surface(
        color = if (isCurrentTurn) Color(0xFF251D17) else ImigongoColors.SurfaceDark.copy(alpha = 0.75f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isCurrentTurn) 1.8.dp else 1.dp,
            color = if (isCurrentTurn) accentColor else Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentTurn) accentColor else Color.Gray.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentTurn) ImigongoColors.CreamWhite else ImigongoColors.MutedIvory
                    )
                    Text(
                        text = when {
                            isThinkingAi -> "Calculating strategic move…"
                            isCurrentTurn -> "Active Turn • Choose a pit"
                            else -> "Waiting…"
                        },
                        fontSize = 11.sp,
                        color = if (isCurrentTurn) ImigongoColors.MustardGoldLight else ImigongoColors.MutedIvory.copy(alpha = 0.65f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(label = "INKA", value = stats.totalSeeds.toString(), highlight = isCurrentTurn)
                StatPill(label = "MOVES", value = stats.activePits.toString(), highlight = false)
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    highlight: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) ImigongoColors.MustardGoldLight else ImigongoColors.CreamWhite
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = ImigongoColors.MutedIvory
        )
    }
}

@Composable
fun CarvedIgisoroBoard(
    viewModel: MainScreenViewModel,
    gameState: BoardState,
    gameMode: GameMode
) {
    val isP2HumanInteractive =
        gameState.currentPlayer == Player.PLAYER_2 &&
            gameState.phase == GamePhase.IDLE &&
            gameMode == GameMode.PVP

    val isP1HumanInteractive =
        gameState.currentPlayer == Player.PLAYER_1 &&
            gameState.phase == GamePhase.IDLE

    Card(
        colors = CardDefaults.cardColors(containerColor = ImigongoColors.WoodCarvedBase),
        border = BorderStroke(2.5.dp, ImigongoColors.MustardGold.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(16.dp, RoundedCornerShape(26.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E2016),
                            ImigongoColors.WoodCarvedBase,
                            Color(0xFF1E140D),
                            ImigongoColors.WoodCarvedBase,
                            Color(0xFF2E2016)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Territory directional hint for North (Player 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NORTH OUTER ROW  ⟶",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImigongoColors.MutedIvory.copy(alpha = 0.55f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "⟵  INNER ROW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImigongoColors.MutedIvory.copy(alpha = 0.55f),
                    letterSpacing = 1.sp
                )
            }

            // Row 0: Player 2 Outer Row
            BoardRow(
                viewModel = viewModel,
                gameState = gameState,
                row = 0,
                isInteractive = isP2HumanInteractive
            )

            // Row 1: Player 2 Inner Row
            BoardRow(
                viewModel = viewModel,
                gameState = gameState,
                row = 1,
                isInteractive = isP2HumanInteractive
            )

            // Center Carved Spine with Imigongo Frieze & Column Numbers (1..8)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    ImigongoBorder(modifier = Modifier.fillMaxSize())
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 1..8) {
                        Text(
                            text = col.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImigongoColors.MustardGold.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Row 2: Player 1 Inner Row
            BoardRow(
                viewModel = viewModel,
                gameState = gameState,
                row = 2,
                isInteractive = isP1HumanInteractive
            )

            // Row 3: Player 1 Outer Row
            BoardRow(
                viewModel = viewModel,
                gameState = gameState,
                row = 3,
                isInteractive = isP1HumanInteractive
            )

            // Territory directional hint for South (Player 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "INNER ROW  ⟶",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImigongoColors.MutedIvory.copy(alpha = 0.55f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "⟵  SOUTH OUTER ROW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImigongoColors.MutedIvory.copy(alpha = 0.55f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun BoardRow(
    viewModel: MainScreenViewModel,
    gameState: BoardState,
    row: Int,
    isInteractive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (col in 0..7) {
            val seeds = gameState.board[row][col]
            val pitIndex = GameLogic.get1DIndexForRowCol(row, col)

            val isCurrentSowingPit =
                (gameState.phase == GamePhase.SOWING || gameState.phase == GamePhase.CAPTURE_AVAILABLE) &&
                    gameState.currentPitIndex != -1 &&
                    GameLogic.getCoords(gameState.currentPlayer, gameState.currentPitIndex) == Pair(row, col)

            // Highlight opponent pits that are targeted during a capture prompt
            val isCaptureTarget =
                gameState.phase == GamePhase.CAPTURE_AVAILABLE &&
                    gameState.captureColumn == col &&
                    ((gameState.currentPlayer == Player.PLAYER_1 && row in 0..1) ||
                        (gameState.currentPlayer == Player.PLAYER_2 && row in 2..3))

            val canSelect = isInteractive && seeds >= 2

            PitView(
                seeds = seeds,
                isPlayable = canSelect,
                isHighlighted = isCurrentSowingPit,
                isCaptureTarget = isCaptureTarget,
                isInnerRow = row == 1 || row == 2,
                onClick = {
                    if (canSelect) {
                        viewModel.selectPit(pitIndex)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PitView(
    seeds: Int,
    isPlayable: Boolean,
    isHighlighted: Boolean,
    isCaptureTarget: Boolean,
    isInnerRow: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isHighlighted -> 1.12f
            isCaptureTarget -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PitScale"
    )

    val borderColor = when {
        isHighlighted -> ImigongoColors.MustardGoldLight
        isCaptureTarget -> ImigongoColors.RedOchre
        isPlayable -> ImigongoColors.MustardGold.copy(alpha = 0.85f)
        isInnerRow -> Color(0xFF5A4332).copy(alpha = 0.6f)
        else -> Color(0xFF473528).copy(alpha = 0.45f)
    }

    val borderWidth = when {
        isHighlighted || isCaptureTarget -> 2.5.dp
        isPlayable -> 1.6.dp
        else -> 1.dp
    }

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(enabled = isPlayable, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carved Wooden Pit Bowl with 3D Inka Seeds inside
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .shadow(if (isPlayable || isHighlighted) 6.dp else 2.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when {
                            isHighlighted -> listOf(Color(0xFF6B2418), Color(0xFF2E0D08))
                            isCaptureTarget -> listOf(Color(0xFF7A1E1E), Color(0xFF330A0A))
                            isPlayable -> listOf(Color(0xFF3B281B), ImigongoColors.PitDeepShadow)
                            else -> listOf(Color(0xFF261A12), ImigongoColors.PitDeepShadow)
                        }
                    )
                )
                .border(borderWidth, borderColor, CircleShape)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            SeedClusterCanvas(
                seedCount = seeds,
                isHighlighted = isHighlighted,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // High-contrast Seed Count Badge underneath each pit
        Surface(
            color = when {
                isHighlighted -> ImigongoColors.MustardGold
                isCaptureTarget -> ImigongoColors.RedOchre
                seeds >= 2 && isPlayable -> Color(0xFF3A2B1F)
                else -> Color.Black.copy(alpha = 0.45f)
            },
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = seeds.toString(),
                color = when {
                    isHighlighted -> ImigongoColors.AbsoluteBlack
                    seeds > 0 -> ImigongoColors.CreamWhite
                    else -> ImigongoColors.MutedIvory.copy(alpha = 0.35f)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun CaptureOpportunityDialog(
    gameState: BoardState,
    onCapture: () -> Unit,
    onPass: () -> Unit
) {
    val col = gameState.captureColumn
    val oppInner = if (gameState.currentPlayer == Player.PLAYER_1) 1 else 2
    val oppOuter = if (gameState.currentPlayer == Player.PLAYER_1) 0 else 3
    val capturableSeeds = if (col in 0..7) {
        gameState.board[oppInner][col] + gameState.board[oppOuter][col]
    } else 0

    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ImigongoColors.SurfaceDark),
            border = BorderStroke(2.dp, ImigongoColors.MustardGold),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.capture_available_title),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ImigongoColors.MustardGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = ImigongoColors.RedOchre.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ImigongoColors.RedOchre)
                ) {
                    Text(
                        text = "Column ${col + 1} • +$capturableSeeds Inka Available",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImigongoColors.MustardGoldLight,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.capture_available_desc),
                    fontSize = 13.sp,
                    color = ImigongoColors.CreamWhite,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onPass,
                        border = BorderStroke(1.dp, ImigongoColors.MutedIvory.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_pass),
                            fontWeight = FontWeight.Bold,
                            color = ImigongoColors.CreamWhite,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onCapture,
                        colors = ButtonDefaults.buttonColors(containerColor = ImigongoColors.RedOchre),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_capture),
                            fontWeight = FontWeight.ExtraBold,
                            color = ImigongoColors.CreamWhite,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VictoryDialog(
    gameState: BoardState,
    gameMode: GameMode,
    p1Stats: PlayerStats,
    p2Stats: PlayerStats,
    onPlayAgain: () -> Unit,
    onReturnLobby: () -> Unit
) {
    Dialog(onDismissRequest = onReturnLobby) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ImigongoColors.SurfaceDark),
            border = BorderStroke(2.5.dp, ImigongoColors.MustardGold),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImigongoEmblem(modifier = Modifier.size(68.dp))

                Spacer(modifier = Modifier.height(10.dp))

                val winTitle = when {
                    gameState.winner == Player.PLAYER_1 -> stringResource(R.string.winner_p1)
                    gameMode == GameMode.PVP -> stringResource(R.string.winner_p2)
                    else -> stringResource(R.string.winner_ai)
                }

                Text(
                    text = winTitle,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ImigongoColors.MustardGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    ImigongoBorder(modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Match Summary Stats
                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Player 1 Inka", fontSize = 11.sp, color = ImigongoColors.MutedIvory)
                            Text(
                                text = p1Stats.totalSeeds.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ImigongoColors.MustardGoldLight
                            )
                            Text(
                                text = "${gameState.totalCapturesP1} captured",
                                fontSize = 10.sp,
                                color = ImigongoColors.MutedIvory
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Opponent Inka", fontSize = 11.sp, color = ImigongoColors.MutedIvory)
                            Text(
                                text = p2Stats.totalSeeds.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ImigongoColors.RedOchre
                            )
                            Text(
                                text = "${gameState.totalCapturesP2} captured",
                                fontSize = 10.sp,
                                color = ImigongoColors.MutedIvory
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImigongoColors.MustardGold,
                        contentColor = ImigongoColors.AbsoluteBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_new_game),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onReturnLobby) {
                    Text(
                        text = "Return to Main Menu",
                        color = ImigongoColors.CreamWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RulesScreen(viewModel: MainScreenViewModel) {
    var activeTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        Triple(R.string.rules_sowing_title, R.string.rules_sowing_desc, "Counter-Clockwise 16-Pit Loop"),
        Triple(R.string.rules_relay_title, R.string.rules_relay_desc, "Occupied Pit -> Pick Up & Continue"),
        Triple(R.string.rules_capture_title, R.string.rules_capture_desc, "Inner Row Landing + Opposite Column Occupied"),
        Triple(R.string.rules_traditional_title, R.string.rules_traditional_desc, "Ndahise (Pass) -> Ndakubye (Front-to-Back Retreat)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.LOBBY) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ImigongoColors.MustardGold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.rules_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ImigongoColors.MustardGold
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImigongoColors.SurfaceDark),
            border = BorderStroke(1.5.dp, ImigongoColors.MustardGold.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        color = ImigongoColors.RedOchre.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ImigongoColors.RedOchre)
                    ) {
                        Text(
                            text = "RULE ${activeTab + 1} OF ${tabs.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImigongoColors.MustardGoldLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(tabs[activeTab].first),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ImigongoColors.MustardGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tabs[activeTab].third,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImigongoColors.MustardGoldLight.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        ImigongoBorder(modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(tabs[activeTab].second),
                        fontSize = 15.sp,
                        color = ImigongoColors.CreamWhite,
                        lineHeight = 23.sp
                    )
                }

                // Step Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in tabs.indices) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (i == activeTab) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == activeTab) ImigongoColors.MustardGold else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { if (activeTab > 0) activeTab-- },
                enabled = activeTab > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Text(text = "Previous", color = ImigongoColors.CreamWhite, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (activeTab < tabs.size - 1) {
                        activeTab++
                    } else {
                        viewModel.navigateTo(AppScreen.LOBBY)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImigongoColors.MustardGold,
                    contentColor = ImigongoColors.AbsoluteBlack
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (activeTab == tabs.size - 1) "Got It • Start" else "Next Rule",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainScreenViewModel,
    soundEnabled: Boolean,
    rotateBoardEnabled: Boolean,
    traditionalRulesEnabled: Boolean,
    sowingSpeed: SowingSpeed
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.LOBBY) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ImigongoColors.MustardGold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.btn_settings),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ImigongoColors.MustardGold
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ImigongoColors.SurfaceDark),
            border = BorderStroke(1.5.dp, ImigongoColors.MustardGold.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "GAMEPLAY & AUDIO PREFERENCES",
                        color = ImigongoColors.MustardGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.setting_sounds),
                        subtitle = "Synthesized wood clicks & capture chimes",
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.soundEnabled.value = it }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.setting_rotate),
                        subtitle = "Flip board 180° on Player 2's turn in local PvP",
                        checked = rotateBoardEnabled,
                        onCheckedChange = { viewModel.rotateBoardEnabled.value = it }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.setting_traditional),
                        subtitle = "Prompt for Kurasa vs Ndahise (Pass) & Ndakubye (Retreat)",
                        checked = traditionalRulesEnabled,
                        onCheckedChange = { viewModel.traditionalRulesEnabled.value = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.setting_speed),
                        color = ImigongoColors.CreamWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SowingSpeed.entries.forEach { speed ->
                            val selected = sowingSpeed == speed
                            Surface(
                                onClick = { viewModel.sowingSpeed.value = speed },
                                color = if (selected) ImigongoColors.RedOchre else Color(0xFF231D19),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) ImigongoColors.MustardGold else Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = speed.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImigongoColors.CreamWhite,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.navigateTo(AppScreen.LOBBY) },
            colors = ButtonDefaults.buttonColors(
                containerColor = ImigongoColors.MustardGold,
                contentColor = ImigongoColors.AbsoluteBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Save & Return",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = ImigongoColors.CreamWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = ImigongoColors.MutedIvory,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ImigongoColors.MustardGold,
                checkedTrackColor = ImigongoColors.RedOchre,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
