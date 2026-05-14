# Product Requirements Document (PRD): Namma Haadi

## 1. Project Overview
**Namma Haadi** (meaning "Our Path") is a community-driven mobile platform designed to empower citizens to report, track, and monitor road-related issues in real-time. The application leverages crowdsourced data to improve road safety and maintenance visibility by connecting local communities with actionable information.

---

## 2. Problem Statement
Many neighborhoods suffer from poor road conditions (potholes, flooding, blockages) that go unreported or take a long time to be addressed. There is often a lack of real-time communication between citizens about immediate road hazards, leading to safety risks and inefficient travel.

---

## 3. Goals & Objectives
*   **Enable Crowdsourcing**: Provide a simple interface for users to report road issues with photos and GPS locations.
*   **Real-time Awareness**: Create a live map and alert system to warn users of hazards.
*   **Community Engagement**: Gamify the reporting process to encourage active participation.
*   **Transparency**: Allow users to track the status of reported issues from "Active" to "Resolved."

---

## 4. Target Audience
*   **Daily Commuters**: People looking for safe and clear routes.
*   **Community Volunteers**: Citizens proactive about improving their local infrastructure.
*   **Local Authorities (Indirectly)**: Using the data to identify high-priority repair areas.

---

## 5. Functional Requirements

### 5.1 User Authentication
*   **Phone Login**: Secure authentication using Phone Number and OTP (One-Time Password).
*   **Profile Management**: Users can set their display name, address, and view their contribution history (reports, updates, points).

### 5.2 Road Issue Reporting
*   **Create Report**: Users can submit a report containing:
    *   **Type**: Pothole, Flooded, Road Work, Pipeline, Blocked, Muddy, etc.
    *   **Severity**: Low, Medium, High, Critical.
    *   **Media**: Attach photos of the issue.
    *   **Location**: Automatically capture GPS coordinates and address.
    *   **Description**: Optional text details.
*   **Status Tracking**: Reports move through stages: `ACTIVE` -> `IN_PROGRESS` -> `RESOLVED`.

### 5.3 Interactive Map
*   **Live Visualization**: View all active reports as markers on a Google Map.
*   **Filtering**: Filter markers based on issue type or severity.
*   **Detail View**: Click a marker to see the full report details and photo.

### 5.4 Road Alerts (Broadcast System)
*   **Urgent Broadcasts**: Users can send "Danger," "Warning," or "Info" alerts to the entire community.
*   **Instant Notifications**: Push notifications for critical alerts in specific areas.
*   **Feed View**: A dedicated list of all active alerts sorted by recency.

### 5.5 Gamification & Leaderboard
*   **Point System**: Earn points for reporting new issues and providing status updates.
*   **Badges**: Unlock badges (e.g., "Road Guardian," "Pothole Hunter") based on contribution levels.
*   **Global Rank**: View a leaderboard showing the top contributors in the community.

---

## 6. Non-Functional Requirements
*   **Performance**: Map markers should load efficiently even with many reports.
*   **Usability**: Simple, declarative UI built with Jetpack Compose for a modern feel.
*   **Reliability**: Real-time data sync using Firebase Firestore.
*   **Offline Support**: Ability to view cached reports when the internet is unstable.

---

## 7. Technical Stack
*   **Frontend**: Kotlin, Jetpack Compose.
*   **Architecture**: MVVM (Model-View-ViewModel) with Hilt for Dependency Injection.
*   **Backend**: Firebase (Firestore for Database, Auth for Login, Storage for Images, FCM for Alerts).
*   **APIs**: Google Maps SDK for Android, Google Play Services Location.

---

## 8. Future Scope
*   **AI Integration**: Automatic hazard detection from uploaded photos.
*   **Routing**: Integration with navigation to suggest routes avoiding "Danger" zones.
*   **Direct Government API**: Link reports directly to local municipal maintenance portals.
*   **Comments & Community Discussion**: Allow users to comment on specific reports.
