package com.domedav.sportmatenobullshit.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.domedav.sportmatenobullshit.data.SportmateApi
import kotlinx.coroutines.launch
import com.domedav.sportmatenobullshit.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.domedav.sportmatenobullshit.data.TokenManager

sealed class TabItem(
    @param:StringRes val titleResId: Int,
    val icon: ImageVector,
    val index: Int
) {
    object Web : TabItem(R.string.menu_web, Icons.Filled.Home, 0)
    object Qr : TabItem(R.string.menu_qr, Icons.Filled.QrCode, 1)
}

@Composable
fun MainScreen(topPadding: Dp) {
    val tabItems = listOf(TabItem.Web, TabItem.Qr)
    val pagerState = rememberPagerState(pageCount = { tabItems.size })
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    val userToken by tokenManager.userToken.collectAsState(initial = null)

    var qrCodeString by remember { mutableStateOf<String?>(null) }
    val api = remember { SportmateApi() }

    // Fetch QR when token changes
    LaunchedEffect(userToken) {
        userToken?.let { token ->
            val qr = api.getQrCode(token, "device_id")
            if (qr != null) qrCodeString = qr
        }
    }

    // Function to fetch/refresh QR
    val refreshQrAction: () -> Unit = {
        userToken?.let { token ->
            coroutineScope.launch {
                val newQr = api.getQrCode(token, "device_id")
                if (newQr != null) qrCodeString = newQr
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabItems.forEach { item ->
                    val title = stringResource(id = item.titleResId)

                    NavigationBarItem(
                        selected = pagerState.currentPage == item.index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(item.index)
                            }
                        },
                        label = { Text(text = title) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) { page ->
            Column(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> WebScreen(
                        topPadding = topPadding,
                        onTokenFound = { token ->
                            coroutineScope.launch {
                                tokenManager.saveToken(token)
                            }
                        }
                    )
                    1 -> QrScreen(
                        topPadding = topPadding,
                        qrData = qrCodeString,
                        onRefresh = refreshQrAction
                    )
                }
            }
        }
    }
}
