const jwt = require('jsonwebtoken');
const mongoose = require('mongoose');
const { User } = require('../models');
const inMemoryStore = require('../models/inMemoryStore');

/**
 * Authentication Middleware (JWT)
 * Protects user-specific routes from unauthorized access by validating the JWT token
 * present in the 'Authorization' header (Bearer scheme) or 'x-auth-token' header.
 *
 * @param {import('express').Request} req
 * @param {import('express').Response} res
 * @param {import('express').NextFunction} next
 */
const authMiddleware = async (req, res, next) => {
  let token;

  const authHeader = req.headers.authorization;
  const xAuthToken = req.headers['x-auth-token'];

  // Check for 'Bearer <token>' in Authorization header
  if (authHeader && authHeader.startsWith('Bearer ')) {
    token = authHeader.split(' ')[1].trim();
  } else if (authHeader) {
    // Fallback for raw token passed directly in Authorization header
    token = authHeader.trim();
  } else if (xAuthToken) {
    // Fallback for custom x-auth-token header
    token = xAuthToken.trim();
  }

  // 1. Verify token presence
  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'Access denied: No authorization token provided. Please include a valid Bearer token in the Authorization header.'
    });
  }

  try {
    // 2. Verify token signature and expiration
    const secret = process.env.JWT_SECRET || 'quantiml_secure_jwt_secret_dev_key_2026_change_in_production';
    const decoded = jwt.verify(token, secret);

    if (!decoded || !decoded.id) {
      return res.status(401).json({
        success: false,
        message: 'Invalid authorization token: Malformed payload.'
      });
    }

    // 3. Find user in database associated with token (exclude password hash)
    let user = null;
    const isDbConnected = mongoose.connection.readyState === 1;

    if (isDbConnected) {
      user = await User.findById(decoded.id).select('-password');
    } else {
      user = inMemoryStore.findUserById(decoded.id);
    }

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Unauthorized: The user account belonging to this token no longer exists.'
      });
    }

    // 4. Attach verified user to request object for downstream controllers
    req.user = user;
    req.userId = (user._id || user.id).toString();

    next();
  } catch (error) {
    console.error('[AuthMiddleware] Token verification failed:', error.message);

    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: 'Authorization token has expired. Please log in again to renew your session.'
      });
    }

    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        message: 'Invalid authorization token signature or structure.'
      });
    }

    return res.status(401).json({
      success: false,
      message: 'Access denied: Token authentication failed.'
    });
  }
};

// Aliases for seamless imports:
// const authMiddleware = require('./middleware/authMiddleware');
// const { protect, authMiddleware } = require('./middleware/authMiddleware');
authMiddleware.protect = authMiddleware;
authMiddleware.authMiddleware = authMiddleware;

module.exports = authMiddleware;
module.exports.protect = authMiddleware;
module.exports.authMiddleware = authMiddleware;
