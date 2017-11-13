package TranslatorPackage.SymbolTable;

import TranslatorPackage.SymbolTable.TypeTable.FieldTableRow;
import TranslatorPackage.SymbolTable.TypeTable.TypeTable;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableSetManager;
import TranslatorPackage.TranslatorExceptions.SemanticException;

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

    //查询类型是否存在
    public void lookupType(String type) throws SemanticException {
        typeTable.getTypeInfo(type);
    }


    //查询域的类型

    public String lookupStructFieldType(String struct_type_name, String field_name) throws SemanticException {
        TypeTableRow type_res = typeTable.getTypeInfo(struct_type_name);
        FieldTableRow field_res = type_res.getField(field_name);
        return field_res.getTypeName();
    }

    /**
     * 一个能够递归查询变量域的函数
     *
     * @param name_id    待查询的变量id
     *                   第一次查询时: 只有一个单独的用户定义标识符名 ,
     *                   此时会将变量名扩展为 表号.变量名.域.类型 .
     *                   第2至n次查询时: 此时变量名可能已为 表号.变量名.域1.域2.域x.类型 的形式.
     *                   不过没有关系 此时还是将该变量名直接传入函数的name_id ,需要访问的field传入field_id
     *                   该方法还是会返回一个正确向域步进的变量名:
     *                   表号.变量名.域1.域2.域x.域x+1.类型(域x+1的类型) 的形式
     *                   例子:
     *                   查询int a的value域  a是第一次被扫入
     *                   调用 accessVariableAndField("a","value")
     *                   return "table_id.a.value.basic"
     * @param field_name 访问的变量名的域名
     * @return 新的变量名
     * @throws SemanticException
     */
    public String accessVariableAndField(String name_id, String field_name) throws SemanticException {
        VariableTableRow result;
        //临时变量时 $txxx变量名是以的形式保存的
        //请求临时变量的时候只需要提供变量名 会去特定的临时变量表里查找

        if (field_name.equals("basic"))
            throw new SemanticException("has reach the basic type");

        int type_index = name_id.lastIndexOf(".");
        String new_name_id, new_type_name, curr_type_name;
        //第一次进入该方法访问域的变量
        if (type_index == -1) {
            //如果时临时变量 进入临时变量表查找
            if (name_id.charAt(0) == '$')
                result = variableTableSetManager.accessVariableByTableId(name_id, TEMP_VAR_TABLE_ID);
            else
                result = variableTableSetManager.requestVariable(name_id);

            if (result == null)
                throw new SemanticException("variable: " + name_id + "has not define");
            curr_type_name = result.getTypeName();
            new_name_id = result.getTable_id() + "." + name_id;

        } else {
            new_name_id = name_id.substring(0, type_index);
            curr_type_name = lookupVariableType(name_id);
            //取下上一次访问域时的类型名
            //根据这个类型名进行下一个域的访问
        }
        TypeTableRow type_info = typeTable.getTypeInfo(curr_type_name);

        try {
            FieldTableRow field_info = type_info.getField(field_name);
            new_type_name = field_info.getTypeName();
        } catch (SemanticException ee) {
            //域访问异常时
            //必须是 以int型变作为下标访问数组 才合法
            if (curr_type_name.split("_")[0].equals("array")) {
                //如果类型不是数组 肯定是访问了未定义的域
                if (ee.getMessage().split(":")[0].equals("no such field")) {
                    //如果是当前作用域的变量 直接可以作为下标
                    new_type_name = this.lookupVariableType(field_name);
                    if (!new_type_name.equals("int"))
                        //如果该变量不为int 而作为下标则不合法
                        throw new SemanticException("un_int var cannot be array index");
                } else
                    new_type_name = null;
            } else
                throw ee;
        }
        //重新生成完成当前访问的id名
        new_name_id += "." + field_name;
        //临时变量的标号id是 -1  在生成完四元式之后和普通的用户定义变量是一样的形式
        // 只不过表id是-1 说明是临时变量表里的东西
        return new_name_id + "." + new_type_name;
    }


    //在目前作用域查询一个变量的类型 用于表达式求值时进行类型判断
    public String lookupVariableType(String name_id) throws SemanticException {
        String format_[] = name_id.split("\\.");
        //第一次进入符号表系统查询 没有之前的附加后缀时
        if (format_.length <= 1) {
            format_ = new String[]{name_id};
            VariableTableRow result = variableTableSetManager.requestVariable(format_[0]);
            if (result == null)
                throw new SemanticException("variable: " + name_id + " has not declare");
            //该变量之前从未进入符号表查询 没有格式化的 名.域.类型 结构
            // 采用从符号表内查询的方法
            return result.getTypeName();
        } else {
            String type_name = format_[format_.length - 1];
            return type_name;
        }
    }


    public String addTempVariable(String type_name) throws SemanticException {
        VariableTableRow res = variableTableSetManager.addTempVariable(type_name);
        return res.getName_id();
    }


    public boolean isVariableExist(String var_name) {
        if (variableTableSetManager.requestVariable(var_name) == null)
            return false;
        return true;
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

