package vmtranslator;

import java.io.*;
import java.util.Random;

import static vmtranslator.Command.C_PUSH;

public class CodeWriter {

    private BufferedWriter bufferedWriter;
    private String currentFileName;
    private static final Random random = new Random();

    public CodeWriter(File outputFile) {

        try {

            this.bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void setCurrentFileName(String currentFileName) {

        this.currentFileName = currentFileName;
        writeLine(String.format("// ------------ %s ------------", currentFileName));
    }

    public void writeLine(String text) {

        try {

            if (text != null) {

                bufferedWriter.write(String.format("%s\n", text));

            } else {

                bufferedWriter.newLine();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void popStackInD() {

        writeLine("@SP");
        writeLine("M=M-1");
        writeLine("A=M");
        writeLine("D=M");
    }

    public void pushDtoStack() {

        writeLine("@SP");
        writeLine("M=M+1");
        writeLine("A=M-1");
        writeLine("M=D");
    }

    public void writeArithmetic(String command) {

        Integer randomInteger = random.nextInt();

        // page 67/80
        writeLine(String.format("// %s", command));
        switch(command) {
            case "add":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=M+D");
                writeLine(null);
                break;
            case "sub":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=M-D");
                writeLine(null);
                break;
            case "neg":
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=-M");
                writeLine(null);
                break;
            case "eq":
                //The VM represents true and false as -1 and 0 respectively
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("D=M-D");
                writeLine(String.format("@EQ%d", randomInteger));
                writeLine("D;JEQ");
                writeLine("D=0");
                writeLine(String.format("@RESULT%d", randomInteger));
                writeLine("0;JMP");
                writeLine(String.format("(EQ%d)", randomInteger));
                writeLine("D=-1");
                writeLine(String.format("(RESULT%d)", randomInteger));
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=D");
                writeLine(null);
                break;
            case "gt":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("D=M-D");
                writeLine(String.format("@GT%d", randomInteger));
                writeLine("D;JGT");
                writeLine("D=0");
                writeLine(String.format("@RESULT%d", randomInteger));
                writeLine("0;JMP");
                writeLine(String.format("(GT%d)", randomInteger));
                writeLine("D=-1");
                writeLine(String.format("(RESULT%d)", randomInteger));
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=D");
                writeLine(null);
                break;
            case "lt":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("D=M-D");
                writeLine(String.format("@LT%d", randomInteger));
                writeLine("D;JLT");
                writeLine("D=0");
                writeLine(String.format("@RESULT%d", randomInteger));
                writeLine("0;JMP");
                writeLine(String.format("(LT%d)", randomInteger));
                writeLine("D=-1");
                writeLine(String.format("(RESULT%d)", randomInteger));
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=D");
                writeLine(null);
                break;
            case "and":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=D&M");
                writeLine(null);
                break;
            case "or":
                popStackInD();
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=D|M");
                writeLine(null);
                break;
            case "not":
                writeLine("@SP");
                writeLine("A=M-1");
                writeLine("M=!M");
                writeLine(null);
                break;
            default:
                break;
        }
    }

    public void writePushPop(Command command, String segment, Integer index) {

        writeLine(String.format("// %s %s %d", command, segment, index));

        if (command.equals(C_PUSH)) {

            if (segment.equals("constant")) {

                writeLine(String.format("@%d", index));
                writeLine("D=A");

            } else if (segment.equals("static")) {

                writeLine(String.format("@%s", segmentResolver(segment, index)));
                writeLine("D=M");

            } else {

                if (index > 0) {

                    writeLine(String.format("@%d", index));
                    writeLine("D=A");
                }

                writeLine(String.format("@%s", segmentResolver(segment, index)));

                if (segment.equals("temp") || segment.equals("pointer")) {

                    if (index > 0) {

                        writeLine("A=D+A");

                    }

                } else {

                    if (index > 0) {

                        writeLine("A=M+D");

                    } else {

                        writeLine(String.format("A=M"));
                    }
                }

                writeLine("D=M");
            }

            pushDtoStack();
            writeLine(null);

        } else if (command.equals(Command.C_POP)) {

            if (segment.equals("static")) {

                popStackInD();
                writeLine(String.format("@%s", segmentResolver(segment, index)));

            } else {

                if (segment.equals("temp") || segment.equals("pointer")) {

                    writeLine(String.format("@%s", segmentResolver(segment, index)));
                    writeLine("D=A");

                } else {

                    writeLine(String.format("@%s", segmentResolver(segment, index)));
                    writeLine("D=M");
                }

                writeLine(String.format("@%d", index));
                writeLine("D=D+A");
                // storing segment + index address in R13
                writeLine("@R13");
                writeLine("M=D");

                popStackInD();

                writeLine("@R13");
                writeLine("A=M");
            }

            writeLine("M=D");
            writeLine(null);
        }
    }

    public String segmentResolver(String segment, Integer index) {

        // page 142-143

        if (segment.equals("local")) {
            return "LCL";
        } else if (segment.equals("argument")) {
            return "ARG";
        } else if (segment.equals("this")) {
            return "THIS";
        } else if (segment.equals("that")) {
            return "THAT";
        } else if (segment.equals("temp")) {
            return "5";
        } else if (segment.equals("pointer")) {
            return "3";
        } else if (segment.equals("static")) {
            return String.format("%s.%d", currentFileName, index);
        }

        return segment;
    }

    public void writeInit() {

        // initialize the VM
        writeLine("// initializing");

        // set SP to RAM[256]
        writeLine("@256");
        writeLine("D=A");
        writeLine("@SP");
        writeLine("M=D");

        // call Sys.init
        writeCall("Sys.init", 0);
    }

    public void writeLabel(String label) {

        /*
        label label - This command labels the current location in the function’s code.
        Only labeled locations can be jumped to from other parts of the program. The scope
        of the label is the function in which it is defined. The label is an arbitrary string
        composed of any sequence of letters, digits, underscore (_), dot (.), and colon (:) that
        does not begin with a digit.
        */

        writeLine(String.format("// label %s", label));
        writeLine(String.format("(%s)", label));
        writeLine(null);
    }

    public void writeGoto(String label) {

        /*
        goto label - This command effects an unconditional goto operation,
        causing execution to continue from the location marked by the label.
        The jump destination must be located in the same function.
        */

        writeLine(String.format("// goto %s", label));
        writeLine(String.format("@%s", label));
        writeLine("0;JMP");
        writeLine(null);
    }

    public void writeIf(String label) {

        /*
        if-goto label - This command effects a conditional goto operation. The stack’s
        topmost value is popped; if the value is not zero, execution continues from the location
        marked by the label; otherwise, execution continues from the next command in
        the program. The jump destination must be located in the same function.
        */

        writeLine(String.format("// if-goto %s", label));
        popStackInD();
        // jump if not zero
        writeLine(String.format("@%s", label));
        writeLine("D;JNE");
        writeLine(null);
    }

    public void writeCall(String functionName, Integer numArgs) {

        /*
        push return-address -
        push LCL -
        push ARG -
        push THIS -
        push THAT -
        ARG = SP-n-5 -
        LCL = SP -
        goto f -
        (return-address) -
        */

        Integer randomInteger = Math.abs(random.nextInt());

        writeLine(String.format("// call %s %d", functionName, numArgs));

        // push return-address
        // create new symbol and get address
        // the return label is a random integer
        writeLine(String.format("@R%d", randomInteger));
        writeLine("D=A");

        pushDtoStack();

        // push LCL, ARG, THIS, and THAT on the stack
        writeLine("@LCL");
        writeLine("D=M");
        pushDtoStack();
        writeLine("@ARG");
        writeLine("D=M");
        pushDtoStack();
        writeLine("@THIS");
        writeLine("D=M");
        pushDtoStack();
        writeLine("@THAT");
        writeLine("D=M");
        pushDtoStack();

        // ARG = SP-n-5
        writeLine("@SP");
        writeLine("D=M");
        writeLine(String.format("@%d", numArgs));
        writeLine("D=D-A");
        writeLine("@5");
        writeLine("D=D-A");
        writeLine("@ARG");
        writeLine("M=D");

        // LCL = SP
        writeLine("@SP");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        writeGoto(functionName);

        // the return label is a random integer
        writeLabel(String.format("R%d", randomInteger));
        writeLine(null);
    }

    public void writeReturn() {

        /*
        FRAME = LCL
        RET = *(FRAME-5)
        *ARG = pop()
        SP = ARG+1
        THAT = *(FRAME-1)
        THIS = *(FRAME-2)
        ARG = *(FRAME-3)
        LCL = *(FRAME-4)
        goto RET
         */

        writeLine("// return");

        // FRAME = LCL
        writeLine("@LCL");
        writeLine("D=M");
        writeLine("@FRAME");
        writeLine("M=D");

        // RET = *(FRAME-5)
        writeLine("@5");
        writeLine("A=D-A");
        writeLine("D=M");
        writeLine("@RET");
        writeLine("M=D");

        // *ARG = pop()
        popStackInD();
        writeLine("@ARG");
        writeLine("A=M");
        writeLine("M=D");

        // SP = ARG+1
        writeLine("@ARG");
        writeLine("D=M");
        writeLine("@SP");
        writeLine("M=D+1");

        // THAT = *(FRAME-1)
        writeLine("@FRAME");
        writeLine("A=M-1");
        writeLine("D=M");
        writeLine("@THAT");
        writeLine("M=D");

        // THIS = *(FRAME-2)
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@2");
        writeLine("D=D-A");
        writeLine("A=D");
        writeLine("D=M");
        writeLine("@THIS");
        writeLine("M=D");

        // ARG = *(FRAME-3)
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@3");
        writeLine("D=D-A");
        writeLine("A=D");
        writeLine("D=M");
        writeLine("@ARG");
        writeLine("M=D");

        // LCL = *(FRAME-4)
        writeLine("@FRAME");
        writeLine("D=M");
        writeLine("@4");
        writeLine("D=D-A");
        writeLine("A=D");
        writeLine("D=M");
        writeLine("@LCL");
        writeLine("M=D");

        // goto return address (in the caller's code)
        writeLine("@RET");
        writeLine("A=M");
        writeLine("0;JMP");

        writeLine(null);
    }

    public void writeFunction(String functionName, Integer numLocals) {

        writeLine(String.format("// function %s %d\n", functionName, numLocals));
        writeLabel(functionName);
        for (int i = 0; i < numLocals; i++) {
            writePushPop(C_PUSH, "constant", 0);
        }

        writeLine(null);
    }

    public void close() {

        try {

            bufferedWriter.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
