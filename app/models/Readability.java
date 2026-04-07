package models;

/**
 * Utility class for computing Flesch readability scores.
 * @author Zenghui WU
 */
public class Readability {

    /**
     * Creates a {@code Readability} instance.
     *
     * <p>This constructor is not required for normal usage because all
     * computation methods are static, but it is provided explicitly it is created for test.
     * @author ZW
     */
    public Readability() {
    }

    /**
     * Immutable result object that stores readability metrics computed from text.
     *
     * @author Zenghui WU
     */

    public static class ReadabilityScores {
        /**
         * The Flesch Reading-Ease score.
         * @author ZW
         */

        public final double fleschReadingEase;
        /**
         * The Flesch-Kincaid-Grade Level score.
         */

        public final double fleschKincaidGrade;
        /**
         * Number of sentences detected in the input text.
         */
        public final int sentences;
        /**
         * Number of words detected in the input text.
         */
        public final int words;
        /**
         * Number of syllables estimated in the input text.
         */
        public final int syllables;

        /**
         * Constructs a {@code ReadabilityScores} object.
         *
         * @param fre the Flesch-Reading Ease score
         * @param fkg the Flesch-Kincaid Grade Level score
         * @param s the number of sentences
         * @param w the number of words
         * @param sy the number of syllables
         * @author ZW
         */
        public ReadabilityScores(double fre, double fkg, int s, int w, int sy) {
            this.fleschReadingEase = fre;
            this.fleschKincaidGrade = fkg;
            this.sentences = s;
            this.words = w;
            this.syllables = sy;
        }
    }
    /**
     * Computes readability metrics for the given text.
     *
     * <p>If the input text is {@code null} or empty after trimming,
     * all returned values are {@code 0}.
     *
     * @param text the input text to analyze
     * @return a {@code ReadabilityScores} object containing the computed metrics
     * @author ZW
     */
    public static ReadabilityScores compute(String text) {
        if (text == null) text = "";
        String cleaned = text.trim();
        if (cleaned.isEmpty()) {
            return new ReadabilityScores(0.0, 0.0, 0, 0, 0);
        }

        int sentences = countSentences(cleaned);
        int words = countWords(cleaned);
        int syllables = countSyllables(cleaned);

        sentences = Math.max(sentences, 1);
        words = Math.max(words, 1);

        double fre = 206.835
                - 1.015 * ((double) words / sentences)
                - 84.6 * ((double) syllables / words);

        double fkg = 0.39 * ((double) words / sentences)
                + 11.8 * ((double) syllables / words)
                - 15.59;

        return new ReadabilityScores(round2(fre), round2(fkg), sentences, words, syllables);
    }

    /**
     * Counts the number of sentences in the text using punctuation delimiters.
     *
     * <p>Sentence boundaries are estimated by splitting on one or more of:
     * {@code .}, {@code !}, or {@code ?}.
     *
     * @param t the input text
     * @return the estimated sentence count, minimum {@code 1}
     * @author ZW
     */
    private static int countSentences(String t) {
        String[] parts = t.split("[.!?]+");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isEmpty()) count++;
        }
        return Math.max(count, 1);
    }

    /**
     * Counts the number of words in the text.
     *
     * <p>Words are separated using one or more whitespace characters.
     *
     * @param t the input text
     * @return the number of detected words
     * @author ZW
     */
    private static int countWords(String t) {
        //split the text wherever there are more than one or more whitespace characters;
        String[] parts = t.trim().split("\\s+");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isEmpty()) count++;
        }
        return count;
    }

    /**
     * Counts the total number of syllables in the text.
     *
     * <p>Non-letter characters are removed before counting syllables
     * word by word.
     *
     * @param t the input text
     * @return the estimated total syllable count as int
     * @author ZW
     */
    private static int countSyllables(String t) {
        //remove everything that is not a lowercase English letter or whitespace
        String[] words = t.toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
        int total = 0;
        for (String w : words) {
            if (w.isEmpty()) continue;
            total += syllablesInWord(w);
        }
        return total;
    }

    /**
     * Estimates the number of syllables in a single word.
     *
     * <p>This method uses a simple heuristic:
     * <ul>
     *   <li>removes a trailing silent {@code e}</li>
     *   <li>counts groups of adjacent vowels as one syllable</li>
     *   <li>ensures at least one syllable per word</li>
     * </ul>
     *
     * @param w the word to evaluate
     * @return the estimated number of syllables, minimum {@code 1}
     */
    private static int syllablesInWord(String w) {
        w = w.replaceAll("e$", "");
        int count = 0;
        boolean prevVowel = false;
        for (char c : w.toCharArray()) {
            boolean vowel = "aeiouy".indexOf(c) >= 0;
            if (vowel && !prevVowel) count++;
            prevVowel = vowel;
        }
        return Math.max(count, 1);
    }
    /**
     * Rounds a floating-point value to two decimal places.
     *
     * @param x the value to round
     * @return the rounded value
     * @author ZW
     */
    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}