# Suraksha AR (सुरक्षा AR)
An Augmented Reality Vocational Safety Training Simulator for Mining & Heavy Industry

Suraksha AR is an Android application designed to train underground coal mine workers and industrial technicians on critical safety protocols, statutory hazard identification, and emergency response. The application combines camera-based Augmented Reality overlays, interactive 3D simulations, bilingual voice guidance in Hindi and English, and automated assessment modules compliant with the Directorate General of Mines Safety (DGMS) Coal Mines Regulations (CMR 2017).

---

## Background & Motivation

Underground mining environments present severe occupational hazards, including flammable methane build-ups, spontaneous coal combustion, conveyor entanglement, and high-voltage arc flashes. Traditional classroom training often fails to convey the spatial awareness and urgency required during underground emergencies. 

Suraksha AR was developed to bridge this gap by enabling workers to practice hazard recognition, Lockout/Tagout (LOTO) procedures, and evacuation drills directly on handheld mobile devices without exposing them to physical danger.

---

## Key Features

### 1. Augmented Reality & 3D Interactive Drills
- **Camera Passthrough**: Real-time camera feed provides an AR view of the user's surroundings.
- **Interactive 3D Overlays**: Lightweight, hardware-accelerated WebGL / Three.js scenes rendered transparently over the camera feed.
- **Physical Interactions**: Trainees interact directly with 3D hazard models—turning gas isolation valves, pulling emergency trip cords, operating electrical grounding switches, and inspecting safety gear.

### 2. Five Vocational Training Modules (DGMS Compliant)
1. **Underground Gas & Mine Ventilation (DGMS CMR 2017 Reg 169 & 170)**
   - Monitor Methane ($CH_4$) and Carbon Monoxide ($CO$) concentrations.
   - Recognize statutory evacuation thresholds (e.g., 1.2% $CH_4$ electrical cutoff).
   - Operate main pipeline gas isolation valves in simulated high-leak scenarios.
2. **Underground Fire & Emergency Evacuation (DGMS CMR 2017 Reg 141 & 142)**
   - Experience simulated conveyor belt fires with smoke stratification layers.
   - Practice low-crawl navigation along illuminated egress paths.
   - Navigate to the sealed Refuge Bay airlock within statutory oxygen timelines.
3. **Conveyor & Machinery Guarding / LOTO (DGMS Tech Circular 04 of 2018)**
   - Identify dangerous conveyor drive drum pinch points and nip hazards.
   - Trigger emergency pull-cord trip switches.
   - Apply mechanical padlocks and tags at the central Lockout/Tagout (LOTO) station.
4. **Substation Electrical Isolation & Arc Flash (DGMS CMR 2017 Reg 182)**
   - Inspect 3.3kV flameproof transformer enclosures.
   - Safely engage mechanical grounding knife switches to discharge residual capacitance.
5. **PPE & Dräger Self-Rescuer Inspection (DGMS CMR 2017 Reg 142)**
   - Perform pre-shift 3D inspection on Self-Contained Self-Rescuers (SCSR).
   - Check casing integrity, tamper seal, pressure gauge, and mouthpiece assembly.

### 3. Bilingual Voice Guidance (Hindi & English)
- Integrated Android native `TextToSpeech` engine configured for vocational mine workers.
- Instant toggle between **Hindi (`hi_IN`)** and **Indian English (`en_IN`)**.
- Audio narrations of standard operating procedures, alarm warnings, and quiz questions.

### 4. Local Assessment & Certification
- Multi-question evaluation at the end of each module.
- Detailed bilingual explanations for every correct and incorrect answer.
- Offline data persistence via Android Room (SQLite) database, tracking scores, completion timestamps, and digital badge awards.

---

## Architecture & Tech Stack

```
                          ┌───────────────────────────┐
                          │     Jetpack Compose UI    │
                          │   (Material 3 / Dark)     │
                          └─────────────┬─────────────┘
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 ▼                      ▼                      ▼
      ┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐
      │   ThreeDWebView    │ │  VoiceManager TTS  │ │   MainViewModel    │
      │ (Three.js / WebGL) │ │ (Hindi / English)  │ │   (StateFlow)      │
      └──────────┬─────────┘ └────────────────────┘ └──────────┬─────────┘
                 │                                             │
                 ▼                                             ▼
      ┌────────────────────┐                        ┌────────────────────┐
      │  CameraX Preview   │                        │  Room Database     │
      │ (AR Passthrough)   │                        │  (Offline Storage) │
      └────────────────────┘                        └────────────────────┘
```

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3
- **AR / 3D Graphics**: CameraX / Camera2 background feed with embedded Three.js / WebGL runtime
- **Local Persistence**: Android Room Database (KSP)
- **Audio**: Android Native `TextToSpeech` Engine
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **Minimum SDK**: Android API 24 (Android 7.0 Nougat)
- **Target SDK**: Android API 36

---

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                # App navigation, screens, and view model
├── audio/
│   └── SurakshaVoiceManager.kt    # TTS engine handling Hindi and English speech
├── data/
│   ├── AppDatabase.kt             # Room database configuration
│   ├── ModuleEntity.kt            # Room data entities for progress and quiz scoring
│   ├── ModuleDao.kt               # Data access object with reactive Flow queries
│   ├── ModuleRepository.kt        # Repository bridging database and ViewModel
│   └── TrainingContent.kt         # DGMS SOP data, regulatory thresholds, and quiz banks
└── ui/
    ├── ThreeDEnvironment.kt       # Three.js 3D simulation canvas & JavaScript interfaces
    └── theme/
        ├── Color.kt               # High-contrast industrial safety color palette
        ├── Theme.kt               # Material 3 theme configuration
        └── Type.kt                # Typography definitions
```

---

## Building and Running the App

### Requirements
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK Platform 36
- Physical Android device with camera support recommended for AR features

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/suraksha-ar.git
   cd suraksha-ar
   ```

2. **Setup environment variables (optional):**
   ```bash
   cp .env.example .env
   ```

3. **Build the debug APK via Gradle:**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on connected device:**
   ```bash
   ./gradlew installDebug
   ```

---

## Permissions

The app requests the following Android permissions:
- `android.permission.CAMERA`: Required to stream the live environment background for Augmented Reality overlays.
- `android.permission.INTERNET`: Required to load local WebGL / Three.js assets and font resources.

---

## Statutory References
- Directorate General of Mines Safety (DGMS), Government of India: https://www.dgms.gov.in
- Coal Mines Regulations (CMR), 2017:
  - Reg. 141 & 142 — Mine Fires, Spontaneous Heating, and Rescue Apparatus.
  - Reg. 169 & 170 — Inflammable and Noxious Gases, Ventilation Standards.
  - Reg. 182 — Safety in Electrical Installations and Substations.
- DGMS Technical Circular No. 04 of 2018 — Conveyor Belting and Nip Point Safeguards.

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
