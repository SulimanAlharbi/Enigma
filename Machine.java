package enigma;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Suliman
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRoters = numRotors;
        _pawls = pawls;

        if (numRotors <= 1) {
            throw error("Number of Rotors must be greater than 1");
        } else if (0 > pawls || pawls >= numRotors) {
            throw error("pawls must be 0 <= PAWLS < NUMROTORS pawls");
        } else {
            Object[] allrotorsarray = allRotors.toArray();
            for (int i = 0; i < allrotorsarray.length; i += 1) {
                Rotor currentRoter = (Rotor) allrotorsarray[i];
                String rotorName = currentRoter.name();
                _listOfrotors.put(rotorName, currentRoter);

            }
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRoters;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return (Rotor) _machineRoters.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _machineRoters.clear();
        int numMovingRotors = 0;
        for (int i = 0; i < rotors.length; i += 1) {
            String nameOfRotor = rotors[i];
            if (!_listOfrotors.containsKey(nameOfRotor)) {
                throw error("Rotor isn't a valid rotor", nameOfRotor);
            } else {
                Rotor currentRotor = (Rotor) _listOfrotors.get(nameOfRotor);
                if (i == 0 && !currentRotor.reflecting()) {
                    throw error("reflector must be in the first position");
                } else if (i != 0 && currentRotor.reflecting()) {
                    throw error("reflector must be in the first position");
                } else if (_machineRoters.contains(currentRotor)) {
                    throw error("Rotor is already in machine");
                } else if (currentRotor.rotates()) {
                    numMovingRotors += 1;
                    _machineRoters.add(currentRotor);
                    if (numMovingRotors > _pawls) {
                        throw error("more Rotors than pawls");
                    }
                } else {
                    _machineRoters.add(currentRotor);
                }

            }

        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != (numRotors() - 1)) {
            throw error("setting is of incorrect size");
        } else {
            char[] settingArray = setting.toCharArray();
            for (int i = 0; i < settingArray.length; i += 1) {
                char currentRotorSetting = settingArray[i];
                if (!_alphabet.contains(currentRotorSetting)) {
                    throw error("invalid setting");
                }
                Rotor currentRotor = (Rotor) _machineRoters.get(i + 1);
                currentRotor.set(currentRotorSetting);

            }
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        int[] advacingcommands = new int[_machineRoters.size() -  1];
        for (int i = _machineRoters.size() - 1; i > 0; i -= 1) {
            Rotor currentRotor = (Rotor) _machineRoters.get(i);
            Rotor nextRotor = (Rotor) _machineRoters.get(i - 1);
            if (currentRotor.atNotch() && nextRotor.rotates()) {
                advacingcommands[i - 1] = 1;
                advacingcommands[i - 2] = 1;
            } else if (i == _machineRoters.size() - 1) {
                currentRotor.advance();
            }
        }
        for (int i = advacingcommands.length - 1; i >= 0; i -= 1) {
            if (advacingcommands[i] == 1) {
                Rotor currentRotor = (Rotor) _machineRoters.get(i + 1);
                currentRotor.advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _machineRoters.size() - 1; i >= 0; i -= 1) {
            Rotor currentRotor = (Rotor) _machineRoters.get(i);
            c = currentRotor.convertForward(c);
        }
        for (int i = 1; i < _machineRoters.size(); i += 1) {
            Rotor currentRotor = (Rotor) _machineRoters.get(i);
            c = currentRotor.convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll("\\s", "");
        char[] msgArray = msg.toCharArray();
        char[] result = new char[msgArray.length];
        for (int i = 0; i < msgArray.length; i += 1) {
            char currentChar = msgArray[i];
            int indexOfChar = alphabet().toInt(currentChar);
            int resultindex = convert(indexOfChar);
            result[i] = alphabet().toChar(resultindex);
        }

        return String.valueOf(result);
    }

    /** Returns all possiable rotors.*/
    public HashMap getListofRotors() {
        return _listOfrotors;
    }

    void setRotorsRings(String ringSetting) {
        if (ringSetting.length() != (numRotors() - 1)) {
            throw error("setting is of incorrect size");
        } else {
            char[] settingArray = ringSetting.toCharArray();
            for (int i = 0; i < settingArray.length; i += 1) {
                char currentRotorSetting = settingArray[i];
                Rotor currentRotor = (Rotor) _machineRoters.get(i + 1);
                currentRotor.setRing(currentRotorSetting);

            }
        }

    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of rotors. */
    private int _numRoters;
    /** Number of Pawls. */
    private int _pawls;
    /** Plugboard Permutation. */
    private Permutation _plugboard;
    /** List of all rotors. */
    private HashMap<String, Rotor> _listOfrotors = new HashMap<String, Rotor>();
    /** List of rotors used. */
    private ArrayList<Rotor> _machineRoters = new ArrayList<Rotor>();
}
