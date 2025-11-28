package expo.modules.liquidglassnative

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import expo.modules.liquidglassnative.components.LiquidBottomTab
import expo.modules.liquidglassnative.components.LiquidBottomTabs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class BottomTabsContentView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
    private val onTabSelected by EventDispatcher<Map<String, Any>>()

    private data class TabsProps(
        val selectedTabIndex: Int = 0,
        val tabsCount: Int = 3,
        val tabLabels: List<String>? = null,
        val tabIcons: List<String>? = null,
        val iconTintEnabled: Boolean = true
    )

    private var props by mutableStateOf(TabsProps())

    private val composeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setContent {
            val isLightTheme = !isSystemInDarkTheme()
            val contentColor = if (isLightTheme) Color.Black else Color.White

            val backdrop = rememberLayerBackdrop()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Background with backdrop - use color background
                // to avoid ColorDrawable casting issues and resource dependency
                Box(
                    modifier = Modifier
                        .layerBackdrop(backdrop)
                        .fillMaxSize()
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 하단에 탭 배치
                    var selectedTabIndex by remember(props.selectedTabIndex) {
                        mutableIntStateOf(props.selectedTabIndex)
                    }

                    LaunchedEffect(props.selectedTabIndex) {
                        selectedTabIndex = props.selectedTabIndex
                    }

                    LiquidBottomTabs(
                        selectedTabIndex = { selectedTabIndex },
                        onTabSelected = { index ->
                            selectedTabIndex = index
                            onTabSelected(mapOf("index" to index))
                        },
                        backdrop = backdrop,
                        tabsCount = props.tabsCount,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 36f.dp, vertical = 32f.dp)
                    ) {
                        repeat(props.tabsCount) { index ->
                            val label = props.tabLabels?.getOrNull(index) ?: "Tab ${index + 1}"
                            val iconName = props.tabIcons?.getOrNull(index)

                            LiquidBottomTab({
                                selectedTabIndex = index
                                onTabSelected(mapOf("index" to index))
                            }) {
                                val context = LocalContext.current
                                if (iconName != null) {
                                    // URI로 시작하면 파일/네트워크 이미지로 처리
                                    if (iconName.startsWith("file://") || iconName.startsWith("http://") || iconName.startsWith("https://") || iconName.startsWith("content://") || iconName.startsWith("asset://")) {
                                        var iconPainter: androidx.compose.ui.graphics.painter.Painter? by remember { mutableStateOf(null) }

                                        LaunchedEffect(iconName) {
                                            android.util.Log.d("BottomTabs", "Loading icon from URI: $iconName")
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val bitmap = when {
                                                        iconName.startsWith("asset://") -> {
                                                            // React Native asset 경로 처리
                                                            val assetPath = iconName.replace("asset://", "").replace("assets/", "")
                                                            if (assetPath.isNotEmpty()) {
                                                                context.assets.open(assetPath).use { stream ->
                                                                    BitmapFactory.decodeStream(stream)
                                                                }
                                                            } else {
                                                                null
                                                            }
                                                        }
                                                        iconName.startsWith("file://") -> {
                                                            // file:// URI 처리
                                                            val uri = Uri.parse(iconName)
                                                            val path = uri.path
                                                            if (path != null) {
                                                                val file = java.io.File(path)
                                                                if (file.exists()) {
                                                                    BitmapFactory.decodeFile(file.absolutePath)
                                                                } else {
                                                                    context.contentResolver.openInputStream(uri)?.use { stream ->
                                                                        BitmapFactory.decodeStream(stream)
                                                                    }
                                                                }
                                                            } else {
                                                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                                                    BitmapFactory.decodeStream(stream)
                                                                }
                                                            }
                                                        }
                                                        iconName.startsWith("http://") || iconName.startsWith("https://") -> {
                                                            // HTTP/HTTPS URI - 네트워크 요청
                                                            try {
                                                                val url = URL(iconName)
                                                                url.openConnection().getInputStream().use { stream ->
                                                                    BitmapFactory.decodeStream(stream)
                                                                }
                                                            } catch (e: Exception) {
                                                                android.util.Log.e("BottomTabs", "Failed to load HTTP icon: $iconName", e)
                                                                null
                                                            }
                                                        }
                                                        else -> {
                                                            // content:// URI
                                                            val uri = Uri.parse(iconName)
                                                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                                                BitmapFactory.decodeStream(stream)
                                                            }
                                                        }
                                                    }

                                                    if (bitmap != null) {
                                                        iconPainter = BitmapPainter(bitmap.asImageBitmap())
                                                        android.util.Log.d("BottomTabs", "Icon loaded successfully: $iconName")
                                                    } else {
                                                        android.util.Log.w("BottomTabs", "Failed to decode bitmap from: $iconName")
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("BottomTabs", "Failed to load icon: $iconName", e)
                                                }
                                            }
                                        }

                                        if (iconPainter != null) {
                                            if (props.iconTintEnabled) {
                                                val iconColorFilter = ColorFilter.tint(contentColor)
                                                Box(
                                                    Modifier
                                                        .size(28f.dp)
                                                        .paint(iconPainter!!, colorFilter = iconColorFilter)
                                                )
                                            } else {
                                                Box(
                                                    Modifier
                                                        .size(28f.dp)
                                                        .paint(iconPainter!!)
                                                )
                                            }
                                        } else {
                                            Box(Modifier.size(28f.dp))
                                        }
                                    } else {
                                        // Drawable 리소스 이름으로 시도
                                        val iconResId = context.resources.getIdentifier(
                                            iconName,
                                            "drawable",
                                            context.packageName
                                        )
                                        if (iconResId != 0) {
                                            val icon = painterResource(iconResId)
                                            if (props.iconTintEnabled) {
                                                val iconColorFilter = ColorFilter.tint(contentColor)
                                                Box(
                                                    Modifier
                                                        .size(28f.dp)
                                                        .paint(icon, colorFilter = iconColorFilter)
                                                )
                                            } else {
                                                Box(
                                                    Modifier
                                                        .size(28f.dp)
                                                        .paint(icon)
                                                )
                                            }
                                        } else {
                                            Box(Modifier.size(28f.dp))
                                        }
                                    }
                                } else {
                                    Box(Modifier.size(28f.dp))
                                }
                                BasicText(
                                    label,
                                    style = TextStyle(contentColor, 12f.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        addView(composeView)
    }

    fun updateProps(
        selectedTabIndex: Int? = null,
        tabsCount: Int? = null,
        tabLabels: List<String>? = null,
        tabIcons: List<String>? = null,
        iconTintEnabled: Boolean? = null
    ) {
        props = props.copy(
            selectedTabIndex = selectedTabIndex ?: props.selectedTabIndex,
            tabsCount = tabsCount ?: props.tabsCount,
            tabLabels = tabLabels ?: props.tabLabels,
            tabIcons = tabIcons ?: props.tabIcons,
            iconTintEnabled = iconTintEnabled ?: props.iconTintEnabled
        )
    }
}

