# SalaryBox

Android-based staff attendance and salary management application with separate Admin and Staff workflows, face enrollment, face-based attendance verification, and location-based attendance records.

## Overview

SalaryBox is an Android application designed to simplify staff attendance management.

The application provides separate workflows for Administrators and Staff members. Administrators can manage staff profiles, enroll employee faces, view salary information, and review attendance records. Staff members can mark attendance using face verification with captured location, date, time, and attendance image.

## Key Features

### Admin

- Secure admin login
- View staff members
- Add staff members
- View individual staff profiles
- View employee salary information
- Enroll staff face using device camera
- Check face enrollment status
- View attendance history
- Monitor staff attendance records

### Staff

- Staff login
- Personalized staff dashboard
- Face enrollment status
- Face-based attendance verification
- Attendance marking using camera verification
- Automatic date and time capture
- Latitude and longitude capture
- Attendance history
- Captured attendance image

## Application Workflow

```text
Login
  |
  +-------------------+
  |                   |
Admin Dashboard    Staff Dashboard
  |                   |
  |                   +--> Face Verification
  |                           |
  |                           +--> Mark Attendance
  |                                  |
  |                                  +--> Date + Time
  |                                  +--> Location
  |                                  +--> Image
  |                                  |
  |                                  +--> Attendance History
  |
  +--> Staff List
  |      |
  |      +--> Staff Profile
  |             |
  |             +--> Face Enrollment
  |
  +--> Add Staff
  |
  +--> Attendance History# SalaryBox

> Android-based staff attendance and salary management application with admin-controlled staff management, face enrollment, face-verified attendance, and location-based attendance records.

## Overview

SalaryBox is an Android application designed to simplify staff attendance management.

The application provides separate workflows for **Administrators** and **Staff members**. Administrators can manage staff profiles, enroll employee faces, and review attendance records, while staff members can mark attendance using face verification with captured location and timestamp information.

---

## Key Features

### Admin

- Secure admin login
- View staff members
- Add staff members
- View individual staff profiles
- View employee salary information
- Enroll staff face using the device camera
- Check face enrollment status
- View attendance history
- Monitor staff attendance records

### Staff

- Staff login
- Personalized staff dashboard
- Face enrollment status
- Face-based attendance verification
- Attendance marking using camera verification
- Automatic date and time capture
- Latitude and longitude capture
- Attendance history
- Captured attendance image

---

## Application Workflow

```text
                    ┌───────────────┐
                    │     Login     │
                    └───────┬───────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
       ┌──────────────┐            ┌──────────────┐
       │     Admin    │            │    Staff     │
       │   Dashboard  │            │   Dashboard  │
       └──────┬───────┘            └──────┬───────┘
              │                           │
      ┌───────┼────────┐                  │
      │       │        │                  ▼
      ▼       ▼        ▼          Face Verification
   Staff     Add    Attendance             │
   List     Staff     History              ▼
      │                           Mark Attendance
      ▼                                  │
Staff Profile                           ▼
      │                           Date + Time +
      ▼                           Location + Image
Face Enrollment                           │
                                         ▼
                                  Attendance History
