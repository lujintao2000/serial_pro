package com.tuling.serialize.util;

/**
 * Created by Administrator on 2020-06-08.
 */
public class LinkedList {
    private Node start = null;
    private Node end = null;

    public static void main(String[] args){
        LinkedList list = new LinkedList();
        list.add("xioawang");
        list.add("zhiguo");
        list.add("huabing");
        System.out.print(list);
    }

    public void add(Object element){
        Node node = new Node(element);
       if(start == null){
           start = node;
       }
       if(end != null){
           end.setNext(node);
           node.setPrevious(end);
       }
       end = node;
    }


    public static class Node{
        private Object value;
        //private Node previous;
        private Node next;

        private Node previous;

        public Node(){
        }

        public Node(Object value){
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }
}
