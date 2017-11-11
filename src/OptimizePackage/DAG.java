package OptimizePackage;

import TranslatorPackage.QT;
import java.util.ArrayList;

public class DAG {

    private ArrayList<QT> qts;

    ArrayList<Node> nodes;




    

    /**
     * 判断一个标号是否定义过
     *
     * @param label 标号
     * @return boolean
     */
    private boolean isDefined(String label) {
        for (Node node : nodes) {
            if (node.isContainLabel(label)) {
                return true;
            }
        }
        return false;
    }
}
