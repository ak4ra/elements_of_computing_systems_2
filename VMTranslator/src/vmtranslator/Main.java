package vmtranslator;

import java.io.File;

public class Main {

    private static void parseWrite(CodeWriter codeWriter, File file) {
        File readFile = new File(String.valueOf(file));
        Parser parser = new Parser(readFile);

        codeWriter.setCurrentFileName(file.getName());

        while (parser.hasMoreCommands()) {
            parser.advance();

            if (parser.commandType().equals(Command.C_ARITHMETIC)) {
                codeWriter.writeArithmetic(parser.arg1());
            } else if (parser.commandType().equals(Command.C_PUSH)) {
                codeWriter.writePushPop(Command.C_PUSH, parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(Command.C_POP)) {
                codeWriter.writePushPop(Command.C_POP, parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(Command.C_LABEL)) {
                codeWriter.writeLabel(parser.arg1());
            } else if (parser.commandType().equals(Command.C_GOTO)) {
                codeWriter.writeGoto(parser.arg1());
            } else if (parser.commandType().equals(Command.C_IF)) {
                codeWriter.writeIf(parser.arg1());
            } else if (parser.commandType().equals(Command.C_FUNCTION)) {
                codeWriter.writeFunction(parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(Command.C_RETURN)) {
                codeWriter.writeReturn();
            } else if (parser.commandType().equals(Command.C_CALL)) {
                codeWriter.writeCall(parser.arg1(), parser.arg2());
            }
        }
        parser.close();
    }

    public static void main(String[] args) {
        if (!(args.length > 0)) {
            System.out.println("please provide a file argument");
            System.exit(1);
        }

        File fileOrDirectory = new File(args[0]);
        String outputFileName;

        if (fileOrDirectory.exists()) {
            // initialize the code writer
            if (fileOrDirectory.toString().endsWith(".vm")) {
                outputFileName = fileOrDirectory.toString().replace(".vm", ".asm");
            } else {
                outputFileName = fileOrDirectory.toString() + ".asm";
            }

            CodeWriter codeWriter = new CodeWriter(new File(outputFileName));
            codeWriter.writeInit();

            if (fileOrDirectory.isDirectory()) {
                for (File currentFile : fileOrDirectory.listFiles()) {
                    if (currentFile.toString().endsWith(".vm")) {
                        parseWrite(codeWriter, currentFile);
                    }
                }
            } else if (fileOrDirectory.isFile()) {
                if (fileOrDirectory.toString().endsWith(".vm")) {
                    parseWrite(codeWriter, fileOrDirectory);
                } else {
                    System.out.println("the file specified is invalid");
                    System.exit(1);
                }
            }
            codeWriter.close();
        } else {
            System.out.println("the specified file/directory does not exist");
            System.exit(1);
        }
    }
}
