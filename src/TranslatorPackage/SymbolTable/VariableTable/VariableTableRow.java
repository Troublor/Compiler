package TranslatorPackage.SymbolTable.VariableTable;

/**
 * 变量表里的表项
 * 保存命名空间里各个变量的具体信息
 */


public class VariableTableRow{
    private String type;
    //变量类型
    private int offset;
    //相对于表头的偏移量 在生成目标语言是需要用它进行基址寻址
    private String name_id;
    //标识符名字 需要用它来在变量表中找寻该变量
    //必须是唯一的 在插入时  变量表会进行检查

    public VariableTableRow(String name_id, String type, int offset) {
        this.type = type;
        this.offset = offset;
        this.name_id = name_id;
    }

    public int getOffset() {
        return offset;
    }

    public String getName_id() {
        return name_id;
    }

    public String getType() {
        return type;
    }
}
