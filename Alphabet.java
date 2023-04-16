package enigma;

import java.util.ArrayList;


import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Suliman
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars.isEmpty()) {
            throw error("Alphabet must contain characters");
        }
        char[] charArray = chars.toCharArray();
        for (int i = 0; i < charArray.length; i += 1) {
            char currentChar = charArray[i];
            if (_charArrayList.contains(currentChar)) {
                throw error("Input contains duplicate characters", currentChar);
            }
            if (currentChar == ')') {
                throw error("Input contains illegal characters", currentChar);
            }
            if (currentChar == '(') {
                throw error("Input contains illegal characters", currentChar);
            }
            if (currentChar == '*') {
                throw error("Input contains illegal characters", currentChar);
            } else {
                _charArrayList.add(currentChar);
            }

        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _charArrayList.size();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _charArrayList.contains(ch);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return (char) _charArrayList.get(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        return _charArrayList.indexOf(ch);
    }

    /** Array List of characters passed into the constructer.**/
    private ArrayList<Character> _charArrayList = new ArrayList<>();

}
