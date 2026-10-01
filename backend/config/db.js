const mongoose = require('mongoose');

/**
 * MongoDB Database Connection Manager
 * Connects to MongoDB using Mongoose with connection pooling, lifecycle event listeners,
 * and graceful shutdown handling.
 */
const connectDB = async () => {
  const mongoUri = process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/quantiml_db';
  const isCloudUri = mongoUri.includes('@') || mongoUri.startsWith('mongodb+srv');

  console.log(`[Database] Initiating connection to MongoDB (${isCloudUri ? 'MongoDB Atlas' : 'Local MongoDB'})...`);

  // Connection options for resilience
  const options = {
    serverSelectionTimeoutMS: process.env.NODE_ENV === 'test' ? 500 : 5000, // Quick fallback in test environment
    socketTimeoutMS: 45000,         // Close sockets after 45 seconds of inactivity
  };

  try {
    const conn = await mongoose.connect(mongoUri, options);

    console.log('----------------------------------------------------');
    console.log(' MongoDB Connected Successfully!');
    console.log(` Database Host : ${conn.connection.host}`);
    console.log(` Database Name : ${conn.connection.name}`);
    console.log(` Port          : ${conn.connection.port || 'Default (Atlas SRV)'}`);
    console.log(` Connection URI: ${isCloudUri ? mongoUri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@') : mongoUri}`);
    console.log('----------------------------------------------------');

    return conn;
  } catch (error) {
    console.error('====================================================');
    console.error(' MongoDB Connection Error:');
    console.error(` Error Message : ${error.message}`);
    console.error(` Target URI    : ${isCloudUri ? mongoUri.replace(/\/\/[^:]+:[^@]+@/, '//***:***@') : mongoUri}`);

    // Actionable debugging guidance for developers
    if (error.message.includes('ECONNREFUSED') || error.message.includes('127.0.0.1:27017')) {
      console.error('\n Troubleshoot Local MongoDB:');
      console.error(' 1. Check if the local MongoDB daemon is running:');
      console.error('    - Linux:   sudo systemctl status mongod (or sudo systemctl start mongod)');
      console.error('    - macOS:   brew services start mongodb-community');
      console.error('    - Windows: net start MongoDB (or launch mongod.exe)');
      console.error(' 2. Or switch to MongoDB Atlas in .env:');
      console.error('    Set MONGO_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/quantiml_db\n');
    } else if (error.message.includes('bad auth') || error.message.includes('Authentication failed')) {
      console.error('\n Troubleshoot Authentication:');
      console.error(' - Verify the database username and password in MONGO_URI (.env file).');
      console.error(' - Ensure special characters in the password are URL-encoded if applicable.\n');
    }
    console.error('====================================================');
    try {
      await mongoose.disconnect();
    } catch (_) {}
    console.warn('[Database] Running in standalone mode with resilient in-memory database store.');
    console.warn('[Database] To connect real MongoDB, configure MONGO_URI in backend/.env');
    return null;
  }
};

// Mongoose Connection Lifecycle Event Listeners
mongoose.connection.on('connected', () => {
  console.log('[Mongoose Event] Connected to MongoDB.');
});

mongoose.connection.on('error', (err) => {
  console.error('[Mongoose Event] Connection error occurred:', err.message);
});

mongoose.connection.on('disconnected', () => {
  console.log('[Mongoose Event] Disconnected from MongoDB.');
});

// Handle graceful shutdown on process termination
if (process.env.NODE_ENV !== 'test') {
  process.on('SIGINT', async () => {
    try {
      await mongoose.connection.close();
      console.log('[Mongoose] Database connection closed due to app termination (SIGINT).');
      process.exit(0);
    } catch (err) {
      console.error('[Mongoose] Error during graceful close:', err);
      process.exit(1);
    }
  });

  process.on('SIGTERM', async () => {
    try {
      await mongoose.connection.close();
      console.log('[Mongoose] Database connection closed due to app termination (SIGTERM).');
      process.exit(0);
    } catch (err) {
      console.error('[Mongoose] Error during graceful close:', err);
      process.exit(1);
    }
  });
}

module.exports = connectDB;
