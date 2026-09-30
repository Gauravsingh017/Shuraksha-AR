# Suraksha AR (सुरक्षा AR)
**Augmented Reality Vocational Safety Training Simulator for Mining & Heavy Industry**

Suraksha AR is a production-grade Android application designed to train underground coal mine workers and industrial technicians on critical safety protocols, statutory hazard mitigation, and emergency response. The application combines live camera-based Augmented Reality overlays, interactive 3D WebGL simulations, bilingual voice guidance in Hindi and English, module-separated assessment quizzes, and comprehensive DGMS study manuals compliant with the Directorate General of Mines Safety (DGMS) Coal Mines Regulations (CMR 2017).

---

## Key Features

### 1. High-Contrast Industrial UI Design
- **Enhanced Contrast & Visual Separation**: Built on a solid industrial cool slate canvas (`#E2E7ED`) paired with crisp pure white card surfaces (`#FFFFFF`), structured borders (`#CBD5E1`), and elevated drop shadows (3dp–6dp) to eliminate background camouflage.
- **Professional Standard**: Clean typography and authentic industrial design without distracting emoji artifacts.

### 2. Condition-Specific Problem Response & Immediate Worker Action Steps
In every simulation and training module, workers are presented with clear, condition-specific guidance when hazardous problems arise:
- **Condition Alert Banners**: Immediate threat status identification (e.g., Active Threat vs. Hazard Neutralized).
- **Step-by-Step Action Protocols**: Sequenced instructions guiding workers on the exact physical actions required (e.g., valve isolation, self-rescuer donning, LOTO padlock placement, earth grounding knife switch engagement).

### 3. DGMS Statutory Guidelines & Laws in Vocal Audio Format
- **Bilingual Voice Engine**: Native Android Text-to-Speech (TTS) engine supporting instant toggles between **Hindi (`hi_IN`)** and **Indian English (`en_IN`)**.
- **Voice-Enabled Laws & Guidelines**: Dedicated audio buttons allow workers to listen aloud to DGMS rules, Coal Mines Regulations (CMR 2017), and statutory parameters directly in the field or classroom.

### 4. Five Vocational Training Modules (DGMS CMR 2017 Compliant)
1. **Underground Gas & Mine Ventilation (DGMS CMR 2017 Reg 169 & 170)**
   - *Problem Condition*: Methane ($CH_4$) gas leak exceeding 1.25% statutory limit.
   - *Immediate Action*: Stop machinery, isolate electrical power, evacuate return airway, open auxiliary ventilation doors.
   - *DGMS Mandate*: Reg. 169 inflammable gas checks; 30 $m^3$/min minimum fresh air per worker.
2. **Underground Fire & Emergency Evacuation (DGMS CMR 2017 Reg 141 & 142)**
   - *Problem Condition*: Active conveyor belt fire with dense smoke and carbon monoxide.
   - *Immediate Action*: Don SCSR mask, clamp nose clip, activate deluge system, follow green dynamic floor arrows to sealed Refuge Bay.
   - *DGMS Mandate*: Reg. 142 self-rescuer carriage; maximum 300m refuge bay spacing.
3. **Conveyor & Machinery Guarding / LOTO (DGMS CMR 2017 Reg 184 & Tech Circular 04 of 2018)**
   - *Problem Condition*: Drive drum blockage, rotating pinch-point nip hazard.
   - *Immediate Action*: Pull emergency trip-wire, apply master LOTO padlock, tag lockout, verify zero electrical and mechanical energy.
   - *DGMS Mandate*: Reg. 184 machinery interlocking and mandatory nip guards.
4. **Substation Electrical Isolation & Arc Flash (DGMS CMR 2017 Reg 182 & CEA Regulations)**
   - *Problem Condition*: 3.3kV flameproof transformer enclosure insulation breach and arc flash risk.
   - *Immediate Action*: Don Class 4 arc-rated PPE, open upstream vacuum breaker, engage mechanical earth grounding knife switch to discharge busbars.
   - *DGMS Mandate*: Reg. 182 flameproof electrical apparatus and automatic earth leakage protection.
5. **Mandatory PPE & Dräger Self-Rescuer Muster (DGMS CMR 2017 Reg 142 & Mines Act 1952 Sec 67)**
   - *Problem Condition*: Damaged hermetic seal / moisture indicator breach on life-saving self-rescuer.
   - *Immediate Action*: Reject damaged unit, inspect cap lamp for 12hr continuous illumination, surrender contraband before shaft entry.
   - *DGMS Mandate*: Section 67 Mines Act strict prohibition of matches, lighters, and non-certified electronic contraband.

### 5. Module-Separated Assessment Quizzes
- **Independent Module Evaluation**: Quizzes are conducted separately after each module drill to test specific hazards, thresholds, and regulations.
- **Pass Benchmarks & Certifications**: 70%+ pass mark unlocks the vocational competency badge and certificate with grade calculation (Grade A, Qualified, Retest).
- **Audio Explanation**: Every question and post-submission explanation can be narrated aloud in Hindi or English.

### 6. Vocational Study Manual & Exam Prep Guide
- **Pre & Post-Module Study Manual**: Trainees can review detailed manuals with high-yield revision notes, DOs and DON'Ts, and statutory threshold tables before taking quizzes or after drills to achieve higher marks.
- **Full Narration Mode**: Complete manual text can be listened to in spoken voice format for accessible learning.

### 7. Local Offline Room Database Persistence
- Stores module progression, drill completion states, quiz scores, and certificate timestamps completely offline using Android Room (SQLite).

---

## Technical Stack

```
                        ┌───────────────────────────────┐
                        │      Jetpack Compose UI       │
                        │    (Material Design 3 M3)     │
                        └───────────────┬───────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
  ┌────────────────────┐     ┌────────────────────┐     ┌────────────────────┐
  │   ThreeDWebView    │     │ SurakshaVoiceTTS   │     │   MainViewModel    │
  │ (Three.js / WebGL) │     │ (Hindi & English)  │     │ (StateFlow / Coro) │
  └──────────┬─────────┘     └────────────────────┘     └──────────┬─────────┘
             │                                                     │
             ▼                                                     ▼
  ┌────────────────────┐                                ┌────────────────────┐
  │  CameraX Preview   │                                │   Room Database    │
  │ (AR Passthrough)   │                                │ (Offline SQLite)   │
  └────────────────────┘                                └────────────────────┘
```

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3)
- **AR / 3D Graphics**: CameraX / Camera2 background feed with embedded Three.js / WebGL runtime
- **Local Persistence**: Android Room Database (KSP)
- **Speech Engine**: Native Android `TextToSpeech` (Bilingual `hi_IN` / `en_IN`)
- **Architecture**: MVVM with Repository Pattern and Kotlin Coroutines / StateFlow

---

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                # Jetpack Compose UI, navigation, AR screens, quizzes, manuals
├── audio/
│   └── SurakshaVoiceManager.kt    # Bilingual TTS engine handling Hindi and English voice output
├── data/
│   ├── AppDatabase.kt             # Room database configuration
│   ├── ModuleEntity.kt            # Room data entities for progress and quiz scoring
│   ├── ModuleDao.kt               # Data access object with reactive Flow queries
│   ├── ModuleRepository.kt        # Repository bridging database and ViewModel
│   └── TrainingContent.kt         # Comprehensive DGMS SOPs, emergency steps, laws, and quiz banks
└── ui/
    ├── ThreeDEnvironment.kt       # Three.js 3D WebGL simulation canvas & JavaScript interfaces
    └── theme/
        ├── Color.kt               # Industrial safety high-contrast color palette
        ├── Theme.kt               # Material 3 theme configuration
        └── Type.kt                # Typography definitions
```

---

## Building and Running the App

### Requirements
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK Platform 36 (Minimum SDK: 24)
- Physical Android device with camera support recommended for live AR feed

### Steps
1. **Build debug APK:**
   ```bash
   gradle assembleDebug
   ```
2. **Install on device:**
   ```bash
   gradle installDebug
   ```

---

## Statutory References & Standards
- **Directorate General of Mines Safety (DGMS)**, Ministry of Labour and Employment, Govt. of India: https://www.dgms.gov.in
- **Coal Mines Regulations (CMR), 2017**:
  - Reg. 141 & 142 — Mine Fires, Spontaneous Heating, and Rescue Apparatus.
  - Reg. 169 & 170 — Inflammable and Noxious Gases, Auxiliary Ventilation Standards.
  - Reg. 182 & 184 — Electrical Installations, Flameproof Protection, Machinery Guarding.
- **Mines Act, 1952**: Section 67 (Prohibition of Contraband).
- **DGMS Technical Circular No. 04 of 2018**: Conveyor Belting and Nip Point Safeguards.

---

## License
This project is licensed under the Apache License 2.0.
