import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Compiler {
	static final Path OUTPUT_DIRECTORY = Paths.get("")
											  .toAbsolutePath()
											  .resolve("compiled_files");

	public static void main(String[] args) {
		if (args.length < 1) {
			error("please provide a file or directory argument");
		}

		File inputFileOrDirectory = new File(args[0]);
		if (!inputFileOrDirectory.exists()) {
			error("the specified file or directory does not exist");
		}

		// delete all files in the output directory
		for (File f : new File(String.valueOf(OUTPUT_DIRECTORY)).listFiles()) {
			f.delete();
		}

		if (inputFileOrDirectory.isDirectory()) {
			List<File> filesClassesList = Arrays.asList(inputFileOrDirectory.listFiles());
			for (File currentFile : filesClassesList) {
				if (currentFile.getName().endsWith(".jack")) {
					String outputFileName = currentFile.getName().replace(".jack", ".vm");
					File outputFile = new File(String.valueOf(Paths.get(OUTPUT_DIRECTORY.toString(), outputFileName)));
					Analyzer analyzer = new Analyzer(currentFile, outputFile);
					analyzer.analyze();
				}
			}
		} else {
			if (!inputFileOrDirectory.getName().endsWith(".jack")) {
				error("the specified file is invalid");
			}
			String outputFileName = inputFileOrDirectory.getName().replace(".jack", ".vm");
			File outputFile = new File(String.valueOf(Paths.get(OUTPUT_DIRECTORY.toString(), outputFileName)));

			Analyzer analyzer = new Analyzer(inputFileOrDirectory, outputFile);
			analyzer.analyze();
		}
	}

	private static void error(String errorMessage) {
		System.out.println(errorMessage);
		System.exit(1);
	}
}
