const mongoose = require('mongoose');
const { Transaction, User } = require('../models');
const inMemoryStore = require('../models/inMemoryStore');

const isDbConnected = () => mongoose.connection.readyState === 1;

/**
 * Helper: Calculate round-up amount given amount, roundToMultiple, and multiplier
 * (Matches the Android QuantAiNlpClassifier logic)
 */
const calculateRoundUp = (amount, target = 10, multiplier = 1.0) => {
  if (amount <= 0) return 0.0;
  const remainder = amount % target;
  const baseRoundUp = remainder === 0 ? target : target - remainder;
  return Number((baseRoundUp * multiplier).toFixed(2));
};

/**
 * @desc    Get user's transactions (with optional filters)
 * @route   GET /api/transactions
 * @access  Private
 */
const getTransactions = async (req, res) => {
  try {
    const { status, category, limit = 50, page = 1, format } = req.query;
    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    let transactions = [];
    let total = 0;

    if (isDbConnected()) {
      const query = { userId };
      if (status) query.status = status;
      if (category) query.category = category;

      const skip = (parseInt(page) - 1) * parseInt(limit);

      transactions = await Transaction.find(query)
        .sort({ timestamp: -1 })
        .skip(skip)
        .limit(parseInt(limit));

      total = await Transaction.countDocuments(query);
    } else {
      const result = inMemoryStore.getTransactions(userId, {
        status,
        category,
        limit,
        page
      });
      transactions = result.data;
      total = result.total;
    }

    // If client requested flat array format (e.g. Retrofit List<TransactionResponseDto>)
    if (format === 'array' || req.headers['x-response-format'] === 'array') {
      return res.status(200).json(transactions);
    }

    return res.status(200).json({
      success: true,
      count: transactions.length,
      total,
      page: parseInt(page),
      data: transactions
    });
  } catch (error) {
    console.error('[TransactionController] getTransactions error:', error.message);
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Log a new transaction & compute round-up micro-investment
 * @route   POST /api/transactions or POST /api/transactions/log
 * @access  Private
 */
const createTransaction = async (req, res) => {
  try {
    const {
      merchant,
      amount,
      roundUpAmount: customRoundUp,
      totalSpent: customTotalSpent,
      category,
      status = 'ROUNDED_UP',
      source = 'MANUAL',
      rawSmsBody = null,
      timestamp
    } = req.body;

    if (!merchant || amount === undefined || amount === null) {
      return res.status(400).json({
        success: false,
        message: 'Please provide merchant name and amount'
      });
    }

    const numAmount = parseFloat(amount);
    if (isNaN(numAmount) || numAmount <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Amount must be a positive number'
      });
    }

    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    let roundToTarget = 10;
    let multiplier = 1.0;
    let threshold = 500.0;

    if (isDbConnected()) {
      const user = await User.findById(userId);
      roundToTarget = user?.roundUpTarget || 10;
      multiplier = user?.roundUpMultiplier || 1.0;
      threshold = user?.autoInvestThreshold || 500.0;
    } else {
      const user = inMemoryStore.findUserById(userId);
      roundToTarget = user?.roundUpTarget || 10;
      multiplier = user?.roundUpMultiplier || 1.0;
      threshold = user?.autoInvestThreshold || 500.0;
    }

    // Use provided round-up or automatically calculate based on user preferences
    const computedRoundUp =
      customRoundUp !== undefined && customRoundUp !== null
        ? parseFloat(customRoundUp)
        : calculateRoundUp(numAmount, roundToTarget, multiplier);

    const calculatedTotalSpent =
      customTotalSpent !== undefined && customTotalSpent !== null
        ? parseFloat(customTotalSpent)
        : Number((numAmount + computedRoundUp).toFixed(2));

    const txCategory = category || 'DISCRETIONARY';

    let txObj = null;
    let summary = null;

    if (isDbConnected()) {
      const transaction = await Transaction.create({
        userId,
        merchant: merchant.trim(),
        amount: numAmount,
        roundUpAmount: computedRoundUp,
        totalSpent: calculatedTotalSpent,
        category: txCategory,
        status,
        source: source || (rawSmsBody ? 'BANK_SMS' : 'MANUAL'),
        rawSmsBody,
        timestamp: timestamp ? new Date(timestamp) : new Date()
      });

      summary = await Transaction.getWalletSummary(userId);
      txObj = transaction.toObject();
    } else {
      const newTxn = inMemoryStore.createTransaction({
        userId,
        merchant: merchant.trim(),
        amount: numAmount,
        roundUpAmount: computedRoundUp,
        totalSpent: calculatedTotalSpent,
        category: txCategory,
        status,
        source: source || (rawSmsBody ? 'BANK_SMS' : 'MANUAL'),
        rawSmsBody,
        timestamp: timestamp || new Date().toISOString()
      });

      summary = inMemoryStore.getWalletStats(userId);
      txObj = { ...newTxn };
    }

    const isThresholdReached = summary.walletBalance >= threshold;

    return res.status(201).json({
      ...txObj,
      _id: txObj._id || txObj.id,
      id: txObj.id || txObj._id,
      success: true,
      message: 'Transaction logged and round-up calculated successfully',
      data: txObj,
      wallet: {
        walletBalance: summary.walletBalance,
        totalInvested: summary.totalInvested,
        investThreshold: threshold,
        isThresholdReached,
        progressPercent: Math.min(
          100,
          Number(((summary.walletBalance / threshold) * 100).toFixed(1))
        )
      }
    });
  } catch (error) {
    console.error('[TransactionController] createTransaction error:', error.message);
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Batch log multiple transactions
 * @route   POST /api/transactions/batch
 * @access  Private
 */
const logBatchTransactions = async (req, res) => {
  try {
    const { transactions } = req.body;

    if (!Array.isArray(transactions) || transactions.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Please provide an array of transactions under the "transactions" field'
      });
    }

    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    let roundToTarget = 10;
    let multiplier = 1.0;

    if (isDbConnected()) {
      const user = await User.findById(userId);
      roundToTarget = user?.roundUpTarget || 10;
      multiplier = user?.roundUpMultiplier || 1.0;

      const formattedDocs = transactions.map((tx) => {
        const numAmount = parseFloat(tx.amount);
        const computedRoundUp =
          tx.roundUpAmount !== undefined && tx.roundUpAmount !== null
            ? parseFloat(tx.roundUpAmount)
            : calculateRoundUp(numAmount, roundToTarget, multiplier);

        const totalSpent = tx.totalSpent
          ? parseFloat(tx.totalSpent)
          : Number((numAmount + computedRoundUp).toFixed(2));

        return {
          userId,
          merchant: tx.merchant ? tx.merchant.trim() : 'Unknown Merchant',
          amount: numAmount,
          roundUpAmount: computedRoundUp,
          totalSpent,
          category: tx.category || 'DISCRETIONARY',
          status: tx.status || 'ROUNDED_UP',
          source: tx.source || (tx.rawSmsBody ? 'BANK_SMS' : 'MANUAL'),
          rawSmsBody: tx.rawSmsBody || null,
          timestamp: tx.timestamp ? new Date(tx.timestamp) : new Date()
        };
      });

      const inserted = await Transaction.insertMany(formattedDocs);
      const summary = await Transaction.getWalletSummary(userId);

      return res.status(201).json({
        success: true,
        message: `Successfully logged ${inserted.length} transactions`,
        count: inserted.length,
        data: inserted,
        wallet: {
          walletBalance: summary.walletBalance,
          totalInvested: summary.totalInvested
        }
      });
    } else {
      const user = inMemoryStore.findUserById(userId);
      roundToTarget = user?.roundUpTarget || 10;
      multiplier = user?.roundUpMultiplier || 1.0;

      const created = [];
      for (const tx of transactions) {
        const numAmount = parseFloat(tx.amount);
        const computedRoundUp =
          tx.roundUpAmount !== undefined && tx.roundUpAmount !== null
            ? parseFloat(tx.roundUpAmount)
            : calculateRoundUp(numAmount, roundToTarget, multiplier);

        const totalSpent = tx.totalSpent
          ? parseFloat(tx.totalSpent)
          : Number((numAmount + computedRoundUp).toFixed(2));

        const item = inMemoryStore.createTransaction({
          userId,
          merchant: tx.merchant ? tx.merchant.trim() : 'Unknown Merchant',
          amount: numAmount,
          roundUpAmount: computedRoundUp,
          totalSpent,
          category: tx.category || 'DISCRETIONARY',
          status: tx.status || 'ROUNDED_UP',
          source: tx.source || (tx.rawSmsBody ? 'BANK_SMS' : 'MANUAL'),
          rawSmsBody: tx.rawSmsBody || null,
          timestamp: tx.timestamp || new Date().toISOString()
        });
        created.push(item);
      }

      const summary = inMemoryStore.getWalletStats(userId);

      return res.status(201).json({
        success: true,
        message: `Successfully logged ${created.length} transactions`,
        count: created.length,
        data: created,
        wallet: {
          walletBalance: summary.walletBalance,
          totalInvested: summary.totalInvested
        }
      });
    }
  } catch (error) {
    console.error('[TransactionController] logBatchTransactions error:', error.message);
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Get single transaction by ID
 * @route   GET /api/transactions/:id
 * @access  Private
 */
const getTransactionById = async (req, res) => {
  try {
    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    if (isDbConnected()) {
      const transaction = await Transaction.findOne({
        _id: req.params.id,
        userId
      });

      if (!transaction) {
        return res.status(404).json({
          success: false,
          message: 'Transaction not found'
        });
      }

      return res.status(200).json({
        success: true,
        data: transaction
      });
    } else {
      const transaction = inMemoryStore.transactions.find(
        t => (t._id === req.params.id || t.id === req.params.id) &&
             (String(t.userId) === String(userId) || String(t.userId) === 'user_arjun_001')
      );

      if (!transaction) {
        return res.status(404).json({
          success: false,
          message: 'Transaction not found'
        });
      }

      return res.status(200).json({
        success: true,
        data: transaction
      });
    }
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Deploy all accumulated 'ROUNDED_UP' spare change into 'INVESTED'
 * @route   POST /api/transactions/deploy
 * @access  Private
 */
const deployThresholdTransactions = async (req, res) => {
  try {
    const userId = req.userId || (req.user && (req.user.id || req.user._id));

    if (isDbConnected()) {
      const user = await User.findById(userId);
      const activeDestination = user?.selectedAssetDestination || 'NIFTY_50_INDEX';

      const pendingTransactions = await Transaction.find({
        userId,
        status: 'ROUNDED_UP'
      });

      if (pendingTransactions.length === 0) {
        return res.status(200).json({
          success: true,
          deployedCount: 0,
          totalDeployedAmount: 0.0,
          message: 'No pending spare change to deploy at this time'
        });
      }

      const totalDeployedAmount = Number(
        pendingTransactions
          .reduce((sum, item) => sum + item.roundUpAmount, 0)
          .toFixed(2)
      );

      const updateResult = await Transaction.updateMany(
        { userId, status: 'ROUNDED_UP' },
        { $set: { status: 'INVESTED', assetDestination: activeDestination } }
      );

      const summary = await Transaction.getWalletSummary(userId);

      return res.status(200).json({
        success: true,
        deployedCount: updateResult.modifiedCount,
        deployedAmount: totalDeployedAmount,
        totalDeployedAmount,
        assetDestination: activeDestination,
        walletBalance: summary.walletBalance,
        totalInvested: summary.totalInvested,
        portfolioBreakdown: {
          nifty50Amount: summary.nifty50Amount || 0,
          digitalGoldAmount: summary.digitalGoldAmount || 0,
          flexiCapAmount: summary.flexiCapAmount || 0
        },
        message: `Successfully deployed ₹${totalDeployedAmount} into ${activeDestination}!`
      });
    } else {
      const result = inMemoryStore.deployTransactions(userId);
      const summary = inMemoryStore.getWalletStats(userId);

      return res.status(200).json({
        success: true,
        deployedCount: result.deployedCount,
        deployedAmount: result.totalDeployedAmount,
        totalDeployedAmount: result.totalDeployedAmount,
        walletBalance: summary.walletBalance,
        totalInvested: summary.totalInvested,
        message: `Successfully deployed ₹${result.totalDeployedAmount} into your investment portfolio!`
      });
    }
  } catch (error) {
    console.error('[TransactionController] deploy error:', error.message);
    return res.status(500).json({ success: false, message: error.message });
  }
};

/**
 * @desc    Get live wallet metrics, progress to threshold, and portfolio stats
 * @route   GET /api/transactions/stats
 * @access  Private
 */
const getWalletStats = async (req, res) => {
  try {
    const userId = req.userId || (req.user && (req.user.id || req.user._id));
    let threshold = 500.0;

    if (isDbConnected()) {
      const user = await User.findById(userId);
      threshold = user?.autoInvestThreshold || 500.0;
      const summary = await Transaction.getWalletSummary(userId);

      const progressPercent = Math.min(
        100,
        Number(((summary.walletBalance / threshold) * 100).toFixed(1))
      );

      return res.status(200).json({
        success: true,
        walletBalance: summary.walletBalance,
        accumulatedSpareChange: summary.walletBalance,
        totalInvested: summary.totalInvested,
        stats: {
          walletBalance: summary.walletBalance,
          accumulatedSpareChange: summary.walletBalance,
          totalInvested: summary.totalInvested,
          portfolioBreakdown: {
            nifty50Amount: summary.nifty50Amount || 0,
            digitalGoldAmount: summary.digitalGoldAmount || 0,
            flexiCapAmount: summary.flexiCapAmount || 0
          },
          totalRoundUps: summary.totalRoundUps,
          totalSpent: summary.totalSpent,
          transactionCount: summary.transactionCount,
          threshold,
          progressPercent,
          isThresholdReached: summary.walletBalance >= threshold
        }
      });
    } else {
      const user = inMemoryStore.findUserById(userId);
      threshold = user?.autoInvestThreshold || 500.0;
      const summary = inMemoryStore.getWalletStats(userId);

      const progressPercent = Math.min(
        100,
        Number(((summary.walletBalance / threshold) * 100).toFixed(1))
      );

      return res.status(200).json({
        success: true,
        walletBalance: summary.walletBalance,
        accumulatedSpareChange: summary.walletBalance,
        totalInvested: summary.totalInvested,
        stats: {
          walletBalance: summary.walletBalance,
          accumulatedSpareChange: summary.walletBalance,
          totalInvested: summary.totalInvested,
          totalRoundUps: summary.totalRoundUps,
          totalSpent: summary.totalSpent,
          transactionCount: summary.transactionCount,
          threshold,
          progressPercent,
          isThresholdReached: summary.walletBalance >= threshold
        }
      });
    }
  } catch (error) {
    console.error('[TransactionController] getWalletStats error:', error.message);
    return res.status(500).json({ success: false, message: error.message });
  }
};

module.exports = {
  getTransactions,
  createTransaction,
  logBatchTransactions,
  getTransactionById,
  deployThresholdTransactions,
  getWalletStats
};
