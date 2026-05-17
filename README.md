# FutureMe Mail App

FutureMe Mail App is a Kotlin Android application that allows users to write letters to their future selves.  
The project integrates an Android frontend, backend API, database storage, Firebase Cloud Messaging, and scheduled notification / email delivery workflow.

「信運草」專案是一個「寫信給未來的自己」Android App。使用者可以撰寫信件、選擇未來接收日期，系統會將資料送至後端儲存，並搭配通知與 email 流程，模擬完整的未來信件服務。

## Project Purpose

This project was developed as a mobile application and system integration practice project.  
It focuses on Android app development, REST API communication, backend integration, database storage, and notification workflow design.

## Features

- Write a letter to the future self
- Select a future receive date
- Submit letter data from Android app to backend API
- Store future mail records in database
- Firebase Cloud Messaging token handling
- Scheduled notification / email workflow
- Multi-page Android app navigation
- Calendar / overview concept for future letters

## Tech Stack

### Android App

- Kotlin
- Android Studio
- AndroidX
- Retrofit
- OkHttp
- Firebase Cloud Messaging
- Material UI components

### Backend

- Node.js
- Express
- MySQL / MariaDB
- Firebase Admin SDK
- Email sending workflow
- Environment variable configuration

### Tools and Workflow

- Git / GitHub
- Branch-based team collaboration
- Android Studio
- Linux server deployment concept

## System Architecture

Android App
    ↓ Retrofit API Request
Node.js / Express Backend
    ↓
MySQL / MariaDB Database
    ↓
Scheduled task / notification workflow
    ↓
Firebase Cloud Messaging / Email

## Demo

- [Video](https://reurl.cc/jmalvy)

## Docs

- [Report](https://docs.google.com/document/d/1FDA-Yr5ck-3UhqH9ynpLNdpzo8fJfy7Uy2mrFMYucTI/edit?usp=sharing)
- [Presentation](https://canva.link/naqaftayvolv7hw)

## My Contributions
- UI / app flow planning
- Android screen and interaction design
- Theme and visual consistency
- API integration discussion
- Notification and future mail workflow planning
- Git-based team collaboration
  
## Course Connection

| Course | Connection |
|---|---|
| Mobile App Development | Kotlin Android app, activity flow, notification permission, FCM |
| Web Fundamentals | REST API, HTTP request / response, JSON data exchange |
| Database Systems | Store future mail records in MySQL / MariaDB |
| Software Project Practice	| Team collaboration, Git workflow, frontend-backend integration |

## What I Learned

Through this project, I learned how a mobile app can communicate with a backend API and how data can be stored and processed beyond the local device.
I also practiced team-based development, branch management, and the importance of separating frontend, backend, and database responsibilities.

## Notes

This project is a course / team project prototype.
It is intended to demonstrate mobile app development and system integration concepts, not a commercial production service.
