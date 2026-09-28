package com.example.hydro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hydro.HydroApp
import com.example.hydro.ui.confirminfo.ConfirmInfoScreen
import com.example.hydro.ui.idcapture.IdCaptureScreen
import com.example.hydro.ui.nfc.NfcScanScreen
import com.example.hydro.ui.onboarding.OnboardingScreen
import com.example.hydro.ui.otp.OtpScreen
import com.example.hydro.ui.otp.PHONE_ARG
import com.example.hydro.ui.register.RegisterScreen
import com.example.hydro.ui.register.RegisterSuccessScreen
import com.example.hydro.ui.register.USERNAME_ARG

/** Tên các màn hình (đường dẫn điều hướng). */
private object Routes {
    const val ONBOARDING = "onboarding"
    const val OTP = "otp/{$PHONE_ARG}"
    const val ID_CAPTURE = "id_capture"
    const val NFC_SCAN = "nfc_scan"
    const val CONFIRM_INFO = "confirm_info"
    const val REGISTER = "register"
    const val REGISTER_SUCCESS = "register_success/{$USERNAME_ARG}"
    const val HOME = "home"

    fun otp(phone: String) = "otp/$phone"
    fun registerSuccess(username: String) = "register_success/$username"
}

/**
 * Sơ đồ chuyển màn: Onboarding → OTP → Chụp CCCD → Quét chip → Xác nhận thông tin
 * → Tạo tài khoản đăng nhập → Đăng ký thành công → Màn chính.
 */
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
                    navController.navigate(Routes.ID_CAPTURE) {
                        // Xoá màn nhập thông tin và OTP khỏi lịch sử: đã xác thực thì không quay lại được nữa
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                // Quay về đúng màn onboarding (an toàn cả khi bấm nút Back hai lần liền)
                onBack = { navController.popBackStack(Routes.ONBOARDING, inclusive = false) },
            )
        }

        composable(Routes.ID_CAPTURE) {
            IdCaptureScreen(
                onSubmitted = { navController.navigate(Routes.NFC_SCAN) },
            )
        }

        composable(Routes.NFC_SCAN) {
            NfcScanScreen(
                onCompleted = { navController.navigate(Routes.CONFIRM_INFO) },
                // Quay về màn chụp CCCD (ảnh đã chụp vẫn còn), an toàn cả khi bấm Back hai lần liền
                onBack = { navController.popBackStack(Routes.ID_CAPTURE, inclusive = false) },
            )
        }

        composable(Routes.CONFIRM_INFO) {
            ConfirmInfoScreen(
                onSubmitted = {
                    navController.navigate(Routes.REGISTER) {
                        // Xoá các màn eKYC khỏi lịch sử: đã xác nhận thì không quay lại sửa được nữa
                        popUpTo(Routes.ID_CAPTURE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack(Routes.NFC_SCAN, inclusive = false) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = { username ->
                    navController.navigate(Routes.registerSuccess(username)) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.REGISTER_SUCCESS,
            arguments = listOf(navArgument(USERNAME_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            RegisterSuccessScreen(
                username = backStackEntry.arguments?.getString(USERNAME_ARG).orEmpty(),
                onStart = {
                    navController.navigate(Routes.HOME) {
                        // Bấm Back ở màn chính sẽ thoát app, không quay lại màn thành công
                        popUpTo(Routes.REGISTER_SUCCESS) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HydroApp()
        }
    }
}
