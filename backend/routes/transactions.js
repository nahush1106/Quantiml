const express = require('express');
const router = express.Router();
const {
  getTransactions,
  createTransaction,
  logBatchTransactions,
  getTransactionById,
  deployThresholdTransactions,
  getWalletStats
} = require('../controllers/transactionController');
const { protect } = require('../middleware/authMiddleware');

// All transaction routes require authentication
router.use(protect);

/**
 * @route   POST /api/transactions
 * @desc    Log a new expense transaction & automatically compute spare change round-up
 * @access  Private
 */
router.post('/', createTransaction);

/**
 * @route   POST /api/transactions/log
 * @desc    Explicit transaction logging route (supports manual entries & webhook listeners)
 * @access  Private
 */
router.post('/log', createTransaction);

/**
 * @route   POST /api/transactions/sms
 * @desc    Dedicated route for logging bank SMS notifications (auto-parsed debit SMS)
 * @access  Private
 */
router.post('/sms', createTransaction);

/**
 * @route   POST /api/transactions/batch
 * @desc    Batch log multiple transactions (ideal for SMS inbox sync or offline mobile sync)
 * @access  Private
 */
router.post('/batch', logBatchTransactions);

/**
 * @route   GET /api/transactions
 * @desc    Get paginated transaction history (filters: ?status=ROUNDED_UP|INVESTED, ?category=ESSENTIAL|DISCRETIONARY, ?format=array)
 * @access  Private
 */
router.get('/', getTransactions);

/**
 * @route   GET /api/transactions/stats
 * @desc    Get live wallet metrics, accumulated round-ups, total invested, and threshold progress
 * @access  Private
 */
router.get('/stats', getWalletStats);

/**
 * @route   POST /api/transactions/deploy
 * @desc    Trigger instant deployment of accumulated spare change into investment portfolio
 * @access  Private
 */
router.post('/deploy', deployThresholdTransactions);

/**
 * @route   GET /api/transactions/:id
 * @desc    Get transaction details by ID
 * @access  Private
 */
router.get('/:id', getTransactionById);

module.exports = router;
