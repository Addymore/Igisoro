package com.example.igisoro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Curated Rwandan Imigongo & Carved Wood Color Palette
object ImigongoColors {
    val RedOchre = Color(0xFFC83E2B)         // Traditional terracotta / brick red
    val RedOchreDeep = Color(0xFF7D2014)     // Deep oxide shadow
    val MustardGold = Color(0xFFE5A93B)      // Royal Amasaka gold
    val MustardGoldLight = Color(0xFFF7D07A) // Highlight gold
    val CreamWhite = Color(0xFFFAF6F0)       // Warm kaolin ivory
    val MutedIvory = Color(0xFFC9BFB3)       // Soft secondary text
    val AbsoluteBlack = Color(0xFF0E0C0A)    // Obsidian charcoal
    val SurfaceDark = Color(0xFF181411)      // Elevated dark surface
    val WoodCarvedBase = Color(0xFF281C14)   // Carved Umber board surface
    val WoodCarvedRim = Color(0xFF3E2C20)    // Polished wood rim
    val PitDeepShadow = Color(0xFF120C08)    // Inner pit shadow
    val EmeraldSuccess = Color(0xFF3B9E66)   // Safe / active indicator
}

/**
 * Full-screen subtle geometric Imigongo tapestry background with traditional
 * chevron zigzags ("Intango") and warm vignette lighting.
 */
@Composable
fun ImigongoBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Base dark obsidian background
        drawRect(color = ImigongoColors.AbsoluteBlack)

        // Subtle repeating geometric chevron columns
        val periodX = 120f
        val periodY = 88f
        val bandThickness = 18f

        val cols = (width / periodX).toInt() + 2
        val rows = (height / periodY).toInt() + 2

        val palette = listOf(
            ImigongoColors.RedOchre.copy(alpha = 0.16f),
            ImigongoColors.MustardGold.copy(alpha = 0.12f),
            ImigongoColors.CreamWhite.copy(alpha = 0.07f),
            ImigongoColors.RedOchreDeep.copy(alpha = 0.18f)
        )

        for (r in -1..rows) {
            val baseY = r * periodY
            val color = palette[(r + 10) % palette.size]

            val path = Path().apply {
                moveTo(0f, baseY)
                for (c in 0..cols) {
                    val xMid = c * periodX + periodX / 2f
                    val xEnd = (c + 1) * periodX
                    lineTo(xMid, baseY + periodY * 0.45f)
                    lineTo(xEnd, baseY)
                }
                for (c in cols downTo 0) {
                    val xEnd = (c + 1) * periodX
                    val xMid = c * periodX + periodX / 2f
                    lineTo(xEnd, baseY + bandThickness)
                    lineTo(xMid, baseY + periodY * 0.45f + bandThickness)
                }
                close()
            }

            drawPath(path = path, color = color)
            drawPath(
                path = path,
                color = ImigongoColors.CreamWhite.copy(alpha = 0.04f),
                style = Stroke(width = 1.2f)
            )
        }

        // Radial vignette overlay so UI content pops cleanly
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    ImigongoColors.AbsoluteBlack.copy(alpha = 0.78f),
                    ImigongoColors.AbsoluteBlack.copy(alpha = 0.94f)
                ),
                center = Offset(width / 2f, height * 0.35f),
                radius = maxOf(width, height) * 0.75f
            )
        )
    }
}

/**
 * Crisp horizontal Imigongo geometric frieze (used as decorative dividers and board center spine).
 */
@Composable
fun ImigongoBorder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(color = ImigongoColors.AbsoluteBlack)

        val unitWidth = (h * 1.6f).coerceAtLeast(18f)
        val count = (w / unitWidth).toInt() + 1

        for (i in 0..count) {
            val startX = i * unitWidth
            val midX = startX + unitWidth / 2f
            val endX = startX + unitWidth
            val midY = h / 2f

            // Outer diamond
            val outerDiamond = Path().apply {
                moveTo(midX, 1f)
                lineTo(endX, midY)
                lineTo(midX, h - 1f)
                lineTo(startX, midY)
                close()
            }
            drawPath(
                path = outerDiamond,
                color = if (i % 2 == 0) ImigongoColors.RedOchre else ImigongoColors.MustardGold
            )
            drawPath(
                path = outerDiamond,
                color = ImigongoColors.CreamWhite.copy(alpha = 0.65f),
                style = Stroke(width = 1.3f)
            )

            // Inner nested diamond
            val inset = h * 0.24f
            if (midY > inset) {
                val innerDiamond = Path().apply {
                    moveTo(midX, inset)
                    lineTo(endX - inset * 1.4f, midY)
                    lineTo(midX, h - inset)
                    lineTo(startX + inset * 1.4f, midY)
                    close()
                }
                drawPath(path = innerDiamond, color = ImigongoColors.AbsoluteBlack)
                drawCircle(
                    color = ImigongoColors.CreamWhite,
                    radius = (h * 0.08f).coerceAtLeast(1.5f),
                    center = Offset(midX, midY)
                )
            }
        }
    }
}

/**
 * Hero Rwandan Imigongo Shield Emblem for the Title / Lobby Screen.
 */
@Composable
fun ImigongoEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val maxR = min(w, h) / 2f

        // Outer glow ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ImigongoColors.MustardGold.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = center,
                radius = maxR
            ),
            radius = maxR,
            center = center
        )

        // Concentric Imigongo star-diamonds (Itangaza motif)
        val layers = 5
        for (i in layers downTo 1) {
            val r = maxR * 0.88f * (i.toFloat() / layers)
            val diamond = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y)
                lineTo(center.x, center.y + r)
                lineTo(center.x - r, center.y)
                close()
            }
            val fill = when (i % 3) {
                0 -> ImigongoColors.CreamWhite
                1 -> ImigongoColors.RedOchre
                else -> ImigongoColors.AbsoluteBlack
            }
            drawPath(path = diamond, color = fill)
            drawPath(
                path = diamond,
                color = ImigongoColors.MustardGold,
                style = Stroke(width = 2.2f)
            )
        }

        // Four miniature golden Inka seeds in the center
        val seedOffset = maxR * 0.11f
        val seedRadius = maxR * 0.065f
        listOf(
            Offset(center.x - seedOffset, center.y - seedOffset),
            Offset(center.x + seedOffset, center.y - seedOffset),
            Offset(center.x - seedOffset, center.y + seedOffset),
            Offset(center.x + seedOffset, center.y + seedOffset)
        ).forEach { pos ->
            drawCircle(color = ImigongoColors.MustardGoldLight, radius = seedRadius, center = pos)
        }
    }
}

/**
 * Renders 3D-shaded Inka (seeds) inside a carved wooden pit.
 */
@Composable
fun SeedClusterCanvas(
    seedCount: Int,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (seedCount <= 0) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f
        val visibleSeeds = seedCount.coerceAtMost(12)
        val seedRadius = (radius * 0.21f).coerceIn(3f, 8f)

        if (visibleSeeds == 1) {
            drawSeedSphere(center, seedRadius * 1.15f, isHighlighted)
        } else {
            // Ring 1 (up to 6 seeds)
            val innerRingCount = visibleSeeds.coerceAtMost(6)
            val orbit1 = radius * 0.36f
            for (i in 0 until innerRingCount) {
                val angle = (2.0 * Math.PI * i / innerRingCount) - Math.PI / 4.0
                val pos = Offset(
                    x = center.x + (orbit1 * cos(angle)).toFloat(),
                    y = center.y + (orbit1 * sin(angle)).toFloat()
                )
                drawSeedSphere(pos, seedRadius, isHighlighted)
            }

            // Ring 2 (seeds 7..12)
            if (visibleSeeds > 6) {
                val outerCount = visibleSeeds - 6
                val orbit2 = radius * 0.62f
                for (i in 0 until outerCount) {
                    val angle = (2.0 * Math.PI * i / outerCount) + Math.PI / 6.0
                    val pos = Offset(
                        x = center.x + (orbit2 * cos(angle)).toFloat(),
                        y = center.y + (orbit2 * sin(angle)).toFloat()
                    )
                    drawSeedSphere(pos, seedRadius * 0.9f, isHighlighted)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeedSphere(
    center: Offset,
    radius: Float,
    isHighlighted: Boolean
) {
    // Drop shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.55f),
        radius = radius,
        center = Offset(center.x + 1.2f, center.y + 1.6f)
    )
    // Seed body (warm polished ivory/amber pearl)
    drawCircle(
        brush = Brush.radialGradient(
            colors = if (isHighlighted) {
                listOf(Color.White, ImigongoColors.MustardGoldLight, ImigongoColors.MustardGold)
            } else {
                listOf(ImigongoColors.CreamWhite, Color(0xFFDCCBB8), Color(0xFF8C7662))
            },
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
            radius = radius * 1.35f
        ),
        radius = radius,
        center = center
    )
}
