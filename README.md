# LiquidGlass-RN

React Native에서 Android의 Liquid Glass (Backdrop) 효과를 사용할 수 있도록 네이티브 브릿지를 제공하는 프로젝트입니다.

## 📋 목차

1. [Liquid Glass 이펙트 원리](#1-liquid-glass-이펙트-원리)
2. [렌더링 로직](#2-렌더링-로직)
3. [코틀린 라이브러리 사용](#3-코틀린-라이브러리-사용)
4. [React Native 네이티브 브릿지](#4-react-native-네이티브-브릿지)
5. [시연](#5-시연)

---

## 1. Liquid Glass 이펙트 원리

Liquid Glass 이펙트는 **블러(Blur)**와 **렌즈 굴절(Lens Refraction)** 두 가지 핵심 기술의 조합으로 구현됩니다.

### 1.1 블러 이펙트 (Blur Effect)

**구현 원리:**
- Android 12+ (API 31+)의 `RenderEffect.createBlurEffect()` API 사용
- 가우시안 블러 알고리즘으로 배경을 흐리게 처리
- 블러 반경(radius)을 조절하여 흐림 정도 제어

**특징:**
- 실시간으로 배경을 캡처하여 블러 처리
- 여러 효과를 체인으로 연결 가능

### 1.2 렌즈 굴절 이펙트 (Lens Refraction Effect)

**구현 원리:**
- Android 13+ (API 33+)의 **Runtime Shader (AGSL)** 사용
- **SDF (Signed Distance Field)**로 모양 계산
- **그라디언트 기반 굴절**로 배경 좌표 왜곡

**핵심 개념:**
- `refractionHeight`: 렌즈의 두께/높이 (굴절이 적용되는 깊이)
- `refractionAmount`: 굴절 강도 (배경이 얼마나 왜곡되는지)
- `circleMap()`: 원형 렌즈의 곡률을 시뮬레이션하는 수학 함수

**굴절 계산 과정:**
1. SDF로 모양 계산
2. 표면 법선 벡터 계산 (굴절 방향 결정)
3. 굴절된 좌표로 배경 샘플링

#### 색수차 (Chromatic Aberration)

선택적으로 RGB 채널을 약간씩 다른 좌표에서 샘플링하여 프리즘 효과를 생성할 수 있습니다.

---

## 2. 렌더링 로직

Liquid Glass 이펙트의 렌더링은 **3단계 파이프라인**으로 진행됩니다:

### 2.1 렌더링 파이프라인

```
1. 배경 콘텐츠를 GraphicsLayer에 캡처 (recordLayer)
   ↓
2. 캡처된 레이어에 RenderEffect 적용 (블러 + 렌즈 체인)
   ↓
3. 효과가 적용된 레이어를 최종 렌더링 (drawLayer)
```

**핵심**: 배경을 먼저 오프스크린 레이어에 캡처한 후, GPU에서 효과를 적용하고, 최종적으로 렌더링합니다.

### 2.2 상세 단계

#### Step 1: 배경 캡처 (recordLayer)
- `recordLayer()`는 배경 콘텐츠를 **오프스크린 GraphicsLayer**에 기록합니다
- 이 시점에서는 아직 화면에 그려지지 않고, 레이어에 저장만 됩니다
- `compositingStrategy = CompositingStrategy.Offscreen`으로 설정되어 별도 레이어로 관리됩니다

#### Step 2: RenderEffect 적용
- `blur()`와 `lens()`는 각각 `RenderEffect`를 생성합니다
- 효과들은 **체인으로 연결**되어 순차적으로 적용됩니다:
  ```
  배경 레이어 → Blur Effect → Lens Effect → 최종 레이어
  ```
- `renderEffect`를 GraphicsLayer에 할당하면, GPU에서 자동으로 효과가 적용됩니다
- **중요**: 이 단계는 GPU에서 처리되므로 CPU 부하가 적습니다

#### Step 3: 최종 렌더링 (drawLayer)
- `drawLayer()`는 RenderEffect가 적용된 GraphicsLayer를 화면에 그립니다
- 이 시점에서 블러와 렌즈 효과가 모두 적용된 배경이 렌더링됩니다
- 이후 표면 색상과 실제 콘텐츠가 위에 그려집니다

### 2.3 레이어 구조

#### 렌더링 순서 (하단 → 상단)

```
┌─────────────────────────┐
│   Content (텍스트/아이콘)  │  ← Step 4: 최상위 콘텐츠
├─────────────────────────┤
│   Surface Color         │  ← Step 3: 반투명 표면 색상
├─────────────────────────┤
│   GraphicsLayer         │  ← Step 2-3: 효과 적용된 레이어
│   ├─ Lens Refraction    │     (블러 + 렌즈가 GPU에서 적용됨)
│   ├─ Blur Effect        │
│   └─ Background Capture │  ← Step 1: 배경 캡처
└─────────────────────────┘
```

**핵심 포인트:**
1. **캡처**: 배경을 GraphicsLayer에 기록 (아직 화면에 안 그려짐)
2. **효과 적용**: RenderEffect를 레이어에 할당 (GPU에서 처리)
3. **렌더링**: 효과가 적용된 레이어를 화면에 그림
4. **합성**: 표면 색상과 콘텐츠를 위에 그려 최종 완성

---

## 3. 코틀린 라이브러리 사용

원본 라이브러리는 **AndroidLiquidGlass/backdrop**에 있으며, Jetpack Compose에서 바로 사용할 수 있습니다.

### 3.1 기본 사용법

```kotlin
val backdrop = rememberLayerBackdrop()

Row(
    modifier = Modifier.drawBackdrop(
        backdrop = backdrop,
        shape = { ContinuousCapsule },
        effects = {
            vibrancy()
            blur(2f.dp.toPx())
            lens(12f.dp.toPx(), 24f.dp.toPx())
        }
    )
) {
    Text("Button")
}
```

### 3.2 효과 조합

```kotlin
effects = {
    vibrancy()                    // 채도 조정
    blur(8f.dp.toPx())           // 블러
    lens(
        refractionHeight = 12f.dp.toPx(),
        refractionAmount = 24f.dp.toPx(),
        depthEffect = true,
        chromaticAberration = true
    )
}
```

---

## 4. React Native 네이티브 브릿지

React Native에서는 코틀린 코드를 직접 사용할 수 없으므로, **네이티브 브릿지**를 통해 연결해야 합니다.

### 4.1 브릿지 구조

```
┌─────────────────────────────────────────┐
│   React Native (JavaScript)             │
│   - LiquidButton.js                     │
│   - BottomTabs.js                       │
└──────────────┬──────────────────────────┘
               │ requireNativeComponent
               │ Props 전달 / Events 수신
               ↓
┌─────────────────────────────────────────┐
│   Native Bridge (Kotlin)                │
│   - ViewManager                         │
│   - ComposeView                         │
└──────────────┬──────────────────────────┘
               │ Compose UI
               ↓
┌─────────────────────────────────────────┐
│   Native Library (Kotlin)               │
│   - LiquidButton.kt                     │
│   - LiquidBottomTabs.kt                 │
│   - Backdrop Effects                    │
└─────────────────────────────────────────┘
```

### 4.2 브릿지 구성 요소

#### 1. React Native 컴포넌트 (JavaScript)
- React Native에서 사용할 컴포넌트 인터페이스 제공
- Props를 네이티브로 전달
- 네이티브 이벤트를 JavaScript 콜백으로 변환

#### 2. ViewManager (Kotlin)
- React Native와 네이티브 코드 사이의 다리 역할
- Props를 네이티브 타입으로 변환 (예: `"#FF0000"` → `Color`)
- 이벤트를 JavaScript로 전달 (`RCTEventEmitter`)
- ComposeView를 통해 UI 렌더링

#### 3. Package 등록
- `LiquidButtonPackage`를 `MainApplication`에 등록하여 ViewManager를 등록

### 4.3 데이터 흐름

**Props 전달 (JS → Native):**
```
JavaScript: <LiquidButton title="Button" tint="#0088FF" />
     ↓
Bridge: String → Color 변환
     ↓
Kotlin: LiquidButton(tint = Color(0xFF0088FF))
```

**이벤트 전달 (Native → JS):**
```
Kotlin: sendPressEvent()
     ↓
Bridge: RCTEventEmitter.receiveEvent()
     ↓
JavaScript: onPress(event)
```

### 4.4 타입 변환

| JavaScript | Kotlin | 변환 방법 |
|-----------|--------|----------|
| `string` | `String` | 직접 전달 |
| `number` | `Int` / `Float` | 직접 전달 |
| `"#FF0000"` | `Color` | `Color.parseColor()` |
| `require('./icon.png')` | `Uri` | `Image.resolveAssetSource()` |
| `function` | `Callback` | `RCTEventEmitter` |

### 4.5 주요 파일 구조

```
LiquidGlassRN/
├── src/components/
│   ├── LiquidButton.js          # JS 컴포넌트
│   └── BottomTabs.js            # JS 컴포넌트
└── android/app/src/main/java/com/liquidglassrn/
    ├── LiquidButtonViewManager.kt      # ViewManager
    ├── BottomTabsContentViewManager.kt # ViewManager
    ├── LiquidButtonPackage.kt           # Package 등록
    └── components/
        ├── LiquidButton.kt              # 네이티브 컴포넌트
        └── LiquidBottomTabs.kt          # 네이티브 컴포넌트
```

---

## 5. 시연

### 5.1 설치 및 실행

```bash
cd LiquidGlassRN
npm install
npm run android
```

### 5.2 사용 예시

#### LiquidButton

```javascript
<LiquidButton
  title="Button"
  tint="#0088FF"
  surfaceColor="#5FFFFFF"
  blurRadius={4}
  lensX={16}
  lensY={32}
  onPress={() => console.log('Pressed')}
/>
```

#### BottomTabs

```javascript
<BottomTabs
  selectedTabIndex={selectedTab}
  tabsCount={4}
  tabLabels={['Home', 'Search', 'Profile', 'Settings']}
  tabIcons={[
    require('./assets/home.png'),
    require('./assets/search.png'),
    require('./assets/profile.png'),
    require('./assets/settings.png'),
  ]}
  onTabSelected={(index) => setSelectedTab(index)}
/>
```

### 5.3 데모 화면

1. **HomeScreen**: 메인 카탈로그 화면
2. **ButtonsScreen**: 다양한 LiquidButton 스타일 데모
3. **BottomTabsScreen**: LiquidBottomTabs 인터랙션 데모

### 5.4 주요 기능

✅ **블러 효과**: 배경을 흐리게 처리  
✅ **렌즈 굴절**: 실제 유리처럼 배경 굴절  
✅ **색수차**: 프리즘 효과 (선택적)  
✅ **인터랙티브 애니메이션**: 터치 시 반응  
✅ **커스터마이징**: 색상, 블러, 굴절 강도 조절  

---

## 📚 참고 자료

- [Android Backdrop 라이브러리 문서](https://kyant.gitbook.io/backdrop)
- [React Native Native Modules 가이드](https://reactnative.dev/docs/native-modules-android)
- [Jetpack Compose 문서](https://developer.android.com/jetpack/compose)

## 📄 라이선스

Apache License 2.0
