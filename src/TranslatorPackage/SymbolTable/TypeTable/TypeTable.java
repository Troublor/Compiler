package TranslatorPackage.SymbolTable.TypeTable;

import TranslatorPackage.SymbolTable.SemanticExcption;

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
     *  是否添加成功 重复添加类型会抛exception
     */
    public void addType(String type_name) throws SemanticExcption {
        if (tableRowMap.get(type_name) != null || !type_name.equals("basic"))
            throw new SemanticExcption("repeating type occurred");
        tableRowMap.put(type_name,new TypeTableRow(type_name));
    }


    /**
     * 对类型里添加属性 (域) 这是真正能存储内容的地方了
     *
     * @param type_name  要被添加的类型名
     * @param field_type 要添加进去域类型
     * @param field_name 域的名字 也不能重
     */
    public void addFieldOnType(String type_name, String field_type, String field_name) throws SemanticExcption {
        TypeTableRow selectedType = tableRowMap.get(type_name);
        if(selectedType.getField(field_name) != null)
            throw new SemanticExcption("field name : " + field_name + " has already exist");

        TypeTableRow addFieldType = tableRowMap.get(field_type);

        if(addFieldType == null)
            throw new SemanticExcption("no such type : " + field_type + " which add as a field");

        selectedType.addField(field_name, addFieldType.getOffset(), field_type);
    }

    /**
     * 新建数组类型的实时会调用这个方法
     *
     * @param arr_elem_type_name 添加的数组内元素类型
     * @param array_len          数组长度
     * @throws SemanticExcption 数组元素类型未声明直接抛出异常
     */

    public void addFieldOnArrayType(String arr_elem_type_name, int array_len) throws SemanticExcption {
        String array_type_name = "array_" + arr_elem_type_name + "_" + array_len;
        //数组类型的类型名有这样的格式 ： array_元素类型_长度
        TypeTableRow selectedType = tableRowMap.get(array_type_name);
        if (selectedType != null)
            return;
        //如果这个数组之前声明过（类型被创建过） 那么直接拿来用就好

        TypeTableRow arr_elem_type = tableRowMap.get(arr_elem_type_name);
        //检查数组类型是否存在
        if (arr_elem_type == null)
            throw new SemanticExcption("type : " + arr_elem_type_name + " has not declared before .");

        //添加数组类型表项
        addType(array_type_name);
        selectedType = tableRowMap.get(array_type_name);

        //向表项中填域 以数字作为域名 便于数组的循秩访问
        for (int i = 0; i < array_len; i++)
            selectedType.addField(String.valueOf(i), arr_elem_type.getOffset(), arr_elem_type.getName());

    }
}
