package com.example.data.nlp

import com.example.data.model.TransactionCategory
import java.util.Locale

/**
 * QuantAI NLP Categorization Engine
 * Classifies merchant names & SMS descriptions into ESSENTIAL vs DISCRETIONARY.
 *
 * Essential: Groceries, Utilities, Healthcare, Medicines, Public Transit, Fuel, Education.
 * Discretionary: Dining, Cafes, Movies, Shopping, Gaming, Nightlife, Fast Food, OTT subscriptions.
 */
object QuantAiNlpClassifier {

  private val essentialKeywords = setOf(
    "grocery", "groceries", "blinkit", "zepto", "instamart", "bigbasket",
    "supermarket", "dmart", "nature basket", "spencer", "milk", "vegetables",
    "fruits", "pharmacy", "apollo", "medplus", "1mg", "pharmeasy", "hospital",
    "clinic", "doctor", "medicine", "bescom", "tneb", "mahadiscom", "cesc",
    "electricity", "water", "gas", "indane", "hp gas", "bharat gas", "broadband",
    "wifi", "airtel", "jio", "vi", "bsnl", "metro", "irctc", "indian railways",
    "bus", "uber auto", "fuel", "petrol", "diesel", "hpcl", "bpcl", "iocl",
    "school", "college", "tuition", "rent", "society"
  )

  private val discretionaryKeywords = setOf(
    "starbucks", "cafe", "coffee", "third wave", "blue tokai", "ccd", "barista",
    "zomato", "swiggy", "mcdonalds", "kfc", "burger king", "dominos", "pizza hut",
    "subway", "barbeque nation", "pub", "brewery", "restaurant", "bistro",
    "dine", "cinema", "pvr", "inox", "cinepolis", "bookmyshow", "netflix",
    "amazon prime", "spotify", "hotstar", "youtube premium", "steam", "playstation",
    "zara", "h&m", "uniqlo", "myntra", "ajio", "nykaa", "amazon", "flipkart",
    "nike", "adidas", "puma", "salon", "spa", "resort", "flight", "makemytrip"
  )

  fun classify(merchant: String, rawText: String? = null): TransactionCategory {
    val textToAnalyze = "$merchant ${rawText.orEmpty()}".lowercase(Locale.ROOT)

    // Check discretionary keywords first for high precision
    if (discretionaryKeywords.any { textToAnalyze.contains(it) }) {
      return TransactionCategory.DISCRETIONARY
    }

    // Check essential keywords
    if (essentialKeywords.any { textToAnalyze.contains(it) }) {
      return TransactionCategory.ESSENTIAL
    }

    // Default heuristics:
    // If unknown, mark as DISCRETIONARY to encourage micro-savings safely
    return TransactionCategory.DISCRETIONARY
  }

  /**
   * Calculates round-up amount based on round-up target (e.g. ₹10 or ₹50) and multiplier.
   * If spend is exact multiple of target, standard round-up is either target or ₹0 depending on mode.
   * Quantiml default: round to next multiple of 10. (e.g. 93 -> 100, diff = 7. If 90 -> 100, diff = 10).
   */
  fun calculateRoundUp(amount: Double, roundToMultiple: Int = 10, multiplier: Double = 1.0): Double {
    if (amount <= 0.0) return 0.0
    val target = roundToMultiple.toDouble()
    val remainder = amount % target
    val baseRoundUp = if (remainder == 0.0) target else (target - remainder)
    return String.format(Locale.US, "%.2f", baseRoundUp * multiplier).toDouble()
  }
}
