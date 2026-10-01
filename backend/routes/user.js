const express = require('express');
const router = express.Router();
const {
  registerUser,
  loginUser,
  getUserProfile,
  updateUserSettings
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

/**
 * Public User Routes
 */

/**
 * @route   POST /api/user/register
 * @desc    Direct registration endpoint for user account creation
 * @access  Public
 */
router.post('/register', registerUser);

/**
 * @route   POST /api/user/login
 * @desc    Direct login endpoint for user authentication
 * @access  Public
 */
router.post('/login', loginUser);

/**
 * Protected User Routes (Require valid JWT Bearer token)
 */

/**
 * @route   GET /api/user/profile
 * @desc    Retrieve logged-in user profile, KYC contact info, and investment targets
 * @access  Private
 */
router.get('/profile', protect, getUserProfile);

/**
 * @route   GET /api/user/me
 * @desc    Alias to get current user details
 * @access  Private
 */
router.get('/me', protect, getUserProfile);

/**
 * @route   PUT /api/user/settings
 * @desc    Update round-up target (₹10, ₹20, ₹50, ₹100), multiplier (1x-10x), or autoInvestThreshold
 * @access  Private
 */
router.put('/settings', protect, updateUserSettings);

module.exports = router;
