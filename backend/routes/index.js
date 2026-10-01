const express = require('express');
const router = express.Router();

const authRoutes = require('./auth');
const transactionRoutes = require('./transactions');
const userRoutes = require('./user');

// Mount sub-routes
router.use('/auth', authRoutes);
router.use('/transactions', transactionRoutes);
router.use('/user', userRoutes);

// Base API route check & interactive endpoint documentation
router.get('/', (req, res) => {
  res.status(200).json({
    status: 'ONLINE',
    version: '1.0.0',
    service: 'Quantiml Fintech Backend API',
    endpoints: {
      userRegistration: [
        'POST /api/auth/register (Public - full user registration)',
        'POST /api/user/register (Public - alias user registration)',
        'POST /api/auth/login (Public - authentication returning JWT)',
        'POST /api/user/login (Public - alias authentication)'
      ],
      userProfile: [
        'GET  /api/auth/me (Protected - current user profile)',
        'GET  /api/user/profile (Protected - profile & KYC)',
        'PUT  /api/user/settings (Protected - update roundUpTarget, multiplier, threshold)'
      ],
      transactionLogging: [
        'POST /api/transactions (Protected - log transaction & calculate round-up)',
        'POST /api/transactions/log (Protected - explicit transaction logging)',
        'POST /api/transactions/sms (Protected - bank SMS auto-log)',
        'POST /api/transactions/batch (Protected - batch log multiple transactions)',
        'GET  /api/transactions (Protected - get transactions with query filters)',
        'GET  /api/transactions/:id (Protected - get single transaction details)',
        'GET  /api/transactions/stats (Protected - wallet balance & threshold metrics)',
        'POST /api/transactions/deploy (Protected - deploy spare change to portfolio)'
      ]
    }
  });
});

module.exports = router;
