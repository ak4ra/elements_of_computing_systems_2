import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import enums.DeclarationOrUse;
import enums.IdentifierKind;
import enums.TokenType;

public class CompilationEngine {

	private       Tokenizer   tokenizer;
	private final VMWriter    vmWriter;
	private final SymbolTable symbolTable;

	private final HashMap<String, Subroutine> subroutines = new LinkedHashMap<>();
	private final Random                      random      = new Random();

	private String         currentClassName;
	private String         currentSubroutineDeclarationKind;
	private IdentifierKind currentIdentifierKind;
	private String         currentIdentifierType;

	public CompilationEngine(VMWriter vmWriter) {
		this.vmWriter = vmWriter;
		this.symbolTable = new SymbolTable();
	}

	public void throwError() {
		throw new Error();
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public void compile() {
		tokenizer.advance();
		// the first lexeme should be the "class" keyword
		if (!tokenizer.tokenValue().equals("class")) {
			System.out.println("'class' keyword expected");
			throwError();
		} else {
			compileClass();
		}
	}

	public void advanceIfMoreTokens() {
		if (tokenizer.hasMoreTokens()) {
			// System.out.println(tokenizer.tokenValue());
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

	public String resolveSegment(IdentifierKind identifierKind) {
		if (identifierKind.equals(IdentifierKind.VAR)) {
			return "local";
		} else if (identifierKind.equals(IdentifierKind.ARGUMENT)) {
			return "argument";
		} else if (identifierKind.equals(IdentifierKind.STATIC)) {
			return "static";
		} else if (identifierKind.equals(IdentifierKind.FIELD)) {
			return "this";
		} else if (identifierKind.equals(IdentifierKind.NONE)) {
			throw new Error("identifier kind NONE returned at resolveSegment()");
		}
		return null;
	}

	// valid types are one of the three below, or an identifier/className
	public Boolean checkIfIsValidType() {
		return tokenizer.tokenValue().equals("int") ||
			   tokenizer.tokenValue().equals("char") ||
			   tokenizer.tokenValue().equals("boolean") ||
			   tokenizer.tokenType().equals(TokenType.IDENTIFIER);
	}

	public void compileSpecificSymbol(String symbol, String errorMessage) {
		/*
		 * '{' | '}' | '(' | ')' | '[' | ']' | '.' | ',' | ';' | '+' | '-' | '*' | '/' |
		 * '&' | '|' | '<' | '>' | '=' | ' ~ '
		 */

		if (tokenizer.tokenValue().equals(symbol)) {
			advanceIfMoreTokens();
		} else {
			System.out.println(errorMessage);
			throwError();
		}
	}

	public void compileType() {
		// 'int' | 'char' | 'boolean' | className
		if (checkIfIsValidType()) {
			currentIdentifierType = tokenizer.tokenValue();
			advanceIfMoreTokens();
		} else {
			System.out.println("invalid type" + tokenizer.tokenValue());
			throwError();
		}
	}

	public void compileIdentifier(IdentifierKind identifierKind, DeclarationOrUse declarationOrUse,
								  Boolean isOneOfFourValidKinds
	) {
		// A sequence of letters, digits, and underscore ( '_' ) not starting with a
		// digit.
		if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			if (declarationOrUse.equals(DeclarationOrUse.DECLARATION)) {
				if (identifierKind.equals(IdentifierKind.CLASS)) {
					currentClassName = tokenizer.tokenValue();

					// if it is a subroutine declaration, start a new subroutine symbol table
				} else if (identifierKind.equals(IdentifierKind.SUBROUTINE)) {
					// start new subroutine
					// if it is a method, push the current class/object as "this"
					symbolTable.startSubroutine();
					if (currentSubroutineDeclarationKind.equals("method")) {
						symbolTable.define("this", currentClassName, IdentifierKind.ARGUMENT);
					}

					// if it is a valid variable, create an entry in the symbol table
				} else if (isOneOfFourValidKinds) {
					symbolTable.define(tokenizer.tokenValue(), currentIdentifierType, identifierKind);
				}
			} else if (declarationOrUse.equals(DeclarationOrUse.USE)) {
			}
			advanceIfMoreTokens();
		} else {
			System.out.printf("invalid %s%n", identifierKind);
			throwError();
		}
	}

	public void compileCheckForAdditionalVarNamesDeclaration() {
		while (tokenizer.tokenValue().equals(",")) {
			advanceIfMoreTokens();
			compileIdentifier(currentIdentifierKind, DeclarationOrUse.DECLARATION, true);
		}
	}

	public void compileClass() {
		// 'class' className '{' classVarDec* subroutineDec* '}'
		advanceIfMoreTokens();

		// identifier expected (name of the class)
		compileIdentifier(IdentifierKind.CLASS, DeclarationOrUse.DECLARATION, false);

		// symbol { expected, opening bracket for the class block
		compileSpecificSymbol("{", "{ expected");

		// the only valid options are one of the 5 keywords that begin a classVarDec or
		// subroutineDec, or a }
		while (tokenizer.tokenType().equals(TokenType.KEYWORD)) {
			if (tokenizer.tokenValue().equals("static") ||
				tokenizer.tokenValue().equals("field")) {
				compileClassVarDec();
			} else if (tokenizer.tokenValue().equals("constructor") ||
					   tokenizer.tokenValue().equals("function") ||
					   tokenizer.tokenValue().equals("method") ||
					   tokenizer.tokenValue().equals("void")) {
				compileSubroutine();
			} else {
				System.out.println("invalid keyword");
				throwError();
			}
		}

		if (!tokenizer.tokenValue().equals("}")) {
			System.out.println("keyword or } expected");
			throwError();
		}

		// there should be no more tokens after the class block ends
		if (tokenizer.hasMoreTokens()) {
			System.out.println("unexpected lexeme");
			throwError();
		}
	}

	public void compileClassVarDec() {
		// ( 'static' | 'field' ) type varName ( ',' varName)* ';'
		resolveIdentifierKind();
		advanceIfMoreTokens();
		compileType();

		// one or more varNames separated by , expected
		compileIdentifier(currentIdentifierKind, DeclarationOrUse.DECLARATION, true);
		// check for additional varNames
		compileCheckForAdditionalVarNamesDeclaration();
		compileSpecificSymbol(";", "; expected after class variable declaration");
	}

	public void compileSubroutine() {
		/*
		 * ( 'constructor' | 'function' | 'method' ) ( 'void' | type) subroutineName '('
		 * parameterList ')' subroutineBody
		 */
		currentSubroutineDeclarationKind = tokenizer.tokenValue();
		String subroutineReturnType;
		String subroutineName;

		advanceIfMoreTokens();

		// return type expected
		if (tokenizer.tokenValue().equals("void")) {
			subroutineReturnType = "void";
			advanceIfMoreTokens();
		} else {
			subroutineReturnType = tokenizer.tokenValue();
			compileType();
		}

		subroutineName = tokenizer.tokenValue();
		subroutines.put(subroutineName,
						new Subroutine(subroutineName, subroutineReturnType, currentSubroutineDeclarationKind)
		);
		compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.DECLARATION, false);

		compileSpecificSymbol("(", "( expected");
		if (!tokenizer.tokenValue().equals(")")) {
			compileParameterList();
		}
		compileSpecificSymbol(")", ") expected");

		// subroutine body '{' varDec* statements '}'
		/*
		 * Within a VM function corresponding to a Jack method or a Jack constructor,
		 * access to the fields of the this object is obtained by first pointing the
		 * virtual this segment to the current object (using pointer 0 ) and then
		 * accessing individual fields via this index references, where index is an
		 * non-negative integer.
		 */
		compileSpecificSymbol("{", "{ expected");

		// 0 or more var declarations expected
		while (tokenizer.tokenValue().equals("var")) {
			compileVarDec();
		}

		// write the function after the local variables have been recorded
		vmWriter.writeFunction(String.format("%s.%s", currentClassName, subroutineName),
							   symbolTable.varCount(IdentifierKind.VAR)
		);

		// set "this"
		if (currentSubroutineDeclarationKind.equals("method")) {
			// if it is a method, get the "this" from the first argument and set THIS
			// accordingly
			vmWriter.writePush("argument", 0);
			vmWriter.writePop("pointer", 0);
		} else if (currentSubroutineDeclarationKind.equals("constructor")) {
			// if it is a constructor, allocate memory depending on the number of
			// class-scope variables and
			// set the root of THIS to that memory address
			vmWriter.writePush("constant",
							   (symbolTable.varCount(IdentifierKind.STATIC) +
								symbolTable.varCount(IdentifierKind.FIELD)
							   )
			);
			vmWriter.writeCall("Memory.alloc", 1);
			vmWriter.writePop("pointer", 0);
		}
		compileStatements();
		compileSpecificSymbol("}", "} expected");
	}

	public void compileParameterList() {
		// ((type varName) ( ',' type varName)*)?
		compileType();

		// varName/identifier expected;
		compileIdentifier(IdentifierKind.ARGUMENT, DeclarationOrUse.DECLARATION, true);
		// check for additional varNames, along with type
		while (tokenizer.tokenValue().equals(",")) {
			compileSpecificSymbol(",", ", expected");
			compileType();
			compileIdentifier(IdentifierKind.ARGUMENT, DeclarationOrUse.DECLARATION, true);
		}
	}

	public void compileVarDec() {
		// 'var' type varName ( ',' varName)* ';'
		if (tokenizer.tokenValue().equals("var")) {
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
	}

	public void compileStatements() {
		// statement*
		while (tokenizer.tokenValue().equals("let") || tokenizer.tokenValue().equals("if")
			   || tokenizer.tokenValue().equals("while") || tokenizer.tokenValue().equals("do")
			   || tokenizer.tokenValue().equals("return")) {
			switch (tokenizer.tokenValue()) {
				case "let" -> compileLet();
				case "if" -> compileIf();
				case "while" -> compileWhile();
				case "do" -> compileDo();
				case "return" -> compileReturn();
			}
		}
	}

	public void compileDo() {
		// 'do' subroutineCall ';'
		/*
		 * When translating a do sub statement where sub is a void method or function,
		 * the caller of the corresponding VM function must pop (and ignore) the
		 * returned value (which is always the constant 0)
		 */
		advanceIfMoreTokens();
		compileSubroutineCall();
		compileSpecificSymbol(";", "; expected");
		vmWriter.writePop("temp", 0);
	}

	public void compileLet() {
		// 'let' varName ( '[' expression ']' )? '=' expression ';'
		advanceIfMoreTokens();
		String currentIdentifier = tokenizer.tokenValue();
		compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, true);
		boolean arrayAccess = false;

		// [ expression ]
		// it is an array access
		if (tokenizer.tokenValue().equals("[")) {
			arrayAccess = true;
			compileSpecificSymbol("[", "[ expected");
			compileExpression();
			compileSpecificSymbol("]", "] expected");
			// push the array address from the variable
			vmWriter.writePush(resolveSegment(symbolTable.kindOf(currentIdentifier)),
							   symbolTable.indexOf(currentIdentifier)
			);
			// add the calculated index to the address
			vmWriter.writeArithmetic("add");
		}
		compileSpecificSymbol("=", "= expected");
		compileExpression();

		// if it is an array access and not a declaration
		if (symbolTable.typeOf(currentIdentifier).equals("Array") && arrayAccess) {
			// save the value of the expression in a temp variable
			vmWriter.writePop("temp", 0);
			// point the THAT memory segment to the complete index address calculated before
			// the "="
			vmWriter.writePop("pointer", 1);
			// push the expression value back on the stack
			vmWriter.writePush("temp", 0);
			// save it in THAT
			vmWriter.writePop("that", 0);
		} else {
			// pop the result of the expression into the variable
			vmWriter.writePop(resolveSegment(symbolTable.kindOf(currentIdentifier)),
							  symbolTable.indexOf(currentIdentifier)
			);
		}
		compileSpecificSymbol(";", "; expected");
	}

	public void compileWhile() {
		// 'while' '(' expression ')' '{' statements '}'
		advanceIfMoreTokens();

		/*
		 * label L1 VM code for computing ~(cond) if-goto L2 VM code for executing s1
		 * goto L1 label L2 ...
		 */
		Integer labelRandom = Math.abs(random.nextInt());

		vmWriter.writeLabel(String.format("WHILE1_%d", labelRandom));
		compileSpecificSymbol("(", "( expected");
		compileExpression();
		compileSpecificSymbol(")", ") expected");
		vmWriter.writeArithmetic("not");
		vmWriter.writeIf(String.format("WHILE2_%d", labelRandom));
		compileSpecificSymbol("{", "{ expected");
		compileStatements();
		compileSpecificSymbol("}", "} expected");
		vmWriter.writeGoto(String.format("WHILE1_%d", labelRandom));
		vmWriter.writeLabel(String.format("WHILE2_%d", labelRandom));
	}

	public void compileReturn() {
		// 'return' expression? ';'
		/*
		 * VM functions corresponding to void Jack methods and functions must return the
		 * constant 0 as their return value.
		 */
		advanceIfMoreTokens();

		if (!tokenizer.tokenValue().equals(";")) {
			compileExpression();
		} else {
			vmWriter.writePush("constant", 0);
		}
		compileSpecificSymbol(";", "; expected");
		vmWriter.writeReturn();
	}

	public void compileIf() {
		/*
		 * 'if' '(' expression ')' '{' statements '}' ( 'else' '{' statements '}' )?
		 */
		/*
		 * VM code for computing ~(cond) if-goto L1 VM code for executing s1 goto L2
		 * label L1 VM code for executing s2 label L2 ...
		 */

		Integer labelRandom = Math.abs(random.nextInt());

		advanceIfMoreTokens();
		compileSpecificSymbol("(", "( expected");
		compileExpression();
		compileSpecificSymbol(")", ") expected");
		vmWriter.writeArithmetic("not");
		vmWriter.writeIf(String.format("IF1_%d", labelRandom));
		compileSpecificSymbol("{", "{ expected");
		compileStatements();
		compileSpecificSymbol("}", "} expected");
		vmWriter.writeGoto(String.format("IF2_%d", labelRandom));
		vmWriter.writeLabel(String.format("IF1_%d", labelRandom));

		while (tokenizer.tokenValue().equals("else")) {
			advanceIfMoreTokens();
			compileSpecificSymbol("{", "{ expected");
			compileStatements();
			compileSpecificSymbol("}", "} expected");
		}
		vmWriter.writeLabel(String.format("IF2_%d", labelRandom));
	}

	public void compileExpression() {
		// term (op term)*
		compileTerm();
		while (tokenizer.tokenValue().equals("+") || tokenizer.tokenValue().equals("-")
			   || tokenizer.tokenValue().equals("*") || tokenizer.tokenValue().equals("/")
			   || tokenizer.tokenValue().equals("&") || tokenizer.tokenValue().equals("|")
			   || tokenizer.tokenValue().equals("<") || tokenizer.tokenValue().equals(">")
			   || tokenizer.tokenValue().equals("=")) {
			String operator = tokenizer.tokenValue();

			compileSpecificSymbol(tokenizer.tokenValue(), String.format("%s expected", tokenizer.tokenValue()));
			compileTerm();
			if (operator.equals("+")) {
				vmWriter.writeArithmetic("add");
			} else if (operator.equals("-")) {
				vmWriter.writeArithmetic("sub");
			} else if (operator.equals("*")) {
				vmWriter.writeArithmetic("call Math.multiply 2");
			} else if (operator.equals("/")) {
				vmWriter.writeArithmetic("call Math.divide 2");
			} else if (operator.equals("&")) {
				vmWriter.writeArithmetic("and");
			} else if (operator.equals("|")) {
				vmWriter.writeArithmetic("or");
			} else if (operator.equals("<")) {
				vmWriter.writeArithmetic("lt");
			} else if (operator.equals(">")) {
				vmWriter.writeArithmetic("gt");
			} else if (operator.equals("=")) {
				vmWriter.writeArithmetic("eq");
			}
		}
	}

	public void compileTerm() {
		/*
		 * integerConstant | stringConstant | keywordConstant | varName | varName '['
		 * expression ']' | subroutineCall | '(' expression ')' | unaryOp term
		 */
		if (tokenizer.tokenType().equals(TokenType.INT_CONST)) {
			vmWriter.writePush("constant", Integer.valueOf(tokenizer.tokenValue()));
			advanceIfMoreTokens();
		} else if (tokenizer.tokenType().equals(TokenType.STRING_CONST)) {
			/*
			 * String constants are created using the OS constructor String.new(length)
			 * String assignments like x="cc...c" are handled using a series of calls to the
			 * OS routine String.appendChar(nextChar)
			 */
			vmWriter.writePush("constant", tokenizer.tokenValue().length());
			vmWriter.writeCall("String.new", 1);
			for (int i = 0; i < tokenizer.tokenValue().length(); i++) {
				vmWriter.writePush("constant", (int) tokenizer.tokenValue().charAt(i));
				vmWriter.writeCall("String.appendChar", 2);
			}
			advanceIfMoreTokens();

		} else if (tokenizer.tokenValue().equals("true") || tokenizer.tokenValue().equals("false")
				   || tokenizer.tokenValue().equals("null") || tokenizer.tokenValue().equals("this")) {
			/*
			 * null and false are mapped to the constant 0. True is mapped to the constant
			 * -1 (this constant can be obtained via push constant 1 followed by neg ).
			 */
			if (tokenizer.tokenValue().equals("null") || tokenizer.tokenValue().equals("false")) {
				vmWriter.writePush("constant", 0);
			} else if (tokenizer.tokenValue().equals("true")) {
				vmWriter.writePush("constant", 1);
				vmWriter.writeArithmetic("neg");
			} else if (tokenizer.tokenValue().equals("this")) {
				vmWriter.writePush("pointer", 0);
			}
			advanceIfMoreTokens();

		} else if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			// varName | varName '[' expression ']'
			String currentIdentifier = tokenizer.tokenValue();
			advanceIfMoreTokens();

			if (tokenizer.tokenValue().equals("[")) {
				// the term is varName '[' expression ']'
				// array access
				// array index calculated within the brackets
				compileSpecificSymbol("[", "[ expected");
				compileExpression();
				compileSpecificSymbol("]", "] expected");

				// push the variable that contains the array memory address
				vmWriter.writePush(resolveSegment(symbolTable.kindOf(currentIdentifier)),
								   symbolTable.indexOf(currentIdentifier)
				);
				// add it to the calculated index to get the complete address of the index
				vmWriter.writeArithmetic("add");
				// set the THAT memory segment to the index address
				vmWriter.writePop("pointer", 1);
				// push the contents of that memory address
				vmWriter.writePush("that", 0);

			} else if (tokenizer.tokenValue().equals("(") || tokenizer.tokenValue().equals(".")) {
				// the term is a subroutineCall
				tokenizer.reverse();
				compileSubroutineCall();
			} else {
				// the term is a single identifier
				vmWriter.writePush(resolveSegment(symbolTable.kindOf(currentIdentifier)),
								   symbolTable.indexOf(currentIdentifier)
				);
			}

		} else if (tokenizer.tokenValue().equals("(")) {
			// '(' expression ')'
			compileSpecificSymbol("(", "( expected");
			compileExpression();
			compileSpecificSymbol(")", ") expected");

		} else if (tokenizer.tokenValue().equals("-") || tokenizer.tokenValue().equals("~")) {
			// unaryOp term
			String operator = tokenizer.tokenValue();

			compileSpecificSymbol(tokenizer.tokenValue(), String.format("%s expected", tokenizer.tokenValue()));
			compileTerm();
			if (operator.equals("-")) {
				vmWriter.writeArithmetic("neg");
			} else if (operator.equals("~")) {
				vmWriter.writeArithmetic("not");
			}
		} else {
			System.out.println("invalid term");
			throwError();
		}
	}

	public void compileSubroutineCall() {
		// subroutineName '(' expressionList ')' | (className | varName) '.'
		// subroutineName '(' expressionList ')'
		// look ahead to check if the identifier is a subroutineName, className, or
		// varName
		if (tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			String subroutineName = "";
			Integer subroutineCallArgumentsCount = 0;
			String subroutineOrClassOrVarName = tokenizer.tokenValue();
			advanceIfMoreTokens();

			if (tokenizer.tokenValue().equals("(")) {
				// the identifier is a subroutine
				tokenizer.reverse();
				compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.USE, false);
				// push "this" as first argument
				vmWriter.writePush("pointer", 0);
				subroutineCallArgumentsCount++;
				// set subroutine name
				subroutineName = String.format("%s.%s", currentClassName, subroutineOrClassOrVarName);

			} else if (tokenizer.tokenValue().equals(".")) {
				// the identifier is a className or varName
				tokenizer.reverse();
				compileIdentifier(symbolTable.kindOf(tokenizer.tokenValue()), DeclarationOrUse.USE, false);
				compileSpecificSymbol(".", ". expected");

				if (symbolTable.contains(subroutineOrClassOrVarName)) {
					// subroutineOrClassOrVarName is a varName
					/*
					 * Before calling a VM function, the caller (itself a VM function) must push the
					 * function’s arguments onto the stack. If the called VM function corresponds to
					 * a Jack method, the first pushed argument must be a reference to the object on
					 * which the method is supposed to operate.
					 */
					vmWriter.writePush(resolveSegment(symbolTable.kindOf(subroutineOrClassOrVarName)),
									   symbolTable.indexOf(subroutineOrClassOrVarName)
					);
					subroutineCallArgumentsCount++;
					subroutineName = String.format("%s.%s", symbolTable.typeOf(subroutineOrClassOrVarName),
												   tokenizer.tokenValue()
					);

				} else {
					subroutineName = String.format("%s.%s", subroutineOrClassOrVarName, tokenizer.tokenValue());
				}
				compileIdentifier(IdentifierKind.SUBROUTINE, DeclarationOrUse.USE, false);

			} else {
				System.out.println("invalid subroutine call");
				throwError();
			}
			compileSpecificSymbol("(", "( expected");

			if (!tokenizer.tokenValue().equals(")")) {
				subroutineCallArgumentsCount = compileExpressionList(subroutineCallArgumentsCount);
			}
			compileSpecificSymbol(")", ") expected");
			vmWriter.writeCall(subroutineName, subroutineCallArgumentsCount);

		} else {
			System.out.println("invalid subroutine call");
			throwError();
		}
	}

	public Integer compileExpressionList(Integer subroutineCallArgumentsCount) {
		// (expression ( ',' expression)* )?
		compileExpression();
		// increment arguments counter
		subroutineCallArgumentsCount++;

		while (tokenizer.tokenValue().equals(",")) {
			compileSpecificSymbol(",", ", expected");
			compileExpression();
			// increment arguments counter
			subroutineCallArgumentsCount++;
		}
		return subroutineCallArgumentsCount;
	}
}
