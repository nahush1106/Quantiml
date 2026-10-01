const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

/**
 * User Schema
 * Manages user credentials, contact details, and micro-investing preferences.
 */
const UserSchema = new mongoose.Schema(
  {
    fullName: {
      type: String,
      required: [true, 'Full name is required'],
      trim: true,
      maxlength: [100, 'Full name cannot exceed 100 characters']
    },
    email: {
      type: String,
      required: [true, 'Email is required'],
      unique: true,
      lowercase: true,
      trim: true,
      match: [
        /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/,
        'Please enter a valid email address'
      ]
    },
    phone: {
      type: String,
      required: [true, 'Mobile number is required'],
      unique: true,
      trim: true,
      match: [/^[0-9+ -]{7,16}$/, 'Please enter a valid phone number']
    },
    password: {
      type: String,
      required: [true, 'Password or security PIN is required'],
      minlength: [4, 'Password/PIN must be at least 4 characters']
    },
    // Micro-investing configuration
    roundUpTarget: {
      type: Number,
      default: 10, // e.g., round up to next ₹10, ₹50, ₹100
      enum: [10, 20, 50, 100]
    },
    roundUpMultiplier: {
      type: Number,
      default: 1.0, // 1x (standard), 2x, 5x, 10x
      min: 1.0,
      max: 10.0
    },
    autoInvestThreshold: {
      type: Number,
      default: 500.0, // When spare change reaches ₹500, deploy into portfolio
      min: 50.0,
      max: 50000.0
    },
    // Selected Auto-Deploy Investment Destination Strategy
    selectedAssetDestination: {
      type: String,
      default: 'NIFTY_50_INDEX', // NIFTY_50_INDEX, DIGITAL_GOLD, FLEXI_CAP
      enum: ['NIFTY_50_INDEX', 'DIGITAL_GOLD', 'FLEXI_CAP']
    }
  },
  {
    timestamps: true,
    toJSON: {
      // Automatically remove sensitive password hash when sending user objects via API
      transform: function (doc, ret) {
        delete ret.password;
        delete ret.__v;
        return ret;
      }
    }
  }
);

// Pre-save hook: Hash password with bcrypt before saving
UserSchema.pre('save', async function (next) {
  if (!this.isModified('password')) {
    return next();
  }

  try {
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Instance method: Verify entered password / PIN against hashed database password
UserSchema.methods.comparePassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

module.exports = mongoose.model('User', UserSchema);
