package enigma;

import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Suliman
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        addCycle(cycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        char[] cycleArray = cycle.toCharArray();
        char firstChar = 0;
        boolean inCycle = false;
        for (int i = 0; i < cycleArray.length - 1; i += 1) {
            char currentChar = cycleArray[i];
            char nextChar = cycleArray[i + 1];
            if (currentChar == '(') {
                firstChar = nextChar;
                inCycle = true;

            } else if (currentChar == ')') {
                continue;

            } else if (_cycles.containsKey(currentChar)) {
                throw error("Character was already mapped in a cycle");

            } else if (nextChar == ')') {
                _cycles.put(currentChar, firstChar);
                _inverseCycles.put(firstChar, currentChar);
                inCycle = false;

            } else if (currentChar == ' ') {
                if (inCycle) {
                    throw error("Whitespace found in cycle", currentChar);
                }

            } else if (!_alphabet.contains(currentChar)) {
                throw error("Character not found in alphabet", currentChar);
            } else {
                if (inCycle) {
                    _cycles.put(currentChar, nextChar);
                    _inverseCycles.put(nextChar, currentChar);
                } else {
                    throw error("illegal character out side cycles");
                }


            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        if (p >= size()) {
            p = wrap(p);
        }
        char charAtP = _alphabet.toChar(p);
        char permutedChar = charAtP;
        if (_cycles.containsKey(charAtP)) {
            permutedChar = permute(charAtP);
        }
        return _alphabet.toInt(permutedChar);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        if (c >= size()) {
            c = wrap(c);
        }
        char charAtP = _alphabet.toChar(c);
        char permutedChar = charAtP;
        if (_inverseCycles.containsKey(charAtP)) {
            permutedChar = invert(charAtP);
        }
        return _alphabet.toInt(permutedChar);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (_cycles.containsKey(p)) {
            return (char) _cycles.get(p);
        } else {
            return p;
        }
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (_inverseCycles.containsKey(c)) {
            return (char) _inverseCycles.get(c);
        } else {
            return c;
        }
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < size(); i += 1) {
            char currentChar = _alphabet.toChar(i);
            if (_cycles.containsKey(currentChar)) {
                if (_cycles.get(currentChar).equals(currentChar)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** Cycle mapping. */
    private HashMap<Character, Character> _cycles = new HashMap<>();
    /** Inverse cycle mapping. */
    private HashMap<Character, Character> _inverseCycles = new HashMap<>();

}
