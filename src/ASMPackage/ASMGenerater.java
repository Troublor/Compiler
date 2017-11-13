package ASMPackage;

import MiddleDataUtilly.QT;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMGenerater {

    /**
     * 变量活跃信息表
     */
    private HashMap<String, String> active_table;

    /**
     * 四元式序列
     */
    private ArrayList<QT> qts;

    /**
     * 生成的汇编语句序列
     */
    private ArrayList<ASMSentence> sentences;

    public ASMGenerater(ArrayList<QT> qts) {
        active_table = new HashMap<>();
        this.qts = qts;
        sentences = new ArrayList<>();
    }

    /**
     * 根据qts初始化变量的活跃信息表
     */
    private void initializeActiveTable() {
        for (QT qt : qts) {
            if (qt.getOperand_left() != null && !QT.isConstVariable(qt.getOperand_left())) {
                active_table
                    .put(qt.getOperand_left(),
                        QT.isTemporaryVariable(qt.getOperand_left()) ? "n" : "y");
            }
            if (qt.getOperand_right() != null && !QT.isConstVariable(qt.getOperand_right())) {
                active_table
                    .put(qt.getOperand_right(),
                        QT.isTemporaryVariable(qt.getOperand_right()) ? "n" : "y");
            }
            if (qt.getResult() != null && !QT.isConstVariable(qt.getResult())) {
                active_table
                    .put(qt.getResult(), QT.isTemporaryVariable(qt.getResult()) ? "n" : "y");
            }
        }
    }

    /**
     * 为每一个四元式的变量附加上活跃信息
     */
    private void addActiveInfomation() {
        QT qt;
        for (int i = qts.size() - 1; i >= 0; i--) {
            qt = qts.get(i);
            StringBuilder sb_result = null;
            if (qt.getResult() != null) {
                sb_result = new StringBuilder(qt.getResult());
                sb_result.append("->").append(active_table.get(qt.getResult()));
                active_table.put(qt.getResult(), "n");
            }
            StringBuilder sb_left = null;
            if (qt.getOperand_left() != null) {
                sb_left = new StringBuilder(qt.getOperand_left());
                if (!QT.isConstVariable(qt.getOperand_left())) {
                    sb_left.append("->").append(active_table.get(qt.getOperand_left()));
                    active_table.put(qt.getOperand_left(), Integer.toString(i));
                }
            }
            StringBuilder sb_right = null;
            if (qt.getOperand_right() != null) {
                sb_right = new StringBuilder(qt.getOperand_right());
                if (!QT.isConstVariable(qt.getOperand_right())) {
                    sb_right.append("->").append(active_table.get(qt.getOperand_right()));
                    active_table.put(qt.getOperand_right(), Integer.toString(i));
                }
            }

            qts.remove(i);
            qts.add(i, new QT(qt.getOperator(), (sb_left != null) ? sb_left.toString() : null,
                (sb_right != null) ? sb_right.toString() : null,
                (sb_result != null) ? sb_result.toString() : null));
        }
    }

    public void generate() {
        initializeActiveTable();
        addActiveInfomation();
    }
}
