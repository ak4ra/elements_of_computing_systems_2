import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Translator {
	static final Path OUTPUT_DIRECTORY = Paths.get("")
											  .toAbsolutePath()
											  .resolve("translated_files");

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			error("please provide a file or directory argument");
		}

		File fileOrDirectory = new File(args[0]);
		if (!fileOrDirectory.exists()) {
			error("the specified file or directory does not exist");
		}

		String outputFileName;
		if (fileOrDirectory.isDirectory()) {
			outputFileName = fileOrDirectory.getName() + ".asm";
		} else {
			if (!fileOrDirectory.getName().endsWith(".vm")) {
				error("the file specified is invalid");
			}
			outputFileName = fileOrDirectory.getName().replace(".vm", ".asm");
		}
		Path outputFile = Paths.get(OUTPUT_DIRECTORY.toString(), outputFileName);

		// delete all files in the output directory
		for (File f : new File(String.valueOf(OUTPUT_DIRECTORY)).listFiles()) {
			f.delete();
		}

		try (CodeWriter codeWriter = new CodeWriter(new File(outputFile.toString()))) {
			codeWriter.writeInit();

			if (fileOrDirectory.isDirectory()) {
				for (File currentFile : fileOrDirectory.listFiles()) {
					if (currentFile.getName().endsWith(".vm")) {
						parseWrite(codeWriter, currentFile);
					}
				}
			} else if (fileOrDirectory.isFile()) {
				parseWrite(codeWriter, fileOrDirectory);
			}
		}
	}

	private static void parseWrite(CodeWriter codeWriter, File file) throws IOException {
		File readFile = new File(String.valueOf(file));
		try (Parser parser = new Parser(readFile)) {
			codeWriter.setCurrentFileName(file.getName());

			while (parser.hasMoreCommands()) {
				parser.advance();

				switch (parser.commandType()) {
					case C_ARITHMETIC:
						codeWriter.writeArithmetic(parser.arg1());
						break;
					case C_PUSH:
						codeWriter.writePushPop(Command.C_PUSH, parser.arg1(), parser.arg2());
						break;
					case C_POP:
						codeWriter.writePushPop(Command.C_POP, parser.arg1(), parser.arg2());
						break;
					case C_LABEL:
						codeWriter.writeLabel(parser.arg1());
						break;
					case C_GOTO:
						codeWriter.writeGoto(parser.arg1());
						break;
					case C_IF:
						codeWriter.writeIf(parser.arg1());
						break;
					case C_FUNCTION:
						codeWriter.writeFunction(parser.arg1(), parser.arg2());
						break;
					case C_RETURN:
						codeWriter.writeReturn();
						break;
					case C_CALL:
						codeWriter.writeCall(parser.arg1(), parser.arg2());
						break;
					default:
						break;
				}
			}
		}
	}

	private static void error(String errorMessage) {
		System.out.println(errorMessage);
		System.exit(1);
	}
}
