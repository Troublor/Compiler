package ASMPackage;

import MiddleDataUtilly.QT;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ASMArith {
    // represent 4 general registers: eax, ebx, ecx, edx
    private Map<String, register> registers = new HashMap<>();
//    private ASMArith arith;
    private int cur_index = 0;
    private List<QT> qts;
    private ArrayList<ASMSentence> asms = new ArrayList<>();
    private ArrayList<register> order;
    private int end_index;

    public ASMArith(List<QT> qts) {
        this.qts = qts;
        end_index = qts.size();
        registers.put("eax", eax);
        registers.put("ebx", ebx);
        registers.put("ecx", ecx);
        registers.put("edx", edx);
        order = new ArrayList<>(registers.values());
        Collections.sort(order);
    }

    class register implements Comparable<register>{
        String name;
        String content;
        int active_index;

        register(String register_name, String id, int active_index) {
            this.name = register_name;
            this.content = id;
            this.active_index = active_index;
        }
        boolean contains(String id) {
            return this.content != null && this.content.equals(id);
        }

        @Override
        public int compareTo(@NotNull ASMArith.register o) {
            int left_life = this.active_index - cur_index, right_life = o.active_index - cur_index;
            // left
            if (available()) {
                left_life = end_index + 2;
            }
            else if (left_life < 0) {
                left_life = end_index + 1;
            }
            // right is the same
            if (o.available()) {
                right_life = end_index + 2;
            }
            else if (right_life < 0) {
                right_life = end_index + 1;
            }

            return right_life - left_life;
        }

        // 如果该寄存器没有存数据，返回true
        public boolean available() {
            return content == null;
        }

        // 将寄存器的数据置空
        public void free(){
            this.content = null;
        }

        // 表明 该寄存器会被id占用
        public void occupyWith(String id){
            assert !id.startsWith("const");
            this.content = id;
            try {
                String info = getActiveInfo(id);
                // 经过优化后的四元式的每个结果一定是活跃的，（假设不活跃，可以想到一定这条四元式一定会被优化掉）
                this.active_index = info.equals("y") ? end_index : Integer.valueOf(info);
            }
            catch (ASMException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        @Override
        public String toString() {
            return "register{" +
                    "name='" + name + '\'' +
                    ", content='" + content + '\'' +
                    ", active_index=" + active_index +
                    '}';
        }
    }

    register eax = new register("eax",null, 0);
    register ebx = new register("ebx",null, 0);
    register ecx = new register("ecx",null, 0);
    register edx = new register("edx",null, 0);
//    register M = new register("M", null,0);


    void produce(ASMSentence sentence){
        asms.add(sentence);
    }


    // todo : only allow operation of same type
    ArrayList<ASMSentence> getResult() throws ASMException {

        for (QT qt : qts) {
            String operator = qt.getOperator();
            String type = getType(qt.getResult());
            if (type.equals("int")) {
                if (operator.equals("+")) add(qt);
                if (operator.equals("-")) sub(qt);
            }
            else if (type.equals("double")) {

            }
            // char
            else {
            }
            ++cur_index;
        }
        return this.asms;
    }

    // oprand form: 1.a.int->X    2.b.double->X  2.array.a.int->X    const int_1  const double_3.14
    private String getType(String operand) throws ASMException {
        Pattern type_pattern = Pattern.compile(".*(int|double|char).*");
        Matcher m = type_pattern.matcher(operand);
        if (m.find()) {
            return m.group(1);
        }
        throw new ASMException("constant type not matched");
    }


    // return result's occupied register, and left_operand is already in that register
    private register arrange(String left_operand, String result) {
        register Ri = inWhichRegister(left_operand);
        if (Ri != null) {
            if (isActive(left_operand)) {
                if (!left_operand.equals(result)) {
                    // 转移B
                    register Rj = getAvailableRegister();
                    if (Rj != null ) {
                        produce(new ASMSentence("mov", Ri.name, Rj.name));
                        Rj.occupyWith(left_operand);
                        return Ri;
                    }
                    else {
                        produce(new ASMSentence("mov",toAddress(left_operand), Ri.name));
                        return Ri;
                    }
                }
                // B == A
                else {
                    return Ri;
                }
            }
            // B is not active
            else {
                // no need to put b to memory
                return Ri;
            }
        }
        // B not in register
        else {
            Ri = getAvailableRegister();
            // has available register
            if (Ri != null) {
                Ri.occupyWith(result);
                produce(new ASMSentence("mov", Ri.name, toAddress(left_operand)));
                return Ri;
            }
            // occupyWith register being used
            else {
                Ri = arrangeRegister();
                Ri.occupyWith(result);
                produce(new ASMSentence("mov", Ri.name, toAddress(left_operand)));
                return Ri;
            }
        }

    }

    // 定位 id 所在的位置，如果在寄存器，返回寄存器名字，如果在内存，调用toAddrees(),返回在内存中的表示形式
    private String locate(String id) {
        assert !isConstant(id);
        register r = inWhichRegister(id);
        if (r == null) {
            return toAddress(id);
        }
        return r.name;
    }

    private String toASMForm(String item) {
        // not constant, directly use locate()
        if (!isConstant(item)) return locate(item);
        return getValue(item);
    }

    private boolean isActive(String id) {
        Pattern p = Pattern.compile("(.*)(->)(\\d+|y)");
        Matcher m = p.matcher(id);
        return m.find();
    }

    private String getActiveInfo(String id) throws ASMException{
        assert !id.startsWith("const");
        Pattern p = Pattern.compile("(.*)(->)(\\d+|y|n)");
        Matcher m = p.matcher(id);
        if (m.find()) return m.group(3);
        throw new ASMException("invalid active info");
    }


    // todo: mov ri, 1.b.int -> mov ri, b.offset
    private String toAddress(String id) {
        return id;
    }


    // 选中content 所在的寄存器
    private register inWhichRegister(String content) {
        for (register r : registers.values()) {
            if (r.contains(content)) return r;
        }
        return null;
    }


    // 返回一个可用的寄存器，如果有空闲寄存器，会立刻返回空闲寄存器
    // 如果没有空闲寄存器，会选中一个 包含 距离活跃点最远的变量 的寄存器，将那个变量存回内存，并占用它
    private register arrangeRegister() {
        register selected = null;
        if ((selected = getAvailableRegister()) != null) {
            selected.free();
            return selected;
        }
        else {
            Collections.sort(order);
            selected = order.get(0);
            // seleted 原本被占用, 将内容存到内存中
            String id = selected.content;
            selected.free();
            produce(new ASMSentence("move", toAddress(id), selected.name));
            return selected;
        }
    }

    // 选择一个 空闲（不包含数据）的寄存器
    private register getAvailableRegister() {
        Collections.sort(order);
        if (order.get(0).available()) return order.get(0);
        else return null;
    }

    //    String[] registers = {"","","",""};


    // 输入为标志过 活跃 的QT
    // todo: 替换为offset, 其实我觉得更好的是用 id_tableid 或者其他字符串 替换，产生的汇编更有可读性
    void addOrSub(QT qt, String operator) {
        String left_operand = qt.getOperand_left(), right_operand = qt.getOperand_right(), result = qt.getResult();
        if (isConstant(left_operand)) {
            register selected = arrangeRegister();
            produce(new ASMSentence("mov", selected.name, toASMForm(left_operand)));
            produce(new ASMSentence(operator, selected.name, toASMForm(right_operand)));
            selected.occupyWith(result);
        }
        else {
            register selected = arrange(left_operand, result);
            produce(new ASMSentence(operator, selected.name, toASMForm(right_operand)));
        }
    }

    void add(QT qt) {
        addOrSub(qt, "add");
    }

    void sub(QT qt) {
        addOrSub(qt, "sub");
    }

    private String getValue(String constant) {
        assert isConstant(constant);
        if (constant.startsWith("const int") || constant.startsWith("const double")) return constant.split("_")[1];
        // is const char
        return "'" + constant.split("_")[1] + "'";
    }

    private boolean isConstant(String item) {
        return item.startsWith("const");
    }


}
