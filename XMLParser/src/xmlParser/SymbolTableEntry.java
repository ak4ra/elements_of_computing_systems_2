package xmlParser;

import xmlParser.enums.IdentifierKind;

public class SymbolTableEntry {

    private String type;
    private IdentifierKind kind;
    private Integer index;

    public SymbolTableEntry( String type, IdentifierKind kind, Integer index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public IdentifierKind getKind() {
        return kind;
    }

    public Integer getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "SymbolTableEntry{" +
                "kind='" + kind + '\'' +
                ", type=" + type +
                ", index=" + index +
                '}';
    }
}
