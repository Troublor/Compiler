package TranslatorPackage.SymbolTable.TypeTable;

import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.HashMap;
import java.util.Map;


/**
 * 存储类型的表
 */
public class TypeTable {
    private Map<String,TypeTableRow> tableRowMap;
    private String[] basic_type_names = new String[]{
            "int", "double", "char"
    };
    //类型名到具体类型info的map

    public TypeTable(){
        tableRowMap = new HashMap<>();


        for (String type_name : basic_type_names) {
            TypeTableRow basicType = new TypeTableRow(type_name);
            basicType.addField("value", 4, "basic");
            //basic 是最最基本的类型了 在生成目标代码的时候就直接分配内存大小了 不再递归沿着type往下查
            tableRowMap.put(type_name, basicType);
        }

    }

    public boolean isBasicType(String type) {
        for (String i : basic_type_names) {
            if (i.equals(type))
                return true;
        }
        return false;
    }

    //从类型名获取类型info
    public TypeTableRow getTypeInfo(String type_name) throws SemanticException {
        TypeTableRow res = tableRowMap.get(type_name);
        if (res == null)
            throw new SemanticException("type :" + type_name + " has not declare");
        return tableRowMap.get(type_name);
    }

    /**
     * 新增类型 一开始只能定义类型名 必须要先添加属性 类型才有效
     *
     * @param type_name 类型
     *  是否添加成功 重复添加类型会抛exception
     */
    public String declareType(String type_name) throws SemanticException {
        if (tableRowMap.get(type_name) != null)
            throw new SemanticException("repeating declare type, type name : " + type_name + " has occurred");
        if (!type_name.equals("basic"))
            tableRowMap.put(type_name, new TypeTableRow(type_name));
        return type_name;
    }


    /**
     * 对类型里添加属性 (域) 这是真正能存储内容的地方了
     *
     * @param type_name  要被添加的类型名
     * @param field_type 要添加进去域类型
     * @param field_name 域的名字 也不能重
     */
    public String addFieldOnType(String type_name, String field_type, String field_name) throws SemanticException {
        TypeTableRow selectedType = tableRowMap.get(type_name);
        if(selectedType.getField(field_name) != null)
            throw new SemanticException("field name : " + field_name + " has already exist");

        TypeTableRow addFieldType = tableRowMap.get(field_type);

        if(addFieldType == null)
            throw new SemanticException("no such type : " + field_type + " which add as a field");

        selectedType.addField(field_name, addFieldType.getLength(), field_type);
        return selectedType.getName();
    }

    /**
     * 新建数组类型的实时会调用这个方法
     * @param arr_elem_type_name 添加的数组内元素类型
     * @param array_len          数组长度
     * @throws SemanticException 数组元素类型未声明直接抛出异常
     */

    public String declareArrayType(String arr_elem_type_name, int array_len) throws SemanticException {
        String array_type_name = "array_" + arr_elem_type_name + "_" + array_len;
        //数组类型的类型名有这样的格式 ： array_元素类型_长度
        TypeTableRow selectedType = tableRowMap.get(array_type_name);
        if (selectedType != null)
            return selectedType.getName();
        //如果这个数组之前声明过（类型被创建过） 那么直接拿来用就好

        TypeTableRow arr_elem_type = tableRowMap.get(arr_elem_type_name);
        //检查数组类型是否存在
        if (arr_elem_type == null)
            throw new SemanticException("type : " + arr_elem_type_name + " has not declared before .");

        //添加数组类型表项
        declareType(array_type_name);
        selectedType = tableRowMap.get(array_type_name);

        //向表项中填域 以数字作为域名 便于数组的循秩访问
        for (int i = 0; i < array_len; i++)
            selectedType.addField(String.valueOf(i), arr_elem_type.getLength(), arr_elem_type.getName());

        return selectedType.getName();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (TypeTableRow eachRow : tableRowMap.values()) {
            stringBuilder.append("type name: " + eachRow.getName() + "offset" + eachRow.getLength() + "\n");
        }
        return stringBuilder.toString();
    }
}
