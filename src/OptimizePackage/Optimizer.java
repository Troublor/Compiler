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

    /**
     * 对所有的QT进行分析
     * @return 分析完成了的QT
     */
    public ArrayList<QT> optimize() throws QtException{
        ArrayList<QT> result_QTs = new ArrayList<>();
        ArrayList<QT> cache = new ArrayList<>();
        ArrayList<QT> reference_QTs = new ArrayList<>();
        for (QT qt : origin_all_QTs) {
            if (!isArithmeticOperator(qt.getOperator())) {
                if (!cache.isEmpty()) {
                    DAG dag = new DAG(cache);
                    cache = dag.optimite();
                    QT qt1;
                    for (int i = 0; i < cache.size(); i++) {
                        qt1 = cache.get(i);
                        if (qt1.getOperand_left() != null
                            && qt1.getOperand_left().split("\\.").length == 4) {
                            for (QT qt2 : reference_QTs) {
                                if (qt2.getResult().equals(qt1.getOperand_left().split("\\.")[2])) {
                                    cache.add(i, qt2);
                                    i++;
                                }
                            }
                        }
                        if (qt1.getOperand_right() != null
                            && qt1.getOperand_right().split("\\.").length == 4) {
                            for (QT qt2 : reference_QTs) {
                                if (qt2.getResult().equals(qt1.getOperand_right().split("\\.")[2])) {
                                    cache.add(i, qt2);
                                    i++;
                                }
                            }
                        }
                        if (qt1.getResult() != null
                            && qt1.getResult().split("\\.").length == 4) {
                            for (QT qt2 : reference_QTs) {
                                if (qt2.getResult().equals(qt1.getResult().split("\\.")[2])) {
                                    cache.add(i, qt2);
                                    i++;
                                }
                            }
                        }

                    }
                    result_QTs.addAll(cache);
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
    public static boolean isArithmeticOperator(String operator) {
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator
            .equals("/") || operator.equals("=") || operator.equals(">") || operator.equals(">=")
            || operator.equals("<") || operator.equals("<=") || operator.equals("==") || operator
            .equals("!=") || operator.equals("!") || operator.equals("||") || operator.equals("&&")
            || operator.equals("ref");
    }
}
