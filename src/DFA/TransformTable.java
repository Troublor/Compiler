/**
 * Created by troub on 2017/9/7.
 */
package DFA;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * DFA的压缩变换表
 */
public class TransformTable {
    /**
     * 索引列表
     */
    private ArrayList<Index> indexTable;

    /**
     * 构造方法
     */
    public TransformTable(){
        indexTable=new ArrayList<>();
    }

    /**
     * 添加状态
     * @param s String 状态名称
     * @return boolean
     */
    public boolean addState(String s){
        indexTable.add(new Index(s,new SubTable()));
        return true;
    }

    /**
     * 添加转换
     * @param state String 当前状态
     * @param input String 读入字符
     * @param destination String 下一个状态
     * @return boolean
     */
    public boolean addTransform(String state, String input, String destination){
        ListIterator iterator=indexTable.listIterator();
        while (iterator.hasNext()){
            Index index=(Index)iterator.next();
            if (index.getState().equals(state)){
                return index.addTransform(input,destination);
            }
        }
        return false;
    }

    /**
     * 设置一个状态读入其他字符默认转到的状态
     * @param state 当前状态
     * @param destination 目标状态
     * @return boolean
     */
    public boolean addDefaultTransform(String state,String destination){
        ListIterator iterator=indexTable.listIterator();
        while (iterator.hasNext()){
            Index index=(Index)iterator.next();
            if (index.getState().equals(state)){
                return index.addDefaultTransform(destination);
            }
        }
        return false;
    }

    /**
     * 进行转换
     * @param state String 当前状态
     * @param input String 读入字符
     * @return String 下一个状态
     */
    public String transform(String state, String input){
        ListIterator iterator=indexTable.listIterator();
        while (iterator.hasNext()){
            Index index=(Index)iterator.next();
            if (index.getState().equals(state)){
                return index.transform(input);
            }
        }
        return "no";
    }
}

/**
 * 索引表
 * 状态——该状态的子表
 */
class Index{
    /**
     * 当前状态
     */
    private String state;

    /**
     * 子表
     */
    private SubTable subTable;

    /**
     * 构造函数
     */
    public Index(String s, SubTable st){
        state=s;
        subTable=st;
    }

    /**
     * 获取子表
     * @return SubTable
     */
    public SubTable getSubTable(){
        return subTable;
    }

    /**
     * 获取当前状态
     */
    public String getState(){
        return state;
    }

    /**
     * 添加转换
     * @param input String 读入字符
     * @param destination String 下一个状态
     * @return boolean
     */
    public boolean addTransform(String input, String destination){
        return subTable.addTransform(input,destination);
    }

    /**
     * 设置默认转到的状态
     * @param d 转到的状态
     * @return boolean
     */
    public boolean addDefaultTransform(String d){
        return subTable.addDefaultTransform(d);
    }

    /**
     * 进行转换
     * @param input String 读入字符
     * @return String 下一个状态
     */
    public String transform(String input){
        return subTable.transform(input);
    }
}

/**
 * 子表
 * 读入字符——下一个状态
 */
class SubTable{
    /**
     * （可选择设置的）输入其他字符转到的状态
     */
    private String defaultDestination;

    /**
     * 状态转换列表
     */
    private ArrayList<Transform> table;

    {
        /*
         * 默认报错
         */
        defaultDestination="no";
    }

    /**
     * 构造方法
     */
    public SubTable(){
        table=new ArrayList<>();
    }

    /**
     * 添加转换函数
     * @param i String 读入的字符
     * @param d String 转移到的状态
     */
    public boolean addTransform(String i,String d){
        //检查是否有重复，确定的有限自动机，确定的
        ListIterator iterator=table.listIterator();
        while (iterator.hasNext()){
            Transform transform=(Transform)iterator.next();
            if (transform.getInput().equals(i)){
                return false;
            }
        }
        table.add(new Transform(i,d));
        return true;
    }

    /**
     * 设置默认转到的状态
     * @param d 转到的状态
     * @return boolean
     */
    public boolean addDefaultTransform(String d){
        defaultDestination=d;
        return true;
    }

    /**
     * 转换方法
     * @param input String 读入的字符
     * @return String 返回下一个状态
     */
    public String transform(String input){
        ListIterator iterator=table.listIterator();
        while (iterator.hasNext()){
            Transform t=(Transform)iterator.next();
            if (t.getInput().equals(input)){
                return t.getDestination();
            }
        }
        return defaultDestination;
    }
}

class Transform{
    /**
     * 转换函数输入
     */
    private String input;

    /**
     * 转换函数转移目的状态
     */
    private String destination;

    /**
     * 构造方法
     */
    public Transform(String i,String d){
        input=i;
        destination=d;
    }

    /**
     * 获取输入
     */
    public String getInput(){
        return input;
    }

    /**
     * 获取目的状态
     */
    public String getDestination(){
        return destination;
    }

}