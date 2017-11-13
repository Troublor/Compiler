package OptimizePackage;

import MiddleDataUtilly.QT;
import java.util.HashSet;

/**
 * DAG图中的一个节点的类（只在package内可用)
 */
class Node {
    private String main_label;
    private HashSet<String> extra_labels;
    private String operator;
    private Node left_child;
    private Node middle_child;
    private Node right_child;

    /**
     * 有三个子节点的
     * 构造方法
     *
     * @param main_label 主标记
     * @param operator 操作符(如果是常数这一项为null)
     * @param left_child 左子节点
     * @param middle_child 中子节点
     * @param right_child 右子节点
     */
    public Node(String main_label, String operator,
        Node left_child, Node middle_child, Node right_child) {
        this.extra_labels = new HashSet<>();
        this.main_label = main_label;
        this.operator = operator;
        this.left_child = left_child;
        this.middle_child = middle_child;
        this.right_child = right_child;
    }

    /**
     * 只有两个子节点的
     * 构造方法
     *
     * @param main_label 主标记
     * @param operator 操作符(如果是常数这一项为null)
     * @param left_child 左子节点
     * @param right_child 右子节点
     */
    public Node(String main_label, String operator,
        Node left_child, Node right_child) {
        this.extra_labels = new HashSet<>();
        this.main_label = main_label;
        this.operator = operator;
        this.left_child = left_child;
        this.middle_child = null;
        this.right_child = right_child;
    }


    public String getMain_label() {
        return main_label;
    }

    public HashSet<String> getExtra_labels() {
        return extra_labels;
    }

    public String getOperator() {
        return operator;
    }

    public Node getLeft_child() {
        return left_child;
    }

    public Node getMiddle_child() {
        return middle_child;
    }

    public Node getRight_child() {
        return right_child;
    }


    /**
     * 判断是否包含一个标号
     *
     * @param s 标号
     * @return boolean
     */
    public boolean containLabel(String s) {
        if (main_label.equals(s)) {
            return true;
        }
        for (String label : extra_labels) {
            if (label.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是不是主标记
     *
     * @param label 标记
     * @return boolean
     */
    public boolean isMainLabel(String label) {
        return main_label.equals(label);
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("(" + operator + ")  " + main_label + " | ");
        for (String label : extra_labels) {
            r.append(label).append(",  ");
        }

        return r.toString();
    }

    /**
     * 添加标记
     * 优先级，常数 > 用户定义变量> 临时变量
     * @param label 标记
     */
    public void addLabel(String label) throws QtException{
        if (QT.isConstVariable(label)) {
            extra_labels.add(main_label);
            main_label = label;
        } else if (QT.isTemporaryVariable(label)) {
            extra_labels.add(label);
        } else {
            if (QT.isTemporaryVariable(main_label)) {
                extra_labels.add(main_label);
                main_label = label;
            } else {
                extra_labels.add(label);
            }
        }
    }
}
