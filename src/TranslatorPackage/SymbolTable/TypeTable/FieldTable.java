package TranslatorPackage.SymbolTable.TypeTable;

import TranslatorPackage.SymbolTable.VariableTable.VariableTable;


/**
 * 数据类型以及元素\动作都相同 从VariableTable集成
 * 重新封装了接口
 */
public class FieldTable extends VariableTable {
    public FieldTable() {
        //独立的表 parent用不上 直接为null
        // offset仅保存相对量
        super(null, 0, 0);
    }

    public boolean addField(String field_name, int field_offset, String field_type) {
        return super.addVariable(field_type, field_name, field_offset);
    }

    public FieldTableRow getField(String field_name) {
        return (FieldTableRow) super.getVariable(field_name);
    }
}
