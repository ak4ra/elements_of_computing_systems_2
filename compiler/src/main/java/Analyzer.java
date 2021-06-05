import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Analyzer implements AutoCloseable {
	private final BufferedReader    bufferedReader;
	private final BufferedWriter    bufferedWriterVM;
	private final Tokenizer         tokenizer;
	private final CompilationEngine compilationEngine;
	private final VMWriter          vmWriter;

	public Analyzer(File inputFile, File outputFile) throws IOException {
		this.bufferedReader = new BufferedReader(new FileReader(inputFile));
		this.bufferedWriterVM = new BufferedWriter(new FileWriter(outputFile));
		this.tokenizer = new Tokenizer(bufferedReader);
		this.vmWriter = new VMWriter(bufferedWriterVM);
		this.compilationEngine = new CompilationEngine(vmWriter);
	}

	public void analyze() throws IOException {
		tokenizer.tokenize();
		compilationEngine.setTokenizer(tokenizer);
		compilationEngine.compile();
		close();
	}

	@Override
	public void close() throws IOException {
		bufferedReader.close();
		bufferedWriterVM.close();
		vmWriter.close();
	}
}
