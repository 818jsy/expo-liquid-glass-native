package expo.modules.liquidglassnative

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.BitmapFactory
import android.net.Uri
import java.net.URL
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import expo.modules.liquidglassnative.components.LiquidBottomTab
import expo.modules.liquidglassnative.components.LiquidBottomTabs

class BottomTabsContentViewManager : SimpleViewManager<ComposeView>() {
    
    companion object {
        const val REACT_CLASS = "BottomTabsContentView"
        const val EVENT_ON_TAB_SELECTED = "onTabSelected"
    }
    
    override fun getName(): String {
        return REACT_CLASS
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
        return mapOf(
            "topTabSelected" to mapOf("registrationName" to EVENT_ON_TAB_SELECTED)
        )
    }

    override fun createViewInstance(reactContext: ThemedReactContext): ComposeView {
        val view = ComposeView(reactContext)
        updateViewContent(view, TabsProps())
        return view
    }
    
    @ReactProp(name = "selectedTabIndex", defaultInt = 0)
    fun setSelectedTabIndex(view: ComposeView, selectedTabIndex: Int) {
        val props = (view.tag as? TabsProps) ?: TabsProps()
        updateViewContent(view, props.copy(selectedTabIndex = selectedTabIndex))
    }
    
    @ReactProp(name = "tabsCount", defaultInt = 3)
    fun setTabsCount(view: ComposeView, tabsCount: Int) {
        val props = (view.tag as? TabsProps) ?: TabsProps()
        updateViewContent(view, props.copy(tabsCount = tabsCount))
    }
    
    @ReactProp(name = "tabLabels")
    fun setTabLabels(view: ComposeView, tabLabels: ReadableArray?) {
        val props = (view.tag as? TabsProps) ?: TabsProps()
        val labels = if (tabLabels != null) {
            (0 until tabLabels.size()).map { tabLabels.getString(it) ?: "Tab ${it + 1}" }
        } else {
            null
        }
        updateViewContent(view, props.copy(tabLabels = labels))
    }
    
    @ReactProp(name = "tabIcons")
    fun setTabIcons(view: ComposeView, tabIcons: ReadableArray?) {
        val props = (view.tag as? TabsProps) ?: TabsProps()
        val icons = if (tabIcons != null) {
            (0 until tabIcons.size()).mapNotNull { 
                val iconName = tabIcons.getString(it)
                if (iconName != null && iconName.isNotEmpty()) iconName else null
            }
        } else {
            null
        }
        updateViewContent(view, props.copy(tabIcons = icons))
    }
    
    @ReactProp(name = "iconTintEnabled", defaultBoolean = true)
    fun setIconTintEnabled(view: ComposeView, iconTintEnabled: Boolean) {
        val props = (view.tag as? TabsProps) ?: TabsProps()
        updateViewContent(view, props.copy(iconTintEnabled = iconTintEnabled))
    }
    
    private data class TabsProps(
        val selectedTabIndex: Int = 0,
        val tabsCount: Int = 3,
        val tabLabels: List<String>? = null,
        val tabIcons: List<String>? = null,
        val iconTintEnabled: Boolean = true
    )
    
    private fun updateViewContent(view: ComposeView, props: TabsProps) {
        view.tag = props
        val reactContext = view.context as ReactContext
        view.setContent {
            val isLightTheme = !isSystemInDarkTheme()
            val contentColor = if (isLightTheme) Color.Black else Color.White

            val backdrop = rememberLayerBackdrop()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Background with backdrop - use color background
                // to avoid ColorDrawable casting issues with system drawables
                Box(
                    modifier = Modifier
                        .layerBackdrop(backdrop)
                        .fillMaxSize()
                        .background(if (isLightTheme) Color(0xFFF5F5F5) else Color(0xFF121212))
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
                            sendTabSelectedEvent(reactContext, view, index)
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
                                sendTabSelectedEvent(reactContext, view, index)
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
    
    override fun updateExtraData(root: ComposeView, extraData: Any?) {
        val props = (root.tag as? TabsProps) ?: TabsProps()
        updateViewContent(root, props)
    }
    
    private fun sendTabSelectedEvent(reactContext: ReactContext, view: android.view.View, index: Int) {
        val event: WritableMap = Arguments.createMap()
        event.putInt("index", index)
        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(view.id, "topTabSelected", event)
    }
}
