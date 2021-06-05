import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// TODO: make AutoCloseable
public class Analyzer {
	private       BufferedReader    bufferedReader;
	private       BufferedWriter    bufferedWriterVM;
	private final Tokenizer         tokenizer;
	private final CompilationEngine compilationEngine;
	private final VMWriter          vmWriter;

	public Analyzer(File inputFile, File outputFile) {
		try {
			this.bufferedReader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			this.bufferedWriterVM = new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.tokenizer = new Tokenizer(bufferedReader);
		this.vmWriter = new VMWriter(bufferedWriterVM);
		this.compilationEngine = new CompilationEngine(vmWriter);
	}

	public void analyze() {
		tokenizer.tokenize();
		compilationEngine.setTokenizer(tokenizer);
		compilationEngine.compile();
		close();
	}

	public void close() {
		try {
			bufferedReader.close();
			vmWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
