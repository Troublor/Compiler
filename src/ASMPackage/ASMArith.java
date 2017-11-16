package ASMPackage;

import MiddleDataUtilly.QT;
import TranslatorPackage.TranslatorExceptions.SemanticException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ASMArith {
    // represent 4 general registers: eax, ebx, ecx, edx
    private Map<String, register> registers = new HashMap<>();
    private ASMGenerater asmGenerater;
    private int cur_index = 0;
    private List<QT> qts;
    private ArrayList<ASMSentence> asms = new ArrayList<>();
    private ArrayList<register> order;
    private int end_index;

    public ASMArith(List<QT> qts, ASMGenerater asmGenerater) {
        this.qts = qts;
        this.asmGenerater = asmGenerater;
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

        // 只将寄存器置为空闲，不对（如果有数据的话）寄存器的数据做任何处理
        public void free() {
            content = null;
        }



        // 将寄存器的释放（同时（如果有数据的话）将寄存器的数据存回内存）
        public void release(){
            if (!available()) {
                produce("mov", toAddress(content), this.name);
                content = null;
            }
        }

        // 表明 该寄存器会被id占用
        public void occupyWith(String id){
            assert !id.startsWith("const");
            if (!available() && !this.content.equals(id)) release();
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


//    void produce(ASMSentence sentence){
//        asms.add(sentence);
//    }

    void produce(String operator,String ...operands) {
        asms.add(new ASMSentence(operator, operands));
    }

    // todo : only allow operation of same type
    ArrayList<ASMSentence> produceASM() throws ASMException {
        for (QT qt : qts) {
           dispatch(qt);
        }
        return this.asms;
    }

    private void dispatch(QT qt) throws ASMException{
        String operator = qt.getOperator();
        String type = getType(qt.getResult());

        switch (type) {
            // char 也是32位的，所以操作和int操作完全一样
            case "char":
            case "int": {
                switch (operator) {
                    case "=": { assign(qt);break;}
                    case "+": { iadd(qt); break;}
                    case "-": { isub(qt);break;}
                    case "*": { imul(qt); break;}
                    case "/": { idiv(qt); break;}
                    case "<":
                    case ">":
                    case "<=":
                    case ">=":
                    case "==": {
                        compare(qt);
                        break;
                    }
                    case "&&":
                    case "||": {
                        logicOperation(qt);
                    }
                }
                break;
            }
            case "double" : {
                switch (operator) {
                    case "=": { assign(qt);break;}
                    case "+": { fadd(qt); break;}
                    case "-": { fsub(qt);break;}
                    case "*": { fmul(qt); break;}
                    case "/": { fdiv(qt); break;}
                }
                break;
            }
            default: {
                throw new ASMException("no matching operation");
            }
        }
        ++cur_index;
    }

    private void assign(QT qt) {
        String left_operand = qt.getOperand_left(), result = qt.getResult();
        register r = inWhichRegister(result);
        if (r == null) r = arrangeRegister();
        r.occupyWith(result);
        produce("mov", r.name, toASMForm(left_operand));
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
                        produce("mov", Ri.name, Rj.name);
                        Rj.occupyWith(left_operand);
                        Ri.content = result;
                        return Ri;
                    }
                    else {
                        Ri.occupyWith(result);
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
                produce("mov", Ri.name, toAddress(left_operand));
                return Ri;
            }
            // occupyWith register being used
            else {
                Ri = arrangeRegister();
                Ri.occupyWith(result);
                produce("mov", Ri.name, toAddress(left_operand));
                return Ri;
            }
        }

    }

    // 定位 id 所在的位置，如果在寄存器，优先返回寄存器名字，如果在内存，调用toAddress(),返回在内存中的表示形式
    // 如果确定要一个 id 的内存地址，请直接调用 toAddress)_
    private String locate(String id) {
        assert !isConstant(id);
        register r = inWhichRegister(id);
        if (r == null) {
            return toAddress(id);
        }
        return r.name;
    }

    private String toASMForm(String item) {
        // if not constant, directly use locate()
        if (!isConstant(item)) {
            if (isArray(item)) {
                // like  1.a.t.int->t
                // do specific staff
            }
            else {
                return locate(item);
            }
        }
        // is  constant;
        return getValue(item);
    }

    // todo: recognize array
    private boolean isArray(String item) {
        return false;
    }

    /*
    *   about active information
    * */
    private boolean isActive(String id) {
        Pattern p = Pattern.compile("(.*)(->)(\\d+|y)");
        Matcher m = p.matcher(id);
        return m.find();
    }

    // id should be variable, not constant
    // return: -1.a.int->X  -->   X
    private String getActiveInfo(String id) throws ASMException{
        assert !id.startsWith("const");
        Pattern p = Pattern.compile("(.*)(->)(\\d+|y|n)");
        Matcher m = p.matcher(id);
        if (m.find()) return m.group(3);
        throw new ASMException("invalid active info");
    }


    // todo: mov ri, 1.b.int -> mov ri, b.offset
    private String toAddress(String id) {
        id = id.split("->")[0];
        return asmGenerater.toASMOprd(id, "esi");
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
            return selected;
        }
        else {
            Collections.sort(order);
            selected = order.get(0);
            // seleted 原本被占用, 将内容存到内存中
            selected.release();
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
    // including isub, iadd
    void iaddOrSub(QT qt, String operator) {
        String left_operand = qt.getOperand_left(), right_operand = qt.getOperand_right(), result = qt.getResult();
        if (isConstant(left_operand)) {
            register selected = arrangeRegister();
            produce("mov", selected.name, toASMForm(left_operand));
            produce(operator, selected.name, toASMForm(right_operand));
            selected.occupyWith(result);
        }
        else {
            register selected = arrange(left_operand, result);
            produce(operator, selected.name, toASMForm(right_operand));
        }
    }

    void iadd(QT qt) {
        iaddOrSub(qt, "add");
    }

    void isub(QT qt) {
        iaddOrSub(qt, "sub");
    }


    private void idiv(QT qt) {
        idivOrImul(qt, "idiv");
    }

    private void imul(QT qt) {
        idivOrImul(qt, "imul");
    }

    private void idivOrImul(QT qt, String operator) {
        // compared to imul, idiv need to expand left_operand to eax & edx
        String left_operand = qt.getOperand_left(), right_operand = qt.getOperand_right(), result = qt.getResult();
        if (isConstant(left_operand)) {
            eax.release();
            produce("mov", eax.name, toASMForm(left_operand));
        }
        // else left_operand is not constant
        else {
            register r = inWhichRegister(left_operand);
            // left_operand is in register;
            if (r != null) {
                // left_operand is not in eax
                if (r != eax) {
                    eax.release();
                    produce("mov", eax.name, r.name);
                    // if left_operand is result, this may result in 2 same variable in the registers(r & eax),
                    // so free register r (without put the value back to memory, because now the result variable has new value)
                    //  example: * a 2 a      or * a a a
                    if (left_operand.equals(result)) {
                        r.free();
                    }
                }
                // left_operand is in eax
                else {
                    // pass
                }
            }
            // left not in register
            else  {
                eax.release();
                produce("mov", eax.name, toAddress(left_operand));
            }
        }
        // now left is in eax
        edx.release();
        if (operator.equals("idiv")) {
            produce("cdq");  // 32bit to 64bit EAX->EDX:EAX
        }
        produce(operator, toASMForm(right_operand));
        eax.occupyWith(result);
    }

    private void floatOperation(QT qt, String operator) throws ASMException {
        String[] operands = {qt.getOperand_left(),qt.getOperand_right()};
        String result = qt.getResult();
        // load operands to float register
        produce("finit");
        for (String operand: operands) {
            pushFPU(operand);
        }
        produce(operator);
        // fstp:  store the result and pops the register stack.
        produce("fstp", "dword [" + toAddress(result) + "]");

    }


    // push operand to FPU stack;
    private void pushFPU(String operand) throws ASMException{
        String type = getType(operand);
        if (isConstant(operand)) {
            produce("push", "dword " + toASMForm(operand));
//            switch (type) {
//                case "char": {
//                    // because toASMForm("const char_a")returns 'a',  'a' -> 97
//                    produce("push");
////                    produce("push", "dword " +
////                            String.valueOf(
////                                    (int)(getValue(operand).charAt(0))
////                            ));
//                }
//                case "int": {
//                    produce("push", "dword " + toASMForm(operand));
//                    break;
//                }
//                case "double": {
//                    produce("push", "dword " + toASMForm(operand));
//                    break;
//                }
//            }
        }
        // not constant
        else {
            produce("push", "dword [" + toAddress(operand) + "]");
        }
        switch (type) {
            case "char":
            case "int": {
                produce("fild", "dword [esp]");
                break;
            }
            case "double": {
                produce("fld", "dword [esp]");
                break;
            }
        }
        // todo: pop 回来的 数据不再需要， 定义一个null来存没用的数据（不然会占用一个寄存器）
        produce("pop", "[null]");

    }

    private void compare(QT qt)throws ASMException {
        String left_operand = qt.getOperand_left(), right_operand = qt.getOperand_right(), result = qt.getResult();
        String left_type = getType(left_operand), right_type = getType(right_operand);
        String type = (left_type.equals("double")||right_type.equals("double")) ? "double" : "int";
        String operation = qt.getOperator();
        switch (type) {
            case "int": {
                register r = null;
                if (!isConstant(left_operand)) {
                    r = inWhichRegister(left_operand);
                }
                if (r == null) {
                    r = arrangeRegister();
                    r.occupyWith(result);
                    produce("mov", r.name, toASMForm(left_operand));
                }
                produce("cmp", r.name, toASMForm(right_operand));
                switch (operation) {
                    case "<": {
                        ilt(qt);
                        break;
                    }
                    case ">": {
                        igt(qt);
                        break;
                    }
                    case "<=": {
                        ile(qt);
                        break;
                    }
                    case ">=": {
                        ige(qt);
                        break;
                    }
                    case "==": {
                        ieq(qt);
                        break;
                    }
                }
                break;
            }
            case "double": {
                produce("finit");
                pushFPU(right_operand);
                pushFPU(left_operand);
                produce("fcomip");
                switch (operation) {
                    case "<": {
                        flt(qt);
                        break;
                    }
                    case ">": {
                        fgt(qt);
                        break;
                    }
                    case "<=": {
                        fle(qt);
                        break;
                    }
                    case ">=": {
                        fge(qt);
                        break;
                    }
                    case "==": {
                        feq(qt);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void ieq(QT qt) {
        icompare(qt, "je");
    }

    private void ige(QT qt) {
        icompare(qt, "jge");
    }

    private void ile(QT qt) {
        icompare(qt, "jle");
    }

    private void igt(QT qt) {
        icompare(qt, "jg");
    }

    private void ilt(QT qt) {
        icompare(qt, "jl");
    }

    private void icompare(QT qt, String ord) {
        String true_label = newLabel();
        String end_label = newLabel();
        produce(ord, true_label);
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce("jmp", end_label);
        produce(true_label+":\n");
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce(end_label+":\n");

    }

    private void feq(QT qt) {
        String true_label = newLabel();
        String end_label = newLabel();
        produce("jz", true_label);
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce("jmp", end_label);
        produce(true_label+":\n");
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce(end_label+":\n");
    }

    private void fge(QT qt) {
        String false_label = newLabel();
        String end_label = newLabel();
        produce("jc", false_label);
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce("jmp", end_label);
        produce(false_label+":\n");
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce(end_label+":\n");
    }

    private void fle(QT qt) {
        String true_label = newLabel();
        String end_label = newLabel();
        produce("jc", true_label);
        produce("jz", true_label);
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce("jmp", end_label);
        produce(true_label+":\n");
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce(end_label+":\n");
    }


    private void fgt(QT qt) {
        String false_label = newLabel();
        String end_label = newLabel();
        produce("jc", false_label);
        produce("jz", false_label);
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce("jmp", end_label);
        produce(false_label+":\n");
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce(end_label+":\n");
    }

    private void flt(QT qt) {
        String true_label = newLabel();
        String end_label = newLabel();
        produce("jc", true_label);
        assign(new QT("=", "const int_0", null, qt.getResult()));
        produce("jmp", end_label);
        produce(true_label+":\n");
        assign(new QT("=", "const int_1", null, qt.getResult()));
        produce(end_label+":\n");
    }

    private void logicOperation(QT qt) {
        String operator = qt.getOperator(), result = qt.getResult();
        switch (operator) {
            case "&&": {
                iaddOrSub(qt, "and");
                break;
            }
            case "||": {
                iaddOrSub(qt, "or");
                break;
            }
        }
        register r = inWhichRegister(result);
        assert r != null;
        String end_label  = newLabel();
        produce("jz", end_label);
        produce("mov", r.name, "1");
        produce(end_label + ":\n");
    }


    static int i = 0;


    private String newLabel() {
        return "l" + i++;
    }


    private void fdiv(QT qt) throws ASMException {
        floatOperation(qt, "fdiv");
    }

    private void fmul(QT qt) throws ASMException {
        floatOperation(qt, "fmul");
    }

    private void fsub(QT qt) throws ASMException {
        floatOperation(qt, "fsub");
    }

    private void fadd(QT qt) throws ASMException {
        floatOperation(qt, "fadd");
    }

    private String getValue(String constant) {
        assert isConstant(constant);
        if (constant.startsWith("const int") ) return constant.split("_")[1];
        if (constant.startsWith("const double")) return "__float32__(" + constant.split("_")[1] + ")";
        // is const char
        return "'" + constant.split("_")[1] + "'";
    }

    private boolean isConstant(String item) {
        return item.startsWith("const");
    }


}
