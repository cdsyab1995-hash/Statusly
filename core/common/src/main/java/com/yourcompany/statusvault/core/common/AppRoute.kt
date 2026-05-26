package com.yourcompany.statusvault.core.common

sealed class AppRoute(val route: String) {
    data object Onboarding : AppRoute("onboarding")
    data object Statuses : AppRoute("statuses")
    data object History : AppRoute("history")
    data object Settings : AppRoute("settings")
    data object Preview : AppRoute("preview")
}
