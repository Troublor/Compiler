package TranslatorPackage.SymbolTable.TypeTable;

import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;

/**
 * 从Variable继承
 * 因为variable里保存的也是相对位移
 * 所以方法 数据都一样 满足需求
 */

public class FieldTableRow extends VariableTableRow {
    public FieldTableRow(String field_name, String field_type, int field_offset) {
        super(field_name, field_type, field_offset);
    }


    public String getFieldName() {
        return super.getName_id();
    }
}
