package cvam.dignity.dashyhub.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a single module/card on the dashboard.
 */
data class DashboardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val accentColor: Color
)

/**
 * The core list of available dashboard modules configured for the app.
 */
val dashboardItems = listOf(
    DashboardItem(
        title = "Aadhaar",
        subtitle = "Update, Download, Verify",
        icon = Icons.Default.Fingerprint,
        route = "aadhaar",
        accentColor = Color(0xFF1E88E5) // Blue
    ),
    DashboardItem(
        title = "Image Tools",
        subtitle = "Crop, Resize, Compress",
        icon = Icons.Default.Image,
        route = "image_tools",
        accentColor = Color(0xFF43A047) // Green
    ),
    DashboardItem(
        title = "PDF Tools",
        subtitle = "Merge, Split, Convert",
        icon = Icons.Default.PictureAsPdf,
        route = "pdf_tools",
        accentColor = Color(0xFFE53935) // Red
    ),
    DashboardItem(
        title = "Other Tools",
        subtitle = "Utilities and Settings",
        icon = Icons.Default.Build,
        route = "other_tools",
        accentColor = Color(0xFF8E24AA) // Purple
    )
)