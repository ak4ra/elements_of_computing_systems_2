package nand2tetris.compiler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (!(args.length > 0)) {

            System.out.println("please provide a file argument");
            System.exit(1);
        }

        File fileOrDirectory = new File(args[0]);

        if (fileOrDirectory.exists()) {

            if (fileOrDirectory.isDirectory()) {

                List<File> filesClassesList = Arrays.asList(fileOrDirectory.listFiles());

                for (File currentFile : filesClassesList) {

                    if (currentFile.toString().endsWith(".jack")) {

                        Analyzer analyzer = new Analyzer(currentFile);
                        analyzer.analyze();
                    }
                }

            } else if (fileOrDirectory.isFile()) {

                if (fileOrDirectory.toString().endsWith(".jack")) {

                    Analyzer analyzer = new Analyzer(fileOrDirectory);
                    analyzer.analyze();

                } else {

                    System.out.println("the specified file is invalid");
                    System.exit(1);
                }
            }

        } else {

            System.out.println("the specified file/directory does not exist");
            System.exit(1);
        }
    }
}