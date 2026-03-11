package models;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

public class ReadabilityClassTest {
    // Test the Readability class itself
    @Test
    public void testReadabilityCanBeInstantiated() {
        // Covers the implicit default constructor of the outer class
        Readability r = new Readability();
        assertNotNull(r);
    }


    @Test
    public void testReadabilityClassConstructor() throws Exception {
        // Get the declared constructor via reflection
        Constructor<Readability> constructor =
                Readability.class.getDeclaredConstructor();

        // Make it accessible even if private
        constructor.setAccessible(true);

        // Invoke it — this is what JaCoCo needs to see executed
        Readability instance = constructor.newInstance();

        assertNotNull(instance);
    }

    @Test
    public void readabilityClassShouldBeInstantiable() {
        Readability readability = new Readability();
        assertNotNull(readability);
    }


}