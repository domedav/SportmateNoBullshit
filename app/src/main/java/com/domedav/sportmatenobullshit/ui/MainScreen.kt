package com.domedav.sportmatenobullshit.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.domedav.sportmatenobullshit.R
import com.domedav.sportmatenobullshit.data.PreferencesManager
import com.domedav.sportmatenobullshit.data.SportmateApi
import com.domedav.sportmatenobullshit.data.TokenManager
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val prefsManager = remember { PreferencesManager(context) }

    val userToken by tokenManager.userToken.collectAsState(initial = null)
    val savedTabIndex by prefsManager.getLastTab().collectAsState(initial = null)

    var qrCodeString by remember { mutableStateOf<String?>(null) }
    val api = remember { SportmateApi() }

    var isConnected by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabItems.size }
    )

    // Function to fetch/refresh QR
    val refreshQrAction: () -> Unit = {
        userToken?.let { token ->
            coroutineScope.launch {
                val newQr = api.getQrCode(token, "device_id")
                if (newQr != null) qrCodeString = newQr
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pagerState.currentPage == 1) {
                refreshQrAction() // As soon as we exit the app, refresh the code, this ensures we can enter or leave into the building, if the app was not used for a while, and is cached
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(savedTabIndex) {
        savedTabIndex?.let { index ->
            if (pagerState.currentPage != index) {
                pagerState.scrollToPage(index)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        prefsManager.saveLastTab(pagerState.currentPage)
    }

    LaunchedEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        isConnected = cm.activeNetwork != null
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: Network) { isConnected = true }
            override fun onLost(n: Network) { isConnected = false }
        }
        cm.registerNetworkCallback(request, callback)
    }

    if (!isConnected) {
        Scaffold{ innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding(), top = innerPadding.calculateTopPadding(), start = 24.dp, end = 24.dp)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.msg_no_internet),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.msg_no_internet_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        return
    }

    // Fetch QR when token changes
    LaunchedEffect(userToken) {
        userToken?.let { token ->
            val qr = api.getQrCode(token, "device_id")
            if (qr != null) qrCodeString = qr
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1) {
            refreshQrAction()
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
            userScrollEnabled = false, // annoying here, turned off
            beyondViewportPageCount = 1, // keep pages loaded when scrolled away
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
                        },
                        hasToken = userToken != null
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
