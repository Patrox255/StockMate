# StockMate: Intelligent Stock Assistant

StockMate is a modern Android application built to solve the everyday problem of managing stock of food, therefore allowing you to rely on the app when going to the store. It tracks your daily stock consumption, including prepared dishes, offering an intelligent assistant that predicts and streamlines your kitchen management.

## Key Features

* **Intelligent Auto-Deductions:** The app learns from your habits. Based on the predictions made by ML models (Linear regression or Random Forest) it provides suggestions for daily deductions. This means you don't have to manually modify the stock of each product every day and instead can just tweak the suggested values if needed.
* **Dish & Recipe Management:** Link products to specific dishes using customizable portions and multipliers (e.g., "1 glass" of milk, "2 slices" of bread).
* **Smart Inventory & Analytics:** Keep track of your current stock levels vs. target goals with visual history charts. These charts also utilize the mentioned predictions, generating prediction lines connected to the actual usage indicators.
* **ML-powered Stats:** On the main inventory screen tracked products also display the predicted number of days until they run out of stock, allowing for easier decision-making when grocery shopping.
* **Product & Dish Images Management:** There are various ways of applying a specific image to a product or a dish. You can either import a photo from your gallery or rely on external APIs to look up a suitable photo (currently supported are **Pixabay** and **OpenFoodFacts**).

## Tech Stack & Architecture

**UI & Design**
* **Jetpack Compose:** Fully declarative UI toolkit.
* **Material Design 3:** Theming, custom typography via **Google Fonts**, and custom component wrappers.
* **Vico Charts:** Animated and interactive data visualization for stock history.
* **Coil:** Fast, asynchronous image loading.
* **Calvin Reorderable:** Smooth drag-and-drop interactions for lists.

**Core & Architecture**
* **Dagger Hilt:** Dependency Injection.
* **Navigation Compose:** Screen routing.
* **Coroutines & Flows:** Reactive, asynchronous programming and state hoisting.

**Data & Local Storage**
* **Room:** SQLite object-mapping for app's entities (Products, Dishes, Ingredients, Logs) and transactional queries.
* **Preferences DataStore:** Modern, asynchronous key-value storage for app settings.


**Networking**
* **Retrofit & OkHttp:** Type-safe REST API client with HTTP logging interceptors.

### App Preview
<details>
  <summary>Preview: Main Inventory with filtering, sorting and qucik access to stock management (Click to expand)</summary>
  <br>
  <div align="center">
    <img width="419" height="825" alt="main_inventory" src="https://github.com/user-attachments/assets/4e048596-fc1a-4751-97b9-ec06f901e0db" />
  </div>
</details>
<details>
  <summary>Preview: Image Management</summary>
  <br>
  <div align="center">
    <img width="419" height="845" alt="image_manager" src="https://github.com/user-attachments/assets/f21e5984-884f-46e5-bc05-6e9add7ae070" />
  </div>
</details>
<details>
  <summary>Preview: Dish example</summary>
  <br>
  <div align="center">
    <img width="419" height="845" alt="dish_example" src="https://github.com/user-attachments/assets/07a68dfb-18db-4bd9-9f0c-2085a743a903" />
  </div>
</details>
<details>
  <summary>Preview: Stock Log Chart</summary>
  <br>
  <div align="center">
    <img width="419" height="845" alt="stock_chart" src="https://github.com/user-attachments/assets/0906ad52-893d-4d2c-9d9d-e78dc3c9390d" />
  </div>
</details>
<details>
  <summary>Preview: Auto Stock Deductions</summary>
  <br>
  <div align="center">
    <img width="419" height="845" alt="auto_deductions" src="https://github.com/user-attachments/assets/6e2f3471-12b9-426c-888f-51c43f2c0d11" />
  </div>
</details>

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites
* **Android Studio**
* **Minimum SDK:** API 24 (Android 7.0).
* **Pixabay API Key:** Required for the image search feature connected to Pixabay service. Add `PIXABAY_API_KEY="your_key_here"` to the `secrets.properties` file located in the main directory (utilize `secrets.defaults.properties` file to avoid errors).

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Patrox255/StockMate.git](https://github.com/Patrox255/StockMate.git)
2. **Open the project:**
Launch Android Studio and select `File -> Open`, then navigate to the cloned StockMate directory.

3. **Sync and Build:** 
Let Gradle sync the project dependencies.

4. **Run the app:** Select your emulator or physical device and click the Run button (Shift + F10).

### Testing & Mock Data (Developer Options)

To fully experience the app's capabilities (especially the ML predictions, auto-deductions, and Vico charts) without manually adding dozens of products, you can use the built-in database seeder:

1. Launch the app and navigate to the **Settings** screen.
2. Scroll down to the **Developer Options** section.
3. Click **"Seed Database (Mock Data)"**.
4. The app will instantly populate with realistic products (Milk, Eggs, Bread, etc.), custom dishes (e.g., Pancakes, Scrambled Eggs), and generate 7 days of consumption history for few products logs to power the analytical charts.

*(Note: You can easily reset the app state anytime using the* ***"Clear Database"*** *button in the same menu).*