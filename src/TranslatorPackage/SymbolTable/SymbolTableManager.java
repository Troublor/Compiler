package TranslatorPackage.SymbolTable;

import TranslatorPackage.SymbolTable.TypeTable.TypeTable;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableSetManager;

public class SymbolTableManager {


    private TypeTable typeTable = new TypeTable();
    private VariableTableSetManager variableTableSetManager = new VariableTableSetManager(typeTable);

    public static final int TEMP_VAR_TABLE_ID = -1;

    //声明结构体类型 目前用不上 hhh'
    public String declareStructType(String type_name) throws SemanticException {
        return typeTable.declareType(type_name);
    }

    //定义结构体里的域
    public String defineFieldOnStructType(String type_name, String field_type, String field_name) throws SemanticException {
        return typeTable.addFieldOnType(type_name, field_type, field_name);
    }


    //定义变量 定义失败(重复 类型未定义)会抛异常
    public void defineVariable(String name_id, String type) throws SemanticException {
        variableTableSetManager.addVariable(name_id, type);
    }

    // 定义数组类型 返回数组类型的格式化的类型名字符串: array_类型名_长度
    public String defineArrayType(String arr_elem_type_name, int array_len) throws SemanticException {
        return typeTable.declareArrayType(arr_elem_type_name, array_len);
    }

    //访问变量的域  在生成求值的四元式需要用到
    //返回也是一个格式化的字符串: 所在符号表id.变量名.域名
    //为了四元式可读性 这个格式化串 在后面目标代码生成的时候再转为偏移量
    // 要是访问出错也是报异常
    public String accessVariableAndField(String name_id, String field_name) throws SemanticException {
        VariableTableRow result;
        //临时变量时 $txxx变量名是以的形式保存的
        //请求临时变量的时候只需要提供变量名 会去特定的临时变量表里查找

        if (name_id.charAt(0) == '$')
            result = variableTableSetManager.accessVariableByTableId(name_id, TEMP_VAR_TABLE_ID);
        else
            result = variableTableSetManager.requestVariable(name_id);

        if (result == null)
            throw new SemanticException("variable: " + name_id + "has not define");
        TypeTableRow typeInfo = typeTable.getTypeInfo(lookupVariableType(name_id));
        typeInfo.getField(field_name);
        //临时变量的标号id是 -1  在生成完四元式之后和普通的用户定义变量是一样的形式
        // 只不过表id是-1 说明是临时变量表里的东西
        return result.getTable_id() + "." + result.getName_id() + "." + field_name;
    }


    //在目前作用域查询一个变量的类型 用于表达式求值时进行类型判断
    public String lookupVariableType(String name_id) throws SemanticException {
        VariableTableRow result = variableTableSetManager.requestVariable(name_id);
        if (result == null)
            throw new SemanticException("variable: " + name_id + "has not declare");
        return result.getType();
    }


    public String addTempVariable(String type_name) throws SemanticException {
        VariableTableRow res = variableTableSetManager.addTempVariable(type_name);
        return res.getName_id();
    }


    //进入一个新的块 时需要创建一个新的变量表
    public void stepIntoNewBlock() {
        variableTableSetManager.addTable();
    }

    public void stepBackBlock() {
        variableTableSetManager.traceBackToParentBlock();
    }





    /*
            目标代码生成部分
     */


    public String lookUpVariableOffset(String QT_identifier) throws SemanticException {

        return "";

    }

    public void assignTempVariableTablePos() {
        variableTableSetManager.assignTempVarPos();
    }
}

