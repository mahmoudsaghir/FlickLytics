package models;

import org.junit.Test;
import java.lang.reflect.Constructor;
import static org.junit.Assert.*;


/**
 * Unit tests for the {@link Readability} class.
 *This test class focuses on constructor coverage for the outer
 * {@code Readability} utility class, including direct instantiation
 * and reflective construction for code coverage purposes.
 *
 * @author Zenghui WU
 */
public class ReadabilityClassTest {
    /**
     * Verifies that the {@link Readability} class can be instantiated
     * using its no-argument constructor.
     */
    @Test
    public void testReadabilityCanBeInstantiated() {
        // Covers the implicit default constructor of the outer class
        Readability r = new Readability();
        assertNotNull(r);
    }
    /**
     * Verifies that the declared constructor of {@link Readability}
     * can be accessed and invoked through reflection.
     *This test is useful for improving code coverage tools such as JaCoCo,
     * especially when constructor execution needs to be explicitly observed.
     *
     * @throws Exception if reflection fails to access or invoke the constructor
     */

    @Test
    public void testReadabilityClassConstructor() throws Exception {
        // Get the declared constructor via reflection
        Constructor<Readability> constructor =
                Readability.class.getDeclaredConstructor();

        // Make it accessible even if private
        constructor.setAccessible(true);
        Readability instance = constructor.newInstance();
        assertNotNull(instance);
    }
    /**
     * Verifies again that a {@link Readability} object can be created
     * successfully through normal instantiation.
     */
    @Test
    public void readabilityClassShouldBeInstantiable() {
        Readability readability = new Readability();
        assertNotNull(readability);
    }


}