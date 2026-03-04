# LBO – Firebase Setup Guide

## 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add Project** → Name it **LBO**
3. Enable Google Analytics (optional)
4. Click **Create Project**

## 2. Add Android App

1. Click **Add App** → Select **Android**
2. Package name: `com.lbo.app`
3. App nickname: `LBO`
4. Download `google-services.json`
5. **Place it in `app/` directory**

## 3. Enable Authentication

1. Go to **Authentication** → **Sign-in method**
2. Enable **Email/Password**
3. Enable **Google** sign-in
4. Copy the **Web client ID** and replace `YOUR_WEB_CLIENT_ID` in `strings.xml`

## 4. Create Firestore Database

1. Go to **Firestore Database** → **Create database**
2. Start in **test mode** (update rules below for production)
3. Select region closest to your users

## 5. Firestore Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null &&
        (request.auth.uid == userId ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
    }

    // Providers collection
    match /providers/{providerId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'provider';
      allow update: if request.auth != null &&
        (resource.data.userId == request.auth.uid ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
      allow delete: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Bookings collection
    match /bookings/{bookingId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null &&
        (resource.data.customerId == request.auth.uid ||
         resource.data.providerId == request.auth.uid ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
    }

    // Reviews collection
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
    }

    // Community posts
    match /community_posts/{postId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Categories
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

## 6. Firebase Storage

1. Go to **Storage** → **Get Started**
2. Apply these rules:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    match /documents/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 7. Firebase Cloud Messaging (FCM)

1. Go to **Project Settings** → **Cloud Messaging**
2. FCM is enabled by default with Firebase BOM
3. The app handles token registration automatically

## 8. Create Admin Account

1. Register with email `admin@lbo.com` (or change in `Constants.kt`)
2. Go to Firestore Console → `users` collection
3. Find the user document → Change `role` field to `admin`

## 9. Create Firestore Indexes

Go to **Firestore** → **Indexes** → Add composite indexes:

| Collection | Fields | Order |
|---|---|---|
| bookings | customerId (Asc), createdAt (Desc) | |
| bookings | providerId (Asc), createdAt (Desc) | |
| providers | isApproved (Asc), rating (Desc) | |
| providers | category (Asc), isApproved (Asc) | |
| reviews | providerId (Asc), createdAt (Desc) | |

## 10. Run the App

```bash
# Open in Android Studio
# Sync Gradle files
# Ensure google-services.json is in app/
# Run on emulator or device (API 24+)
```

---

## Navigation Flow

```
Auth Flow:
  Login ─→ Register
  Login ─→ Forgot Password
  Login ─→ [Role-based Home]

Customer Flow:
  Home ─→ Search ─→ Provider Detail ─→ Book Service
  Home ─→ My Bookings ─→ Rate & Review
  Home ─→ Community
  Home ─→ Profile ─→ Logout

Provider Flow:
  Dashboard ─→ Edit Profile / Upload Docs
  Dashboard ─→ Accept/Reject/Complete Bookings
  Dashboard ─→ Community
  Dashboard ─→ Profile ─→ Logout

Admin Flow:
  Dashboard ─→ Approve/Reject Providers
  Dashboard ─→ Manage Categories
  Dashboard ─→ Create Community Post
  Dashboard ─→ View All Bookings
  Dashboard ─→ Profile ─→ Logout
```
