package vmtranslator;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static vmtranslator.Command.*;

public class Parser {

    private BufferedReader bufferedReader;
    private List<String> currentCommand;
    private String nextCommand;

    public Parser(File inputFile) {
        try {
            this.bufferedReader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Boolean hasMoreCommands() {
        try {
            nextCommand = bufferedReader.readLine();
            while (nextCommand != null
                    && (nextCommand.trim().length() == 0 || nextCommand.trim().substring(0, 2).equals("//"))) {
                nextCommand = bufferedReader.readLine();
            }
            return nextCommand != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void advance() {
        // remove comments
        if (nextCommand.indexOf("/") != -1) {
            currentCommand = Arrays.asList(nextCommand.substring(0, nextCommand.indexOf("/")).trim().split(" "));
        } else {
            currentCommand = Arrays.asList(nextCommand.trim().split(" "));
        }
        System.out.println(currentCommand);
    }

    public Command commandType() {
        // return the type of the current VM command
        // C_ARITHMETIC is returned for all the arithmetic commands
        switch (currentCommand.get(0)) {
        case "push":
            return C_PUSH;
        case "pop":
            return C_POP;
        case "label":
            return C_LABEL;
        case "goto":
            return C_GOTO;
        case "if-goto":
            return C_IF;
        case "function":
            return C_FUNCTION;
        case "call":
            return C_CALL;
        case "return":
            return C_RETURN;
        default:
            return C_ARITHMETIC;
        }
    }

    public String arg1() {
        // return first argument of the current command
        // if C_ARITHMETIC, return the command
        // should not be called if current command is C_RETURN
        if (commandType().equals(C_ARITHMETIC)) {
            return currentCommand.get(0);
        } else {
            return currentCommand.get(1);
        }
    }

    public Integer arg2() {
        // return second argument of the current command
        // call only if current command is C_PUSH, C_POP, C_FUNCTION, or C_CALL
        return Integer.valueOf(currentCommand.get(2));
    }

    public void close() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
