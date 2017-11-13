package OptimizePackage;

import MiddleDataUtilly.QT;
import java.util.ArrayList;

/**
 * 对外的类
 */
public class Optimizer {
    private ArrayList<QT> origin_all_QTs;
    public Optimizer(ArrayList<QT> qts) {
        origin_all_QTs = qts;
    }
    public ArrayList<QT> optimize() {
        ArrayList<QT> result_QTs = new ArrayList<>();
        ArrayList<QT> cache = new ArrayList<>();
        ArrayList<QT> reference_QTs = new ArrayList<>();
        for (QT qt : origin_all_QTs) {
            if (!isArithmeticOperator(qt.getOperator())) {
                if (!cache.isEmpty()) {
                    DAG dag = new DAG(cache);
                    result_QTs.addAll(reference_QTs);
                    result_QTs.addAll(dag.generateQts());
                    cache.clear();
                    reference_QTs.clear();
                }
                result_QTs.add(qt);
                continue;
            }
            if (qt.getOperator().equals("ref")) {
                reference_QTs.add(qt);
            } else {
                cache.add(qt);
            }
        }
        return result_QTs;
    }

    /**
     * 判断是不是算术运算符（需要优化的）
     * @param operator 运算符
     * @return boolean
     */
    private static boolean isArithmeticOperator(String operator) {
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator
            .equals("/") || operator.equals(">") || operator.equals(">=") || operator.equals("<")
            || operator.equals("<=") || operator.equals("==") || operator.equals("!=") || operator
            .equals("!") || operator.equals("||") || operator.equals("&&") || operator
            .equals("ref");
    }
}
