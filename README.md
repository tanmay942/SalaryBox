# SalaryBox

Android staff attendance and salary management application.

## Overview

SalaryBox is an Android application designed to manage staff information,
staff attendance, face-based attendance verification, and attendance history
from an admin interface.

## Main Screens

- Login
- Admin — Staff List
- Admin — Add Staff
- Admin — Face Enrollment
- Staff — Mark Attendance
- Admin — Staff Profile / Attendance History

## Core Features

- Admin login
- Staff management
- Add and maintain staff profiles
- Staff face enrollment using the phone's front camera
- Face recognition when marking attendance
- Attendance capture with date and time
- Current latitude and longitude capture
- Attendance records visible to the administrator

## Technical Requirements

The application uses the Android platform and Kotlin.

The application requires:

- Front camera access for face enrollment and attendance verification
- Location access for capturing attendance coordinates
- Persistent storage for staff and attendance records
- Android device/emulator with the required permissions enabled

## Project Structure

```text
SalaryBox/
├── app/
│   └── src/
│       └── main/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
