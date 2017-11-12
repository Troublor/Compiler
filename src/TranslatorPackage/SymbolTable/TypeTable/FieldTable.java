package TranslatorPackage.SymbolTable.TypeTable;


import java.util.HashMap;
import java.util.Map;


/**
 */
public class FieldTable {
    private Map<String, FieldTableRow> fieldsMap;
    private int length;
    public FieldTable() {
        fieldsMap = new HashMap<>();
        length = 0;
    }

    public boolean addField(String field_name, int field_offset, String field_type) {
        FieldTableRow res = getField(field_name);
        if (res != null)
            return false;
        fieldsMap.put(field_name, new FieldTableRow(field_name, field_type, length));
        length += field_offset;
        return true;
    }

    public FieldTableRow getField(String field_name) {
        return fieldsMap.get(field_name);
    }
}
