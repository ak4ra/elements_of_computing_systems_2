public class Subroutine {

	private String name;
	private String returnType; // 'void' | type
	private String declarationKind; // 'constructor' | 'function' | 'method'

	public Subroutine(String name, String returnType, String declarationKind) {
		this.name = name;
		this.returnType = returnType;
		this.declarationKind = declarationKind;
	}

	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getDeclarationKind() {
		return declarationKind;
	}
}
