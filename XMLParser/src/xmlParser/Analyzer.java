package xmlParser;

import java.io.*;

public class Analyzer {

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Tokenizer tokenizer;
    private CompilationEngine compilationEngine;

    public Analyzer(File inputFile) {

        try {

            this.bufferedReader = new BufferedReader(new FileReader(inputFile));

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

        try {

            this.bufferedWriter = new BufferedWriter(new FileWriter(new File(inputFile.toString().replace(".jack", "_output.xml"))));

        } catch (IOException e) {

            e.printStackTrace();
        }

        this.tokenizer = new Tokenizer(bufferedReader);
        this.compilationEngine = new CompilationEngine(bufferedWriter);
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
            bufferedWriter.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
