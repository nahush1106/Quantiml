const jwt = require('jsonwebtoken');
const mongoose = require('mongoose');
const { User } = require('../models');
const inMemoryStore = require('../models/inMemoryStore');

/**
 * Generate JWT Token helper
 */
const generateToken = (id) => {
  return jwt.sign(
    { id: String(id) },
    process.env.JWT_SECRET || 'quantiml_secure_jwt_secret_dev_key_2026_change_in_production',
    { expiresIn: process.env.JWT_EXPIRES_IN || '30d' }
  );
};

const isDbConnected = () => mongoose.connection.readyState === 1;

/**
 * @desc    Register a new user
 * @route   POST /api/auth/register or POST /api/user/register
 * @access  Public
 */
const registerUser = async (req, res) => {
  try {
    const { fullName, email, phone, mobileNumber, password, passCode } = req.body;

    const effectivePhone = phone || mobileNumber;
    const effectivePassword = password || passCode;

    // 1. Validation
    if (!fullName || !email || !effectivePhone || !effectivePassword) {
      return res.status(400).json({
        success: false,
        message: 'Please provide all required fields: fullName, email, phone (or mobileNumber), password (or passCode)'
      });
    }

    if (effectivePassword.length < 4) {
      return res.status(400).json({
        success: false,
        message: 'Password or security PIN must be at least 4 characters'
      });
    }

    // 2. Check for existing user by email or phone
    const cleanEmail = email.toLowerCase().trim();
    const cleanPhone = effectivePhone.trim();

    // Basic format validation
    const emailRegex = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/;
    if (!emailRegex.test(cleanEmail)) {
      return res.status(400).json({
        success: false,
        message: 'Please provide a valid email address (e.g. name@example.com)'
      });
    }

    if (isDbConnected()) {
      const existingUser = await User.findOne({
        $or: [{ email: cleanEmail }, { phone: cleanPhone }]
      });

      if (existingUser) {
        const matchField = existingUser.email === cleanEmail ? 'Email' : 'Phone number';
        return res.status(400).json({
          success: false,
          message: `${matchField} is already registered. Please login instead.`
        });
      }

      // 3. Create user in MongoDB
      const user = await User.create({
        fullName: fullName.trim(),
        email: cleanEmail,
        phone: cleanPhone,
        password: effectivePassword.trim()
      });

      const token = generateToken(user._id);

      const userProfile = {
        id: user._id.toString(),
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        roundUpTarget: user.roundUpTarget,
        roundUpMultiplier: user.roundUpMultiplier,
        autoInvestThreshold: user.autoInvestThreshold,
        selectedAssetDestination: user.selectedAssetDestination || 'NIFTY_50_INDEX'
      };

      return res.status(201).json({
        success: true,
        message: 'Account registered successfully',
        token,
        user: userProfile
      });
    } else {
      // In-Memory store fallback
      const existingUser = inMemoryStore.findUserByIdentifier(cleanEmail) || inMemoryStore.findUserByIdentifier(cleanPhone);
      if (existingUser) {
        return res.status(400).json({
          success: false,
          message: 'An account with this email or phone is already registered. Please login.'
        });
      }

      const user = await inMemoryStore.createUser({
        fullName: fullName.trim(),
        email: cleanEmail,
        phone: cleanPhone,
        password: effectivePassword.trim()
      });

      const token = generateToken(user.id);
      const userProfile = {
        id: user.id,
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        roundUpTarget: user.roundUpTarget,
        roundUpMultiplier: user.roundUpMultiplier,
        autoInvestThreshold: user.autoInvestThreshold
      };

      return res.status(201).json({
        success: true,
        message: 'Account registered successfully (Local Secure Store)',
        token,
        user: userProfile
      });
    }
  } catch (error) {
    console.error('[UserController] Register error:', error.message);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error during user registration'
    });
  }
};

/**
 * @desc    Authenticate user & get token
 * @route   POST /api/auth/login or POST /api/user/login
 * @access  Public
 */
const loginUser = async (req, res) => {
  try {
    const { identifier, email, phone, mobileNumber, password, passCode } = req.body;

    const effectiveId = identifier || email || phone || mobileNumber;
    const effectivePass = password || passCode;

    if (!effectiveId || !effectivePass) {
      return res.status(400).json({
        success: false,
        message: 'Please provide email/phone and password/passCode'
      });
    }

    const cleanIdentifier = effectiveId.trim();

    if (isDbConnected()) {
      const user = await User.findOne({
        $or: [{ email: cleanIdentifier.toLowerCase() }, { phone: cleanIdentifier }]
      });

      if (!user) {
        return res.status(400).json({
          success: false,
          message: 'Invalid credentials: No account found with this email or mobile'
        });
      }

      const isMatch = await user.comparePassword(effectivePass.trim());
      if (!isMatch) {
        return res.status(400).json({
          success: false,
          message: 'Invalid credentials: Incorrect password or PIN'
        });
      }

      const token = generateToken(user._id);

      const userProfile = {
        id: user._id.toString(),
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        roundUpTarget: user.roundUpTarget,
        roundUpMultiplier: user.roundUpMultiplier,
        autoInvestThreshold: user.autoInvestThreshold,
        selectedAssetDestination: user.selectedAssetDestination || 'NIFTY_50_INDEX'
      };

      return res.status(200).json({
        success: true,
        message: 'Logged in successfully',
        token,
        user: userProfile
      });
    } else {
      // In-Memory store fallback
      const user = inMemoryStore.findUserByIdentifier(cleanIdentifier);
      if (!user) {
        return res.status(400).json({
          success: false,
          message: 'Invalid credentials: No account found with this email or mobile'
        });
      }

      const isMatch = await user.matchPassword(effectivePass.trim());
      if (!isMatch) {
        return res.status(400).json({
          success: false,
          message: 'Invalid credentials: Incorrect password or PIN'
        });
      }

      const token = generateToken(user.id);
      const userProfile = {
        id: user.id,
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        roundUpTarget: user.roundUpTarget,
        roundUpMultiplier: user.roundUpMultiplier,
        autoInvestThreshold: user.autoInvestThreshold,
        selectedAssetDestination: user.selectedAssetDestination || 'NIFTY_50_INDEX'
      };

      return res.status(200).json({
        success: true,
        message: 'Logged in successfully',
        token,
        user: userProfile
      });
    }
  } catch (error) {
    console.error('[UserController] Login error:', error.message);
    return res.status(500).json({
      success: false,
      message: error.message || 'Server error during login'
    });
  }
};

/**
 * @desc    Get current logged in user profile
 * @route   GET /api/user/profile or /api/auth/me
 * @access  Private
 */
const getUserProfile = async (req, res) => {
  try {
    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    let userProfile = null;
    if (isDbConnected()) {
      const user = await User.findById(userId);
      if (user) {
        userProfile = {
          id: user._id.toString(),
          fullName: user.fullName,
          email: user.email,
          phone: user.phone,
          roundUpTarget: user.roundUpTarget,
          roundUpMultiplier: user.roundUpMultiplier,
          autoInvestThreshold: user.autoInvestThreshold,
          selectedAssetDestination: user.selectedAssetDestination || 'NIFTY_50_INDEX',
          createdAt: user.createdAt
        };
      }
    } else {
      const user = inMemoryStore.findUserById(userId);
      if (user) {
        userProfile = {
          id: user.id,
          fullName: user.fullName,
          email: user.email,
          phone: user.phone,
          roundUpTarget: user.roundUpTarget,
          roundUpMultiplier: user.roundUpMultiplier,
          autoInvestThreshold: user.autoInvestThreshold,
          selectedAssetDestination: user.selectedAssetDestination || 'NIFTY_50_INDEX',
          createdAt: user.createdAt
        };
      }
    }

    if (!userProfile) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    return res.status(200).json({
      ...userProfile,
      success: true,
      user: userProfile
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Update user round-up and threshold preferences
 * @route   PUT /api/user/settings
 * @access  Private
 */
const updateUserSettings = async (req, res) => {
  try {
    const { roundUpTarget, roundUpMultiplier, autoInvestThreshold, selectedAssetDestination } = req.body;
    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    let userProfile = null;

    if (isDbConnected()) {
      const updates = {};
      if (roundUpTarget !== undefined) updates.roundUpTarget = roundUpTarget;
      if (roundUpMultiplier !== undefined) updates.roundUpMultiplier = roundUpMultiplier;
      if (autoInvestThreshold !== undefined) updates.autoInvestThreshold = autoInvestThreshold;
      if (selectedAssetDestination !== undefined) updates.selectedAssetDestination = selectedAssetDestination;

      const updatedUser = await User.findByIdAndUpdate(
        userId,
        { $set: updates },
        { new: true, runValidators: true }
      );

      if (updatedUser) {
        userProfile = {
          id: updatedUser._id.toString(),
          fullName: updatedUser.fullName,
          email: updatedUser.email,
          phone: updatedUser.phone,
          roundUpTarget: updatedUser.roundUpTarget,
          roundUpMultiplier: updatedUser.roundUpMultiplier,
          autoInvestThreshold: updatedUser.autoInvestThreshold,
          selectedAssetDestination: updatedUser.selectedAssetDestination || 'NIFTY_50_INDEX'
        };
      }
    } else {
      const updatedUser = inMemoryStore.updateUserSettings(userId, {
        roundUpTarget,
        roundUpMultiplier,
        autoInvestThreshold,
        selectedAssetDestination
      });

      if (updatedUser) {
        userProfile = {
          id: updatedUser.id,
          fullName: updatedUser.fullName,
          email: updatedUser.email,
          phone: updatedUser.phone,
          roundUpTarget: updatedUser.roundUpTarget,
          roundUpMultiplier: updatedUser.roundUpMultiplier,
          autoInvestThreshold: updatedUser.autoInvestThreshold,
          selectedAssetDestination: updatedUser.selectedAssetDestination || 'NIFTY_50_INDEX'
        };
      }
    }

    if (!userProfile) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    return res.status(200).json({
      ...userProfile,
      success: true,
      message: 'Settings updated successfully',
      user: userProfile
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

module.exports = {
  registerUser,
  loginUser,
  getUserProfile,
  updateUserSettings
};
