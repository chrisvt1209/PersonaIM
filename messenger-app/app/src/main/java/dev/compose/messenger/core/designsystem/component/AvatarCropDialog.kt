package dev.compose.messenger.core.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CropViewportSize = 240.dp

/** Square pan/pinch-zoom crop step before an image is stylized into an avatar. */
@Composable
fun AvatarCropDialog(
    sourceBitmap: Bitmap,
    onConfirm: (Bitmap) -> Unit,
    onCancel: () -> Unit,
) {
    val viewportSizePx = with(LocalDensity.current) { CropViewportSize.toPx() }
    val cropState = rememberSquareCropState(sourceBitmap, viewportSizePx)

    PersonaDialog(
        title = "Crop Photo",
        onDismissRequest = onCancel,
        confirmText = "Use Photo",
        onConfirm = { onConfirm(cropState.cropResult()) },
    ) {
        Text(
            text = "Pinch to zoom, drag to reposition",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SquareCropCanvas(
            state = cropState,
            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}
