# VC Eats – Cafeteria Ordering App

VC Eats is a mobile application designed for students and staff of Varsity College to easily browse and order meals from the campus cafeteria. The app provides a smooth ordering experience, supports cart management, and offers a streamlined checkout process.

---

## Features
- Browse cafeteria menu items by category (Breakfast, Lunch, Beverages, Snacks)
- Add items to cart and update quantities
- View cart with subtotal, service fee, and total
- Checkout and proceed to payment
- Bottom navigation for Menu, Orders, Alerts, and Settings
- Real-time cart badge updates
- Responsive design and error handling for empty menus

---

## Purpose
The purpose of VC Eats is to digitize the cafeteria ordering process for Varsity College students and staff, saving time and improving convenience. Users can browse menus, add items to a cart, and complete orders from their mobile devices.  

---

## Architecture
The app uses a **Model-View-ViewModel (MVVM)** architecture to separate concerns and improve maintainability:

```
CustomerMenuActivity (View)
│
▼
MenuItemAdapter & CartAdapter
│
▼
CartManager (Singleton)
│
▼
MenuItem & CartItem (Models)
│
▼
Retrofit API Service (Network)
```

Key points:
- **MVVM pattern** ensures clear separation of UI and business logic.
- **CartManager** handles cart operations globally.
- **Retrofit** manages API requests securely with bearer tokens.
- **Coroutines** used for asynchronous network operations.

---

## Tech Stack
- **Kotlin** – Android app development language  
- **Android Studio** – IDE for development  
- **Retrofit** – REST API client  
- **Coroutines** – Asynchronous operations  
- **Material Design Components** – UI/UX  
- **GitHub** – Version control and project management  
- **GitHub Actions** – CI/CD workflows  

---

## Installation

1. Clone the repository:  
```bash
git clone https://github.com/ST10356476/VC_Eats.git
cd vc-eats
```
2. Open the project in **Android Studio**.
3. Sync Gradle and build the project.
4. Run the app on an emulator or physical device.

---

## Release Notes

### Version 1.0.0 – Initial Release
- Implemented full menu browsing
- Added cart management with subtotal, service fee, and total
- Added bottom navigation for Menu, Orders, Alerts, and Settings
- Added checkout functionality

### Version 1.1.0 – UI & UX Improvements
- Updated menu item cards with images
- Improved cart dialog UI
- Added empty cart and empty menu states
- Added material design components for a modern look

### Version 1.2.0 – Network & Security
- Integrated Retrofit for API calls
- Added bearer token authentication
- Added error handling for network failures
- Implemented coroutines for async network operations

---

## License
This project is licensed under the MIT License.
