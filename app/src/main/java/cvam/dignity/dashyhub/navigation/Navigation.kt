package cvam.dignity.dashyhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cvam.dignity.dashyhub.dashboard.DashboardScreen
import cvam.dignity.dashyhub.settings.AppSettingsScreen

import cvam.dignity.dashyhub.tools.aadhaar.AadhaarScreen
import cvam.dignity.dashyhub.tools.aadhaar.AadhaarStudioScreen
import cvam.dignity.dashyhub.tools.image.ImageToolsScreen
import cvam.dignity.dashyhub.tools.image.passport_photo_maker.PassportPhotoScreen
import cvam.dignity.dashyhub.tools.neonpen.NeonPenScreen
import cvam.dignity.dashyhub.tools.neonpen.NeonPenSettingsScreen
import cvam.dignity.dashyhub.tools.other.OtherToolsScreen
import cvam.dignity.dashyhub.tools.other.WhatsappCheckerScreen
import cvam.dignity.dashyhub.tools.other.boga.BogaScannerScreen
import cvam.dignity.dashyhub.tools.pdf.PdfToolsScreen
import cvam.dignity.dashyhub.tools.screenshottaker.ScreenshotTakerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }

        composable("app_settings") {
            AppSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("aadhaar") {
            AadhaarScreen(navController = navController)
        }

        composable("image_tools") {
            ImageToolsScreen(navController = navController)
        }

        composable("passport_photo_maker") {
            PassportPhotoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("pdf_tools") {
            PdfToolsScreen()
        }

        composable("other_tools") {
            OtherToolsScreen(navController = navController)
        }

        composable("aadhaar_studio") {
            AadhaarStudioScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("whatsapp_checker") {
            WhatsappCheckerScreen()
        }

        composable("boga") {
            BogaScannerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("neon_pen") {
            NeonPenScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("app_settings") }
            )
        }

        composable("neon_pen_settings") {
            NeonPenSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("screenshot_taker") {
            ScreenshotTakerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}