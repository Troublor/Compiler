import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by troub on 2017/10/10.
 */
public class Grammar {
    /**
     * 推导产生式集合
     */
    private ArrayList<Deriver> derivers;

    private HashSet<String> VT;

    private HashSet<String> VN;

    private String startVN;

    Grammar(){
        derivers = new ArrayList<>();
        VT = new HashSet<>();
        VN = new HashSet<>();
        startVN = "";
        LL1AnalyseTable = new HashMap<>();
    }

    public boolean isVT(String s){
        return VT.contains(s);
    }

    public boolean isVN(String s){
        return VN.contains(s);
    }

    public void addVT(String vt){
        VT.add(vt);
    }

    public void addVT(HashSet<String> vts){
        VT.addAll(vts);
    }

    public void addVN(String vn){
        VN.add(vn);
    }

    public HashSet<String> getVN() {
        return VN;
    }

    public void addVN(HashSet<String> vns){
        VN.addAll(vns);
    }

    /**
     * 判定一个字符串是否是一个语义动作（以_AC_开头)
     * @param s 需判断的字符串
     * @return boolean
     */
    public boolean isAction(String s) {
        if (s.length() <= 4) {
            return false;
        }
        String t = s.substring(0, 4);
        return t.equals("_AC_");
    }

    /**
     * 通过非终结符获取产生式集合
     * @param source 非终结符
     * @return ArrayList<Deriver> Deriver产生式集合
     */
    public ArrayList<Deriver> getDeriversBySource(String source){
        ArrayList<Deriver> arrayList = new ArrayList<>();
        for (Deriver deriver : derivers){
            if (deriver.getSource().equals(source)){
                arrayList.add(deriver);
            }
        }
        return  arrayList;
    }

    /**
     * 推导，通过非终结符获取推导出的产生式的列表（一个非终结符可能有多个推导）
     * @param s string 非终结符
     * @return ArrayList<ArrayList<String>> 多个推导结果的列表
     **/
    public ArrayList<ArrayList<String>> derive(String s){
        ArrayList<ArrayList<String>> r = new ArrayList<>();
        for (Deriver deriver : derivers){
            if (deriver.getSource().equals(s)){
                r.add(deriver.getDestination());
            }
        }
        return r;
    }

    public ArrayList<Deriver> getDerivers() {
        return derivers;
    }

    /**
     * 是否可推出空（仅单步推导）
     * @param s string 非终结符
     * @return boolean
     */
    public boolean hasNullDestination(String s){
        for (ArrayList<String> destination : derive(s)){
            if (isEmpty(destination)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判定一个产生式右部是否为空（只有动作没有终结符非终结符就是空）
     * @param destination ArrayList<String> 一个产生式右部
     * @return boolean
     */
    public boolean isEmpty(ArrayList<String> destination){
        if (destination.isEmpty()){
            return true;
        } else {
            //如果destinain的List不为空，但是里面全是语义动作，那也视为空
            boolean flag = false;
            for (String d : destination) {
                if (!isAction(d)) {
                    flag = true;
                }
            }
            if (!flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 找到一个产生式右部的第一个有效符号（忽略动作）
     * 如果没有，返回null
     * @param arrayList 产生式右部
     * @return 第一个有效符号（忽略动作）
     */
    public String getFirst(ArrayList<String> arrayList) {
        if (this.isEmpty(arrayList)) {
            return null;
        }
        int i = 0;
        while (this.isAction(arrayList.get(i))) {
            i++;
            if (i == arrayList.size()) {
                return null;
            }
        }
        return arrayList.get(i);
    }

    /**
     * 找到一个产生式右部的第一个有效符号的index（忽略动作）
     * 如果没有，返回null
     * @param arrayList 产生式右部
     * @return 第一个有效符号的index（忽略动作）
     */
    public int getIndexOfFirst(ArrayList<String> arrayList) {
        String object = getFirst(arrayList);
        if (object == null) {
            return -1;
        }
        return arrayList.indexOf(object);
    }

    /**
     * 获取推导式右部的下一个符号（忽略动作）
     * 如果到末尾了就返回null
     * @param arrayList ArrayList 推导式右部
     * @param object 当前符号
     * @return object 下一个符号
     * @throws InvalidLabelException 如果检测到非法字符抛出异常
     */
    public String getNext(ArrayList<String> arrayList, String object) throws InvalidLabelException{
        int index = arrayList.indexOf(object);
        int j = index + 1;
        if (j == arrayList.size()) {
            return null;
        }
        while (this.isAction( arrayList.get(j))) {
            j++;
            if (j == arrayList.size()) {
                return null;
            }
        }
        if (!this.isVN(arrayList.get(j)) && !this.isVT(arrayList.get(j))) {
            throw new InvalidLabelException(
                "InvalidLabelException: " + arrayList.get(j) + "is not VT or VN or Action\n"
            );
        }
        return arrayList.get(j);
    }

    /**
     * 重载
     * 获取推导式右部的下一个符号（忽略动作）
     * 如果到末尾了就返回null
     * @param arrayList ArrayList 推导式右部
     * @param index 当前符号的index
     * @return Object 下一个符号的index
     * @throws InvalidLabelException 如果检测到非法字符抛出异常
     */
    public String getNext(ArrayList<String> arrayList, int index) throws InvalidLabelException{
        return this.getNext(arrayList, arrayList.get(index));
    }

    /**
     * 获取推导式右部下一个符号的index（忽略动作）
     * 如果末尾了返回-1
     * @param arrayList ArrayList 推导式右部
     * @param object 当前符号
     * @return int 下一个符号的index
     * @throws InvalidLabelException 如果检测到非法字符抛出异常
     */
    public int getIndexOfNext(ArrayList<String> arrayList, String object) throws InvalidLabelException{
        String next = this.getNext(arrayList, object);
        if (next == null) {
            return -1;
        }
        return arrayList.indexOf(next);
    }

    /**
     * 重载
     * 获取推导式右部下一个符号的index（忽略动作）
     * 如果末尾了返回-1
     * @param arrayList ArrayList 推导式右部
     * @param index 当前符号的index
     * @return int 下一个符号的index
     * @throws InvalidLabelException 如果检测到非法字符抛出异常
     */
    public int getIndexOfNext(ArrayList<String> arrayList, int index) throws InvalidLabelException {
        return this.getIndexOfNext(arrayList, arrayList.get(index));
    }

    /**
     * 判断是否是最后一个符号（忽略动作）
     * @param arrayList ArrayList 推导式右部
     * @param object 当前符号
     * @return boolean
     * @throws InvalidLabelException 如果检测到非法字符抛出异常
     */
    public boolean isLast(ArrayList<String> arrayList, String object) throws InvalidLabelException {
        return this.getNext(arrayList, object) == null;
    }

    /**
     * 获取从某一位置开始之后的第一个符合条件的对象的index
     * @param arrayList 列表
     * @param startIndex 开始查找的index
     * @param object 查找的对象
     * @return 找到的index，找不到为-1
     */
    private int nextIndexOf(ArrayList<String> arrayList, int startIndex, String object) {
        ArrayList<String> a = new ArrayList<>();
        for (int i = startIndex; i < arrayList.size(); i++) {
            a.add(arrayList.get(i));
        }
        if (a.indexOf(object) <= -1) {
            return -1;
        } else {
            return a.indexOf(object) + startIndex;
        }
    }

    /**
     * 是否能推出空（多步）(只要有一种推导路径推出空即可）
     * @param s String非终结符
     * @return boolean
     */
    public boolean canDeriveNull(String s) throws InvalidLabelException{
        if (this.hasNullDestination(s)) {
            return true;
        }
        outer:
        for (Deriver deriver : this.getDeriversBySource(s)) {
            ArrayList<String> destination = deriver.getDestination();
            boolean flag = false;
            for (String string : destination) {
                if (this.isVT(string)) {
                    //有vt就一定不能推出空
                    continue outer;
                } else if (this.isAction(string)) {
                    continue;
                } else if (this.isVN(string)){
                    if (!this.canDeriveNull(string)) {
                        flag = true;
                        break;
                    }
                } else {
                    throw new InvalidLabelException(
                        "InvalidLabelException: " + string
                            + "is not a valid VT or VN or Action [in deriver: " + deriver.toString()
                            + "]\n");
                }
            }
            if (!flag) {
                //如果destination遍历完都没有找到能推出空的当前产生式能推出空
                return true;
            }
        }
        return false;
    }

    /**
     * 添加推导产生式
     * @param deriver Deriver 产生式
     */
    public void addDeriver(Deriver deriver){
        derivers.add(deriver);
    }

    /**
     * 添加推到产生式
     * @param s String 源
     * @param d ArrayList<String>目标
     */
    public void addDeriver(String s, ArrayList<String> d){
        derivers.add(new Deriver(s, d));
    }

    /**
     * 添加推到产生式
     * @param s String 源
     * @param d String[] 目标
     */
    public void addDeriver(String s, String[] d){
        derivers.add(new Deriver(s, d));
    }

    /**
     * 获取序号为i的推导产生式
     * @param i int 序号
     * @return Deriver
     */
    public Deriver getDeriver(int i){
        return derivers.get(i);
    }

    public String getStartVN(){
        return startVN;
    }

    public void setStartVN(String startVN) {
        this.startVN = startVN;
    }

    /**
     * LL1分析准备
     *
     **/
    /**
     * LL1分析表
     */
    private HashMap<String, HashMap<String, Integer>> LL1AnalyseTable;

    /**
     * 获取分析表
     * 之前一定要调用generateAnalyzeTable()
     * @return analyseTable
     */
    public HashMap<String, HashMap<String, Integer>> getLL1AnalyseTable() {
        return LL1AnalyseTable;
    }

    /**
     * 生成LL1分析表
     */
    public void generateLL1AnalyzeTable() throws InvalidLabelException, InvalidLL1GrammarException{
        HashSet<String> selectCollection;
        ArrayList<Deriver> derivers;

        for (String vn : this.getVN()){
            HashMap<String, Integer> row = new HashMap<>();
            row.clear();
            derivers = this.getDeriversBySource(vn);
            HashSet<String> allInOne = new HashSet<>();
            int totalSize = 0;
            for (Deriver deriver : derivers){
                selectCollection = getSelectCollection(deriver);
                allInOne.addAll(selectCollection);
                totalSize += selectCollection.size();
                for (String select : selectCollection){
                    row.put(select, this.getDerivers().indexOf(deriver));
                }
            }
            if (totalSize != allInOne.size()) {
                //集合并集在size比每一个集合的size的和小，则不是ll1文法
                throw new InvalidLL1GrammarException("不符合LL1文法\n");
            }
            LL1AnalyseTable.put(vn, row);
        }
    }

    /**
     * 生成一个推导式的select集合
     * @param deriver 需要生成select集合的推导式
     * @return HashSet<String> select集合
     */
    private HashSet<String> getSelectCollection(Deriver deriver) throws InvalidLabelException{
        ArrayList<String> destination = deriver.getDestination();
        HashSet<String> temp;
        boolean flag = false;
        for (String s : destination){
            if (this.isVT(s)){
                flag = true;
                //肯定不能推出空了
                break;
            }
            if (!this.canDeriveNull(s)){
                flag = true;
                //如果有一个非终结符不能推出空，那也不能推出空
                break;
            }
        }
        if (!flag){
            //可空
            HashSet<String> hashSet = getFollowCollection(deriver.getSource());
            temp = getFirstCollection(deriver.getDestination());
            hashSet.addAll(temp);
            return hashSet;
        }else {
            return getFirstCollection(deriver.getDestination());
        }
    }

    /**
     * 生成一个非终结符的follow集合
     * @param S 要生成follow集合的非终结符
     * @return HashSet<String> follow集合
     */
    private HashSet<String> getFollowCollection(String S) throws InvalidLabelException{
        HashSet<String> hashSet = new HashSet<>();
        HashSet<String> follow, temp;
        int k;
        //如果当前非终结符是开始，他的follow有一个#
        if (this.getStartVN().equals(S)) {
            hashSet.add("#");
        }
        outer:
        //查找所有产生式，找到右部包含S的产生式
        for (Deriver deriver : this.getDerivers()) {
            if (!deriver.getDestination().contains(S)) {
                //如果右部不包含S则下一个
                continue;
            }
            ArrayList<String> destination = deriver.getDestination();
            k = -1;
            k = this.nextIndexOf(deriver.getDestination(), k + 1, S);
            while (k > -1) {
                //循环找右部中的每一个S
                String nextOfS = getNext(destination, k);
                if (nextOfS == null) {
                    //如果S是最后一个了
                    if (deriver.getSource().equals(S)) {
                        //自身循环，不取
                        continue outer;
                    }
                    follow = getFollowCollection(deriver.getSource());
                    hashSet.addAll(follow);
                    continue outer;
                } else if (this.isVN(nextOfS)) {
                    //如果不是S不是最后一个（忽略动作）
                    //且S的下一个是VN
                    //吧next of s 的first加入hashset
                    for (
                        ArrayList<String> arrayList :
                        this.derive(nextOfS)
                        ) {
                        if (arrayList == null) {
                            continue;
                        }
                        temp = getFirstCollection(arrayList);
                        hashSet.addAll(temp);
                    }
                    while (this.canDeriveNull(nextOfS)) {
                        //只要next of s能推出空,就查看再下一个符号
                        nextOfS = this.getNext(destination, nextOfS);
                        if (nextOfS == null) {
                            //如果到最后了，又要找自己本产生式的source的follow集合了
                            if (deriver.getSource().equals(S)) {
                                //自身循环，不取
                                continue outer;
                            }
                            follow = getFollowCollection(deriver.getSource());
                            hashSet.addAll(follow);
                            break;
                        }
                        if (this.isVT(nextOfS)) {
                            //如果再下一个是终结符
                            hashSet.add(nextOfS);
                            break;
                        } else {
                            //如果再下一个还是非终结符
                            for (
                                ArrayList<String> arrayList :
                                this.derive(nextOfS)
                                ) {
                                if (arrayList == null) {
                                    continue;
                                }
                                temp = getFirstCollection(arrayList);
                                if (temp == null) {
                                    continue;
                                }
                                hashSet.addAll(temp);
                            }
                        }
                    }
                } else if (this.isVT(nextOfS)) {
                        hashSet.add(nextOfS);
                }
                k = this.nextIndexOf(deriver.getDestination(), k + 1, S);
            }
        }
        return hashSet;
    }

    /**
     * 生成一个推导式右部的first集合
     * @param a ArrayList<String> 推导式右部
     * @return HashSet<String> first集合
     */
    private HashSet<String> getFirstCollection(ArrayList<String> a) throws InvalidLabelException{
        HashSet<String> firstCollection = new HashSet<>();
        HashSet<String> temp;
        if (this.isEmpty(a)){
            //如果本右部为空（忽略动作）
            return firstCollection;
        }

        //循环找到第一个不是动作的标号（终结符或非终结符）
        int i = this.getIndexOfFirst(a);
        String s = (String) this.getFirst(a);
        if (s == null) {
            //如果没有第一个（为空）
            return firstCollection;
        } else if (this.isVT(s)) {
            //如果第一个是终结符
            firstCollection.add(s);
        } else if (this.isVN(s)) {
            //如果第一个是非终结符
            for (ArrayList<String> arrayList : this.derive(s)) {
                temp = getFirstCollection(arrayList);
                firstCollection.addAll(temp);
            }
            outer:
            while (this.canDeriveNull(s)) {
                //如果本符号是非终结符且可为空
                s = (String) this.getNext(a, s);
                if (s == null) {
                    //如果当前已经是最后一个
                    break;
                }
                while (this.isVT(s)) {
                    //循环找到下一个非终结符
                    s = (String) this.getNext(a, s);
                    if (s == null) {
                        //如果当前已经是最后一个非终结符
                        break outer;
                    }
                }
                for (ArrayList<String> arrayList : this.derive(s)) {
                    temp = getFirstCollection(arrayList);
                    firstCollection.addAll(temp);
                }
            }

        } else {
            throw new InvalidLabelException(
                "InvalidLabelException: " + s + " 不是合法的终结符或非终结符或语义动\n"
            );
        }
        return firstCollection;
    }

}