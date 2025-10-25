package com.example.weatherly

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    data object Main : Screen("Main", R.string.Main, Icons.Default.Home)
    data object Forecast : Screen("Forecast", R.string.Forecast, Icons.AutoMirrored.Filled.List)
    data object Settings : Screen("Settings", R.string.Settings, Icons.Default.Settings)
}