package com.mylove.app

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.mylove.app.data.LoveStore
import com.mylove.app.notify.LoveNotifications
import com.mylove.app.notify.PartnerPollScheduler
import com.mylove.app.notify.PartnerSync
import com.mylove.app.ui.LoveBackground
import com.mylove.app.ui.LoveBottomBar
import com.mylove.app.ui.GuideOverlay
import com.mylove.app.ui.MyLoveTheme
import com.mylove.app.ui.OnboardingScreen
import com.mylove.app.ui.screens.CalendarScreen
import com.mylove.app.ui.screens.MeScreen
import com.mylove.app.ui.screens.TogetherScreen
import com.mylove.app.ui.screens.UsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyLoveApp() {
    val context = LocalContext.current
    val store = remember {
        LoveStore(
            context.applicationContext.getSharedPreferences("my_love", Context.MODE_PRIVATE)
        )
    }

    val systemDark = isSystemInDarkTheme()
    val dark = when (store.themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    MyLoveTheme(darkTheme = dark, paletteId = store.paletteId) {
        LaunchedEffect(
            store.notificationsEnabled,
            store.petType,
            store.petLevel,
            store.missMe,
            store.missTa,
            store.isConfigured
        ) {
            if (store.notificationsEnabled) {
                LoveNotifications.update(context, store)
            } else {
                LoveNotifications.cancel(context)
            }
        }

        LaunchedEffect(store.notificationsEnabled, store.syncToken) {
            PartnerPollScheduler.update(
                context,
                store.notificationsEnabled && store.syncToken.isNotBlank()
            )
        }

        LaunchedEffect(store.notificationsEnabled, store.syncToken) {
            if (store.notificationsEnabled && store.syncToken.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    runCatching { PartnerSync.check(context) }
                }
            }
        }

        if (!store.isConfigured) {
            Box(Modifier.fillMaxSize()) {
                val backgroundBackdrop = rememberLayerBackdrop { drawContent() }
                Box(
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(backgroundBackdrop)
                ) {
                    LoveBackground(Modifier.fillMaxSize())
                }
                OnboardingScreen(
                    store = store,
                    backdrop = backgroundBackdrop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            var tab by rememberSaveable { mutableIntStateOf(0) }

            Box(Modifier.fillMaxSize()) {
                // 卡片取样：只包含背景，避免把卡片自身画进玻璃
                val backgroundBackdrop = rememberLayerBackdrop { drawContent() }
                // 底栏取样：包含背景 + 页面内容
                val contentBackdrop = rememberLayerBackdrop { drawContent() }

                Box(
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(contentBackdrop)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .layerBackdrop(backgroundBackdrop)
                    ) {
                        LoveBackground(Modifier.fillMaxSize())
                    }

                    when (tab) {
                        0 -> UsScreen(store, backgroundBackdrop, Modifier.fillMaxSize())
                        1 -> CalendarScreen(store, backgroundBackdrop, Modifier.fillMaxSize())
                        2 -> TogetherScreen(store, backgroundBackdrop, Modifier.fillMaxSize())
                        else -> MeScreen(store, backgroundBackdrop, Modifier.fillMaxSize())
                    }
                }

                LoveBottomBar(
                    backdrop = contentBackdrop,
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (!store.guideSeen) {
                    GuideOverlay(onFinish = { store.guideSeen = true })
                }
            }
        }
    }
}