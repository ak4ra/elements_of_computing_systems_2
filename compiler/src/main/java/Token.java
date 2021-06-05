import enums.TokenType;

public class Token {

	private String    token;
	private TokenType tokenType;

	public Token(String token, TokenType tokenType) {
		this.token = token;
		this.tokenType = tokenType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public String toString() {
		return "Token{" + "token='" + token + '\'' + ", tokenType=" + tokenType + '}';
	}
}
