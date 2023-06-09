package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Suliman
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine mainMachine = readConfig();

        if (!_input.hasNext("\\*")) {
            throw error("Input didn't start with a ");
        }
        while (_input.hasNext() && _input.hasNextLine()) {
            String line = _input.nextLine();
            if (line.equals("")) {
                line = _input.nextLine();
                _output.println();
            }
            setUp(mainMachine, line);
            String convertedMsg = "";
            while (_input.hasNextLine()) {
                if (!_input.hasNext("\\*")) {
                    convertedMsg = mainMachine.convert(_input.nextLine());
                    printMessageLine(convertedMsg);
                } else {
                    break;
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (_config.hasNext("\\d")) {
                throw error("there wasn't an alphabet");
            }
            _alphabet = new Alphabet(_config.next());
            if (!_config.hasNext("\\d")) {
                throw error("Didn't indicate number of rotors");
            }
            int numRotors = Integer.parseInt(_config.next());
            if (!_config.hasNext("\\d")) {
                throw error("Didn't indicate number of pawls");
            }
            int numPawls = Integer.parseInt(_config.next());
            Collection<Rotor> allRoters = new ArrayList<>();
            while (_config.hasNext()) {
                allRoters.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRoters);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorName = _config.next();
            String notches = _config.next();
            String cycles = "";
            while (_config.hasNext("\\([a-zA-Z\\d\\-_.)(]*\\)")) {
                cycles += _config.next();
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            if (notches.startsWith("M")) {
                return new MovingRotor(rotorName, perm, notches.substring(1));
            } else if (notches.startsWith("N")) {
                return new FixedRotor(rotorName, perm);
            } else if (notches.startsWith("R")) {
                return new Reflector(rotorName, perm);
            } else {
                throw error("Bad Rotor type");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner settingScan = new Scanner(settings);
        if (!settingScan.next().equals("*")) {
            throw error("Setting must start with an asterisk ");
        }
        String cycles = "";
        ArrayList<String> arrayofRotors = new ArrayList<String>();
        for (int i = 0; settingScan.hasNext(); i += 1) {
            if (settingScan.hasNext("\\([a-zA-Z\\d)(]*\\)")) {
                cycles += settingScan.next();
            } else {
                arrayofRotors.add(settingScan.next());
            }
        }
        Permutation plugboardperm = new Permutation(cycles, _alphabet);
        M.setPlugboard(plugboardperm);
        int arraysize = arrayofRotors.size();
        String rotorsettings = arrayofRotors.get(arraysize - 1);
        String rotorsettings2 = arrayofRotors.get(arraysize - 2);
        if (!M.getListofRotors().containsKey(rotorsettings2)) {
            arrayofRotors.remove(arraysize - 1);
            arrayofRotors.remove(arraysize - 2);
            Object[] listObj = arrayofRotors.toArray();
            String[] r = Arrays.copyOf(listObj, listObj.length, String[].class);
            M.insertRotors(r);
            M.setRotors(rotorsettings2);
            M.setRotorsRings(rotorsettings);
        } else {
            arrayofRotors.remove(arraysize - 1);
            Object[] listObj = arrayofRotors.toArray();
            String[] r = Arrays.copyOf(listObj, listObj.length, String[].class);
            M.insertRotors(r);
            M.setRotors(rotorsettings);
        }


    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        char[] msgArray = msg.toCharArray();
        String msgBlock = "";
        for (int i = 0; i < msgArray.length; i += 1) {
            if (msgBlock.length() == 5) {
                _output.print(msgBlock);
                _output.print(" ");
                msgBlock = "";
            }
            msgBlock +=  msgArray[i];
        }
        if (!msgBlock.isEmpty()) {
            _output.print(msgBlock);
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
