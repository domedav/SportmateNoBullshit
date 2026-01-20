package com.domedav.sportmatenobullshit.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.domedav.sportmatenobullshit.R
import com.domedav.sportmatenobullshit.utils.WebAppInterface

@Composable
@SuppressLint("SetJavaScriptEnabled")
fun WebScreen(
    topPadding: Dp,
    onTokenFound: (String) -> Unit
) {
    val context = LocalContext.current
    val startUrl = stringResource(R.string.app_url)

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

            loadUrl(startUrl)
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
            SwipeRefreshLayout(ctx).apply {
                (webView.parent as? ViewGroup)?.removeView(webView)

                addView(webView)
                setOnRefreshListener {
                    webView.reload()
                    isRefreshing = false
                }
            }
        }
    )
}
