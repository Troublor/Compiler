package TranslatorPackage.SymbolTable.TypeTable;

import java.util.HashMap;
import java.util.Map;


/**
 * 存储类型的表
 */
public class TypeTable {
    private Map<String,TypeTableRow> tableRowMap;
    //类型名到具体类型info的map

    public TypeTable(){
        tableRowMap = new HashMap<>();
        TypeTableRow basicType = new TypeTableRow("int");
        basicType.addField("value", 1, "basic");
        //basic 是最最基本的类型了 在生成目标代码的时候就直接分配内存大小了 不再递归沿着type往下查
        tableRowMap.put("int", basicType);

        basicType = new TypeTableRow("float");
        basicType.addField("value", 1, "basic");
        tableRowMap.put("float", basicType);

        basicType = new TypeTableRow("bool");
        basicType.addField("value", 1, "basic");
        tableRowMap.put("bool", basicType);
    }

    //从类型名获取类型info
    public TypeTableRow getTypeInfo(String type_name) {
        return tableRowMap.get(type_name);
    }

    /**
     * 新增类型 一开始只能定义类型名 必须要先添加属性 类型才有效
     *
     * @param type_name 类型
     * @return 是否添加成功 重复添加报错
     */
    public boolean addType(String type_name) {
        if (tableRowMap.get(type_name) != null || !type_name.equals("basic"))
            return false;
        tableRowMap.put(type_name,new TypeTableRow(type_name));
        return true;
    }


    /**
     * 对类型里添加属性 (域) 这是真正能存储内容的地方了
     *
     * @param type_name  要被添加的类型名
     * @param field_type 要添加进去域类型
     * @param field_name 域的名字 也不能重
     * @return 是否成功
     */
    public boolean addFieldOnType(String type_name, String field_type, String field_name) {
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
