import enums.IdentifierKind;

public record SymbolTableEntry(String type, IdentifierKind kind, Integer index) {}
