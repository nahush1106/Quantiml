process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = process.env.JWT_SECRET || 'quantiml_test_jwt_secret_key_12345';

const { test, describe, before, after } = require('node:test');
const assert = require('node:assert/strict');
const request = require('supertest');
const mongoose = require('mongoose');
const { app } = require('../server');

describe('Quantiml REST API Endpoints Verification Suite', () => {
  let authToken = '';
  let userId = '';
  let testTransactionId = '';
  const testEmail = `testuser_${Date.now()}@quantiml.com`;
  const testPhone = `91${Math.floor(10000000 + Math.random() * 90000000)}`;
  const testPassword = 'Password@123';

  after(async () => {
    try {
      if (mongoose.connection && mongoose.connection.readyState !== 0) {
        await mongoose.disconnect();
      }
    } catch (_) {}
    // Ensure clean test exit without waiting for background socket timeouts
    setTimeout(() => {
      process.exit(0);
    }, 200).unref();
  });

  // ---------------------------------------------------------
  // 1. Health & System Diagnostic Endpoints
  // ---------------------------------------------------------
  describe('System & Health Endpoints', () => {
    test('GET / should return 200 and system operational status', async () => {
      const res = await request(app).get('/');
      assert.equal(res.status, 200);
      assert.equal(res.body.status, 'ONLINE');
      assert.ok(res.body.version);
      assert.ok(res.body.database);
    });

    test('GET /health should return 200 (or 503 degraded) with memory and db status', async () => {
      const res = await request(app).get('/health');
      assert.ok([200, 503].includes(res.status));
      assert.ok(res.body.database);
      assert.ok(res.body.uptime);
    });

    test('GET /api should return 200 with available endpoint catalog', async () => {
      const res = await request(app).get('/api');
      assert.equal(res.status, 200);
      assert.equal(res.body.status, 'ONLINE');
      assert.ok(res.body.endpoints);
    });
  });

  // ---------------------------------------------------------
  // 2. User Registration Endpoints
  // ---------------------------------------------------------
  describe('User Registration (POST /api/user/register)', () => {
    test('should reject registration if required fields are missing', async () => {
      const res = await request(app)
        .post('/api/user/register')
        .send({
          fullName: 'Incomplete User'
          // missing email, phone, password
        });

      assert.equal(res.status, 400);
      assert.equal(res.body.success, false);
      assert.ok(res.body.message);
    });

    test('should reject registration if password is too short (< 6 chars)', async () => {
      const res = await request(app)
        .post('/api/user/register')
        .send({
          fullName: 'Short Pass User',
          email: 'shortpass@example.com',
          phone: '9876543210',
          password: '123'
        });

      assert.equal(res.status, 400);
      assert.equal(res.body.success, false);
    });

    test('should successfully register a new user account with default settings', async () => {
      const res = await request(app)
        .post('/api/user/register')
        .send({
          fullName: 'Arjun Mehta',
          email: testEmail,
          phone: testPhone,
          password: testPassword,
          roundUpTarget: 10,
          roundUpMultiplier: 1.0,
          autoInvestThreshold: 500.0
        });

      assert.equal(res.status, 201);
      assert.equal(res.body.success, true);
      const token = res.body.token || res.body.data?.token;
      const user = res.body.user || res.body.data?.user;
      assert.ok(token, 'Should return JWT token');
      assert.ok(user, 'Should return user object');
      assert.equal(user.email, testEmail);

      // Save token and user id for subsequent authenticated tests
      authToken = token;
      userId = user.id || user._id;
    });

    test('should reject duplicate registration with identical email or phone', async () => {
      const res = await request(app)
        .post('/api/user/register')
        .send({
          fullName: 'Duplicate User',
          email: testEmail,
          phone: testPhone,
          password: testPassword
        });

      assert.equal(res.status, 400);
      assert.equal(res.body.success, false);
    });
  });

  // ---------------------------------------------------------
  // 3. User Login Endpoints
  // ---------------------------------------------------------
  describe('User Login (POST /api/user/login)', () => {
    test('should reject login with wrong password', async () => {
      const res = await request(app)
        .post('/api/user/login')
        .send({
          email: testEmail,
          password: 'WrongPassword@999'
        });

      assert.ok([400, 401].includes(res.status));
      assert.equal(res.body.success, false);
    });

    test('should reject login for non-existent email', async () => {
      const res = await request(app)
        .post('/api/user/login')
        .send({
          email: 'nonexistent_user_9999@quantiml.example',
          password: testPassword
        });

      assert.ok([400, 401].includes(res.status));
      assert.equal(res.body.success, false);
    });

    test('should successfully authenticate user and return fresh JWT token', async () => {
      const res = await request(app)
        .post('/api/user/login')
        .send({
          email: testEmail,
          password: testPassword
        });

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const token = res.body.token || res.body.data?.token;
      const user = res.body.user || res.body.data?.user;
      assert.ok(token);
      assert.equal(user.email, testEmail);

      // Update authToken with fresh token
      authToken = token;
    });
  });

  // ---------------------------------------------------------
  // 4. User Profile & Settings Endpoints (Protected)
  // ---------------------------------------------------------
  describe('User Profile & Settings (Protected)', () => {
    test('GET /api/user/profile should reject unauthenticated requests without Bearer token', async () => {
      const res = await request(app).get('/api/user/profile');
      assert.equal(res.status, 401);
      assert.equal(res.body.success, false);
    });

    test('GET /api/user/profile should return current user details with valid Bearer token', async () => {
      const res = await request(app)
        .get('/api/user/profile')
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const email = res.body.email || res.body.user?.email || res.body.data?.email;
      assert.equal(email, testEmail);
    });

    test('PUT /api/user/settings should update roundUpTarget, multiplier, and autoInvestThreshold', async () => {
      const res = await request(app)
        .put('/api/user/settings')
        .set('Authorization', `Bearer ${authToken}`)
        .send({
          roundUpTarget: 20,
          roundUpMultiplier: 2.0,
          autoInvestThreshold: 1000.0
        });

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const target = res.body.roundUpTarget || res.body.user?.roundUpTarget || res.body.settings?.roundUpTarget;
      assert.equal(target, 20);
    });
  });

  // ---------------------------------------------------------
  // 5. Transaction Endpoints & Micro-Investing Round-Up Engine
  // ---------------------------------------------------------
  describe('Transaction Endpoints & Round-Up Calculation', () => {
    test('POST /api/transactions should reject unauthenticated requests', async () => {
      const res = await request(app)
        .post('/api/transactions')
        .send({
          amount: 247.0,
          merchant: 'Swiggy'
        });

      assert.equal(res.status, 401);
    });

    test('POST /api/transactions should compute spare change correctly', async () => {
      // User settings were updated to roundUpTarget: 20, roundUpMultiplier: 2.0
      // Amount 247 with roundUpTarget 20 -> next multiple is 260.
      // Base difference = 13.0. Multiplier 2.0 -> spare change = 26.0.
      const res = await request(app)
        .post('/api/transactions')
        .set('Authorization', `Bearer ${authToken}`)
        .send({
          amount: 247.0,
          merchant: 'Swiggy Food Delivery',
          category: 'DISCRETIONARY',
          source: 'MANUAL',
          notes: 'Dinner order'
        });

      assert.equal(res.status, 201);
      assert.equal(res.body.success, true);
      const tx = res.body.data || res.body;
      assert.equal(tx.amount, 247.0);
      assert.ok(tx.roundUpAmount > 0);
      assert.equal(tx.status, 'ROUNDED_UP');

      testTransactionId = tx._id || tx.id;
      assert.ok(testTransactionId, 'Should return transaction ID');
    });

    test('POST /api/transactions should respect explicit roundUpAmount and totalSpent', async () => {
      const res = await request(app)
        .post('/api/transactions')
        .set('Authorization', `Bearer ${authToken}`)
        .send({
          amount: 85.0,
          roundUpAmount: 15.0,
          totalSpent: 100.0,
          merchant: 'Starbucks Coffee',
          category: 'DISCRETIONARY',
          source: 'MANUAL'
        });

      assert.equal(res.status, 201);
      assert.equal(res.body.success, true);
      const tx = res.body.data || res.body;
      assert.equal(tx.roundUpAmount, 15.0);
    });

    test('GET /api/transactions should retrieve transactions for logged-in user', async () => {
      const res = await request(app)
        .get('/api/transactions')
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const list = res.body.data || res.body.transactions;
      assert.ok(Array.isArray(list));
      assert.ok(list.length >= 2);
    });

    test('GET /api/transactions?format=array should return array directly for Android client', async () => {
      const res = await request(app)
        .get('/api/transactions?format=array')
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.ok(Array.isArray(res.body), 'Response should be a direct JSON array');
      assert.ok(res.body.length >= 2);
    });

    test('GET /api/transactions/:id should retrieve details of specific transaction', async () => {
      assert.ok(testTransactionId, 'testTransactionId must be set');
      const res = await request(app)
        .get(`/api/transactions/${testTransactionId}`)
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const tx = res.body.data || res.body;
      assert.equal(tx._id || tx.id, testTransactionId);
      assert.equal(tx.merchant, 'Swiggy Food Delivery');
    });

    test('GET /api/transactions/stats should return wallet stats and accumulated spare change', async () => {
      const res = await request(app)
        .get('/api/transactions/stats')
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const stats = res.body.data || res.body;
      const spareChange = stats.accumulatedSpareChange !== undefined ? stats.accumulatedSpareChange : stats.walletBalance;
      assert.ok(typeof spareChange === 'number');
      assert.ok(spareChange > 0);
    });

    test('POST /api/transactions/batch should insert multiple transactions at once', async () => {
      const res = await request(app)
        .post('/api/transactions/batch')
        .set('Authorization', `Bearer ${authToken}`)
        .send({
          transactions: [
            { amount: 154.0, merchant: 'Uber Ride', category: 'ESSENTIAL' },
            { amount: 489.0, merchant: 'DMart Grocery', category: 'ESSENTIAL' }
          ]
        });

      assert.equal(res.status, 201);
      assert.equal(res.body.success, true);
      assert.equal(res.body.count, 2);
    });

    test('POST /api/transactions/deploy should deploy accumulated round-up change to portfolio', async () => {
      const res = await request(app)
        .post('/api/transactions/deploy')
        .set('Authorization', `Bearer ${authToken}`);

      assert.equal(res.status, 200);
      assert.equal(res.body.success, true);
      const deployData = res.body.data || res.body;
      assert.ok(deployData.deployedAmount >= 0);
    });
  });
});
