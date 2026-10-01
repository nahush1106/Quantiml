const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const mongoose = require('mongoose');

// 1. Load environment variables from .env file
dotenv.config();

// 2. Import database connection configuration
const connectDB = require('./config/db');

// 3. Import aggregated API routes
const apiRoutes = require('./routes');

// 4. Initialize Express application
const app = express();

// 5. Connect to MongoDB (Local MongoDB with fallback / Atlas)
connectDB();

// 6. Security & Parsing Middleware
app.use(
  cors({
    origin: '*', // Allows local Android emulator (10.0.2.2), physical devices (LAN), and web clients
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Response-Format']
  })
);

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// 7. Request Logger Middleware (Useful during mobile and API testing)
app.use((req, res, next) => {
  const startTime = Date.now();
  const timestamp = new Date().toISOString();

  res.on('finish', () => {
    const duration = Date.now() - startTime;
    const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';
    console.log(`[${timestamp}] ${req.method} ${req.originalUrl} ${res.statusCode} (${duration}ms) - IP: ${clientIp}`);
  });

  next();
});

// 8. Root Health & Information Route
app.get('/', (req, res) => {
  const isDbConnected = mongoose.connection.readyState === 1;

  res.status(200).json({
    status: 'ONLINE',
    app: 'Quantiml Fintech Backend API',
    description: 'AI-driven expense tracking and automated micro-investing round-up engine',
    version: '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    database: {
      type: 'MongoDB',
      status: isDbConnected ? 'CONNECTED' : 'DISCONNECTED',
      name: mongoose.connection.name || 'quantiml_db',
      host: mongoose.connection.host || '127.0.0.1'
    },
    documentation: '/api',
    health: '/health',
    timestamp: new Date().toISOString()
  });
});

// 9. Dedicated System Health Check Endpoint
app.get('/health', (req, res) => {
  const dbStateMap = {
    0: 'DISCONNECTED',
    1: 'CONNECTED',
    2: 'CONNECTING',
    3: 'DISCONNECTING'
  };

  const dbStatus = dbStateMap[mongoose.connection.readyState] || 'UNKNOWN';
  const isHealthy = mongoose.connection.readyState === 1;

  return res.status(isHealthy ? 200 : 503).json({
    status: isHealthy ? 'HEALTHY' : 'DEGRADED',
    uptime: `${Math.floor(process.uptime())}s`,
    database: {
      status: dbStatus,
      host: mongoose.connection.host || 'unknown',
      name: mongoose.connection.name || 'unknown'
    },
    memoryUsage: process.memoryUsage(),
    timestamp: new Date().toISOString()
  });
});

// 10. Mount All API Routes under /api
app.use('/api', apiRoutes);

// 11. 404 Route Not Found Handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Endpoint '${req.originalUrl}' with method [${req.method}] was not found on this server`,
    suggestion: 'Visit GET /api for the full list of available API endpoints'
  });
});

// 12. Centralized Global Error Handler Middleware
app.use((err, req, res, next) => {
  console.error('[Global Error Handler]:', err.stack || err.message);

  let statusCode = err.status || err.statusCode || 500;
  let message = err.message || 'Internal Server Error';

  // Mongoose Bad ObjectId (CastError)
  if (err.name === 'CastError') {
    statusCode = 400;
    message = `Resource not found with invalid identifier: ${err.value}`;
  }

  // Mongoose Duplicate Key Error (code 11000)
  if (err.code === 11000) {
    statusCode = 400;
    const field = Object.keys(err.keyValue || {})[0] || 'Field';
    message = `${field} already exists. Please choose a different value.`;
  }

  // Mongoose Validation Error
  if (err.name === 'ValidationError') {
    statusCode = 400;
    message = Object.values(err.errors)
      .map((val) => val.message)
      .join(', ');
  }

  // JWT Errors
  if (err.name === 'JsonWebTokenError') {
    statusCode = 401;
    message = 'Invalid authentication token provided';
  }

  if (err.name === 'TokenExpiredError') {
    statusCode = 401;
    message = 'Authentication token has expired. Please log in again';
  }

  res.status(statusCode).json({
    success: false,
    message,
    ...(process.env.NODE_ENV === 'development' ? { stack: err.stack } : {})
  });
});

// 13. Server Startup & Listening on Configured Port
// Use BACKEND_PORT or default to 5000 to avoid conflicting with container platform port 8080
const PORT = process.env.BACKEND_PORT || (process.env.PORT && process.env.PORT !== '8080' ? process.env.PORT : 5000);
const HOST = '0.0.0.0';

let server = null;
if (process.env.NODE_ENV !== 'test') {
  server = app.listen(PORT, HOST, () => {
    console.log(`\n======================================================`);
    console.log(` Quantiml Express Backend Server Initialized!`);
    console.log(`======================================================`);
    console.log(` Environment  : ${process.env.NODE_ENV || 'development'}`);
    console.log(` Port         : ${PORT}`);
    console.log(` Host URL     : http://localhost:${PORT}`);
    console.log(` Android Link : http://10.0.2.2:${PORT}`);
    console.log(` API Index    : http://localhost:${PORT}/api`);
    console.log(` Healthcheck  : http://localhost:${PORT}/health`);
    console.log(`======================================================\n`);
  });
}

// 14. Unhandled Rejection & Exception Handlers
if (process.env.NODE_ENV !== 'test') {
  process.on('unhandledRejection', (err) => {
    console.warn(`[Process Warning] Unhandled Promise Rejection: ${err?.message || err}`);
  });

  process.on('uncaughtException', (err) => {
    console.error(`[Process Error] Uncaught Exception: ${err?.message || err}`);
  });
}

// 15. Graceful Process Termination (SIGINT / SIGTERM)
const handleGracefulShutdown = (signal) => {
  console.log(`\n[Process] Received ${signal}. Closing HTTP server gracefully...`);
  if (server) {
    server.close(async () => {
      console.log('[Process] HTTP server closed.');
      try {
        await mongoose.connection.close();
        console.log('[Process] MongoDB connection closed.');
      } catch (dbErr) {
        console.error('[Process] Error closing MongoDB connection:', dbErr);
      }
      process.exit(0);
    });
  } else {
    process.exit(0);
  }
};

if (process.env.NODE_ENV !== 'test') {
  process.on('SIGINT', () => handleGracefulShutdown('SIGINT'));
  process.on('SIGTERM', () => handleGracefulShutdown('SIGTERM'));
}

module.exports = { app, server };
