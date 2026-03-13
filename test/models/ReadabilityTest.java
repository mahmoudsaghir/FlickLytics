package models;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ReadabilityTest {


    //helper
    private Readability.ReadabilityScores compute(String text){

        return Readability.compute(text);
    }
    //helper
    private int testMethod(String methodName, String input) throws Exception{
        Method method = Readability.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (int) method.invoke(null,input);
    }
    private int invokeCountSyllables(String input) throws Exception {
        Method method = Readability.class.getDeclaredMethod("countSyllables", String.class);
        method.setAccessible(true);
        return (int) method.invoke(null, input);
    }

    @Test
    public void testComputeWithNullText() {
        Readability.ReadabilityScores scores = Readability.compute(null);

        assertEquals(0.0, scores.fleschReadingEase, 0.001);
        assertEquals(0.0, scores.fleschKincaidGrade, 0.001);
        assertEquals(0, scores.sentences);
        assertEquals(0, scores.words);
        assertEquals(0, scores.syllables);
    }

    @Test
    public void testComputeWithEmptyText() {
        Readability.ReadabilityScores scores = Readability.compute("   ");

        assertEquals(0.0, scores.fleschReadingEase, 0.001);
        assertEquals(0.0, scores.fleschKincaidGrade, 0.001);
        assertEquals(0, scores.sentences);
        assertEquals(0, scores.words);
        assertEquals(0, scores.syllables);
    }

    @Test
    public void testComputeWithSimpleSentence() {
        Readability.ReadabilityScores scores = Readability.compute("This is a simple test.");

        assertEquals(117.16, scores.fleschReadingEase, 0.001);
        assertEquals(-1.84, scores.fleschKincaidGrade, 0.001);
        assertEquals(1, scores.sentences);
        assertEquals(5, scores.words);
        assertEquals(5, scores.syllables);
    }

    @Test
    public void testComputeWithMultipleSentences() {
        Readability.ReadabilityScores scores =
                Readability.compute("This is sentence one. This is sentence two!");

        assertEquals(97.03, scores.fleschReadingEase, 0.001);
        assertEquals(0.72, scores.fleschKincaidGrade, 0.001);
        assertEquals(2, scores.sentences);
        assertEquals(8, scores.words);
        assertEquals(10, scores.syllables);
    }

    @Test
    public void testComputeWithShortText() {
        Readability.ReadabilityScores scores = Readability.compute("Hello world.");

        assertEquals(77.91, scores.fleschReadingEase, 0.001);
        assertEquals(2.89, scores.fleschKincaidGrade, 0.001);
        assertEquals(1, scores.sentences);
        assertEquals(2, scores.words);
        assertEquals(3, scores.syllables);
    }
    @Test
    public void testNullInputNullBranch() {
        // text == null → should not throw, treated as empty
        Readability.ReadabilityScores s = compute(null);
        assertNotNull(s);
        assertEquals(0.0, s.fleschReadingEase,  0.001);
        assertEquals(0.0, s.fleschKincaidGrade, 0.001);
        assertEquals(0, s.sentences);
        assertEquals(0, s.words);
        assertEquals(0, s.syllables);
    }

    @Test
    public void testNonNullInputNonNullBranch() {
        // text != null → proceeds normally
        Readability.ReadabilityScores s = compute("Hello world.");
        assertNotNull(s);
        assertTrue(s.words > 0);
    }
    @Test
    public void testEmptyStringEmptyBranch() {
        // cleaned.isEmpty() == true → returns all zeros
        Readability.ReadabilityScores s = compute("");
        assertEquals(0.0, s.fleschReadingEase,  0.001);
        assertEquals(0.0, s.fleschKincaidGrade, 0.001);
        assertEquals(0, s.sentences);
        assertEquals(0, s.words);
        assertEquals(0, s.syllables);
    }

    @Test
    public void testWhitespaceOnlyEmptyBranch() {
        // "   ".trim() == "" → also hits the empty branch
        Readability.ReadabilityScores s = compute("   ");
        assertEquals(0.0, s.fleschReadingEase,  0.001);
        assertEquals(0, s.words);
    }

    @Test
    public void testNonEmptyStringNonEmptyBranch() {
        // cleaned is not empty → computes scores normally
        Readability.ReadabilityScores s = compute("The cat sat on the mat.");
        assertTrue(s.words > 0);
        assertTrue(s.sentences > 0);
        assertTrue(s.syllables > 0);
    }

    @Test
    public void testConsecutivePunctuationEmptyPartBranch() {
        // "Hello!! World?" splits on [.!?]+ giving ["Hello", " World", ""]
        // The empty tail part hits the false branch (p.trim().isEmpty())
        Readability.ReadabilityScores s = compute("Hello!! World?");
        assertNotNull(s);
        assertEquals(2, s.sentences);
    }

    @Test
    public void testTrailingPunctuationEmptyPartBranch() {
        // "Done." → split gives ["Done", ""] → empty tail hits false branch
        Readability.ReadabilityScores s = compute("Done.");
        assertNotNull(s);
        assertEquals(1, s.sentences);
    }

    @Test
    public void testMultipleSentencesNonEmptyPartBranch() {
        // Both parts are non-empty → hits the true branch multiple times
        Readability.ReadabilityScores s = compute("Hello world. Goodbye world.");
        assertEquals(2, s.sentences);
    }

    @Test
    public void testMultipleSpacesBetweenWordsEmptyTokenBranch() {
        // "hello   world" — split("\\s+") on trimmed string gives clean tokens
        // but a string like "  hello  world  " after trim is "hello  world"
        // split("\\s+") gives ["hello","world"] — both non-empty (true branch)
        Readability.ReadabilityScores s = compute("hello   world.");
        assertEquals(2, s.words);
    }

    @Test
    public void testSingleWord_Line67_TrueBranch() {
        Readability.ReadabilityScores s = compute("Hello.");
        assertEquals(1, s.words);
    }

    @Test
    public void testManyWords_Line67_TrueBranch() {
        Readability.ReadabilityScores s = compute("The quick brown fox jumps over the lazy dog.");
        assertEquals(9, s.words);
    }

    @Test
    public void testPunctuationHeavyTextEmptyWordBranch() {
        // replaceAll("[^a-z\\s]", " ") turns numbers/symbols into spaces,
        // then split("\\s+") may produce empty strings at the boundary
        // e.g. "123 abc" → "    abc" → split may give ["","","","abc"]
        Readability.ReadabilityScores s = compute("Test 123 abc.");
        assertNotNull(s);
        assertTrue(s.syllables > 0);
    }

    @Test
    public void testAllPunctuationEmptyWordBranch() {
        // "!!!" → replaceAll → "   " → split → all empty tokens → continue fires
        Readability.ReadabilityScores s = compute("!!!");
        assertNotNull(s);
        // all tokens are empty, so syllables = 0, but Math.max(words,1) protects compute
    }

    @Test
    public void testNormalWordsNonEmptyBranch() {
        // Normal words → no empty tokens, straight to syllablesInWord
        Readability.ReadabilityScores s = compute("Beautiful sunshine today.");
        assertTrue(s.syllables > 0);
    }

    @Test
    public void testConsecutiveVowelsFalseBranch() {
        // "beautiful" has consecutive vowels "eau" → prevVowel==true → count++ skipped
        Readability.ReadabilityScores s = compute("beautiful.");
        assertNotNull(s);
        assertTrue("beautiful should have at least 1 syllable", s.syllables >= 1);
    }

    @Test
    public void testWordWithDiphthongFalseBranch() {
        // "queen" → qu-ee-n: 'e','e' consecutive → second e skips count++
        Readability.ReadabilityScores s = compute("queen.");
        assertNotNull(s);
        assertTrue(s.syllables >= 1);
    }

    @Test
    public void testWordWithOnlyConsonantVowelPatternTrueBranch() {
        // "cat" → c(consonant) a(vowel,count++) t(consonant) → clean true-branch only
        Readability.ReadabilityScores s = compute("cat.");
        assertEquals(1, s.syllables);
    }

    @Test
    public void testWordEndingInESilentE() {
        // "take" → silent 'e' removed by replaceAll("e$","") → becomes "tak"
        // t(consonant) a(vowel,count++) k(consonant) → 1 syllable
        Readability.ReadabilityScores s = compute("take.");
        assertEquals(1, s.syllables);
    }

    @Test
    public void testKnownSentence_Integration() {
        // "The cat sat." — 1 sentence, 3 words
        Readability.ReadabilityScores s = compute("The cat sat.");
        assertEquals(1, s.sentences);
        assertEquals(3, s.words);
        assertTrue(s.syllables >= 3);
        // Scores should be real numbers, not zero
        assertNotEquals(0.0, s.fleschReadingEase,  0.001);
        assertNotEquals(0.0, s.fleschKincaidGrade, 0.001);
    }

    @Test
    public void testScoresAreRoundedToTwoDecimals() {
        Readability.ReadabilityScores s = compute("The quick brown fox.");
        // round2 ensures at most 2 decimal places
        assertEquals(s.fleschReadingEase,
                Math.round(s.fleschReadingEase * 100.0) / 100.0, 0.0001);
        assertEquals(s.fleschKincaidGrade,
                Math.round(s.fleschKincaidGrade * 100.0) / 100.0, 0.0001);
    }

    @Test
    public void testSingleWordSingleSentence() {
        // Minimum viable input — Math.max guards prevent division by zero
        Readability.ReadabilityScores s = compute("Hello.");
        assertEquals(1, s.sentences);
        assertEquals(1, s.words);
        assertTrue(s.syllables >= 1);
    }

    @Test
    public void testMultipleSentencesMultipleWords() {
        Readability.ReadabilityScores s =
                compute("She sells seashells. By the seashore.");
        assertEquals(2, s.sentences);
        assertEquals(6, s.words);
        assertTrue(s.syllables >= 6);
    }
    @Test
    public void testCountSentences_skipsEmptySplitPart() throws Exception {
        // ".Hello!" -> split gives ["", "Hello"]
        // empty part should NOT be counted, "Hello" should be counted
        int result = testMethod("countSentences", ".Hello!");
        assertEquals(1, result);
    }
    @Test
    public void testCountWords_trueBranch_nonEmptyWord() throws Exception {
        int result = testMethod("countWords", "one two three");
        assertEquals(3, result);
    }

    @Test
    public void testCountWords_falseBranch_emptyWord() throws Exception {
        // needed to hit the false side of:
        // if (!p.trim().isEmpty()) count++;
        int result = testMethod("countWords", "");
        assertEquals(0, result);
    }

    @Test
    public void testCountSyllables_skipsEmptyWordFragment() throws Exception {
        // "!!!" becomes spaces, then split can produce an empty fragment
        // which should hit: if (w.isEmpty()) continue;
        int result = testMethod("countSyllables", "!!!");
        assertEquals(0, result);
    }

    @Test
    public void testCountSyllables_nonEmptyWordFragment() throws Exception {
        int result = testMethod("countSyllables", "cat dog");
        assertEquals(2, result);
    }
    @Test
    public void testCountSyllables_emptyFragmentBranch() throws Exception {
        assertEquals(1, invokeCountSyllables(" a"));
    }



}