package models;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class ReadabilityTest {

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
}