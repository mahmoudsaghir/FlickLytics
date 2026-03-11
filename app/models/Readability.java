package models;

/**
 * Utility class for computing Flesch readability scores.
 * @author Zenghui WU
 */
public class Readability {

    /**
     * Holds readability scores computed from text.
     * @author Zenghui WU
     */
    public Readability() {
    }
    public static class ReadabilityScores {
        public final double fleschReadingEase;
        public final double fleschKincaidGrade;
        public final int sentences;
        public final int words;
        public final int syllables;

        public ReadabilityScores(double fre, double fkg, int s, int w, int sy) {
            this.fleschReadingEase = fre;
            this.fleschKincaidGrade = fkg;
            this.sentences = s;
            this.words = w;
            this.syllables = sy;
        }
    }

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

    private static int countSentences(String t) {
        String[] parts = t.split("[.!?]+");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isEmpty()) count++;
        }
        return Math.max(count, 1);
    }

    private static int countWords(String t) {
        String[] parts = t.trim().split("\\s+");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isEmpty()) count++;
        }
        return count;
    }

    private static int countSyllables(String t) {
        String[] words = t.toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
        int total = 0;
        for (String w : words) {
            if (w.isEmpty()) continue;
            total += syllablesInWord(w);
        }
        return total;
    }

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

    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}