import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Parser implements AutoCloseable {

	private final BufferedReader bufferedReader;
	private       List<String>   currentCommand;
	private       String         nextCommand;

	public Parser(File inputFile) throws IOException {
		bufferedReader = new BufferedReader(new FileReader(inputFile));
	}

	public boolean hasMoreCommands() throws IOException {
		nextCommand = bufferedReader.readLine();
		while (nextCommand != null &&
			   (nextCommand.isBlank() || nextCommand.trim().startsWith("//"))) {
			nextCommand = bufferedReader.readLine();
		}
		return nextCommand != null;
	}

	public void advance() {
		if (nextCommand.contains("//")) {
			// ignore comments that start mid-line
			currentCommand = Arrays.asList(nextCommand.substring(0, nextCommand.indexOf("//")).trim().split(" "));
		} else {
			currentCommand = Arrays.asList(nextCommand.trim().split(" "));
		}
		System.out.println(currentCommand);
	}

	public Command commandType() {
		switch (currentCommand.get(0)) {
			case "push":
				return Command.C_PUSH;
			case "pop":
				return Command.C_POP;
			case "label":
				return Command.C_LABEL;
			case "goto":
				return Command.C_GOTO;
			case "if-goto":
				return Command.C_IF;
			case "function":
				return Command.C_FUNCTION;
			case "call":
				return Command.C_CALL;
			case "return":
				return Command.C_RETURN;
			default:
				return Command.C_ARITHMETIC;
		}
	}

	public String arg1() {
		// return first argument of the current command
		// if C_ARITHMETIC, return the command
		// C_RETURN does not take any arguments
		if (commandType().equals(Command.C_RETURN)) {
			return null;
		}
		if (commandType().equals(Command.C_ARITHMETIC)) {
			return currentCommand.get(0);
		}
		return currentCommand.get(1);
	}

	public Integer arg2() {
		// return second argument of the current command
		// C_PUSH, C_POP, C_FUNCTION, and C_CALL
		// are the only commands that take two arguments
		switch (commandType()) {
			case C_PUSH:
			case C_POP:
			case C_FUNCTION:
			case C_CALL:
				return Integer.valueOf(currentCommand.get(2));
			default:
				return null;
		}
	}

	@Override
	public void close() throws IOException {
		bufferedReader.close();
	}
}
