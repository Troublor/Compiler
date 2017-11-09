package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.SymbolTable.TypeTable.TypeTable;

import java.util.Map;

/**
 * 这是一个管理变量表的类
 * 变量表往往是多个表的复合结构 需要一个从全局角度维护整个变量表集合
 */
public class VariableTableSetManager {
    //多叉树结构 Manager 在中间语言翻译的时候，需要维护一个当前活跃
    private VariableTable root,currActiveTable;
    private TypeTable typeTable;
    //维护一个类型表 查询类型的工作由Manager承包
    private int offsetTotal;
    //总的offset 再插入新表时需要用到

    private int idTotal;
    //一个自增的表id记录 可以拿来当表的计数
    private Map<Integer,VariableTable>  idVariableTableMap;

    public VariableTableSetManager(){
        typeTable = new TypeTable();
        offsetTotal = 0;
        idTotal = 1;
        root = new VariableTable(null,idTotal,offsetTotal);
        currActiveTable = root;
        idVariableTableMap.put(idTotal,currActiveTable);
        idTotal ++;
    }

    /**
     * 查询变量  返回表的id  加入到四元式中
     *
     * @param name_id 名字表示符
     * @return 所在表的id  不成功返回-1
     */
    public int requestVariable(String name_id) {
        VariableTableRow res = currActiveTable.getVariable(name_id);
        if (res != null) {
            return currActiveTable.getTable_id();
        }
        while (currActiveTable != null) {
            currActiveTable = currActiveTable.getParent();
            res = currActiveTable.getVariable(name_id);
            if (res != null)
                return currActiveTable.getTable_id();
        }
        return -1;
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


}
