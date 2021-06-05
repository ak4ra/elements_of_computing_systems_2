package nand2tetris.compiler;

import java.io.*;

public class Analyzer {

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriterVM;
    private Tokenizer tokenizer;
    private CompilationEngine compilationEngine;
    private VMWriter vmWriter;

    public Analyzer(File inputFile) {

        try {

            this.bufferedReader = new BufferedReader(new FileReader(inputFile));

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

        try {

            this.bufferedWriterVM = new BufferedWriter(new FileWriter(new File(inputFile.toString().replace(".jack", ".vm"))));

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
