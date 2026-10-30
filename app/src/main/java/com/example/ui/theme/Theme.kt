package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CreamDarkColorScheme = darkColorScheme(
    primary = PrimaryCreamDark,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = PrimaryCreamDark,
    secondary = SecondaryTaupeDark,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = SecondaryTaupeDark,
    tertiary = AccentBronze,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = BorderDark
)

private val CreamLightColorScheme = lightColorScheme(
    primary = PrimaryEspresso,
    onPrimary = CreamSurface,
    primaryContainer = SecondaryTaupeContainer,
    onPrimaryContainer = PrimaryEspresso,
    secondary = SecondaryTaupe,
    onSecondary = CreamSurface,
    secondaryContainer = CreamSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentBronze,
    background = CreamBackground,
    surface = CreamSurface,
    surfaceVariant = CreamSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = CreamBorder,
    outlineVariant = CreamBorderSubtle
)

@Composable
fun ImageCompressCloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CreamDarkColorScheme else CreamLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
