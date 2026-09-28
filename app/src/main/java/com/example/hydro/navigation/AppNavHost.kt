package com.example.hydro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hydro.HydroApp
import com.example.hydro.ui.onboarding.OnboardingScreen
import com.example.hydro.ui.otp.OtpScreen
import com.example.hydro.ui.otp.PHONE_ARG

/** Tên các màn hình (đường dẫn điều hướng). */
private object Routes {
    const val ONBOARDING = "onboarding"
    const val OTP = "otp/{$PHONE_ARG}"
    const val HOME = "home"

    fun otp(phone: String) = "otp/$phone"
}

/** Sơ đồ chuyển màn: Onboarding → OTP → Màn chính. */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onSubmitted = { phone -> navController.navigate(Routes.otp(phone)) },
            )
        }

        composable(
            route = Routes.OTP,
            arguments = listOf(navArgument(PHONE_ARG) { type = NavType.StringType }),
        ) {
            OtpScreen(
                onVerified = {
                    navController.navigate(Routes.HOME) {
                        // Xoá các màn đăng ký khỏi lịch sử: bấm Back ở màn chính sẽ thoát app
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                // Quay về đúng màn onboarding (an toàn cả khi bấm nút Back hai lần liền)
                onBack = { navController.popBackStack(Routes.ONBOARDING, inclusive = false) },
            )
        }

        composable(Routes.HOME) {
            HydroApp()
        }
    }
}
