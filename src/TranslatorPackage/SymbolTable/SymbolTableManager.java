package TranslatorPackage.SymbolTable;

import TranslatorPackage.SymbolTable.FunctionTable.FunctionTable;
import TranslatorPackage.SymbolTable.FunctionTable.FunctionTableRow;
import TranslatorPackage.SymbolTable.TypeTable.FieldTableRow;
import TranslatorPackage.SymbolTable.TypeTable.TypeTable;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTableSetManager;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.ArrayList;
import java.util.List;

public class SymbolTableManager {


    private TypeTable typeTable = new TypeTable();
    private VariableTableSetManager variableTableSetManager = new VariableTableSetManager(typeTable);
    private FunctionTable functionTable = new FunctionTable();

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
        String new_name_id, new_type_name,
                curr_type_name, suffix_type_name = null;
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
            suffix_type_name = curr_type_name;

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
            if (new_type_name.equals("basic"))
                suffix_type_name = curr_type_name;
            else
                suffix_type_name = new_type_name;

        } catch (SemanticException ee) {
            //域访问异常时
            //必须是 以int型变作为下标访问数组 才合法
            if (curr_type_name.split("_")[0].equals("array")) {
                //如果类型不是数组 肯定是访问了未定义的域
                if (ee.getMessage().split(":")[0].equals("no such field")) {
                    //如果是当前作用域的变量 直接可以作为下标
                    new_type_name = this.lookupVariableType(field_name);
                    suffix_type_name = new_type_name;
                    if (!new_type_name.equals("int"))
                        //如果该变量不为int 而作为下标则不合法
                        throw new SemanticException("un_int var cannot be array index");
                } else
                    throw ee;

            } else
                throw ee;
        }
        //重新生成完成当前访问的id名
        //临时变量的标号id是 -1  在生成完四元式之后和普通的用户定义变量是一样的形式
        // 只不过表id是-1 说明是临时变量表里的东西
        if (!new_type_name.equals("basic"))
            new_name_id += "." + field_name;

        return new_name_id + "." + suffix_type_name;
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

    public boolean isBasicType(String type_name) {
        return typeTable.isBasicType(type_name);
    }


    public String addTempVariable(String type_name) throws SemanticException {
        VariableTableRow res = variableTableSetManager.addTempVariable(type_name);
        return res.getName_id();
    }


    public VariableTableRow lookupVariable(String var_name) throws SemanticException {
        VariableTableRow res = variableTableSetManager.requestVariable(var_name);
        if (res == null)
            throw new SemanticException("variable" + var_name + " has not declared ");
        return res;
    }


    //进入一个新的块 时需要创建一个新的变量表
    public void stepIntoNewBlock() {
        variableTableSetManager.addTable();
    }

    public void stepBackBlock() {
        variableTableSetManager.traceBackToParentBlock();
    }


    /**
     * 定义函数时 对符号表体系中函数表新增一条表项
     *
     * @param func_name      函数名
     * @param return_type    返回类型
     * @param entry_qt_index 入口的四元式序号 在生成目标代码时回填为指令地址
     * @return 定义成功后的函数名
     * @throws SemanticException 重复定义
     */
    public String definefunction(String func_name, String return_type, int entry_qt_index) throws SemanticException {
        return functionTable.defineFunction(func_name,
                variableTableSetManager.getCurrActiveTable(),
                return_type,
                entry_qt_index);
    }


    /**
     * 向函数添加新的形参
     *
     * @param func_name  函数名
     * @param param_name 形参名
     * @param param_type 形参类型名
     * @throws SemanticException 类型名不存在 变量名重复定义
     */
    public void addParamOnfunc(String func_name, String param_name, String param_type) throws SemanticException {
        functionTable.addParamOnFunction(func_name, param_name, typeTable.getTypeInfo(param_type));
    }


    public void checkFuncName(String func_name) throws SemanticException {
        FunctionTableRow res = functionTable.getFuntionInfo(func_name);
        if (res == null)
            throw new SemanticException("func: " + func_name + " has not declare");
    }

    /**
     * 函数传参时 参数匹配检查
     *
     * @param func_name            函数名
     * @param param_type_name_list 参数类型列表
     * @return 函数形参符号表
     * @throws SemanticException 类型匹配失败
     */
    public List<String> checkFuncParams(String func_name, List<String> param_type_name_list) throws SemanticException {
        FunctionTableRow target_func = functionTable.getFuntionInfo(func_name);
        List<VariableTableRow> requested_param_list = target_func.getParamList();
        List<String> formal_param_names = new ArrayList<>();
        int requested_param_list_size = requested_param_list.size();
        int i;
        for (i = 0; i < param_type_name_list.size(); i++) {
            if (requested_param_list_size >= i) {
                //判断下标是否超另一个数组长度
                VariableTableRow param_info = requested_param_list.get(i);
                //如果所需和所给变量类型都为基本变量 则不需要类型匹配
                if (!(typeTable.isBasicType(param_info.getTypeName())
                        && typeTable.isBasicType(param_type_name_list.get(i)))) {
                    if (!param_type_name_list.get(i).matches(param_info.getTypeName())) {
                        String err_msg = String.format("at call func:%s, request %s ,given %s",
                                func_name, param_info.getTypeName(), param_type_name_list.get(i));
                        throw new SemanticException(err_msg);
                    } else {
                        String param_id = param_info.getName_id();
                        String param_type = param_info.getTypeName();
                        int table_id = param_info.getTable_id();
                        formal_param_names.add(table_id + "." + param_id + "." + param_type);
                    }
                } else {
                    formal_param_names.add(
                            accessVariableAndField(requested_param_list.get(i).getName_id(), "value"));
                }

            }//if size judge
            else {
                String err_msg = "too much args put into func: " + func_name;
                throw new SemanticException(err_msg);
            }
        }//for
        if (i != requested_param_list_size - 1)
            throw new SemanticException("too few args put into func");

        return formal_param_names;
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

