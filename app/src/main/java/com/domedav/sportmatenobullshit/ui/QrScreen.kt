package com.domedav.sportmatenobullshit.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.domedav.sportmatenobullshit.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    topPadding: Dp,
    qrData: String?,
    onRefresh: () -> Unit
) {
    val qrColor = android.graphics.Color.BLACK

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showAboutDialog by remember { mutableStateOf(false) }
    val urlGithubSponsors = stringResource(R.string.about_btn_support_link)

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

    LaunchedEffect(qrData) {
        if (qrData != null) timeLeft = 30
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
            IconButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = {
                        Text(text = stringResource(R.string.about_dialog_title))
                    },
                    text = {
                        Column (modifier = Modifier.verticalScroll(rememberScrollState())){
                            Text(
                                text = stringResource(R.string.about_section_developer),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.about_developer_name),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = stringResource(R.string.about_section_support),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.about_support_description),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = {
                                    uriHandler.openUri(urlGithubSponsors)
                                    showAboutDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = stringResource(R.string.about_btn_support))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(R.string.about_section_dependencies),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val names = context.resources.getStringArray(R.array.dependency_names)
                            val urls = context.resources.getStringArray(R.array.dependency_urls)
                            val dependencies = names.zip(urls)

                            dependencies.forEach { (name, url) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { uriHandler.openUri(url) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text(stringResource(R.string.about_btn_support))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text(stringResource(R.string.about_btn_close))
                        }
                    }
                )
            }

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
                    Text(
                        text = stringResource(R.string.menu_qr_how_to_use, timeLeft),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(260.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.menu_qr_time_left, timeLeft) + " " + pluralStringResource(R.plurals.menu_qr_time_left_seconds_prural, timeLeft),
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
                bitmap[x, y] = if (bitMatrix[x, y]) colorInt else android.graphics.Color.WHITE
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}