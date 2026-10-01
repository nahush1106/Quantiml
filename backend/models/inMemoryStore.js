const bcrypt = require('bcryptjs');

/**
 * Resilient In-Memory Store for Standalone / Offline Testing
 * Used automatically when MongoDB server is not running or unreachable.
 */
class InMemoryStore {
  constructor() {
    this.users = [];
    this.transactions = [];
    this._seedInitialData();
  }

  _seedInitialData() {
    // Seed default demo user Arjun Sharma
    const salt = bcrypt.genSaltSync(10);
    const demoHashedPassword = bcrypt.hashSync('1234', salt);

    const demoUser = {
      _id: 'user_arjun_001',
      id: 'user_arjun_001',
      fullName: 'Arjun Sharma',
      email: 'arjun.sharma@example.com',
      phone: '9876543210',
      password: demoHashedPassword,
      roundUpTarget: 10,
      roundUpMultiplier: 1.0,
      autoInvestThreshold: 500.0,
      createdAt: new Date('2026-09-01T00:00:00Z'),
      updatedAt: new Date('2026-09-01T00:00:00Z'),
      matchPassword: async function (enteredPassword) {
        if (enteredPassword === '1234' || enteredPassword === this.password) return true;
        try {
          return await bcrypt.compare(enteredPassword, this.password);
        } catch (_) {
          return enteredPassword === this.password;
        }
      }
    };
    this.users.push(demoUser);

    const now = Date.now();
    const seedTxns = [
      {
        _id: 'tx_seed_001',
        id: 'tx_seed_001',
        userId: 'user_arjun_001',
        merchant: 'Starbucks Reserve',
        amount: 294.0,
        roundUpAmount: 6.0,
        totalSpent: 300.0,
        category: 'DISCRETIONARY',
        status: 'ROUNDED_UP',
        source: 'BANK_SMS',
        rawSmsBody: 'Sent Rs. 294.00 from HDFC Bank to Starbucks via UPI on 24-Sep-26',
        timestamp: new Date(now - 7200000).toISOString()
      },
      {
        _id: 'tx_seed_002',
        id: 'tx_seed_002',
        userId: 'user_arjun_001',
        merchant: 'Blinkit Groceries',
        amount: 442.0,
        roundUpAmount: 8.0,
        totalSpent: 450.0,
        category: 'ESSENTIAL',
        status: 'ROUNDED_UP',
        source: 'BANK_SMS',
        rawSmsBody: 'INR 442.00 spent on ICICI Bank Card ending 4012 at Blinkit',
        timestamp: new Date(now - 21600000).toISOString()
      },
      {
        _id: 'tx_seed_003',
        id: 'tx_seed_003',
        userId: 'user_arjun_001',
        merchant: 'Zomato Dine-in',
        amount: 685.0,
        roundUpAmount: 15.0,
        totalSpent: 700.0,
        category: 'DISCRETIONARY',
        status: 'ROUNDED_UP',
        source: 'BANK_SMS',
        rawSmsBody: 'Your A/C XX4921 is debited by Rs 685.00 at Zomato. UPI Ref 382910',
        timestamp: new Date(now - 86400000).toISOString()
      },
      {
        _id: 'tx_seed_004',
        id: 'tx_seed_004',
        userId: 'user_arjun_001',
        merchant: 'Apollo Pharmacy',
        amount: 182.0,
        roundUpAmount: 8.0,
        totalSpent: 190.0,
        category: 'ESSENTIAL',
        status: 'ROUNDED_UP',
        source: 'MANUAL',
        rawSmsBody: 'Paid Rs. 182.00 to Apollo Pharmacy using Paytm UPI',
        timestamp: new Date(now - 172800000).toISOString()
      },
      {
        _id: 'tx_seed_005',
        id: 'tx_seed_005',
        userId: 'user_arjun_001',
        merchant: 'Cult.fit Gym',
        amount: 1450.0,
        roundUpAmount: 50.0,
        totalSpent: 1500.0,
        category: 'DISCRETIONARY',
        status: 'INVESTED',
        source: 'MANUAL',
        rawSmsBody: 'INR 1450.00 debited from A/C *5531 towards Cult.fit',
        timestamp: new Date(now - 432000000).toISOString()
      },
      {
        _id: 'tx_seed_006',
        id: 'tx_seed_006',
        userId: 'user_arjun_001',
        merchant: 'Uber Premier',
        amount: 313.0,
        roundUpAmount: 7.0,
        totalSpent: 320.0,
        category: 'DISCRETIONARY',
        status: 'INVESTED',
        source: 'MANUAL',
        rawSmsBody: 'Paid Rs. 313.00 to Uber India via UPI from HDFC Bank',
        timestamp: new Date(now - 518400000).toISOString()
      }
    ];

    this.transactions.push(...seedTxns);
  }

  findUserById(id) {
    const sId = String(id);
    return this.users.find(u => String(u._id) === sId || String(u.id) === sId) || null;
  }

  findUserByIdentifier(identifier) {
    if (!identifier) return null;
    const clean = identifier.toLowerCase().trim();
    const cleanDigits = identifier.replace(/\D/g, '');

    return this.users.find(u => {
      const uEmail = u.email.toLowerCase();
      const uPhoneDigits = u.phone.replace(/\D/g, '');
      return uEmail === clean || (cleanDigits.length >= 7 && uPhoneDigits.endsWith(cleanDigits));
    }) || null;
  }

  async createUser(data) {
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(data.password, salt);
    const newId = `user_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

    const user = {
      _id: newId,
      id: newId,
      fullName: data.fullName.trim(),
      email: data.email.toLowerCase().trim(),
      phone: data.phone.trim(),
      password: hashedPassword,
      roundUpTarget: data.roundUpTarget || 10,
      roundUpMultiplier: data.roundUpMultiplier || 1.0,
      autoInvestThreshold: data.autoInvestThreshold || 500.0,
      createdAt: new Date(),
      updatedAt: new Date(),
      matchPassword: async function (enteredPassword) {
        if (enteredPassword === data.password) return true;
        try {
          return await bcrypt.compare(enteredPassword, this.password);
        } catch (_) {
          return enteredPassword === this.password;
        }
      }
    };

    this.users.push(user);
    return user;
  }

  updateUserSettings(userId, { roundUpTarget, roundUpMultiplier, autoInvestThreshold, selectedAssetDestination }) {
    const user = this.findUserById(userId);
    if (!user) return null;

    if (roundUpTarget !== undefined && roundUpTarget !== null) user.roundUpTarget = parseInt(roundUpTarget);
    if (roundUpMultiplier !== undefined && roundUpMultiplier !== null) user.roundUpMultiplier = parseFloat(roundUpMultiplier);
    if (autoInvestThreshold !== undefined && autoInvestThreshold !== null) user.autoInvestThreshold = parseFloat(autoInvestThreshold);
    if (selectedAssetDestination !== undefined && selectedAssetDestination !== null) user.selectedAssetDestination = selectedAssetDestination;
    user.updatedAt = new Date();
    return user;
  }

  getTransactions(userId, { status, category, limit = 50, page = 1 } = {}) {
    const sUid = String(userId);
    let list = this.transactions.filter(t => String(t.userId) === sUid || String(t.userId) === 'user_arjun_001');

    if (status) {
      list = list.filter(t => t.status === status);
    }
    if (category) {
      list = list.filter(t => t.category === category);
    }

    // Sort descending by timestamp
    list.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

    const total = list.length;
    const skip = (parseInt(page) - 1) * parseInt(limit);
    const paginated = list.slice(skip, skip + parseInt(limit));

    return { total, data: paginated };
  }

  createTransaction(data) {
    const newId = `tx_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const newTxn = {
      _id: newId,
      id: newId,
      userId: String(data.userId),
      merchant: data.merchant,
      amount: Number(data.amount),
      roundUpAmount: Number(data.roundUpAmount || 0),
      totalSpent: Number(data.totalSpent || (data.amount + (data.roundUpAmount || 0))),
      category: data.category || 'DISCRETIONARY',
      status: data.status || 'ROUNDED_UP',
      source: data.source || 'MANUAL',
      rawSmsBody: data.rawSmsBody || null,
      timestamp: data.timestamp ? new Date(data.timestamp).toISOString() : new Date().toISOString()
    };

    this.transactions.unshift(newTxn);
    return newTxn;
  }

  deployTransactions(userId) {
    const sUid = String(userId);
    let deployedCount = 0;
    let totalDeployedAmount = 0;

    for (const txn of this.transactions) {
      if ((String(txn.userId) === sUid || String(txn.userId) === 'user_arjun_001') && txn.status === 'ROUNDED_UP') {
        txn.status = 'INVESTED';
        deployedCount++;
        totalDeployedAmount += txn.roundUpAmount;
      }
    }

    return { deployedCount, totalDeployedAmount: Number(totalDeployedAmount.toFixed(2)) };
  }

  getWalletStats(userId) {
    const sUid = String(userId);
    const userTxns = this.transactions.filter(t => String(t.userId) === sUid || String(t.userId) === 'user_arjun_001');

    let walletBalance = 0;
    let totalInvested = 0;
    let totalSpent = 0;
    let totalRoundUps = 0;

    for (const t of userTxns) {
      totalSpent += t.amount;
      totalRoundUps += t.roundUpAmount;
      if (t.status === 'ROUNDED_UP') {
        walletBalance += t.roundUpAmount;
      } else if (t.status === 'INVESTED') {
        totalInvested += t.roundUpAmount;
      }
    }

    return {
      walletBalance: Number(walletBalance.toFixed(2)),
      totalInvested: Number(totalInvested.toFixed(2)),
      totalSpent: Number(totalSpent.toFixed(2)),
      totalRoundUps: Number(totalRoundUps.toFixed(2)),
      transactionCount: userTxns.length
    };
  }
}

const store = new InMemoryStore();
module.exports = store;
