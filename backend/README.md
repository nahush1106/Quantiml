# Quantiml Node.js & MongoDB Backend

This is the backend REST API for the **Quantiml** micro-investing and automated bank SMS expense tracker.

---

## 1. Project Structure

```text
backend/
├── config/
│   └── db.js            # MongoDB connection configuration (handles local & Atlas)
├── routes/
│   ├── auth.js          # User registration & login routes
│   ├── transactions.js  # Transactions & auto-invest deploy routes
│   └── index.js         # Central API route combiner
├── .env                 # Environment variables (configured for local MongoDB)
├── .env.example         # Template for environment variables
├── package.json         # Project metadata and dependencies
├── server.js            # Main Express entry point
└── README.md            # Setup and transition guide
```

---

## 2. Quick Setup (Local MongoDB)

### Step 1: Make sure MongoDB is running locally
- **macOS (Homebrew)**: `brew services start mongodb-community`
- **Linux**: `sudo systemctl start mongod`
- **Windows**: Start MongoDB service from Services or run `mongod.exe`

### Step 2: Install Dependencies
```bash
cd backend
npm install
```

### Step 3: Start the Server
* **For Development (auto-reload on code change)**:
  ```bash
  npm run dev
  ```
* **For Normal Production**:
  ```bash
  npm start
  ```

Once started, test the server in your browser or Postman at:
```text
http://localhost:5000/
http://localhost:5000/api/
```

---

## 3. How to Switch to MongoDB Atlas Later

When you are ready to move from local MongoDB to MongoDB Atlas in the cloud:

1. Create a free cluster on [MongoDB Atlas](https://www.mongodb.com/atlas).
2. Create a Database User with a username and password.
3. In **Network Access**, whitelist your IP address (or `0.0.0.0/0` for cloud deployment).
4. Click **Connect** -> **Connect your application** (Drivers: Node.js).
5. Open `backend/.env` and replace `MONGO_URI`:
   ```env
   # Comment out local:
   # MONGO_URI=mongodb://127.0.0.1:27017/quantiml_db

   # Paste your Atlas connection string:
   MONGO_URI=mongodb+srv://<username>:<password>@cluster0.abcde.mongodb.net/quantiml_db?retryWrites=true&w=majority
   ```
6. Restart your server (`npm start`). The `config/db.js` file will automatically detect Atlas and connect!

---

## 4. Connecting Android to this Backend

* **Android Emulator**: In the Android app, use `http://10.0.2.2:5000/` (pre-configured in `NetworkClient.kt`). `10.0.2.2` is Android's special alias for your computer's `localhost`.
* **Physical Device**: Connect phone and computer to the same Wi-Fi network and set `NetworkClient.baseUrl` to your computer's local IP (e.g., `http://192.168.1.15:5000/`).
* **Cloud (After Deploying to Render/Railway)**: Set `NetworkClient.baseUrl = "https://your-api.onrender.com/"`.
