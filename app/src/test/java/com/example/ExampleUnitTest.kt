package com.example

import com.example.data.nlp.QuantAiNlpClassifier
import com.example.data.sms.BankSmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testHdfcBankSmsParsing() {
    val sms = "Sent Rs. 340.00 from HDFC Bank A/C **1234 to Starbucks via UPI on 24-Sep-26. Ref: 382910"
    val parsed = BankSmsParser.parse(sms, "VM-HDFCBK")

    assertNotNull(parsed)
    assertEquals(340.0, parsed!!.amount, 0.01)
    assertEquals("Starbucks", parsed.merchant)
    assertEquals("HDFC Bank", parsed.bankName)
    assertEquals("DISCRETIONARY", parsed.category)
    // In Quantiml, exact multiples round up to next 10 (340 -> 350 saves 10)
    assertEquals(10.0, parsed.roundUpAmount, 0.01)
  }

  @Test
  fun testIciciBankSmsWithRoundUp() {
    val sms = "Dear Customer, INR 248.00 spent on ICICI Bank Card ending 4012 at Blinkit on 24-Sep-26"
    val parsed = BankSmsParser.parse(sms, "AD-ICICIB")

    assertNotNull(parsed)
    assertEquals(248.0, parsed!!.amount, 0.01)
    assertEquals("Blinkit", parsed.merchant)
    assertEquals("ICICI Bank", parsed.bankName)
    assertEquals("ESSENTIAL", parsed.category)
    assertEquals(2.0, parsed.roundUpAmount, 0.01) // 248 rounds up to 250 -> 2.0
  }

  @Test
  fun testSbiDebitSmsParsing() {
    val sms = "Your A/C XX4921 is debited by Rs 185.00 on 24-09-2026 at Zomato. UPI Ref 382910 - SBI"
    val parsed = BankSmsParser.parse(sms, "BZ-SBIINB")

    assertNotNull(parsed)
    assertEquals(185.0, parsed!!.amount, 0.01)
    assertEquals("Zomato", parsed.merchant)
    assertEquals("State Bank of India", parsed.bankName)
    assertEquals(5.0, parsed.roundUpAmount, 0.01) // 185 rounds up to 190 -> 5.0
  }

  @Test
  fun testIgnoreOtpAndCreditSms() {
    val otpSms = "Your OTP is 482910 for login. Do not share this OTP with anyone - HDFC Bank"
    assertFalse(BankSmsParser.isBankDebitSms("HDFCBK", otpSms))
    assertNull(BankSmsParser.parse(otpSms))

    val creditSms = "INR 50,000.00 credited to A/C **1234 from Salary Deposit on 24-Sep-26"
    assertFalse(BankSmsParser.isBankDebitSms("HDFCBK", creditSms))
    assertNull(BankSmsParser.parse(creditSms))
  }

  @Test
  fun testOneRupeeUpiTransfer() {
    val sms = "Debited by Re. 1.00 from A/C XX8912 on 28-Sep-26 to Ramesh via UPI. Ref 4829102"
    val parsed = BankSmsParser.parse(sms, "VM-HDFCBK")

    assertNotNull(parsed)
    assertEquals(1.0, parsed!!.amount, 0.01)
    assertEquals("Ramesh", parsed.merchant)
    assertEquals(9.0, parsed.roundUpAmount, 0.01) // 1 Rupee spent rounds up to 10 -> 9.0 spare change!
  }

  @Test
  fun testBankOfIndiaSmsParsing() {
    val sms = "Rs.1.00 debited A/cXX0497 and credited to SAHIL SANTOSH KUMBHAR via UPI Ref No 627223305646 on 29Sep26. Call 18001031906, if not done by you. -BOI"
    val parsed = BankSmsParser.parse(sms, "AD-BOIBK")

    assertNotNull(parsed)
    assertEquals(1.0, parsed!!.amount, 0.01)
    assertEquals("SAHIL SANTOSH KUMBHAR", parsed.merchant)
    assertEquals("Bank of India", parsed.bankName)
    assertEquals(9.0, parsed.roundUpAmount, 0.01)
  }

  @Test
  fun testRoundUpMathWithMultipliers() {
    // 294 rounds to nearest 10: 300 - 294 = 6
    val standardRoundUp = QuantAiNlpClassifier.calculateRoundUp(294.0, roundToMultiple = 10, multiplier = 1.0)
    assertEquals(6.0, standardRoundUp, 0.01)

    // With 2x multiplier
    val doubleRoundUp = QuantAiNlpClassifier.calculateRoundUp(294.0, roundToMultiple = 10, multiplier = 2.0)
    assertEquals(12.0, doubleRoundUp, 0.01)

    // With 50 multiple: 294 rounds to 300 -> 6
    val fiftyRoundUp = QuantAiNlpClassifier.calculateRoundUp(294.0, roundToMultiple = 50, multiplier = 1.0)
    assertEquals(6.0, fiftyRoundUp, 0.01)
  }
}
