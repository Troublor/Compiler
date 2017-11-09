package TranslatorPackage.SymbolTable.VariableTable;

public class VariableTableRow{
    private String type;
    private int offset;
    private String name_id;

    public VariableTableRow(String name_id,String type, int offset){
        this.type = type;
        this.offset = offset;
        this.name_id = name_id;
    }

    public int getOffset() {
        return offset;
    }

    public String getName_id() {
        return name_id;
    }

    public String getType() {
        return type;
    }
}
