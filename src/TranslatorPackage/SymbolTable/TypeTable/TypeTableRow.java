package TranslatorPackage.SymbolTable.TypeTable;


public class TypeTableRow {
    private String name;
    //类型名
    private int offset;
    //  类型的偏移
    private FieldTable fields;


    public TypeTableRow(String name){
        this.name = name;
        fields = new FieldTable();
        offset = 0;
    }


    // 已在上层进行过重复检查
    public void addField(String field_name, int field_offset, String field_type) {
        fields.addField(field_name, field_offset, field_type);
        offset += field_offset;

    }

    public FieldTableRow getField(String field_name) {
        return fields.getField(field_name);
    }

    public int getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

}
