package TranslatorPackage.SymbolTable.TypeTable;

import TranslatorPackage.SymbolTable.VariableTable.VariableTable;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;

public class TypeTableRow {
    private String name;
    private int offset;//  for store
    private VariableTable fields;

    public TypeTableRow(String name){
        //type 中命名空间的定义不支持嵌套
        fields = new VariableTable(null);
        offset = 0;
    }

    public boolean addField(String field_name,int field_offset,String field_type){
        boolean res = fields.addVariable(field_type,field_name,field_offset);
        if (res)
            offset += field_offset;
        return res;

    }

    public VariableTableRow getField(String field_name){
        return fields.getVariable(field_name);
    }

    public int getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

}
