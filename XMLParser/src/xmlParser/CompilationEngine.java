package xmlParser;

import xmlParser.enums.DeclarationOrUse;
import xmlParser.enums.IdentifierKind;

import java.io.*;

import static xmlParser.enums.TokenType.*;

public class CompilationEngine {

    private Tokenizer tokenizer;
    private BufferedWriter bufferedWriterXML;
    private SymbolTable symbolTable;

    private IdentifierKind currentIdentifierKind;
    private String currentIdentifierType;

    public CompilationEngine(BufferedWriter bufferedWriterXML) {
        this.bufferedWriterXML = bufferedWriterXML;
        this.symbolTable = new SymbolTable();
    }

    public void throwError() {

        throw new Error();
    }

    public void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    void writeOpeningXMLTag(String tagName) {

        try {
            bufferedWriterXML.write(String.format("<%s>", tagName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeClosingXMLTag(String tagName) {

        try {
            bufferedWriterXML.write(String.format("</%s>", tagName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeCompleteXMLTag(String tagName, String tagValue) {

        try {
            bufferedWriterXML.write(String.format("<%s> %s </%s>", tagName, tagValue, tagName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compile() {

        tokenizer.advance();

        // the first token should be the "class" keyword
        if (!tokenizer.tokenValue().equals("class")) {

            System.out.println("'class' keyword expected");
            throwError();
        } else {

            compileClass();
        }
    }

    public void advanceIfMoreTokens() {

        if (tokenizer.hasMoreTokens()) {
            System.out.println(tokenizer.tokenValue());
            tokenizer.advance();
        } else {
            System.out.println("unexpected end of file");
            throwError();
        }
    }

    public void resolveIdentifierKind() {

        if (tokenizer.tokenValue().equals("static") || tokenizer.tokenValue().equals("field")
                || tokenizer.tokenValue().equals("var") || tokenizer.tokenValue().equals("argument")) {

            currentIdentifierKind = IdentifierKind.valueOf(tokenizer.tokenValue().toUpperCase());

        } else {

            throw new Error("invalid identifier kind");
        }
    }

    // valid types are one of the three below, or an identifier/className
    public Boolean checkIfIsValidType() {

        if (tokenizer.tokenValue().equals("int") || tokenizer.tokenValue().equals("char")
                || tokenizer.tokenValue().equals("boolean") || tokenizer.tokenType().equals(IDENTIFIER)) {

            return true;
        }

        return false;
    }

    public void compileSpecificSymbol(String symbol, String errorMessage) {

        /*
         * '{' | '}' | '(' | ')' | '[' | ']' | '.' | ',' | ';' | '+' | '-' | '*' | '/' |
         * '&' | '|' | '<' | '>' | '=' | ' ~ '
         */

        if (tokenizer.tokenValue().equals(symbol)) {

            if (tokenizer.tokenValue().equals("&")) {

                writeCompleteXMLTag("symbol", "amp");
                advanceIfMoreTokens();

            } else if (tokenizer.tokenValue().equals("<")) {

                writeCompleteXMLTag("symbol", "lt");
                advanceIfMoreTokens();

            } else if (tokenizer.tokenValue().equals(">")) {

                writeCompleteXMLTag("symbol", "gt");
                advanceIfMoreTokens();

            } else {

                writeCompleteXMLTag("symbol", tokenizer.tokenValue());
                advanceIfMoreTokens();
            }

        } else {

            System.out.println(errorMessage);
            throwError();
        }
    }

    public void compileType() {

        // 'int' | 'char' | 'boolean' | className

        if (checkIfIsValidType()) {

            currentIdentifierType = tokenizer.tokenValue();
            writeCompleteXMLTag("keyword_TYPE", tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else {

            System.out.println("invalid type" + tokenizer.tokenValue());
            throwError();
        }
    }

    public void compileIdentifier(IdentifierKind identifierKind, DeclarationOrUse declarationOrUse,
            Boolean isOneOfFourValidKinds) {

        // A sequence of letters, digits, and underscore ( '_' ) not starting with a digit.

        if (tokenizer.tokenType().equals(IDENTIFIER)) {

            // if it is a declaration, create an entry in the SymbolTable
            if (declarationOrUse.equals(DeclarationOrUse.DECLARATION)) {

                if (identifierKind.equals(IdentifierKind.SUBROUTINE)) {

                    symbolTable.startSubroutine();

                } else if (isOneOfFourValidKinds) {

                    symbolTable.define(tokenizer.tokenValue(), currentIdentifierType, identifierKind);
                }
            }

            writeCompleteXMLTag(String.format("identifier_%s_%s", identifierKind, declarationOrUse),
                    tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else {

            System.out.println(String.format("invalid %s", identifierKind));
            throwError();
        }
    }

    public void compileCheckForAdditionalVarNamesDeclaration() {
        while (tokenizer.tokenValue().equals(",")) {

            writeCompleteXMLTag("symbol", ",");
            advanceIfMoreTokens();

            compileIdentifier(currentIdentifierKind, DeclarationOrUse.DECLARATION, true);
        }
    }

    public void compileClass() {

        // 'class' className '{' classVarDec* subroutineDec* '}'

        writeOpeningXMLTag("class");
        writeCompleteXMLTag("keyword", "class");
        advanceIfMoreTokens();

        // identifier expected (name of the class)
        compileIdentifier(IdentifierKind.CLASS, DeclarationOrUse.DECLARATION, false);

        // symbol { expected, opening bracket for the class block
        compileSpecificSymbol("{", "{ expected");

        // the only valid options are one of the 5 keywords that begin a classVarDec or
        // subroutineDec, or a }
        while (tokenizer.tokenType().equals(KEYWORD)) {

            if (tokenizer.tokenValue().equals("static") || tokenizer.tokenValue().equals("field")) {

                compileClassVarDec();

            } else if (tokenizer.tokenValue().equals("constructor") || tokenizer.tokenValue().equals("function")
                    || tokenizer.tokenValue().equals("method") || tokenizer.tokenValue().equals("void")) {

                compileSubroutine();

            } else {

                System.out.println("invalid keyword");
                throwError();
            }

        }

        if (tokenizer.tokenValue().equals("}")) {

            writeCompleteXMLTag("symbol", tokenizer.tokenValue());

        } else {
            System.out.println("keyword or } expected");
            throwError();
        }

        writeClosingXMLTag("class");

        // there should be no more tokens after the class block ends
        if (tokenizer.hasMoreTokens()) {

            System.out.println("unexpected token");
            throwError();
        }
    }

    public void compileClassVarDec() {

        // ( 'static' | 'field' ) type varName ( ',' varName)* ';'

        writeOpeningXMLTag("classVarDec");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        resolveIdentifierKind();
        advanceIfMoreTokens();

        compileType();

        // one or more varNames separated by , expected
        compileIdentifier(currentIdentifierKind, DeclarationOrUse.DECLARATION, true);

        // check for additional varNames
        compileCheckForAdditionalVarNamesDeclaration();

        compileSpecificSymbol(";", "; expected after class variable declaration");

        writeClosingXMLTag("classVarDec");
    }

    public void compileSubroutine() {

        /*
         * ( 'constructor' | 'function' | 'method' ) ( 'void' | type) subroutineName '('
         * parameterList ')' subroutineBody
         */

        writeOpeningXMLTag("subroutineDec");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();

        // return type expected
        if (tokenizer.tokenValue().equals("void")) {

            writeCompleteXMLTag("keyword", tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else {

            compileType();
        }

        compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.DECLARATION, false);

        compileSpecificSymbol("(", "( expected");

        if (!tokenizer.tokenValue().equals(")")) {

            compileParameterList();
        }

        compileSpecificSymbol(")", ") expected");

        compileSubroutineBody();

        writeClosingXMLTag("subroutineDec");
    }

    public void compileParameterList() {

        // ((type varName) ( ',' type varName)*)?

        writeOpeningXMLTag("parameterList");

        compileType();

        // varName/identifier expected;
        compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, true);

        // check for additional varNames, along with type
        while (tokenizer.tokenValue().equals(",")) {

            compileSpecificSymbol(",", ", expected");

            compileType();

            compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, true);
        }

        writeClosingXMLTag("parameterList");
    }

    public void compileSubroutineBody() {

        // '{' varDec* statements '}'

        writeOpeningXMLTag("subroutineBody");

        compileSpecificSymbol("{", "{ expected");

        // 0 or more var declarations expected
        while (tokenizer.tokenValue().equals("var")) {
            compileVarDec();
        }

        compileStatements();

        compileSpecificSymbol("}", "} expected");

        writeClosingXMLTag("subroutineBody");
    }

    public void compileVarDec() {

        // 'var' type varName ( ',' varName)* ';'

        writeOpeningXMLTag("varDec");

        if (tokenizer.tokenValue().equals("var")) {

            writeCompleteXMLTag("keyword", tokenizer.tokenValue());
            resolveIdentifierKind();
            advanceIfMoreTokens();

        } else {
            System.out.println("var keyword expected");
            throwError();
        }

        compileType();

        // one or more varName expected
        compileIdentifier(IdentifierKind.VAR, DeclarationOrUse.DECLARATION, true);
        compileCheckForAdditionalVarNamesDeclaration();

        compileSpecificSymbol(";", "; expected");

        writeClosingXMLTag("varDec");
    }

    public void compileStatements() {

        // statement*

        writeOpeningXMLTag("statements");

        while (tokenizer.tokenValue().equals("let") || tokenizer.tokenValue().equals("if")
                || tokenizer.tokenValue().equals("while") || tokenizer.tokenValue().equals("do")
                || tokenizer.tokenValue().equals("return")) {

            if (tokenizer.tokenValue().equals("let")) {
                compileLet();
            } else if (tokenizer.tokenValue().equals("if")) {
                compileIf();
            } else if (tokenizer.tokenValue().equals("while")) {
                compileWhile();
            } else if (tokenizer.tokenValue().equals("do")) {
                compileDo();
            } else if (tokenizer.tokenValue().equals("return")) {
                compileReturn();
            }
        }

        writeClosingXMLTag("statements");
    }

    public void compileDo() {

        // 'do' subroutineCall ';'

        writeOpeningXMLTag("doStatement");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();
        compileSubroutineCall();
        compileSpecificSymbol(";", "; expected");
        writeClosingXMLTag("doStatement");
    }

    public void compileLet() {

        // 'let' varName ( '[' expression ']' )? '=' expression ';'

        writeOpeningXMLTag("letStatement");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();
        compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, true);

        // [ expression ]
        if (tokenizer.tokenValue().equals("[")) {
            compileSpecificSymbol("[", "[ expected");
            compileExpression();
            compileSpecificSymbol("]", "] expected");
        }

        compileSpecificSymbol("=", "= expected");
        compileExpression();
        compileSpecificSymbol(";", "; expected");
        writeClosingXMLTag("letStatement");
    }

    public void compileWhile() {

        // 'while' '(' expression ')' '{' statements '}'

        writeOpeningXMLTag("whileStatement");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();
        compileSpecificSymbol("(", "( expected");
        compileExpression();
        compileSpecificSymbol(")", ") expected");
        compileSpecificSymbol("{", "{ expected");
        compileStatements();
        compileSpecificSymbol("}", "} expected");
        writeClosingXMLTag("whileStatement");
    }

    public void compileReturn() {

        // 'return' expression? ';'

        writeOpeningXMLTag("returnStatement");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();

        if (!tokenizer.tokenValue().equals(";")) {
            compileExpression();
        }

        compileSpecificSymbol(";", "; expected");
        writeClosingXMLTag("returnStatement");
    }

    public void compileIf() {

        /*
         * 'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
         */

        writeOpeningXMLTag("ifStatement");
        writeCompleteXMLTag("keyword", tokenizer.tokenValue());
        advanceIfMoreTokens();
        compileSpecificSymbol("(", "( expected");
        compileExpression();
        compileSpecificSymbol(")", ") expected");
        compileSpecificSymbol("{", "{ expected");
        compileStatements();
        compileSpecificSymbol("}", "} expected");

        while (tokenizer.tokenValue().equals("else")) {

            writeCompleteXMLTag("keyword", tokenizer.tokenValue());
            advanceIfMoreTokens();
            compileSpecificSymbol("{", "{ expected");
            compileStatements();
            compileSpecificSymbol("}", "} expected");
        }

        writeClosingXMLTag("ifStatement");
    }

    public void compileExpression() {

        // term (op term)*

        writeOpeningXMLTag("expression");
        compileTerm();

        while (tokenizer.tokenValue().equals("+") || tokenizer.tokenValue().equals("-")
                || tokenizer.tokenValue().equals("*") || tokenizer.tokenValue().equals("/")
                || tokenizer.tokenValue().equals("&") || tokenizer.tokenValue().equals("|")
                || tokenizer.tokenValue().equals("<") || tokenizer.tokenValue().equals(">")
                || tokenizer.tokenValue().equals("=")) {

            compileSpecificSymbol(tokenizer.tokenValue(), String.format("%s expected", tokenizer.tokenValue()));
            compileTerm();
        }

        writeClosingXMLTag("expression");
    }

    public void compileTerm() {

        /*
         * integerConstant | stringConstant | keywordConstant | varName | varName '['
         * expression ']' | subroutineCall | '(' expression ')' | unaryOp term
         */

        writeOpeningXMLTag("term");

        if (tokenizer.tokenType().equals(INT_CONST)) {

            writeCompleteXMLTag("integerConstant", tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else if (tokenizer.tokenType().equals(STRING_CONST)) {

            writeCompleteXMLTag("stringConstant", tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else if (tokenizer.tokenValue().equals("true") || tokenizer.tokenValue().equals("false")
                || tokenizer.tokenValue().equals("null") || tokenizer.tokenValue().equals("this")) {

            writeCompleteXMLTag("keywordConstant", tokenizer.tokenValue());
            advanceIfMoreTokens();

        } else if (tokenizer.tokenType().equals(IDENTIFIER)) {

            // varName | varName '[' expression ']'
            String tempValue = tokenizer.tokenValue();
            advanceIfMoreTokens();

            if (tokenizer.tokenValue().equals("[")) {

                writeCompleteXMLTag("identifier", tempValue);
                compileSpecificSymbol("[", "[ expected");
                compileExpression();
                compileSpecificSymbol("]", "] expected");

            } else if (tokenizer.tokenValue().equals("(") || tokenizer.tokenValue().equals(".")) {

                // subroutineCall
                tokenizer.reverse();
                compileSubroutineCall();

            } else {

                writeCompleteXMLTag("identifier", tempValue);
            }

        } else if (tokenizer.tokenValue().equals("(")) {

            // '(' expression ')'
            compileSpecificSymbol("(", "( expected");
            compileExpression();
            compileSpecificSymbol(")", ") expected");

        } else if (tokenizer.tokenValue().equals("-") || tokenizer.tokenValue().equals("~")) {

            // unaryOp term
            compileSpecificSymbol(tokenizer.tokenValue(), String.format("%s expected", tokenizer.tokenValue()));
            compileTerm();

        } else {

            System.out.println("invalid term");
            throwError();
        }

        writeClosingXMLTag("term");
    }

    public void compileSubroutineCall() {

        // subroutineName '(' expressionList ')' | (className | varName) '.'
        // subroutineName '(' expressionList ')'

        // look ahead to check if the identifier is a subroutineName, className, or
        // varName
        if (tokenizer.tokenType().equals(IDENTIFIER)) {

            advanceIfMoreTokens();

            if (tokenizer.tokenValue().equals("(")) {

                // the identifier is a subroutine
                tokenizer.reverse();
                compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.USE, false);
                compileSpecificSymbol("(", "( expected");

                if (!tokenizer.tokenValue().equals(")")) {
                    compileExpressionList();
                }

                compileSpecificSymbol(")", ") expected");

            } else if (tokenizer.tokenValue().equals(".")) {

                // the identifier is a className or varName
                tokenizer.reverse();
                compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, false);
                compileSpecificSymbol(".", "( expected");
                compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.USE, false);
                compileSpecificSymbol("(", "( expected");

                if (!tokenizer.tokenValue().equals(")")) {
                    compileExpressionList();
                }

                compileSpecificSymbol(")", ") expected");

            } else {
                System.out.println("invalid subroutine call");
                throwError();
            }

        } else {
            System.out.println("invalid subroutine call");
            throwError();
        }
    }

    public void compileExpressionList() {

        // (expression ( ',' expression)* )?

        writeOpeningXMLTag("expressionList");
        compileExpression();

        while (tokenizer.tokenValue().equals(",")) {
            compileSpecificSymbol(",", ", expected");
            compileExpression();
        }

        writeClosingXMLTag("expressionList");
    }
}
