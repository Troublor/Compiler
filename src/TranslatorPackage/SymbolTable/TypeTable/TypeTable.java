package TranslatorPackage.SymbolTable.TypeTable;

import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    private Map<String,TypeTableRow> tableRowMap;

    public TypeTable(){
        tableRowMap = new HashMap<>();
    }

    public TypeTableRow getTypeInfo(String type_name){
        return tableRowMap.get(type_name);
    }

    public boolean addType(String type_name){
        if (tableRowMap.get(type_name) != null)
            return false;
        tableRowMap.put(type_name,new TypeTableRow(type_name));
        return true;
    }

    public boolean addFieldOnType(String type_name,String field_type,String field_name){
        TypeTableRow selectedType = tableRowMap.get(type_name);
        if(selectedType == null)
            return false;
        if(selectedType.getField(field_name) != null)
            return false;

        TypeTableRow addFieldType = tableRowMap.get(field_type);

        if(addFieldType == null)
            return false;

        return selectedType.addField(field_name,addFieldType.getOffset(),field_type);
    }
}
