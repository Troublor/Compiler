package TranslatorPackage.SymbolTable.TypeTable;


import TranslatorPackage.TranslatorExceptions.SemanticException;

public class TypeTableRow {
    private String name;
    //类型名
    private int lenth;
    //  类型的偏移
    private FieldTable fields;


    public TypeTableRow(String name){
        this.name = name;
        fields = new FieldTable();
        lenth = 0;
    }


    // 已在上层进行过重复检查
    public void addField(String field_name, int field_offset, String field_type) {
        fields.addField(field_name, field_offset, field_type);
        lenth += field_offset;

    }

    public FieldTableRow getField(String field_name) {
        FieldTableRow field = fields.getField(field_name);
        return fields.getField(field_name);
    }

    public int getLength() {
        return lenth;
    }

    public String getName() {
        return name;
    }

}
