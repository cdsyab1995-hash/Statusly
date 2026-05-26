package com.yourcompany.statusvault.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.yourcompany.statusvault.core.common.AppRoute

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("Status", AppRoute.Statuses.route, Icons.Outlined.Widgets),
    BottomNavItem("Saved", AppRoute.History.route, Icons.Outlined.CollectionsBookmark),
    BottomNavItem("Settings", AppRoute.Settings.route, Icons.Outlined.Settings),
)
