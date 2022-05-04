package com.tuling.serialize.util;

import java.util.*;

/**
 * 该类主要是方便Context查找是否存入某个对象，它是线程不安全的
 * @author ljt
 * @date 2021-02-14
 */
public class ContextMap{
    private Node[] array = null;
    private List list = new ArrayList();
    //加载因子,它用来衡量集合数组在自动增长之前最多只能达到多满,0.75表示100个桶，最多只能有75个桶中存有元素
    private float loadFactor = 0.75f;
    //定义桶的大小
    private int size = 0;
    //装有元素的桶的数量
    private int count;
    //已存储元素的桶的数量扩容前可以达到的最大值
    private long thresholdSize = 0L;

    private int another;

    public ContextMap(){
        size = 1 << 16;
        array = new Node[size];
    }

    public ContextMap(float loadFactor){
        size = 1 << 16;
        array = new Node[size];
        this.loadFactor = loadFactor;
        thresholdSize = (long)this.loadFactor * size;
        another = size - 1;
    }


    /**
     * 判断集合是否包含一个元素a,满足a == value
     * @param value
     * @return
     */
    public boolean contains(Object value) {
        int position = another & value.hashCode();
        if(array[position] != null){
            if(array[position].contains(new Entry(value))){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 添加元素
     * @param value
     *
     */
    public void add(Object value) {
        if(value == null){
            throw new NullPointerException();
        }
        int position = value.hashCode() & (size - 1);
        Node node = array[position];
        if(node == null){
            node = new Node();
            array[position] = node;
            count++;
            //判断空闲桶数是否充足
            if(count > 0.75 * size){
                //增加容量
                this.increaseCapacity();
            }
        }
        node.add(new Entry(value));
//        list.add(value);
    }

    /**
     * 增加容量
     */
    private void increaseCapacity(){
        int newSize = size << 1;
        Node[] tempArray = new Node[newSize];
        //扩容后数组装有元素的桶的数量
        int newCount = 0;
        for(Object item : list){
            int position = (newSize - 1) & item.hashCode();
            Node node = tempArray[position];
            if(node == null){
                node = new Node();
                tempArray[position] = node;
                newCount++;
            }
            node.add(new Entry(item));
        }
        array = tempArray;
        count = newCount;
        size = newSize;
    }

    /**
     * 用于存储拥有相同位置（根据哈希码求得的在elementData中的位置)的元素
     *
     */
    private class Node{
        private LinkedList list = new LinkedList();

        public Node(){

        }

        public Node(Entry element){
            list.add(element);
        }

        public void add(Entry element){
            list.add(element);
        }

        public boolean contains(Entry element){
            return list.contains(element);
        }
    }

    private class Entry{
//        private int hash;
        private Object element;

        public Entry(Object element){
            this.element = element;
//            this.hash = element.hashCode();
        }

//        public int getHash() {
//            return hash;
//        }

//        public void setHash(int hash) {
//            this.hash = hash;
//        }

        public Object getElement() {
            return element;
        }

        public void setElement(Object element) {
            this.element = element;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

//            if (hash != entry.hash) return false;
            return element == entry.element;
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + element.hashCode();
            return result;
        }
    }
}
