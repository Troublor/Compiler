package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.TranslatorExceptions.SemanticException;
import TranslatorPackage.SymbolTable.TypeTable.TypeTable;
import TranslatorPackage.SymbolTable.TypeTable.TypeTableRow;

import java.util.HashMap;
import java.util.Map;

/**
 * 这是一个管理变量表的类
 * 变量表往往是多个表的复合结构 需要一个从全局角度维护整个变量表集合
 */
public class VariableTableSetManager {
    //多叉树结构 Manager 在中间语言翻译的时候，需要维护一个当前活跃
    private VariableTable currActiveTable;
    private TypeTable typeTable;
    //维护一个类型表 查询类型的工作由Manager承包
    private int offsetTotal;
    //总的offset 再插入新表时需要用到

    private TempVariableTable tempVariableTable;

    private int idTotal;
    //一个自增的表id记录 可以拿来当表的计数
    private Map<Integer,VariableTable>  idVariableTableMap;

    public VariableTableSetManager(TypeTable typeTable) {
        this.typeTable = typeTable;
        offsetTotal = 0;
        idTotal = 1;
        idVariableTableMap = new HashMap<>();

        tempVariableTable = new TempVariableTable();
        idVariableTableMap.put(-1, tempVariableTable);
        //临时变量表号为-1  放在VariableTableManager里统一管理
        //生成四元式的时候也是-1
        currActiveTable = new VariableTable(null, idTotal, offsetTotal);
        idVariableTableMap.put(idTotal,currActiveTable);
        idTotal ++;
    }

    /**
     * 查询变量  返回表的id  加入到四元式中
     *
     * @param name_id 名字表示符
     *
     */

    public VariableTableRow requestVariable(String name_id) {
        VariableTableRow res;
        if (name_id.charAt(0) == '$') {
            res = tempVariableTable.getVariable(name_id);
            return res;
        }
        res = currActiveTable.getVariable(name_id);
        if (res != null) {
            return res;
        }
        VariableTable currActiveTableStore = currActiveTable;
        while (currActiveTable.getParent() != null) {
            traceBackToParentBlock();
            res = currActiveTable.getVariable(name_id);
            if (res != null) {
                currActiveTable = currActiveTableStore;
                return res;
            }
        }
        return null;
    }

    public void addVariable(String name_id, String type_name) throws SemanticException {
        TypeTableRow typeRes = typeTable.getTypeInfo(type_name);
        if (typeRes == null)
            throw new SemanticException("type : " + type_name + "has not declared");

        currActiveTable.addVariable(type_name, name_id, typeRes.getLength());
    }


    public VariableTableRow addTempVariable(String type_name) throws SemanticException {
        return tempVariableTable.addTempVariable(type_name);
    }
    /**
     * 有新块产生的 需要添加一个表
     */
    public void addTable(){
        offsetTotal += currActiveTable.getTableLength();
        currActiveTable = new VariableTable(currActiveTable,idTotal,offsetTotal);
        idVariableTableMap.put(idTotal,currActiveTable);
        idTotal ++;
    }

    public void traceBackToParentBlock() {
        currActiveTable = currActiveTable.getParent();
    }


    public VariableTableRow accessVariableByTableId(String name_id, int table_id) {
        VariableTable variableTable = idVariableTableMap.get(table_id);
        return variableTable.getVariable(name_id);

    }


    public void assignTempVarPos() {
        tempVariableTable.setStartOffset(this.offsetTotal);
    }


    public VariableTable getCurrActiveTable() {
        return currActiveTable;
    }
}
