package com.example.tarik.data.classifier

import kotlin.math.ln
import kotlin.math.exp

// represents one labelled example used to train the classifier
data class TrainingExample(val category: String, val text: String)

// represents the result of classifying a piece of text
// we expose both the winning category and the full probability distribution
data class ClassificationResult(
    val category: String,
    val confidence: Float,
    val allScores: Map<String, Float>
)


// the math of the classifier is as follows: for each candidate category c, we compute
//     log P(c | text) ~ log P(c) + sum over words w of log P(w | c)
// and pick the category with the highest log-score
//
// using logs prevents floating point underflow when multiplying many small probabilities.
// Laplace smoothing (the +1 in the likelihood) means a single unseen word don't zero out the entire product
// for more details on the classifier I used: https://cdn.aaai.org/Workshops/1998/WS-98-05/WS98-05-007.pdf
class BayesianClassifier {

    // priors reffers to how common each category is in the training set
    // log P(c)
    private val logPriors: MutableMap<String, Double> = mutableMapOf()

    // likelihoods reffers to log P(word | category) for every (word, category) pair
    // we store logs directly so we never need to take a log at classify-time
    private val logLikelihoods: MutableMap<String, MutableMap<String, Double>> = mutableMapOf()

    // total word count per category is used as the denominator in the Laplace smoothed likelihood for unseen words
    private val totalWordsPerCategory: MutableMap<String, Int> = mutableMapOf()

    // size of the global vocabulary - the +|V| in the Laplace smoothing
    private var vocabularySize: Int = 0

    var isTrained: Boolean = false
        private set

    // the fallback category when input text has no signal or when the classifier can't find a suitable class with enough confidence.
    private val fallbackCategory = "General"

    //if the classifier outputs less than that treshold for all classes then we label it as general
    private val confidenceThreshold = 0.35f

    fun train(examples: List<TrainingExample>) {
        if (examples.isEmpty()) {
            isTrained = false
            return
        }

        // group examples by category and tokenization
        val examplesByCategory: Map<String, List<List<String>>> =
            examples.groupBy { it.category }
                .mapValues { (_, exs) -> exs.map { tokenise(it.text) } }

        val vocabulary = mutableSetOf<String>()
        examplesByCategory.values.forEach { docs ->
            docs.forEach { tokens -> vocabulary.addAll(tokens) }
        }
        vocabularySize = vocabulary.size

        // count words per category and total documents
        val totalDocs = examples.size
        for ((category, docs) in examplesByCategory) {
            logPriors[category] = ln(docs.size.toDouble() / totalDocs)
            val wordCounts = mutableMapOf<String, Int>()
            var totalWords = 0
            for (tokens in docs) {
                for (token in tokens) {
                    wordCounts[token] = (wordCounts[token] ?: 0) + 1
                    totalWords++
                }
            }
            totalWordsPerCategory[category] = totalWords

            // logs are precomputed for every word in the vocabulary so classify() doesn't need to recompute on every call
            val likelihoodsForCategory = mutableMapOf<String, Double>()
            val denominator = (totalWords + vocabularySize).toDouble()
            for (word in vocabulary) {
                val count = wordCounts[word] ?: 0
                likelihoodsForCategory[word] = ln((count + 1).toDouble() / denominator)
            }
            logLikelihoods[category] = likelihoodsForCategory
        }

        isTrained = true
    }

    fun classify(text: String): ClassificationResult {
        if (!isTrained || text.isBlank()) {
            return ClassificationResult(
                category = fallbackCategory,
                confidence = 1.0f,
                allScores = mapOf(fallbackCategory to 1.0f)
            )
        }

        val tokens = tokenise(text)
        if (tokens.isEmpty()) {
            return ClassificationResult(
                category = fallbackCategory,
                confidence = 1.0f,
                allScores = mapOf(fallbackCategory to 1.0f)
            )
        }

        // compute log P(c | text) for every category
        val logScores = mutableMapOf<String, Double>()
        for ((category, prior) in logPriors) {
            var logScore = prior
            val likelihoods = logLikelihoods[category] ?: continue

            // smoothing for words we never saw at training time using: log P(unseen | c) = log (1 / (totalWords(c) + |V|))
            val unseenWordLogProb = ln(
                1.0 / (totalWordsPerCategory[category]!! + vocabularySize).toDouble()
            )

            for (token in tokens) {
                logScore += likelihoods[token] ?: unseenWordLogProb
            }
            logScores[category] = logScore
        }

        val winner = logScores.maxByOrNull { it.value } ?: return ClassificationResult(fallbackCategory, 1.0f, emptyMap())

        // convert log-scores back to a normalised probability distribution using the log/sum/exp trick to avoid underflow for very negative values
        val maxLog = logScores.values.max()
        val expScores = logScores.mapValues { exp(it.value - maxLog) }
        val sum = expScores.values.sum()
        val normalisedScores = expScores.mapValues { (it.value / sum).toFloat() }

        //selecting winner or general depending on wether threshold is reached
        val winnerConfidence = normalisedScores[winner.key] ?: 0f
        val finalCategory = if (winnerConfidence >= confidenceThreshold) {
            winner.key
        } else {
            fallbackCategory
        }
        return ClassificationResult(
            category = finalCategory,
            confidence = winnerConfidence,
            allScores = normalisedScores
        )
    }

    // we tokenised as such: lowercase, strip punctuation, split on whitespace, drop common english worlds. (Because Naive Bayes rely on bag of words assumption)
    private fun tokenise(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in stopWords }
    }

    companion object {
        // as i said above we also omit these common english words since they carry no category signal and would just add noise to the classifier
        private val stopWords = setOf(
            "the", "and", "for", "with", "from", "that", "this", "was", "were",
            "are", "his", "her", "him", "she", "had", "have", "has", "will",
            "would", "could", "should", "their", "they", "them", "but", "not",
            "out", "into", "onto", "over", "under", "after", "before", "during",
            "while", "between", "among", "across", "many", "some", "all", "any",
            "who", "what", "which", "when", "where", "why", "how", "been", "being"
        )
    }
}
