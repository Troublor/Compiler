package TranslatorPackage.SymbolTable.VariableTable;

import TranslatorPackage.SymbolTable.SemanticException;

import java.util.HashMap;

/**
 * 变量表类 相当于变量表多叉树里的一个节点 存储该命名空间里的各种变量
 */

public class VariableTable{
    private VariableTable parent;
    //父节点 回溯查找时需要用到
    private HashMap<String,VariableTableRow> variables;
    // 以变量名作为key的map  变量名到变量具体信息的映射
    private int startOffset, currLen;
    // 表的相对于所有表的位移
    // 和当前最后一个变量末尾位置的相对于表头的位移
    //    寻址方式:  变量存储空间基址 + 当前表所在位置位移 + 当前表中变量相对于表头位置位移
    // 这是对目标代码生成时的内存信息预分配
    // 用于基址寻址
    private int table_id;
    // 表的id  在生成目标语言时 四元式中需要保存到变量的具体位置信息
    // 因而以 表.变量 的方式确定变量的具体位置
    // 需要以一个id表示表的位置 在生成目标代码时进行查找


    /**
     * 在产生一个新的块时会生成一个表
     *
     * @param parent      父节点 包含这个新块的块
     * @param id          表的id  有manager全局给出
     * @param startOffset 起始offset 也是全局给出 具体作用上面有说
     */
    public VariableTable(VariableTable parent, int id, int startOffset) {
        // id 和offset是一个外部 全局信息 需要从manager那里获得
        variables = new HashMap<>();
        this.parent = parent;
        this.startOffset = startOffset;
        this.currLen = 0;
        this.table_id = id;
    }

    /**
     * 添加变量
     *
     * @param type    变量类型 变量类型的具体信息由manager查找 表项里以string保存即可
     * @param name_id 唯一标示名
     * @param offset  变量类型的长度
     */
    //对符号表添加变量 添加成功返回新变量的表项
    public VariableTableRow addVariable(String type, String name_id, int offset, int table_id) throws SemanticException {
        //查重
        if (variables.get(name_id)!= null)
            throw new SemanticException("variable: " + name_id + " has already existed");
        //type 的合法性已由Manager的typetable查过了 这里直接添加就ok
        VariableTableRow newVariable = new VariableTableRow(name_id, type, currLen, table_id);
        variables.put(name_id, newVariable);
        currLen += offset;
        return newVariable;
    }

    public VariableTableRow getVariable(String name_id) {
        return variables.get(name_id);
    }

    public int getTableLength() {
        return currLen;
    }

    public int getTable_id() {
        return table_id;
    }

    public VariableTable getParent() {
        return parent;
    }

    protected void setStartOffset(int offset) {
        startOffset = offset;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (VariableTableRow variableTableRow : variables.values()) {
            stringBuilder.append("name: " + variableTableRow.getName_id()
                    + "type : " + variableTableRow.getTypeName()
                    + "offset: " + variableTableRow.getOffset() + "\n");
        }
        return stringBuilder.toString();
    }
}
