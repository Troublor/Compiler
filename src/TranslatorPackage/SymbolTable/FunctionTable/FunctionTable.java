package TranslatorPackage.SymbolTable.FunctionTable;

import TranslatorPackage.SymbolTable.TypeTable.FieldTableRow;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;
import TranslatorPackage.SymbolTable.VariableTable.VariableTable;
import TranslatorPackage.TranslatorExceptions.SemanticException;

import java.util.HashMap;
import java.util.Map;

public class FunctionTable {

    private Map<String, FunctionTableRow> functions_map = new HashMap<>();


    /**
     * 函数表
     * 函数的形参表与函数所在语句块的变量表是共用的 便与变量的使用
     * 定义函数 传入函数名 新创建的变量表 返回类型
     * 返回类型需要在 上层进行检查
     *
     * @param func_name      函数名
     * @param param_table    参数表共用的变量表引用 从上传Manager 传入
     * @param return_type    返回类型
     * @param entry_qt_index 入口的四元式序号 在生成目标代码时回填为指令地址
     * @return 定义成功后的函数名
     * @throws SemanticException 重复定义
     */
    public String defineFunction(String func_name, VariableTable param_table, String return_type, int entry_qt_index) throws SemanticException {
        if (getFuntionInfo(func_name) != null)
            throw new SemanticException("redeclare function: " + func_name);
        param_table.addVariable("double", "#ret_val", 4);
        FunctionTableRow new_func = new FunctionTableRow(param_table, func_name, return_type, entry_qt_index);
        functions_map.put(func_name, new_func);
        return func_name;
    }


    /**
     * 添加参数
     * 该函数有上层Manager包装
     *
     * @param func_name  要添加的函数名
     * @param param_name 要添加的变量名
     * @param type_info  变量的类型信息
     * @throws SemanticException
     */
    public void addParamOnFunction(String func_name, String param_name, TypeTableRow type_info) throws SemanticException {
        FunctionTableRow add_target = getFuntionInfo(func_name);
        if (add_target == null)
            throw new SemanticException("doesn't have func: " + func_name);
        add_target.addParam(param_name, type_info);
    }


    /**
     * 查询函数的具体信息
     * 调用时会用到
     *
     * @param func_name 函数名
     * @return 函数具体数据表项
     */
    public FunctionTableRow getFuntionInfo(String func_name) {
        FunctionTableRow res = functions_map.get(func_name);
        return res;
    }



}
