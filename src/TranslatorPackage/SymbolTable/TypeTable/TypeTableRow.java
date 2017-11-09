package TranslatorPackage.SymbolTable.TypeTable;


public class TypeTableRow {
    private String name;
    //类型名
    private int offset;
    //  类型的偏移
    private FieldTable fields;


    public TypeTableRow(String name){
        //type 中命名空间的定义不支持嵌套 所以直接null了
        fields = new FieldTable();
        offset = 0;
    }

    public boolean addField(String field_name,int field_offset,String field_type){
        boolean res = fields.addField(field_name, field_offset, field_type);
        if (res)
            offset += field_offset;
        return res;

    }

    public FieldTableRow getField(String field_name) {
        return (FieldTableRow) fields.getField(field_name);
    }

    public int getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

}
