# Expense Manager 💰🤖

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/AI-Google%20Gemini%20with%20Function%20Calling-orange.svg?logo=google)](https://ai.google.dev)
[![Database](https://img.shields.io/badge/Storage-Room%20(Offline--First)-brightgreen.svg?logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A modern, offline-first personal finance and expense manager for Android powered by an intelligent, real-time Gemini AI assistant equipped with native function calling and tool execution.

---

## 📖 Overview

**Expense Manager** combines the speed and privacy of an offline-first financial tracker with the power of Google's Gemini models. Unlike conventional chatbots that invent or guess financial details, this assistant uses **Gemini Function Calling (Tools)** to query your verified local SQLite/Room database in real time. 

Ask questions naturally like *"How much did I spend on groceries this month?"* or *"What was my biggest expense last week?"*, and receive accurate answers derived directly from your personal records—with built-in confirmation safeguards for destructive edits or deletions.

---

## ✨ Key Features

### 🤖 Intelligent AI Expense Assistant (Gemini Function Calling)
- **Zero Hallucinations:** Queries real local database records through typed function calls rather than fabricating numbers.
- **Natural Language Data Access:** Answers complex queries across dates, categories, merchants, and amounts (e.g., *"Compare my spending between this month and last month"*).
- **Conversational Expense Creation:** Add expenses effortlessly (e.g., *"Add ₹500 for coffee at Starbucks"*).
- **Safety First & Confirmation Modals:** Destructive operations like `updateExpense` and `deleteExpense` present interactive confirmation cards before executing any changes.
- **Thought Signature & Session Integrity:** Full support for Gemini multi-turn reasoning and tool response loops.

### 💳 Comprehensive Expense & Income Tracking
- **Granular Records:** Track amounts, categories, merchants, dates, payment methods, and notes.
- **Category Management:** Customizable categories with distinctive color coding and icons.
- **Payment Methods:** Organize expenses by Cash, Debit Card, Credit Card, UPI, or Net Banking.

### 📊 Visual Analytics & Reports
- **Interactive Visualizations:** Monthly breakdowns, category distribution charts, and period-over-period trend analysis.
- **Summary Metrics:** Total income, total spending, net balance, and average daily burn rate.

### 🎯 Budgets & Financial Goals
- **Category Budgets:** Set spending limits per category with progress bars and alerts when nearing limits.
- **Monthly Reset & Carryover:** Keep your finances disciplined each month.

### 🗓️ Calendar & Timeline Views
- **Day-by-Day Inspection:** View transaction distribution directly across calendar dates.
- **Recurring Bills & Subscriptions:** Track upcoming due dates to avoid late payment fees.

### 🛡️ Privacy, Security & Offline-First Design
- **100% Local Storage:** Transactions stay securely on your device using Android Room Database.
- **App Lock & PIN Security:** Protect sensitive financial data with customizable PIN authentication.
- **Local Backup & Restore:** Export and import transaction archives in JSON format.

---

## 🛠️ Gemini Tool Definitions (Function Calling)

The AI assistant integrates directly with the app's local data layer via the following functions:

| Function | Description | Parameters | Confirmation Required |
| :--- | :--- | :--- | :---: |
| `getExpenses` | Retrieves recent transaction records | `limit`, `categoryName`, `startDate`, `endDate` | No |
| `getExpenseById` | Fetches details of a specific expense record | `id` | No |
| `searchExpenses` | Searches transactions by keyword across merchants and notes | `keyword` | No |
| `getExpensesByCategory` | Aggregates and filters expenses within a category | `categoryName`, `startDate`, `endDate` | No |
| `getTotalExpenses` | Calculates total expenditure within a given date window | `startDate`, `endDate`, `categoryName` | No |
| `getMonthlySummary` | Generates total income, expense, and category totals for a month | `year`, `month` (1–12) | No |
| `getLargestExpense` | Identifies the highest single transaction in a period | `startDate`, `endDate`, `categoryName` | No |
| `addExpense` | Records a new expense into the local database | `amountRupees`, `categoryName`, `merchant`, `description`, `dateIso` | No |
| `updateExpense` | Updates an existing expense record | `id`, `amountRupees`, `categoryName`, `merchant`, `description` | **Yes (User Prompt)** |
| `deleteExpense` | Removes an expense permanently from the database | `id` | **Yes (User Prompt)** |

---

## 🏗️ Architecture & Tech Stack

```
com.example
├── data/
│   ├── database/         # Room Database, DAOs, Entities, Converters
│   ├── gemini/           # Gemini API Client, Tool Schema Definitions, Multi-turn Repository
│   └── repository/       # Offline-first Expense, Budget, and Category Repositories
├── security/             # PIN Encryption & Biometric Session Management
├── ui/
│   ├── components/       # Material 3 UI widgets, cards, charts, and buttons
│   ├── navigation/       # Navigation Compose destination routes & bottom bar
│   ├── screens/          # Dashboard, Transactions, Analytics, Budgets, Calendar, Chat, Settings
│   └── theme/            # Material 3 Dynamic ColorScheme, Typography & Shapes
└── util/                 # Date/Time formatting, Currency formatters (₹, $, etc.)
```

- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3).
- **Language:** [Kotlin 2.0+](https://kotlinlang.org) with Kotlin Coroutines and StateFlow.
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Local Persistence:** Android Jetpack [Room Database](https://developer.android.com/training/data-storage/room) (SQLite).
- **Networking & Serialization:** [Retrofit 2](https://square.github.io/retrofit/) with [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).
- **AI Integration:** Google Gemini REST API (`generateContent`) using native `FunctionDeclaration` and `thoughtSignature` preservation.
- **Testing:** Local JVM testing with JUnit and [Robolectric](https://robolectric.org/).

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later / Command Line with JDK 17+.
- Android SDK 34+.
- A Google Gemini API Key from [Google AI Studio](https://aistudio.google.com/).

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/expense-manager-android.git
   cd expense-manager-android
   ```

2. **Configure your Gemini API Key:**
   Create a `.env` file in the project root directory (or configure via the Secrets panel):
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```

3. **Build the debug APK:**
   ```bash
   gradle assembleDebug
   ```

4. **Run Unit & Robolectric Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 🔒 Privacy & Data Policy

- All personal financial records (expenses, categories, budgets, and bills) are stored **exclusively in a local SQLite database on your device**.
- When using the AI Assistant, only the data strictly relevant to the user's specific query is transmitted via encrypted HTTPS to Google's Gemini endpoint to execute function calls.
- No personal data is stored on remote third-party servers.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
