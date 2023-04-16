package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Suliman
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _setting = setting();

    }

    @Override
    boolean atNotch() {
        Alphabet a = alphabet();
        int s = permutation().wrap(setting() + getRingOffset());
        CharSequence seq = new StringBuilder(1).append(a.toChar(s));
        if (_notches.contains(seq)) {
            return true;
        }
        return false;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    String notches() {
        return _notches;
    }

    /** Rotor notches. */
    private String _notches;
    /** Current setting. */
    private int _setting;

}
