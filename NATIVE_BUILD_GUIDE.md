# RizzGPT — Native App + Keyboard Extension

## Project Structure
```
something-special/
├── www/                    # Web app (auto-copied from root)
├── ios/                    # iOS native project
│   └── App/
│       ├── App/            # Main Capacitor app
│       └── RizzKeyboard/   # iOS Keyboard Extension (Swift)
├── android/                # Android native project
│   └── app/src/main/
│       └── java/com/rizzgpt/app/
│           ├── MainActivity.java
│           └── keyboard/RizzKeyboardService.java
├── *.html, *.js, *.css     # Source web files
├── capacitor.config.json   # Capacitor config
└── package.json            # NPM scripts
```

---

## Prerequisites

### For iOS (Mac required)
- macOS with **Xcode 15+**
- Apple Developer Account ($99/year for App Store)
- CocoaPods: `sudo gem install cocoapods`

### For Android
- **Android Studio** (any OS)
- JDK 17+
- Android SDK 34+

### Both
- Node.js 18+
- npm

---

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Sync web files to native projects
npm run sync

# 3. Open in IDE
npm run ios        # Opens Xcode
npm run android    # Opens Android Studio
```

---

## iOS Build + Keyboard Extension

### Step 1: Open in Xcode
```bash
npm run ios
```

### Step 2: Add Keyboard Extension Target in Xcode
1. In Xcode: **File → New → Target**
2. Search **"Custom Keyboard Extension"**
3. Name it: `RizzKeyboard`
4. Language: **Swift**
5. Bundle ID: `com.rizzgpt.app.RizzKeyboard`
6. Click **Finish**

### Step 3: Replace Generated Files
- Xcode creates default `KeyboardViewController.swift` — **replace its contents** with the file at:
  `ios/App/RizzKeyboard/KeyboardViewController.swift`
- Replace `Info.plist` with `ios/App/RizzKeyboard/Info.plist`

### Step 4: Configure Extension
1. Select the `RizzKeyboard` target in Xcode
2. Go to **Signing & Capabilities**
3. Set Team to your Apple Developer account
4. Bundle ID: `com.rizzgpt.app.RizzKeyboard`

### Step 5: Enable Full Access
The keyboard needs network access (for AI API calls):
- In `Info.plist`, `RequestsOpenAccess` is already set to `true`
- Users will need to enable "Allow Full Access" in Settings → RizzGPT Keyboard

### Step 6: App Groups (for shared data between app & keyboard)
1. Select **App** target → Signing & Capabilities → + Capability → **App Groups**
2. Add group: `group.com.rizzgpt.app`
3. Select **RizzKeyboard** target → same steps → same group

### Step 7: Build & Run
1. Select a real device (keyboard extensions don't work in simulator)
2. Build the **App** scheme first
3. Then build the **RizzKeyboard** scheme
4. On device: Settings → General → Keyboard → Keyboards → Add → RizzGPT Keyboard
5. Enable "Allow Full Access"

---

## Android Build + Keyboard Extension

### Step 1: Open in Android Studio
```bash
npm run android
```

### Step 2: Keyboard is Already Configured
The keyboard service is pre-configured in:
- `AndroidManifest.xml` — service registered with `BIND_INPUT_METHOD`
- `res/xml/method.xml` — IME configuration
- `keyboard/RizzKeyboardService.java` — full keyboard implementation

### Step 3: Build & Run
1. Connect device or start emulator
2. Click **Run** (green play button)
3. On device: Settings → System → Languages & Input → On-screen keyboard → Manage → Enable "RizzGPT Keyboard"
4. In any app, switch keyboard (globe icon or long-press space)

### Step 4: Generate Signed APK
1. Build → Generate Signed Bundle/APK
2. Create keystore (first time)
3. Build release APK or AAB for Play Store

---

## How the Keyboard Works

### User Flow
1. User is in **any messaging app** (WhatsApp, Instagram, Tinder, etc.)
2. Switch to **RizzGPT Keyboard** (globe icon)
3. Type/paste their message in the keyboard's input field
4. Select a **style** (Smooth, Funny, Bold, Flirty, Savage, Sweet)
5. Tap **⚡ Generate**
6. AI generates 5 reply options
7. **Tap any reply** → it types directly into the chat!
8. Hit send in the messaging app

### Architecture
```
┌─────────────────────────────┐
│     Any Messaging App       │
│   (WhatsApp, IG, Tinder)    │
├─────────────────────────────┤
│   RizzGPT Keyboard          │
│  ┌───────────────────────┐  │
│  │ ⚡ RizzGPT        🌐  │  │
│  │ [Smooth][Funny][Bold] │  │
│  │ [Their message... ] ⚡│  │
│  │ ─────────────────────│  │
│  │ > smooth reply 1   ←tap │
│  │ > funny reply 2        │  │
│  │ > bold reply 3          │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

---

## App Store Submission

### iOS App Store
1. Set version in Xcode: 1.0.0
2. Create app in App Store Connect
3. Add screenshots (6.7", 6.1", 5.5")
4. Privacy Policy URL required
5. Archive → Upload to App Store Connect
6. Submit for review

### Google Play Store
1. Create developer account ($25 one-time)
2. Generate signed AAB
3. Create app listing in Play Console
4. Upload AAB
5. Fill in content rating, privacy policy
6. Submit for review

---

## Updating the App

After making changes to web files:
```bash
# Sync changes to native projects
npm run sync

# Then rebuild in Xcode / Android Studio
```

---

## Troubleshooting

**iOS Keyboard not showing up:**
- Must test on real device, not simulator
- Settings → General → Keyboard → Keyboards → Add New Keyboard → RizzGPT
- Toggle "Allow Full Access" ON

**Android Keyboard not showing:**
- Settings → System → Languages & Input → On-screen keyboard → Manage keyboards → Enable RizzGPT
- Long-press spacebar or use globe icon to switch

**API calls failing from keyboard:**
- iOS: Ensure "RequestsOpenAccess" is true in extension Info.plist AND user enabled Full Access
- Android: Ensure INTERNET permission in manifest (already added)

**Web changes not reflecting:**
- Run `npm run sync` to copy web files to native projects
