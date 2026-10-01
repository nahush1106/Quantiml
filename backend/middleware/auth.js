// Re-export from authMiddleware.js for unified middleware access
const authMiddleware = require('./authMiddleware');

module.exports = authMiddleware;
module.exports.protect = authMiddleware.protect;
module.exports.authMiddleware = authMiddleware;
