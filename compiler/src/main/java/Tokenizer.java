import enums.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {

	private final        BufferedReader bufferedReader;
	private final        List<Token>    tokenList             = new ArrayList<>();
	private static final String[]       keywords              = {"class",
																 "constructor",
																 "function",
																 "method",
																 "field",
																 "static",
																 "var",
																 "int",
																 "char",
																 "boolean",
																 "void",
																 "true",
																 "false",
																 "null",
																 "this",
																 "let",
																 "do",
																 "if",
																 "else",
																 "while",
																 "return"
	};
	private static final String[]       symbols               = {"{", "}", "(", ")",
																 "[", "]", ".", ",",
																 ";", "+", "-", "*",
																 "/", "&", "|", "<",
																 ">", "=", "~"
	};
	private              int            currentChar;
	private              int            nextChar;
	private              Token          currentToken;
	private              Integer        currentTokenListIndex = -1;

	public Tokenizer(BufferedReader bufferedReader) {
		this.bufferedReader = bufferedReader;
	}

	public Boolean hasMoreTokens() {
		return tokenList.size() - 1 != currentTokenListIndex;
	}

	public void advance() {
		currentTokenListIndex++;
		currentToken = tokenList.get(currentTokenListIndex);
	}

	public void reverse() {
		currentTokenListIndex--;
		currentToken = tokenList.get(currentTokenListIndex);
	}

	public TokenType tokenType() {
		return currentToken.tokenType();
	}

	public String tokenValue() {
		return currentToken.lexeme();
	}

	public void tokenize() {
		try {
			bufferedReader.mark(1);
			currentChar = bufferedReader.read();

			if (currentChar == -1) {
				// end of file
				return;
			}

			// skip whitespace
			if (Character.isWhitespace(currentChar)) {
				do {
					bufferedReader.mark(1);
					currentChar = bufferedReader.read();
				} while (Character.isWhitespace(currentChar));
				bufferedReader.reset();
				tokenize();

			} else if (currentChar == '/') {
				// detect and skip comment sections
				bufferedReader.mark(1);
				nextChar = bufferedReader.read();

				if (nextChar == '/') {
					bufferedReader.readLine();
					tokenize();
				} else if (nextChar == '*') {
					do {
						currentChar = nextChar;
						nextChar = bufferedReader.read();
					} while (!(currentChar == '*' && nextChar == '/'));
					tokenize();
				} else {
					// the character is the symbol /
					Token token = new Token(String.valueOf((char) currentChar), TokenType.SYMBOL);
					tokenList.add(token);
					bufferedReader.reset();
					tokenize();
				}

				// the character is a symbol (other than  '/')
			} else if (Arrays.asList(symbols).contains(Character.toString(currentChar))) {
				Token token = new Token(String.valueOf((char) currentChar), TokenType.SYMBOL);
				tokenList.add(token);
				tokenize();

				// there is a string constant, a sequence surrounded by double quotes
			} else if (currentChar == '"') {
				StringBuilder newToken = new StringBuilder();
				nextChar = bufferedReader.read();

				while (nextChar != '"') {
					newToken.append((char) nextChar);
					nextChar = bufferedReader.read();
				}
				Token token = new Token(newToken.toString(), TokenType.STRING_CONST);
				tokenList.add(token);
				tokenize();

				// an alphabetic character or "_"
				// if alphabetic, may be the first char of a keyword or identifier
				// if "_", may be the first char of an identifier
			} else if (Character.isAlphabetic(currentChar) || currentChar == '_') {
				StringBuilder newToken = new StringBuilder();
				do {
					newToken.append((char) currentChar);
					bufferedReader.mark(1);
					currentChar = bufferedReader.read();
				} while (Character.isAlphabetic(currentChar) || Character.isDigit(currentChar) || currentChar == '_');
				Token token = new Token(newToken.toString(), resolveTokenType(newToken.toString()));
				bufferedReader.reset();
				tokenList.add(token);
				tokenize();

				// integer constant, a digit not preceded by an alphabetic character or "_"
			} else if (Character.isDigit(currentChar)) {
				StringBuilder newToken = new StringBuilder();
				do {
					newToken.append((char) currentChar);
					bufferedReader.mark(1);
					currentChar = bufferedReader.read();
				} while (Character.isDigit(currentChar));
				Token token = new Token(newToken.toString(), TokenType.INT_CONST);
				bufferedReader.reset();
				tokenList.add(token);
				tokenize();

			} else {
				System.out.println("default case");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private TokenType resolveTokenType(String token) {
		if (Arrays.stream(keywords).anyMatch(token::equals)) {
			return TokenType.KEYWORD;
		}
		return TokenType.IDENTIFIER;
	}
}
