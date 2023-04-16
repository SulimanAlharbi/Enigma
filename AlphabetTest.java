package enigma;

import org.junit.Test;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;


public class AlphabetTest {

    @Test
    public void constructorTest() {
        Alphabet consTest1 = new Alphabet("SBDA");
        Alphabet consTest2 = new Alphabet("SBDA12_");
        Alphabet consTest3 = new Alphabet("SBDAs01");
        assertTrue(consTest1.contains('S'));
        assertTrue(consTest1.contains('A'));
        assertTrue(consTest2.contains('_'));
        assertTrue(consTest3.contains('0'));

        assertEquals(7, consTest2.size());
        assertEquals(7, consTest3.size());

        assertEquals('S', consTest1.toChar(0));
        assertEquals('A', consTest1.toChar(3));
        assertEquals('s', consTest3.toChar(4));
        assertEquals('_', consTest2.toChar(6));

        assertEquals(0, consTest1.toInt('S'));
        assertEquals(3, consTest1.toInt('A'));
        assertEquals(4, consTest3.toInt('s'));
        assertEquals(6, consTest2.toInt('_'));
    }
    @Test(expected = EnigmaException.class)
    public void constructorExceptionTest() {
        Alphabet consTest1 = new Alphabet("ABDASJ");
        Alphabet consTest2 = new Alphabet("sgYYdh0");
        Alphabet consTest3 = new Alphabet("sGHy19)");
        Alphabet consTest4 = new Alphabet("*");
        Alphabet consTest5 = new Alphabet("hHgGjJ99*");


    }


}
