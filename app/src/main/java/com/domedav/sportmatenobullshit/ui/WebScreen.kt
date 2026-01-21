package com.domedav.sportmatenobullshit.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.domedav.sportmatenobullshit.R
import com.domedav.sportmatenobullshit.utils.WebAppInterface
import kotlinx.coroutines.delay

@Composable
@SuppressLint("SetJavaScriptEnabled")
fun WebScreen(
    topPadding: Dp,
    hasToken: Boolean,
    onTokenFound: (String) -> Unit
) {
    val context = LocalContext.current

    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val loggedInUrl = stringResource(R.string.app_url_loggedin)
    val loginUrl = stringResource(R.string.app_url)

    val targetUrl = if(!hasToken) loginUrl else loggedInUrl

    // Script to intercept login response
    val interceptionScript = """
        (function() {
            if (window.isInterceptorInjected) return;
            console.log("Initializing Native Interceptors...");

            // 1. Intercept Fetch API (Modern Apps)
            const { fetch: originalFetch } = window;
            window.fetch = async (...args) => {
                const [resource, config] = args;
                const response = await originalFetch(resource, config);
                
                // Check if this is the login URL
                const url = (typeof resource === 'string') ? resource : resource.url;
                
                if (url && url.includes("/api/auth/login")) {
                    console.log("Fetch Login Detected: " + url);
                    // Clone response to read text without consuming the stream
                    const clone = response.clone();
                    clone.text().then(text => {
                        console.log("Sending response to Android...");
                        window.AndroidInterface.onLoginResponse(text);
                    });
                }
                return response;
            };

            // 2. Intercept XMLHttpRequest (Legacy/Backup)
            var origOpen = XMLHttpRequest.prototype.open;
            XMLHttpRequest.prototype.open = function() {
                this.addEventListener('load', function() {
                    if (this.responseURL.includes("/api/auth/login")) {
                        console.log("XHR Login Detected!");
                        window.AndroidInterface.onLoginResponse(this.responseText);
                    }
                });
                origOpen.apply(this, arguments);
            };

            window.isInterceptorInjected = true;
        })();
    """.trimIndent()

    val webView = remember {
        WebView(context).apply {

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true

                cacheMode = WebSettings.LOAD_DEFAULT

                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                allowContentAccess = true
            }

            addJavascriptInterface(WebAppInterface { token ->
                onTokenFound(token)
            }, "AndroidInterface")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    CookieManager.getInstance().flush()
                    // Inject script safely
                    view?.evaluateJavascript(interceptionScript, null)
                }
            }

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            loadUrl(targetUrl)

            webViewRef = this
        }
    }

    val doublepressBackButton = stringResource(R.string.msg_press_back_again)

    BackHandler(enabled = true) {
        if (webView.canGoBack()) {
            // navigate as webview
            webView.goBack()
        } else {
            if (doubleBackToExitPressedOnce) {
                (context as? Activity)?.finish() // close app
            } else {
                doubleBackToExitPressedOnce = true
                Toast.makeText(context, doublepressBackButton, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(hasToken) {
        if (hasToken) {
            webViewRef?.loadUrl(loggedInUrl)
        }
    }

    LaunchedEffect(doubleBackToExitPressedOnce) {
        if (doubleBackToExitPressedOnce) {
            delay(2000)
            doubleBackToExitPressedOnce = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Ensure cookies are flushed one last time when leaving
            CookieManager.getInstance().flush()

            webView.destroy()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding),
        factory = { ctx ->
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView
        },
        update = { webView ->
            webViewRef = webView
        }
    )
}
