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
        analyseTable = new HashMap<>();
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

    public ArrayList<Deriver> getDeriverBySource(String source){
        ArrayList<Deriver> arrayList = new ArrayList<>();
        for (Deriver deriver : derivers){
            if (deriver.getSource().equals(source)){
                arrayList.add(deriver);
            }
        }
        return  arrayList;
    }

    public void addVN(HashSet<String> vns){
        VN.addAll(vns);
    }

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

    public boolean hasNullDestination(String s){
        for (ArrayList<String> destination : derive(s)){
            if (destination.isEmpty()){
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
    private HashMap<String, HashMap<String, Integer>> analyseTable;

    /**
     * 获取分析表
     * 之前一定要调用generateAnalyzeTable()
     * @return analyseTable
     */
    public HashMap<String, HashMap<String, Integer>> getAnalyseTable() {
        return analyseTable;
    }

    /**
     * 生成LL1分析表
     */
    public void generateAnalyzeTable(){
        HashSet<String> selectCollection;
        ArrayList<Deriver> derivers;

        for (String vn : this.getVN()){
            HashMap<String, Integer> row = new HashMap<>();
            row.clear();
            derivers = this.getDeriverBySource(vn);
            for (Deriver deriver : derivers){
                selectCollection = getSelectCollection(deriver);
                for (String select : selectCollection){
                    row.put(select, this.getDerivers().indexOf(deriver)+1);
                }
            }
            analyseTable.put(vn, row);
        }
    }

    private HashSet<String> getSelectCollection(Deriver deriver){
        ArrayList<String> destination = deriver.getDestination();
        HashSet<String> temp;
        boolean flag = false;
        for (String s : destination){
            if (this.isVT(s)){
                flag = true;
                break;
            }
            if (!this.hasNullDestination(s)){
                flag = true;
                break;
            }
        }
        if (!flag){
            //可空
            HashSet<String> hashSet = getFollowCollection(deriver.getSource());
            temp = getFirstCollection(deriver.getDestination());
            if (temp != null && hashSet != null) {
                hashSet.addAll(temp);
            }
            return hashSet;
        }else {
            return getFirstCollection(deriver.getDestination());
        }
    }

    private HashSet<String> getFollowCollection(String S){
        HashSet<String> hashSet = new HashSet<>();
        HashSet<String> follow, temp;
        int i;
        if (this.getStartVN().equals(S)) {
            hashSet.add("#");
        }
        outer:for (Deriver deriver : this.getDerivers()){
            if (!deriver.getDestination().contains(S)){
                continue;
            }
            i = deriver.getDestination().indexOf(S)+1;
            if (i == deriver.getDestination().size()){
                if (deriver.getSource().equals(S)) {
                    //自身循环，不取
                    continue;
                }
                follow = getFollowCollection(deriver.getSource());
                if (follow == null) {
                    continue;
                }
                hashSet.addAll(follow);
                continue;
            }
            if (this.isVN(deriver.getDestination().get(i))){
                while (this.hasNullDestination(deriver.getDestination().get(i))){
                    for (ArrayList<String> arrayList : this.derive(deriver.getDestination().get(i))){
                        if (arrayList == null) {
                            continue;
                        }
                        temp = getFirstCollection(arrayList);
                        if (temp == null) {
                            continue;
                        }
                        hashSet.addAll(temp);
                    }
                    if (i == deriver.getDestination().size() - 1){
                        if (deriver.getSource().equals(S)) {
                            continue outer;
                        }
                        follow = getFollowCollection(deriver.getSource());
                        if (follow == null) {
                            continue outer;
                        }
                        hashSet.addAll(follow);
                        continue outer;
                    }
                    i++;
                }
                for (ArrayList<String> arrayList : this.derive(deriver.getDestination().get(i))){
                    temp = getFirstCollection(arrayList);
                    if (temp == null) {
                        continue;
                    }
                    hashSet.addAll(temp);
                }
            }else if (this.isVT(deriver.getDestination().get(i))){
                hashSet.add(deriver.getDestination().get(i));
            }else {
                return null;
            }
        }
        return hashSet;
    }

    private HashSet<String> getFirstCollection(ArrayList<String> a){
        HashSet<String> firstCollection = new HashSet<>();
        HashSet<String> temp;
        if (a.isEmpty()){
            return firstCollection;
        }
        if (this.isVT(a.get(0))){
            firstCollection.add(a.get(0));
        }else if (this.isVN(a.get(0))){
            for (ArrayList<String> arrayList : this.derive(a.get(0))){
                temp = getFirstCollection(arrayList);
                if (temp == null) {
                    continue;
                }
                firstCollection.addAll(temp);
            }
        }else {
            return null;
        }
        return firstCollection;
    }

}

class Deriver{
    /**
     * 源符号
     */
    private String source;

    /**
     * 生成式
     */
    private ArrayList<String> destination;

    Deriver(String s, String[] d){
        source = s;
        destination = new ArrayList<>(Arrays.asList(d));
    }

    Deriver(String s, ArrayList<String> d){
        source = s;
        destination = new ArrayList<>(d);
    }

    /**
     * source访问器
     * @return source
     */
    public String getSource(){
        return source;
    }

    /**
     * destination访问器
     * @return destination
     */
    public ArrayList<String> getDestination(){
        return destination;
    }
}