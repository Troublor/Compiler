package OptimizePackage;

import java.util.HashSet;

public class Node {
    private String id;
    private String main_label;
    private HashSet<String> extra_labels;
    private String operator;
    private Node left_child;
    private Node middle_child;
    private Node right_child;

    public Node(String id, String main_label, HashSet<String> extra_labels, String operator,
        Node left_child, Node middle_child, Node right_child) {
        this.id = id;
        this.main_label = main_label;
        this.extra_labels = extra_labels;
        this.operator = operator;
        this.left_child = left_child;
        this.middle_child = middle_child;
        this.right_child = right_child;
    }

    public String getId() {
        return id;
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
     * 格式化标记，调整优先级，将主标记变为用户定义变量，临时变量放入附加标记中
     * （如果有用户定义变量在主标记中的话）
     */
    public void formatLabels() {
        if (this.isTemporaryVariable(main_label)) {
            extra_labels.add(main_label);
            for (String label : extra_labels) {
                if (!this.isTemporaryVariable(label)) {
                    main_label = label;
                    break;
                }
            }
            extra_labels.remove(main_label);
        }
    }

    /**
     * 判断是否是临时变量
     *
     * TODO
     * 根据临时变量符号表，写判断是否是临时变量的逻辑
     *
     * @param s 要判断的变量
     * @return boolean
     */
    private boolean isTemporaryVariable(String s) {

        return false;
    }

    /**
     * 判断是否包含一个标号
     *
     * @param s 标号
     * @return boolean
     */
    public boolean isContainLabel(String s) {
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
}
