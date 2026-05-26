package com.yourcompany.statusvault.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourcompany.statusvault.BuildConfig
import com.yourcompany.statusvault.core.common.AppRoute
import com.yourcompany.statusvault.feature.history.HistoryRoute
import com.yourcompany.statusvault.feature.onboarding.OnboardingRoute
import com.yourcompany.statusvault.feature.settings.SettingsRoute
import com.yourcompany.statusvault.feature.statuslist.PreviewRoute
import com.yourcompany.statusvault.feature.statuslist.StatusListRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Statuses.route,
    ) {
        composable(AppRoute.Onboarding.route) {
            OnboardingRoute(
                onContinue = {
                    navController.navigate(AppRoute.Statuses.route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppRoute.Statuses.route) {
            StatusListRoute(
                onOpenPreview = {
                    navController.navigate(AppRoute.Preview.route) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(AppRoute.History.route) {
            HistoryRoute()
        }
        composable(AppRoute.Settings.route) {
            SettingsRoute(
                privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL,
            )
        }
        composable(AppRoute.Preview.route) {
            PreviewRoute(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
