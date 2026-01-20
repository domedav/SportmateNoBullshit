package com.domedav.sportmatenobullshit.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domedav.sportmatenobullshit.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    topPadding: Dp,
    qrData: String?,
    onRefresh: () -> Unit
) {
    val qrColor = MaterialTheme.colorScheme.onSurface.toArgb()

    var timeLeft by remember { mutableIntStateOf(30) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    fun triggerRefresh() {
        isRefreshing = true
        onRefresh()
        coroutineScope.launch {
            isRefreshing = false
            timeLeft = 30
        }
    }

    // Auto-Refresh Logic
    LaunchedEffect(qrData, timeLeft) {
        if (qrData != null) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            triggerRefresh()
        }
    }

    val qrBitmap = remember(qrData, qrColor) {
        if (qrData != null) generateMaterialQrBitmap(qrData, qrColor) else null
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { triggerRefresh() },
        state = pullRefreshState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            if (qrData == null) {
                Box(modifier = Modifier.fillMaxSize().height(500.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.menu_qr_no_token),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(vertical = 32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(260.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.menu_qr_time_left, timeLeft),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { triggerRefresh() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = stringResource(R.string.menu_qr_btn_refresh))
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// Generates a "Material You" style QR code with dynamic color.
private fun generateMaterialQrBitmap(content: String, colorInt: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 0)

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height

        // Create Bitmap with Alpha support
        val bitmap = createBitmap(width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) colorInt else android.graphics.Color.TRANSPARENT
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}