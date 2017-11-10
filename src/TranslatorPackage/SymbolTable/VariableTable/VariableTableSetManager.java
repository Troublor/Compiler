package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.SymbolTable.SemanticExcption;
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

    private int idTotal;
    //一个自增的表id记录 可以拿来当表的计数
    private Map<Integer,VariableTable>  idVariableTableMap;

    public VariableTableSetManager(TypeTable typeTable) {
        this.typeTable = typeTable;
        offsetTotal = 0;
        idTotal = 1;
        idVariableTableMap = new HashMap<>();
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
        VariableTableRow res = currActiveTable.getVariable(name_id);
        if (res != null) {
            return res;
        }
        VariableTable currActiveTableStore = currActiveTable;
        while (currActiveTable != null) {
            traceBackToParentBlock();
            res = currActiveTable.getVariable(name_id);
            if (res != null) {
                currActiveTable = currActiveTableStore;
                return res;
            }
        }
        return null;
    }

    public VariableTableRow addVariable(String name_id, String type_name) throws SemanticExcption {
        TypeTableRow typeRes = typeTable.getTypeInfo(type_name);
        if (typeRes == null)
            throw new SemanticExcption("type : " + type_name + "has not declared");

        return currActiveTable.addVariable(type_name, name_id, typeRes.getLength(), currActiveTable.getTable_id());
    }


    /**
     * 有新块产生的 需要添加一个表
     */
    public void addTable(){
        offsetTotal += currActiveTable.getTableOffset();
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





}
