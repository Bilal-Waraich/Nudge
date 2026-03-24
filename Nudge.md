# Nudge — App Concept & Development Plan

> *The first app that learns how you think, so you don't have to.*

---

## Table of Contents

1. [The Idea](#1-the-idea)
2. [The Motive](#2-the-motive)
3. [Goals](#3-goals)
4. [What the App Covers](#4-what-the-app-covers)
5. [Core Features](#5-core-features)
6. [Technical Architecture](#6-technical-architecture)
7. [Data Model](#7-data-model)
8. [ML Strategy](#8-ml-strategy)
9. [App Structure & Screens](#9-app-structure--screens)
10. [Sprint Plan](#10-sprint-plan)
11. [Open Questions & Decisions Log](#11-open-questions--decisions-log)
12. [Design Team](#12-design-team)

---

## 1. The Idea

Nudge is a **behavioural pattern engine disguised as a daily planner**. The core insight is simple: most people already know what they *should* do — go to the gym, study, buy groceries, sleep early. What they lack is awareness of *when* they actually do those things well, and *why* they fail at them when they do.

The idea was born at the gym. The observation: you could build an app that prompts you to go to the gym daily, and you log whether you went. If you didn't, you log *why* — tired, hungry, had plans. Over weeks, this data reveals patterns you'd never notice manually. Maybe every Monday you skip because Sunday nights are consistently late. Maybe you always avoid leg day specifically, and that's the day you don't go.

This gym insight generalises completely. The same mechanism applies to studying, grocery shopping, meal prep, sleep, socialising — any recurring task you *intend* to do but don't always execute on efficiently. The app learns your personal operating rhythm and uses it to suggest better scheduling, proactively — not because you asked, but because it knows you.

The differentiator is the **directionality of intelligence**: most apps ask you to optimise yourself. Nudge observes you and does the optimising for you.

---

## 2. The Motive

### The problem with existing tools

- **Habit trackers** (Streaks, Habitica) measure completion but not *why* you fail. They show you a broken streak, not the pattern behind it.
- **Calendar apps** are passive containers. They hold what you schedule, not what you actually do.
- **Productivity apps** (Notion, Todoist) require active maintenance. They're only as good as the effort you put in.
- **Health apps** (Apple Health, Google Fit) track physical metrics but don't connect them to behaviour decisions.

None of these tools **learn you**. They all require you to already understand your own patterns, then encode them manually.

### The opportunity

The phone you carry already knows:
- Where you are at all times (location)
- What time it is and what day it is (temporal context)
- What you've told it you plan to do (calendar)

What's missing is two signals: the **friction signal** — *why* something didn't happen — and the **energy signal** — *how well* it went when it did. Nudge captures both at near-zero cost, then closes the loop with proactive suggestions.

### The personal angle

This is a tool built for the builder first. The founder use case is real: a university student balancing coursework, a part-time job, gym, social life, and personal projects — all competing for finite cognitive bandwidth. The app exists because no existing tool treats you like the complex, context-dependent person you actually are.

---

## 3. Goals

### Product goals

- **G1 — Capture friction at near-zero cost.** Logging whether you did something and why you didn't should take under 5 seconds. If it takes longer, users stop doing it.
- **G2 — Capture success quality, not just success.** Knowing you completed something is only half the signal. Knowing you completed it with high energy on a Wednesday at 7pm is what enables peak window detection. An optional one-tap success context after "done" provides this.
- **G3 — Surface patterns before users ask.** The app should tell you something you didn't already know about yourself within the first 3–4 weeks of use.
- **G4 — Make proactive suggestions, not just charts.** The output is not a dashboard. The output is a recommendation: "You tend to study well on Wednesday evenings — want to block that this week?"
- **G5 — Respect privacy completely.** All ML runs on-device. No behavioural data leaves the phone unless the user explicitly opts in to backup.
- **G6 — Stay out of the way.** The app should feel lightweight, not like another commitment. Notifications are smart, not spammy.

### Technical goals

- **T1 — Shared logic, native UI on both platforms.** KMP for all business logic, data, and ML. SwiftUI for iOS. Jetpack Compose for Android. iOS ships first, Android follows with zero shared-logic rewrite.
- **T2 — Modular ML layer.** The inference engine is behind a Kotlin interface. v1 is rule-based statistics. v2 is Core ML / TFLite behind the same interface. The rest of the app never knows the difference.
- **T3 — Offline-first.** The app works with no internet. SQLDelight on-device is the source of truth.
- **T4 — Scalable data schema.** The event log supports any task type. Adding a new domain requires zero schema changes.

### Business goals (future)

- **B1 — Validate on iOS first.** Ship a polished iOS-only v1, gather real retention metrics.
- **B2 — Open-source the logging SDK.** Build trust with privacy-conscious users and invite contribution.
- **B3 — Freemium model eventually.** Core logging free forever. Advanced pattern insights and calendar integration as a paid tier.

---

## 4. What the App Covers

### v1 scope (what you're building now)

| Domain | What it tracks | Friction signals |
|--------|---------------|-----------------|
| Gym / exercise | Did you go? What type of workout? | Tired, busy, no motivation, wrong time |
| Study sessions | Did you study? For how long? | Too tired, distracted, no suitable place |
| Grocery shopping | Did you shop? Did you stick to the plan? | No time, forgot, too far out of the way |
| Sleep | What time did you go to bed? | Social plans, screens, stress |

These four domains share the same underlying data model. Adding a new domain in v2 is a configuration change, not a code change.

### What it explicitly does NOT do (v1)

- It is not a fitness tracker (no rep counting, no heart rate)
- It is not a calorie counter
- It is not a to-do list manager
- It does not integrate with external calendars (v2 feature)
- It does not run any ML inference (v1 is statistics only)

### v2 extensions (post-validation)

- Calendar integration (Google Calendar, Apple Calendar)
- Location-aware suggestions ("you're leaving work — gym is 400m away")
- On-device ML using both friction and success context as training signals
- Android support — shared logic already exists, only Compose UI to write
- Partner/family shared tasks
- Custom task types defined by the user

---

## 5. Core Features

### 5.1 Daily prompt system

At a configurable time each day, the app sends a notification per tracked task: "Did you go to the gym today?"

**If YES:** An optional success context tap appears — one of three icons (great / okay / struggled). Always skippable in one tap. Over time this becomes the most valuable signal the ML has: not just *when* tasks are completed, but *when* they're completed well.

**If NO:** A Notification Content Extension renders the friction picker directly inside the expanded notification banner. No app open required. See Section 6 for the technical detail.

**Friction taxonomy (v1):**
- 😴 Low energy / tired
- 👥 Social conflict / plans changed
- ⏰ Poor timing / ran out of time
- 🌧 External factor (weather, transport, illness)
- ❓ Just didn't feel like it

**Success context taxonomy (v1):**
- ⚡ Felt great / high energy
- 👍 Felt okay / normal
- 😓 Struggled through / low energy

### 5.2 Interactive home screen widget (App Intents)

Using iOS 17+ App Intents, the home screen widget is fully interactive. Tapping YES or NO on a task calls a `LogEventIntent` that writes to the shared KMP repository directly — no app open, no deep link. The widget updates immediately. This is the primary input mechanism for the near-zero friction logging goal.

### 5.3 Pattern dashboard

Built entirely with Apple's native **Swift Charts** framework. Sections:
- Completion rate heatmap: day of week × task, colour = completion rate
- Energy heatmap: time window × task, colour = success context distribution
- Per-task breakdown: completion rate, top friction code, peak energy window, trend arrow
- Streak and consistency score

All data computed by the shared `GetWeekPatternsUseCase` in Kotlin — identical on iOS and Android.

### 5.4 Suggestion cards (v1 — rule-based)

The statistics engine generates two categories of cards:

**Failure pattern cards** (from friction data):
- "You've skipped the gym 4 out of the last 5 Mondays."
- "Your most common reason for skipping is low energy on weekday mornings."

**Peak performance cards** (from success context data):
- "You consistently report high energy for gym on Saturday mornings — protect that slot."
- "You study best on Wednesday evenings. You've logged 'felt great' 6 out of the last 8 times."

Cards appear on the home screen, are dismissable, and the user's accept/reject response is stored as training signal for v2 ML.

### 5.5 Weekly review

Every Sunday evening, a summary notification with the week's highlights and one suggested change for next week. The user can accept or reject it — stored in `weekly_reviews`.

### 5.6 Task configuration

- Task name and domain (gym, study, grocery, sleep, custom)
- Expected frequency (daily, specific days of week)
- Preferred time window (morning, afternoon, evening)
- Prompt time (when to receive the daily notification)

---

## 6. Technical Architecture

### Why Kotlin Multiplatform (KMP)

KMP shares business logic, the data layer, and ML inference across iOS and Android while keeping UI fully native. SwiftUI on iOS means full access to WidgetKit, App Intents, Core ML, Notification Content Extensions, and CLLocationManager — no compromises. When Android ships, only the Compose UI layer is new. The shared logic is already written and tested.

### IDE setup

| Tool | Purpose |
|------|---------|
| **JetBrains Fleet** | Primary editor for all Kotlin — shared module, use cases, SQLDelight schema. Lighter than Android Studio, native KMP support, modern feel. |
| **Android Studio** | Gradle sync, Android emulator, Android-specific debugging when needed. |
| **Xcode** | iOS builds, SwiftUI development, WidgetKit, App Intents, Notification Content Extension. Cannot be avoided for iOS. |

Fleet is your day-to-day editor for Kotlin. Xcode for iOS-specific Swift work and builds.

### Stack

| Layer | Technology | Scope |
|-------|-----------|-------|
| Shared logic | Kotlin Multiplatform (KMP) | Business logic, data, ML interface, use cases |
| iOS UI | SwiftUI | iOS-only screens and components |
| Android UI | Jetpack Compose | Android-only (v2) |
| Database | SQLDelight | Shared schema, generates type-safe Kotlin + Swift APIs |
| iOS charts | Swift Charts (Apple native) | Heatmaps and bar charts — no custom geometry needed |
| iOS ML (v2) | Core ML adapter (Swift) | Implements `PatternInferenceEngine` interface |
| Android ML (v2) | TFLite adapter (Kotlin) | Implements `PatternInferenceEngine` interface |
| iOS notifications | UNUserNotificationCenter + Notification Content Extension | YES/NO actions + inline friction picker without opening app |
| iOS widgets | WidgetKit + App Intents (iOS 17+) | Fully interactive home and lock screen widgets |
| iOS location | CLLocationManager (Swift) | Implements `LocationService` interface (v2) |
| CI/CD | Fastlane + GitHub Actions | Automated builds for both platforms |
| Testing | KMP test module + XCTest | Shared logic in Kotlin, UI per platform |

### Critical iOS implementation notes

#### Notification Content Extension (Sprint 2)

Standard iOS notification actions cannot render a dynamic custom UI after user interaction. To show the friction picker *inside* the notification without opening the app, a **Notification Content Extension** is required — a separate Swift target in Xcode that renders a custom SwiftUI view inside the expanded notification banner.

Flow:
1. Notification arrives: "Did you go to the gym?"
2. User long-presses to expand → Content Extension renders YES / NO
3. User taps NO → Content Extension reveals the 5-icon friction picker inline
4. User taps a friction icon → Content Extension calls shared KMP `LogEventUseCase`, dismisses
5. App never opens

This is a non-trivial Sprint 2 task. Budget extra time for the Xcode target setup and Swift↔KMP bridging.

#### App Intents for interactive widgets (Sprint 5)

iOS 17+ App Intents allow widgets to execute code directly without launching the app:

```swift
// iosApp/Widgets/LogEventIntent.swift
struct LogEventIntent: AppIntent {
    @Parameter var taskId: String
    @Parameter var outcome: String  // "done" | "skipped"

    func perform() async throws -> some IntentResult {
        NudgeSDK.shared.logEvent(taskId: taskId, outcome: outcome)
        return .result()
    }
}
```

The widget calls `LogEventIntent` on tap, writes to the shared KMP repository, and reloads the timeline. True one-tap logging from the home screen.

### Project folder structure

```
nudge/
├── shared/                              # KMP shared module — Kotlin only
│   └── src/
│       ├── commonMain/kotlin/
│       │   └── com/nudge/
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   ├── Task.kt
│       │       │   │   ├── BehaviourEvent.kt
│       │       │   │   ├── FrictionReason.kt
│       │       │   │   ├── SuccessContext.kt      # ← energy signal
│       │       │   │   └── Suggestion.kt
│       │       │   ├── usecase/
│       │       │   │   ├── LogEventUseCase.kt
│       │       │   │   ├── GetWeekPatternsUseCase.kt
│       │       │   │   ├── GenerateSuggestionsUseCase.kt
│       │       │   │   └── SchedulePromptsUseCase.kt
│       │       │   └── port/
│       │       │       ├── EventRepository.kt
│       │       │       ├── PatternInferenceEngine.kt
│       │       │       └── NotificationService.kt
│       │       └── infrastructure/
│       │           ├── db/
│       │           │   └── SqlDelightEventRepository.kt
│       │           └── ml/
│       │               └── StatisticsEngine.kt    # v1 — pure Kotlin
│       ├── iosMain/kotlin/
│       └── androidMain/kotlin/
│
├── iosApp/                              # iOS — Swift + SwiftUI only
│   └── NudgeApp/
│       ├── App.swift
│       ├── Views/
│       │   ├── TodayView.swift
│       │   ├── PatternsView.swift        # uses Swift Charts
│       │   ├── TasksView.swift
│       │   └── SettingsView.swift
│       ├── Adapters/
│       │   ├── CoreMLPatternEngine.swift  # v2 placeholder
│       │   ├── UNNotificationService.swift
│       │   └── CLLocationAdapter.swift
│       ├── Widgets/
│       │   ├── NudgeWidget.swift          # WidgetKit + App Intents
│       │   └── LogEventIntent.swift
│       └── NotificationExtension/        # Separate Xcode target
│           └── NotificationViewController.swift
│
├── androidApp/                          # Android — Kotlin + Compose (v2)
│   └── src/main/
│       ├── MainActivity.kt
│       ├── ui/
│       │   ├── TodayScreen.kt
│       │   ├── PatternsScreen.kt
│       │   ├── TasksScreen.kt
│       │   └── SettingsScreen.kt
│       └── adapters/
│           ├── TFLitePatternEngine.kt
│           └── AndroidNotificationService.kt
│
└── build.gradle.kts
```

### Key architectural principle: ports & adapters

`domain/port/` defines interfaces in shared Kotlin. Each platform provides concrete adapters. Upgrading is always one file.

```kotlin
// shared/domain/port/PatternInferenceEngine.kt
interface PatternInferenceEngine {
    fun predictOptimalSlot(taskId: String, context: UserContext): SlotSuggestion
    fun updateModel(event: BehaviourEvent)
}

// v1 — ships now, pure Kotlin statistics
class StatisticsEngine(private val repo: EventRepository) : PatternInferenceEngine { ... }

// v2 iOS — swap in later, zero upstream changes
class CoreMLPatternEngine: PatternInferenceEngine { ... }  // Swift
```

---

## 7. Data Model

### Schema (SQLDelight)

```sql
-- tasks.sq
CREATE TABLE tasks (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    domain TEXT NOT NULL,             -- 'exercise' | 'study' | 'shopping' | 'sleep' | 'custom'
    frequency TEXT NOT NULL,          -- JSON: { "type": "weekly", "days": [1,3,5] }
    preferred_window TEXT NOT NULL,   -- 'morning' | 'afternoon' | 'evening'
    prompt_time TEXT NOT NULL,        -- '18:00'
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at INTEGER NOT NULL
);

-- behaviour_events.sq
CREATE TABLE behaviour_events (
    id TEXT NOT NULL PRIMARY KEY,
    task_id TEXT NOT NULL REFERENCES tasks(id),
    scheduled_for INTEGER NOT NULL,
    logged_at INTEGER NOT NULL,
    outcome TEXT NOT NULL,            -- 'done' | 'skipped'
    friction_code TEXT,               -- 'tired' | 'busy' | 'timing' | 'external' | 'unmotivated'
    friction_note TEXT,               -- optional free text
    success_context TEXT,             -- 'great' | 'okay' | 'struggled' | null  (only when outcome='done')
    context_zone TEXT,                -- 'home' | 'work' | 'transit' | null  (v2)
    day_of_week INTEGER NOT NULL,     -- 0-6, denormalised for query speed
    hour_of_day INTEGER NOT NULL,     -- 0-23, denormalised for query speed
    created_at INTEGER NOT NULL
);

-- suggestions.sq
CREATE TABLE suggestions (
    id TEXT NOT NULL PRIMARY KEY,
    task_id TEXT NOT NULL REFERENCES tasks(id),
    type TEXT NOT NULL,               -- 'pattern_insight' | 'schedule_change' | 'route_opportunity' | 'peak_window'
    content TEXT NOT NULL,
    generated_at INTEGER NOT NULL,
    shown_at INTEGER,
    user_response TEXT,               -- 'accepted' | 'rejected' | 'dismissed'
    responded_at INTEGER
);

-- weekly_reviews.sq
CREATE TABLE weekly_reviews (
    id TEXT NOT NULL PRIMARY KEY,
    week_starting INTEGER NOT NULL,
    summary_json TEXT NOT NULL,
    top_suggestion TEXT NOT NULL,
    user_accepted INTEGER,
    created_at INTEGER NOT NULL
);
```

### Design notes

**Why denormalise `day_of_week` and `hour_of_day`?** Pattern queries aggregate by day constantly. Storing at insert time costs two integers per row and saves date arithmetic on every query. The benefit compounds with every week of data.

**Why `success_context` on `behaviour_events`?** This is the missing positive signal. `friction_code` tells the model *when and why* tasks are skipped. `success_context` tells it *when* tasks are completed at peak quality. Without it, the ML knows your failure windows but cannot confidently identify your best windows. It is always nullable and always optional — one extra tap the user can skip.

---

## 8. ML Strategy

### v1 — Statistics engine (ships with the app)

No ML library. Pure Kotlin in the shared module. Fully testable on both platforms. Computes:

- Completion rate per task per day of week
- Completion rate per task per time window
- Most frequent friction code per task
- Success context distribution per time window
- Consistency trend (last 4 weeks vs previous 4 weeks)

Two categories of suggestion rules:

**Failure pattern rules** (friction signal):
```kotlin
if (completionRateForDay(taskId, DayOfWeek.MONDAY) < 0.3 && sampleSize >= 5) {
    yield(Suggestion(
        type = SuggestionType.SCHEDULE_CHANGE,
        content = "You rarely complete this on Mondays — consider moving it to Tuesday."
    ))
}
```

**Peak performance rules** (success context signal):
```kotlin
if (highEnergyRateForWindow(taskId, TimeWindow.WEDNESDAY_EVENING) > 0.7 && sampleSize >= 5) {
    yield(Suggestion(
        type = SuggestionType.PEAK_WINDOW,
        content = "You consistently feel great doing this on Wednesday evenings — protect that slot."
    ))
}
```

### v2 — On-device model

`StatisticsEngine` replaced by platform adapters behind the same `PatternInferenceEngine` interface:

- **iOS:** `CoreMLPatternEngine.swift` — Core ML, best-in-class on iOS
- **Android:** `TFLitePatternEngine.kt` — TensorFlow Lite

The `success_context` column becomes a key training label alongside `outcome`. The model learns to predict both completion probability and completion *quality* given time, day, and context zone. No data leaves the device.

### v3 — Location context (future)

Geofencing adds `context_zone` per event. The pattern engine gains a spatial dimension. Route suggestions become possible: "You complete gym with high energy 70% of the time when you come directly from work — you're leaving work now."

---

## 9. App Structure & Screens

### Navigation structure

```
Tab bar (4 tabs):
├── Today           ← home, daily tasks, suggestion cards
├── Patterns        ← completion heatmap, energy heatmap, trends
├── Tasks           ← manage task list, add/edit/archive
└── Settings        ← notifications, privacy, account

Modal flows:
├── Quick log        ← triggered from notification or widget
├── Success context  ← optional one-tap after "done" (3 icons, skippable)
├── Friction picker  ← inline in Notification Content Extension, or in-app
├── Weekly review    ← Sunday evening modal
└── Onboarding       ← first launch flow
```

### Screen descriptions

**Today (home tab)**
Today's tasks with status (pending / done / skipped). 1–2 suggestion cards at top. Minimal — a glanceable status screen, not a dashboard.

**Patterns tab**
Built with Swift Charts. Completion heatmap (day × task). Energy heatmap (time window × task). Per-task stats: completion rate, top friction code, peak energy window, trend arrow. Suggestion history with user responses.

**Tasks tab**
Task list with active/archived toggle. Add task flow: name → domain → frequency → time window → prompt time → confirm.

**Settings tab**
Notification preferences, daily digest toggle, data export, data wipe. Location and calendar permissions in v2.

**Quick log modal**
Task name, large YES / NO. If YES → optional success context (3 icons, skippable in one tap). If NO → friction picker (5 icons). Maximum two meaningful taps in either path.

**Onboarding flow**
Welcome → add first task → set notification time → grant permissions. Skippable after first task created.

---

## 10. Sprint Plan

Each sprint is 1–2 weeks. Every sprint ends with something usable on your iPhone. iOS throughout; Android added in Sprint 9 as UI-only on top of complete shared logic.

---

### Sprint 0 — KMP project setup (Week 1)
*Goal: project compiles and runs on your iPhone*

- [ ] Generate KMP project via [kmp.jetbrains.com](https://kmp.jetbrains.com) — iOS + Android targets, SwiftUI for iOS
- [ ] Set up JetBrains Fleet as primary Kotlin editor
- [ ] Define shared models: `Task`, `BehaviourEvent`, `SuccessContext`, `FrictionReason`, `Suggestion`
- [ ] SQLDelight schema: `tasks` and `behaviour_events` including `success_context` column from day one
- [ ] Run migration, verify SQLite DB creates on iOS simulator
- [ ] Stub SwiftUI tab bar with 4 empty tabs
- [ ] App runs on physical iPhone via Xcode

**Deliverable:** KMP project compiles for iOS. Empty 4-tab app on your iPhone. Schema with `success_context` exists.

---

### Sprint 1 — Task management (Week 2)
*Goal: create and manage tasks on your iPhone*

- [ ] `LogEventUseCase`, `GetTasksUseCase` in shared Kotlin
- [ ] `SqlDelightEventRepository` wired to SQLDelight
- [ ] SwiftUI: Tasks tab with list view
- [ ] SwiftUI: Add task flow (name → domain → frequency → prompt time)
- [ ] SwiftUI: Edit and archive tasks
- [ ] Tasks persist across restarts
- [ ] Unit tests for all use cases

**Deliverable:** Gym, study, and grocery tasks created and persisted.

---

### Sprint 2 — Daily logging + Notification Content Extension (Week 3–4)
*Goal: full logging loop including inline friction picker in notification*

- [ ] SwiftUI: Today screen with today's pending tasks
- [ ] SwiftUI: Quick log modal — YES / NO, success context picker (optional, skippable), friction picker
- [ ] `LogEventUseCase` writes `outcome` + `success_context` + `friction_code` to shared repository
- [ ] Today screen updates immediately after logging
- [ ] iOS local notifications via `UNUserNotificationCenter` adapter
- [ ] **Notification Content Extension:** separate Xcode target rendering friction picker inside expanded notification
- [ ] YES tap logs done → shows optional success context picker in-app
- [ ] NO tap expands Content Extension with friction icons inline, no app open
- [ ] `UNNotificationService` adapter implements shared `NotificationService`

> **Note:** The Notification Content Extension is a non-trivial Xcode task. The separate target setup, entitlements, and Swift↔KMP bridging all need dedicated time. Do not underestimate this sprint.

**Deliverable:** Full logging loop under 5 seconds. Friction picker works inside notification without opening the app.

---

### Sprint 3 — Pattern visualisation (Week 5–6)
*Goal: the data starts talking back*

- [ ] `GetWeekPatternsUseCase` in shared Kotlin — returns completion rates, friction distribution, success context distribution
- [ ] SwiftUI: Patterns tab using **Swift Charts**
  - Completion heatmap: day × task, colour = completion rate
  - Energy heatmap: time window × task, colour = success context
  - Day-of-week bar chart per task
- [ ] Per-task stats card: completion rate, top friction code, peak energy window
- [ ] Gate: minimum 2 weeks of your own real logged data before building this sprint

**Deliverable:** "I skip gym on Mondays 80% of the time, but Saturday mornings I always feel great" visible as fact.

---

### Sprint 4 — Suggestions engine (Week 7–8)
*Goal: the app says something useful unprompted*

- [ ] `StatisticsEngine.kt` implementing `PatternInferenceEngine` in shared module
- [ ] At least 4 failure pattern rules using `friction_code` aggregations
- [ ] At least 3 peak performance rules using `success_context` aggregations
- [ ] `GenerateSuggestionsUseCase` writes to `suggestions` table
- [ ] SwiftUI: Suggestion cards on Today screen (dismissable, accept/reject)
- [ ] Sunday weekly review notification + SwiftUI modal
- [ ] `weekly_reviews` table populated

**Deliverable:** The app surfaces a failure pattern and a peak window you didn't consciously know about.

---

### Sprint 5 — Interactive widgets + App Intents (Week 9–10)
*Goal: logging works without ever opening the app*

- [ ] WidgetKit extension: home screen widget showing today's tasks
- [ ] `LogEventIntent` AppIntent — YES/NO executed directly from widget tap (iOS 17+)
- [ ] Lock screen widget: today's completion summary
- [ ] Widget timeline reloads immediately after log
- [ ] Haptic feedback on all log actions
- [ ] Empty states for all screens
- [ ] App icon and launch screen
- [ ] Onboarding flow polished

**Deliverable:** Log a task from the home screen without opening the app. The app feels like a real product.

---

### Sprint 6 — Hardening (Week 11–12)
*Goal: it works reliably, not just usually*

- [ ] `BGTaskScheduler` background task for notification rescheduling (survives app kill)
- [ ] Data export (JSON) in Settings
- [ ] Offline resilience: 48h airplane mode test
- [ ] Shared module unit test coverage >80%
- [ ] Patterns tab loads in <300ms with 90 days of data (Swift Charts performance check)
- [ ] TestFlight build submitted
- [ ] 2–3 real users onboarded

**Deliverable:** TestFlight-ready. Real people using it daily.

---

### Sprint 7 — ML foundation (Week 13–14, optional)
*Goal: swap in v2 ML without touching shared logic or SwiftUI*

- [ ] `CoreMLPatternEngine.swift` stub implementing `PatternInferenceEngine`
- [ ] Training data export from `behaviour_events` including `success_context` as label
- [ ] Core ML model design: inputs = (day_of_week, hour_of_day, task_domain), labels = (outcome, success_context)
- [ ] On-device fine-tuning experiment with real data
- [ ] A/B: `StatisticsEngine` vs `CoreMLPatternEngine` on your own device

**Deliverable:** ML adapter wired in and hot-swappable. SwiftUI and shared use cases untouched.

---

### Sprint 8 — Location context (Week 15–16, optional)
*Goal: the app knows where you are*

- [ ] `CLLocationAdapter.swift` implementing shared `LocationService`
- [ ] Geofence zone setup UI: home, work, gym, grocery store
- [ ] Zone transitions written to `behaviour_events.context_zone`
- [ ] Route opportunity suggestion rule: zone + time + success context
- [ ] Privacy screen: explicit explanation of what is stored and where

**Deliverable:** "You're leaving work — the gym is nearby and you usually feel great going from here" fires as a notification.

---

### Sprint 9 — Android (Week 17–18, optional)
*Goal: same app on Android, zero shared logic changes*

- [ ] Jetpack Compose: all 4 tabs using existing shared use cases
- [ ] `AndroidNotificationService.kt` implementing shared `NotificationService`
- [ ] `TFLitePatternEngine.kt` implementing `PatternInferenceEngine` (v2 placeholder)
- [ ] Android Glance widgets with interactive actions
- [ ] Google Play internal test track build

**Deliverable:** Nudge runs on Android. Shared logic untouched.

---

## 11. Open Questions & Decisions Log

### Decided

| Decision | Choice | Reason |
|----------|--------|--------|
| App name | Nudge (internal codename only) | On-brand, but heavily trademarked — needs unique public name before App Store launch |
| Framework | Kotlin Multiplatform (KMP) | Native UI on both platforms, shared logic, no compromises |
| UI — iOS | SwiftUI | Native, full platform API access, best Core ML integration |
| UI — Android | Jetpack Compose (v2) | Native, consistent with KMP Kotlin stack |
| Platform order | iOS first | Have iPhone, Core ML is best-in-class on-device ML |
| Database | SQLDelight | Works in KMP shared module, type-safe APIs for both platforms |
| ML v1 | Statistics only (pure Kotlin) | Ship fast, validate the logging loop first |
| ML privacy | On-device only | Sensitive behavioural data, GDPR simplicity |
| Repo | Private initially | ML engine and data flywheel are the moat |
| Primary IDE | JetBrains Fleet + Xcode | Fleet for Kotlin (lighter, native KMP support), Xcode for Swift and iOS builds |
| Charts | Swift Charts (Apple native) | Saves weeks vs custom geometry, production quality output |
| Success signal | `success_context` column on done events | Enables peak window detection — friction alone only explains failure |
| Notification friction UI | Notification Content Extension | Only way to render custom UI inside notification without opening app |
| Widget interaction | App Intents (iOS 17+) | True one-tap logging from home screen, no app open required |
| Design team size | 2 people | UI/UX designer + illustrator/motion designer — separate surfaces, no blocking |
| Design tool | Figma | Industry standard, Xcode asset export built in |
| Animation format | Lottie JSON via lottie-ios | Designer exports from After Effects, one line of Swift to render |
| Token naming | Figma names mirror Swift names exactly | Frictionless handoff, no translation layer needed |

### Open

| Question | Options | Notes |
|----------|---------|-------|
| Public app name | TBD | "Nudge" is heavily trademarked in HR and fintech software. Must resolve before Sprint 6. Needs to be unique on App Store search. |
| Monetisation | Freemium / one-time purchase / subscription | Decide post-TestFlight validation |
| Backend | None (v1) / Supabase (v2) | Only needed for cross-device sync or backup |
| Open source | Logging SDK only vs full app | Core pattern engine stays private either way |
| Onboarding depth | Minimal (1 task) vs guided (full setup) | Lean minimal for v1 |
| Android timeline | After iOS TestFlight | Shared logic already done; only Compose UI to write |
| Success context prompting frequency | Always / after first 3 completions / random 30% | Too frequent = annoying. Default proposal: shown for first 3 completions per task, then randomly ~30% of the time thereafter |

---

## 12. Design Team

### Overview

The architecture supports a two-person design team working in parallel with development from Sprint 0 onward. Because of the ports & adapters structure, the design team never touches the shared Kotlin module. Their entire world is Figma, the SwiftUI views, and the asset catalog.

The two roles map cleanly onto two distinct visual surfaces in the app:

| Role | Owns | Tools |
|------|------|-------|
| UI/UX designer | Screen designs, component library, design system tokens, interaction specs | Figma |
| Illustrator / motion designer | Friction icons, success context icons, empty state illustrations, app icon, micro-animations | Figma, After Effects, Lottie |

These two roles can work in parallel — functional UI and expressive UI are independent surfaces that don't block each other.

### Workflow

The design team works entirely in Figma. Their output flows into the codebase via a single translation layer:

```
Figma (design team)
    ↓  design tokens + asset exports
NudgeTokens.swift  ←  you maintain this one file
    ↓
iosApp/Views/  ←  SwiftUI screens consume tokens + call shared use cases
    ↓
shared/ KMP module  ←  use cases, data, ML — design team never touches this
```

When the design team updates a colour or spacing value in Figma, you update the corresponding line in `NudgeTokens.swift`. The entire app reflects it. Nothing in the shared Kotlin module ever changes.

### The design system file

Before any screens are designed, agree on a shared token naming convention between Figma and Swift. Token names must match exactly between the two environments so handoff is frictionless.

```swift
// iosApp/DesignSystem/NudgeTokens.swift

extension Color {
    static let nudgePrimary    = Color("NudgePrimary")     // from Xcode asset catalog
    static let nudgeSurface    = Color("NudgeSurface")
    static let nudgeMuted      = Color("NudgeMuted")
    static let nudgeSuccess    = Color("NudgeSuccess")
    static let nudgeWarning    = Color("NudgeWarning")
}

extension Font {
    static let nudgeHeading = Font.system(.title2, design: .rounded, weight: .semibold)
    static let nudgeBody    = Font.system(.body,   design: .default, weight: .regular)
    static let nudgeCaption = Font.system(.caption, design: .default, weight: .medium)
}

// Reusable container used across all screens
struct NudgeCard<Content: View>: View {
    let content: Content
    var body: some View {
        content
            .padding(16)
            .background(Color.nudgeSurface)
            .cornerRadius(16)
    }
}
```

If Figma calls a colour `nudge/surface/primary`, Swift calls it `Color.nudgeSurfacePrimary`. Matching names mean any designer can look at a SwiftUI file and immediately understand what token is being used.

### Asset handoff format

| Asset type | Format | Delivery |
|-----------|--------|----------|
| Icons (standard) | SF Symbols where possible, SVG otherwise | Exported as PDF vector into Xcode `.xcassets` |
| Friction + success context icons | SVG | Exported as PDF vector into Xcode `.xcassets` |
| Empty state illustrations | SVG or Lottie JSON | Via `.xcassets` or `lottie-ios` Swift package |
| Micro-animations | Lottie JSON | Rendered via `lottie-ios` |
| Colours | Xcode color set | Exported as `.xcassets` with dark mode variants |
| App icon | PNG at required sizes | Via Xcode `.appiconset` |

Using Lottie for animations is strongly recommended — designers export directly from After Effects via the Bodymovin plugin, and the `lottie-ios` Swift package renders them natively with a single line of Swift. No custom animation code required.

### What the design team should prioritise first

The design team should not start designing screens until the design system is agreed. The order of work is:

1. **Design system first** — colour palette, typography scale, spacing system, border radii, shadow levels. This becomes `NudgeTokens.swift`.
2. **Core components** — `NudgeCard`, task row, suggestion card, friction icon set, success context icon set. These are used on every screen.
3. **Today screen** — the highest-traffic screen, the one users see daily. Get this right before anything else.
4. **Quick log modal + friction picker** — the core interaction. The 5-second logging goal lives or dies here.
5. **Remaining screens** — Patterns, Tasks, Settings, Onboarding.

### Sprint touchpoints

The design team plugs into the sprint plan at specific points:

| Sprint | Design deliverable needed |
|--------|--------------------------|
| Sprint 0 | Design system tokens agreed, `NudgeTokens.swift` created |
| Sprint 1 | Task list and add-task flow designs finalised |
| Sprint 2 | Quick log modal, friction picker, success context picker designs finalised |
| Sprint 3 | Patterns tab designs including heatmap visual language |
| Sprint 4 | Suggestion card designs, weekly review modal |
| Sprint 5 | Widget designs, empty states, app icon, onboarding screens |
| Sprint 6 | Final polish pass — spacing, typography, dark mode QA |

The illustrator's work (icons, illustrations, animations) should be delivered by Sprint 2 at the latest so the logging loop has its full visual identity from the first real use.

### No architecture changes required

The current KMP + SwiftUI architecture handles this cleanly. The design team's work slots into `iosApp/Views/`, `iosApp/DesignSystem/`, and `iosApp/Assets/` without touching anything in `shared/`. When Android ships in Sprint 9, the design tokens and Figma components are reused — Jetpack Compose has its own equivalent of `NudgeTokens` that the Android UI layer references, and the Figma components serve as the source of truth for both.

---

*Last updated: March 2026*
*Status: Pre-development — Sprint 0 not yet started*
