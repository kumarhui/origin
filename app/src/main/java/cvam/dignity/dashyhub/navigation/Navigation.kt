package cvam.dignity.dashyhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cvam.dignity.dashyhub.dashboard.DashboardScreen

// Assuming these exist in their respective packages as outlined
import cvam.dignity.dashyhub.tools.aadhaar.AadhaarScreen
import cvam.dignity.dashyhub.tools.aadhaar.AadhaarStudioScreen
import cvam.dignity.dashyhub.tools.image.ImageToolsScreen
import cvam.dignity.dashyhub.tools.image.passport_photo_maker.PassportPhotoScreen
import cvam.dignity.dashyhub.tools.other.OtherToolsScreen
import cvam.dignity.dashyhub.tools.other.WhatsappCheckerScreen
import cvam.dignity.dashyhub.tools.other.boga.BogaScannerScreen
import cvam.dignity.dashyhub.tools.pdf.PdfToolsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        // Main Entry Screen
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }

        // Modular Tools Destinations
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
            BogaScannerScreen()
        }
    }
}