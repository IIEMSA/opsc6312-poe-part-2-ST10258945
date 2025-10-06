Here’s a clean **README.md** you can drop in the project root.

---

# SmartPlanner (Android)

A minimal, modern planner app for Android. Users can register/login, manage settings (stored securely), add tasks, and browse tasks via a **weekly calendar bar**. Networking uses Retrofit to a MockAPI backend. UI uses Material 3 with a dark theme.

## ✨ Features

* **Auth**: Firebase Email/Password (secure handling; no client-side password storage).
* **Settings**: Username, email, timezone, dark mode, notifications — saved with **EncryptedSharedPreferences**.
* **Tasks**

  * Add a task (FAB).
  * Swipe **left** to mark done/undone.
  * Swipe **right** to delete with **Undo**.
  * Tasks are filtered by the **selected date** in a weekly calendar bar.
* **REST API**: Retrofit + Moshi + OkHttp logging (MockAPI).
* **Events (sample)**: Load/create demo Events from the API via AppBar actions.

## 🧱 Tech Stack

* **Language**: Kotlin (JVM 17)
* **Min/Target SDK**: 24 / 35
* **UI**: Material Components, RecyclerView, ViewBinding
* **Arch**: ViewModel + LiveData, Repository, Coroutines
* **Networking**: Retrofit, Moshi, OkHttp Logging
* **Auth**: Firebase Authentication
* **Security**: EncryptedSharedPreferences (AES-256)
* **Build**: AGP 8.9.x

## 📁 Project Structure

```
app/
 ├─ src/main/java/com/example/smartplanner/
 │   ├─ App.kt
 │   ├─ data/
 │   │   ├─ model/ (Event.kt, TaskRemote.kt)
 │   │   └─ remote/ (ApiClient.kt, ApiService.kt)
 │   ├─ repo/ (EventRepository.kt, TaskRepository.kt)
 │   ├─ ui/
 │   │   ├─ SplashActivity.kt
 │   │   ├─ login/ (LoginActivity.kt)
 │   │   ├─ register/ (RegisterActivity.kt)
 │   │   ├─ settings/ (SettingsActivity.kt)
 │   │   └─ home/
 │   │       ├─ HomeActivity.kt
 │   │       ├─ Task.kt, TaskAdapter.kt
 │   │       ├─ WeekDay.kt, WeekAdapter.kt
 │   └─ viewmodel/ (SchedulerViewModel.kt, TaskViewModel.kt)
 └─ res/
     ├─ layout/ (activity_*.xml, item_task.xml, dialog_add_task.xml, item_week_day.xml)
     ├─ drawable/ (bg_input*.xml, bg_chip.xml, bg_day_selected.xml, bg_day_unselected.xml, ...)
     ├─ menu/ (top_app_bar_menu.xml, bottom_nav_menu.xml)
     └─ values/ (themes.xml, colors.xml, strings.xml)
```

## 🚀 Getting Started

### 1) Prerequisites

* Android Studio **Koala** or newer
* JDK **17**
* A Firebase project (Authentication enabled)
* A MockAPI project (or any hosted REST that matches the endpoints)

### 2) Clone & Open

```bash
git clone <your-repo-url>
cd SmartPlanner
# Open in Android Studio and sync Gradle
```

### 3) Firebase (Auth)

* Add an Android app in Firebase console with your `applicationId` (**com.example.smartplanner** by default).
* Download `google-services.json` into `app/`.
* In `build.gradle.kts (Module: app)` the Google Services plugin is already applied:

  ```kotlin
  plugins {
      id("com.google.gms.google-services")
  }
  ```
* Auth is used in `LoginActivity` / `RegisterActivity`. Passwords are handled securely by Firebase (TLS + server-side hashing). The app itself does **not** store plaintext passwords.

### 4) API (MockAPI)

* Create resources `events` and `tasks` in MockAPI.
* Copy your base URL into `ApiClient.kt`:

  ```kotlin
  private const val BASE_URL = "https://<YOUR-SUBDOMAIN>.mockapi.io/"
  ```
* Endpoints used:

  * `GET /events?createdBy={uid}`
  * `POST /events`
  * `GET /tasks?createdBy={uid}`
  * `POST /tasks`
  * `DELETE /tasks/{id}`

### 5) Run on Emulator/Device

* Build & run from Android Studio.
* Launcher activity is **SplashActivity**, which routes to Login or Home.
* Log in (or register, then log in).
* Tap the **Settings** icon in the top app bar to edit settings.

## 🧩 Core Screens

### Home

* Top app bar (menu: Settings, Load Events, Create Sample, Logout).
* **Weekly calendar bar** (horizontal) showing the current week (Sun–Sat). Selecting a day filters tasks.
* Task list with swipe gestures.
* **FAB** to add a task for the selected day.

### Settings

* Fields for username, email, timezone, and two switches.
* Values are saved to **EncryptedSharedPreferences** (`secure_settings.xml`).

### Auth

* Email/password register & login via Firebase Auth.

## 🔐 Security Notes

* **Passwords**: Never stored locally; handled by Firebase Auth.
* **Settings**: Stored with **EncryptedSharedPreferences** (AES-256-GCM/SIV).
* **Network**: HTTPS to MockAPI.

## 🛠 Build / Dependencies

Key bits from `app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.example.smartplanner"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.smartplanner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")

    // Firebase (via BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Retrofit/Moshi/OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Encrypted prefs
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

## 📲 Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- If you implement local reminders/notifications: -->
<!-- <uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> -->
```

## 🧪 Testing

* Unit tests: `app/src/test/...`
* Instrumented tests: `app/src/androidTest/...`
* Run from **Run > Run…** or **Run tests** in Android Studio.

## 🧰 Troubleshooting

* **Toolbar/menu not showing**: Ensure `setSupportActionBar(binding.topAppBar)` is called in `HomeActivity.onCreate()` and `onCreateOptionsMenu()` inflates `R.menu.top_app_bar_menu`.
* **Seeing app label ("SmartPlanner") instead of "SyncUp"**:

  * Set `supportActionBar?.title = "SyncUp"` after `setSupportActionBar`.
* **MockAPI errors**:

  * Confirm `BASE_URL` in `ApiClient.kt`.
  * Ensure `events`/`tasks` resources exist and you pass a real `createdBy` (Firebase `uid`).
* **Login issues**:

  * Check Firebase Auth is enabled; try creating a new user and check logs.

## 🗺 Roadmap

* Persist `dueDate` for tasks in the backend (`TaskRemote.dueAt` ISO-8601).
* Mark-done sync (PATCH).
* Pull-to-refresh.
* Search & filters (tag/priority).
* WorkManager reminders (optional).

## 🤝 Contributing

PRs welcome! Please:

* Use feature branches.
* Follow Kotlin style and keep UI modern/dark.
* Add small tests where it makes sense.

## 📄 License

This project is provided as-is for educational use. Add your preferred license (MIT, Apache-2.0, etc.) if publishing.

---

**Screenshots**
*Add a `/screenshots` folder and link images here (Home, Add Task, Settings, Week Bar).*

* Home (Week bar + tasks)
* Add Task dialog
* Settings

---

If you want, I can also generate a tiny **demo dataset** and a Postman collection for the MockAPI endpoints.
