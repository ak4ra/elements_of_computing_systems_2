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

		//        if (!subroutineScopeTable.isEmpty()) {
		//
		//            System.out.println("---- subroutine scope table ----");
		//            for (String name: subroutineScopeTable.keySet()) {
		//                System.out.println(String.format("%s: %s\n", name, subroutineScopeTable.get(name).toString()));
		//            }
		//
		//            System.out.println();
		//        }

		// start a new subroutine scope (reset the subroutine's symbol table)
		subroutineScopeTable = new HashMap<>();
		argumentIndex = 0;
		varIndex = 0;
	}

	public void define(String name, String type, IdentifierKind kind) {

        /*
        define a new identifier of a given name, type, and kind
        and assign it a running index
        STATIC and FIELD identifiers have a class scope
        ARG and VAR identifiers have a subroutine scope

        kind can only be STATIC, FIELD, ARG, or VAR
        */

		if (kind.equals(IdentifierKind.STATIC)) {

			classScopeTable.put(name, new SymbolTableEntry(type, kind, staticIndex));
			staticIndex++;

		} else if (kind.equals(IdentifierKind.FIELD)) {

			classScopeTable.put(name, new SymbolTableEntry(type, kind, fieldIndex));
			fieldIndex++;

		} else if (kind.equals(IdentifierKind.ARGUMENT)) {

			subroutineScopeTable.put(name, new SymbolTableEntry(type, kind, argumentIndex));
			argumentIndex++;

		} else if (kind.equals(IdentifierKind.VAR)) {

			subroutineScopeTable.put(name, new SymbolTableEntry(type, kind, varIndex));
			varIndex++;

		} else {

			throw new Error("invalid identifier kind");
		}
	}

	public Integer varCount(IdentifierKind kind) {

        /*
        return the number of variables of the given kind already defined
        in the current scope
        */

		if (kind.equals(IdentifierKind.STATIC)) {

			return staticIndex;

		} else if (kind.equals(IdentifierKind.FIELD)) {

			return fieldIndex;

		} else if (kind.equals(IdentifierKind.ARGUMENT)) {

			return argumentIndex;

		} else if (kind.equals(IdentifierKind.VAR)) {

			return varIndex;
		}

		return 0;
	}

	public String typeOf(String name) {

        /*
        return the type of the named identifier in the current scope
        */

		if (subroutineScopeTable.containsKey(name)) {

			return subroutineScopeTable.get(name).getType();
		}

		return classScopeTable.get(name).getType();
	}


	/*
	returns the kind of the named identifier in the current scope
	if the identifier is unknown in the current scope,
	return NONE
	*/
	public IdentifierKind kindOf(String name) {
		if (subroutineScopeTable.containsKey(name)) {
			return subroutineScopeTable.get(name).getKind();
		} else if (classScopeTable.containsKey(name)) {
			return classScopeTable.get(name).getKind();
		}

		return IdentifierKind.NONE;
	}

	/*
	returns the index assigned to the named identifier
	*/
	public Integer indexOf(String name) {
		if (subroutineScopeTable.containsKey(name)) {
			return subroutineScopeTable.get(name).getIndex();
		}
		return classScopeTable.get(name).getIndex();
	}

	public void logTables() {
		System.out.println("---- class scope table ----");
		for (String name : classScopeTable.keySet()) {
			System.out.println(String.format("%s: %s\n", name, classScopeTable.get(name).toString()));
		}

		System.out.println("---- subroutine scope table ----");
		for (String name : subroutineScopeTable.keySet()) {
			System.out.println(String.format("%s: %s\n", name, subroutineScopeTable.get(name).toString()));
		}
		System.out.println();
	}

	public Boolean contains(String name) {
		if (classScopeTable.containsKey(name) || subroutineScopeTable.containsKey(name)) {
			return true;
		}
		return false;
	}
}
