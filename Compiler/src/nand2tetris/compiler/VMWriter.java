package nand2tetris.compiler;

import java.io.BufferedWriter;
import java.io.IOException;

public class VMWriter {

    private BufferedWriter bufferedWriter;

    public VMWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    private void writeLine(String line) {

        try {

            bufferedWriter.write(String.format("%s\n", line));

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void writePush(String segment, Integer index) {

        writeLine(String.format("push %s %d", segment, index));
    }

    public void writePop(String segment, Integer index) {

        writeLine(String.format("pop %s %d", segment, index));
    }

    public void writeArithmetic(String command) {

        writeLine(String.format("%s", command));
    }

    public void writeLabel(String label) {

        writeLine(String.format("label %s", label));
    }

    public void writeGoto(String label) {

        writeLine(String.format("goto %s", label));
    }

    public void writeIf(String label) {

        writeLine(String.format("if-goto %s", label));
    }

    public void writeCall(String name, Integer nArgs) {

        writeLine(String.format("call %s %d", name, nArgs));
    }

    public void writeFunction(String name, Integer nLocals) {

        writeLine(String.format("function %s %d", name, nLocals));
    }

    public void writeReturn() {

        writeLine("return");
    }

    public void close() {

        try {

            bufferedWriter.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
