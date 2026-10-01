const mongoose = require('mongoose');

/**
 * Transaction Schema
 * Stores expense transactions, automated spare change round-ups, and bank SMS metadata.
 */
const TransactionSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'User ID is required'],
      index: true
    },
    merchant: {
      type: String,
      required: [true, 'Merchant name is required'],
      trim: true,
      maxlength: [120, 'Merchant name cannot exceed 120 characters']
    },
    // The actual debit / spend amount (e.g., ₹340.00)
    amount: {
      type: Number,
      required: [true, 'Transaction amount is required'],
      min: [0.01, 'Amount must be greater than 0']
    },
    // The spare change round-up calculated (e.g., ₹10.00)
    roundUpAmount: {
      type: Number,
      required: [true, 'Round-up amount is required'],
      min: [0.0, 'Round-up amount cannot be negative'],
      default: 0.0
    },
    // Total debit (amount + roundUpAmount)
    totalSpent: {
      type: Number,
      required: [true, 'Total spent is required']
    },
    // NLP Category: ESSENTIAL (needs) vs DISCRETIONARY (lifestyle/wants)
    category: {
      type: String,
      enum: {
        values: ['ESSENTIAL', 'DISCRETIONARY'],
        message: '{VALUE} is not a valid category'
      },
      default: 'DISCRETIONARY',
      index: true
    },
    // Lifecycle status:
    // ROUNDED_UP: Spare change is accumulating in the wallet
    // INVESTED: Threshold reached and deployed to investment portfolio
    status: {
      type: String,
      enum: {
        values: ['ROUNDED_UP', 'INVESTED'],
        message: '{VALUE} is not a valid status'
      },
      default: 'ROUNDED_UP',
      index: true
    },
    // Deployed Asset Strategy Destination (NIFTY_50_INDEX, DIGITAL_GOLD, FLEXI_CAP)
    assetDestination: {
      type: String,
      default: 'NIFTY_50_INDEX',
      enum: ['NIFTY_50_INDEX', 'DIGITAL_GOLD', 'FLEXI_CAP']
    },
    // Origin source: BANK_SMS auto-capture vs MANUAL user entry
    source: {
      type: String,
      enum: ['MANUAL', 'BANK_SMS'],
      default: 'MANUAL'
    },
    // Raw SMS body if parsed automatically from bank alert (HDFC, ICICI, SBI, etc.)
    rawSmsBody: {
      type: String,
      default: null
    },
    // Transaction date / time
    timestamp: {
      type: Date,
      default: Date.now,
      index: true
    }
  },
  {
    timestamps: true
  }
);

// Compound indexes for optimized query performance on mobile feeds & wallet calculations
TransactionSchema.index({ userId: 1, status: 1 });
TransactionSchema.index({ userId: 1, timestamp: -1 });

// Pre-validate hook: Ensure totalSpent is computed if not explicitly supplied
TransactionSchema.pre('validate', function (next) {
  if (this.amount && this.roundUpAmount !== undefined && !this.totalSpent) {
    this.totalSpent = Number((this.amount + this.roundUpAmount).toFixed(2));
  }
  next();
});

/**
 * Static Helper: Calculate wallet summary for a given user
 * Returns { walletBalance, totalInvested, totalRoundUps, transactionCount }
 */
TransactionSchema.statics.getWalletSummary = async function (userId) {
  const result = await this.aggregate([
    { $match: { userId: new mongoose.Types.ObjectId(userId) } },
    {
      $group: {
        _id: null,
        walletBalance: {
          $sum: {
            $cond: [{ $eq: ['$status', 'ROUNDED_UP'] }, '$roundUpAmount', 0]
          }
        },
        totalInvested: {
          $sum: {
            $cond: [{ $eq: ['$status', 'INVESTED'] }, '$roundUpAmount', 0]
          }
        },
        nifty50Amount: {
          $sum: {
            $cond: [
              { $and: [{ $eq: ['$status', 'INVESTED'] }, { $eq: ['$assetDestination', 'NIFTY_50_INDEX'] }] },
              '$roundUpAmount',
              0
            ]
          }
        },
        digitalGoldAmount: {
          $sum: {
            $cond: [
              { $and: [{ $eq: ['$status', 'INVESTED'] }, { $eq: ['$assetDestination', 'DIGITAL_GOLD'] }] },
              '$roundUpAmount',
              0
            ]
          }
        },
        flexiCapAmount: {
          $sum: {
            $cond: [
              { $and: [{ $eq: ['$status', 'INVESTED'] }, { $eq: ['$assetDestination', 'FLEXI_CAP'] }] },
              '$roundUpAmount',
              0
            ]
          }
        },
        totalRoundUps: { $sum: '$roundUpAmount' },
        totalSpent: { $sum: '$amount' },
        transactionCount: { $sum: 1 }
      }
    }
  ]);

  if (result.length === 0) {
    return {
      walletBalance: 0,
      totalInvested: 0,
      nifty50Amount: 0,
      digitalGoldAmount: 0,
      flexiCapAmount: 0,
      totalRoundUps: 0,
      totalSpent: 0,
      transactionCount: 0
    };
  }

  const summary = result[0];
  delete summary._id;
  return summary;
};

module.exports = mongoose.model('Transaction', TransactionSchema);
