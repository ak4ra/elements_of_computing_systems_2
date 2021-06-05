import enums.IdentifierKind;

import java.util.HashMap;

public class SymbolTable {

	private HashMap<String, SymbolTableEntry> classScopeTable      = new HashMap<>();
	private HashMap<String, SymbolTableEntry> subroutineScopeTable = new HashMap<>();

	private Integer staticIndex   = 0;
	private Integer fieldIndex    = 0;
	private Integer argumentIndex = 0;
	private Integer varIndex      = 0;

	public SymbolTable() {
	}

	public void startSubroutine() {
		// start a new subroutine scope (reset the subroutine's symbol table)
		subroutineScopeTable = new HashMap<>();
		argumentIndex = 0;
		varIndex = 0;
	}

	/*
	define a new identifier of a given name, type, and kind
	and assign it a running index
	STATIC and FIELD identifiers have a class scope
	ARG and VAR identifiers have a subroutine scope

	kind can only be STATIC, FIELD, ARG, or VAR
	*/
	public void define(String name, String type, IdentifierKind kind) {
		switch (kind) {
			case STATIC:
				classScopeTable.put(name, new SymbolTableEntry(type, kind, staticIndex));
				staticIndex++;
				break;
			case FIELD:
				classScopeTable.put(name, new SymbolTableEntry(type, kind, fieldIndex));
				fieldIndex++;
				break;
			case ARGUMENT:
				subroutineScopeTable.put(name, new SymbolTableEntry(type, kind, argumentIndex));
				argumentIndex++;
				break;
			case VAR:
				subroutineScopeTable.put(name, new SymbolTableEntry(type, kind, varIndex));
				varIndex++;
				break;
			default:
				throw new Error("invalid identifier kind");
		}
	}

	/*
	return the number of variables of the given kind already defined
	in the current scope
	*/
	public Integer varCount(IdentifierKind kind) {
		switch (kind) {
			case STATIC:
				return staticIndex;
			case FIELD:
				return fieldIndex;
			case ARGUMENT:
				return argumentIndex;
			case VAR:
				return varIndex;
			default:
				return 0;
		}
	}

	/*
	return the type of the named identifier in the current scope
	*/
	public String typeOf(String name) {
		if (subroutineScopeTable.containsKey(name)) {
			return subroutineScopeTable.get(name).type();
		}
		return classScopeTable.get(name).type();
	}


	/*
	returns the kind of the named identifier in the current scope
	if the identifier is unknown in the current scope,
	return NONE
	*/
	public IdentifierKind kindOf(String name) {
		if (subroutineScopeTable.containsKey(name)) {
			return subroutineScopeTable.get(name).kind();
		} else if (classScopeTable.containsKey(name)) {
			return classScopeTable.get(name).kind();
		}
		return IdentifierKind.NONE;
	}

	/*
	returns the index assigned to the named identifier
	*/
	public Integer indexOf(String name) {
		if (subroutineScopeTable.containsKey(name)) {
			return subroutineScopeTable.get(name).index();
		}
		return classScopeTable.get(name).index();
	}

	public void logTables() {
		System.out.println("---- class scope table ----");
		for (String name : classScopeTable.keySet()) {
			System.out.printf("%s: %s\n%n", name, classScopeTable.get(name).toString());
		}

		System.out.println("---- subroutine scope table ----");
		for (String name : subroutineScopeTable.keySet()) {
			System.out.printf("%s: %s\n%n", name, subroutineScopeTable.get(name).toString());
		}
		System.out.println();
	}

	public Boolean contains(String name) {
		return classScopeTable.containsKey(name) || subroutineScopeTable.containsKey(name);
	}
}
