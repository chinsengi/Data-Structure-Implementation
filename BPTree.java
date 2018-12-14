package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Implementation of a B+ tree to allow efficient access to
 * many different indexes of a large data set. 
 * BPTree objects are created for each type of index
 * needed by the program.  BPTrees provide an efficient
 * range search as compared to other types of data structures
 * due to the ability to perform log_m N lookups and
 * linear in-order traversals of the data items.
 * 
 * @author sapan (sapan@cs.wisc.edu)
 *
 * @param <K> key - expect the value to compare for each item
 * @param <V> value - expect a user-defined type that stores all data for a food item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {

    // Root of the tree
    private Node root;
    
    // Branching factor is the number of children nodes 
    // for internal nodes of the tree
    private int branchingFactor;
    
    
    /**
     * Public constructor
     * 
     * @param branchingFactor 
     */
    public BPTree(int branchingFactor) {
        if (branchingFactor <= 2) {
            throw new IllegalArgumentException(
               "Illegal branching factor: " + branchingFactor);
        }
        this.branchingFactor = branchingFactor;
    }
    
    
    /**
     * Inserts the key and value in the appropriate nodes in the tree
     * 
     * Note: key-value pairs with duplicate keys can be inserted into the tree.
     * 
     * @param key
     * @param value
     */
    @Override
    public void insert(K key, V value) {
    	if(root==null) {
    		LeafNode tmproot = new LeafNode();
    		tmproot.keys.add(key);
    		tmproot.values.add(value);
    		root = tmproot;
    		return;    		
    	}
    	Node tmp = root.insert(key, value);
    	if(tmp!=null) root = tmp;
    	return;
    }
    
    
    /**
     * Gets the values that satisfy the given range 
     * search arguments.
     * 
     * Value of comparator can be one of these: 
     * "<=", "==", ">="
     * 
     * Example:
     *     If given key = 2.5 and comparator = ">=":
     *         return all the values with the corresponding 
     *      keys >= 2.5
     *      
     * If key is null or not found, return empty list.
     * If comparator is null, empty, or not according
     * to required form, return empty list.
     * 
     * @param key to be searched
     * @param comparator is a string
     * @return list of values that are the result of the 
     * range search; if nothing found, return empty list
     */
    @Override
    public List<V> rangeSearch(K key, String comparator) {
        if (!comparator.contentEquals(">=") && 
            !comparator.contentEquals("==") && 
            !comparator.contentEquals("<=") )
            return new ArrayList<V>();
        return root.rangeSearch(key, comparator);
    }
    
    
    /**
     * convert BPTree to string
     * 
     */
    @Override
    public String toString() {
        Queue<List<Node>> queue = new LinkedList<List<Node>>();
        queue.add(Arrays.asList(root));
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
            while (!queue.isEmpty()) {
                List<Node> nodes = queue.remove();
                sb.append('{');
                Iterator<Node> it = nodes.iterator();
                while (it.hasNext()) {
                    Node node = it.next();
                    sb.append(node.toString());
                    if (it.hasNext())
                        sb.append(", ");
                    if (node instanceof BPTree.InternalNode)
                        nextQueue.add(((InternalNode) node).children);
                }
                sb.append('}');
                if (!queue.isEmpty())
                    sb.append(", ");
                else {
                    sb.append('\n');
                }
            }
            queue = nextQueue;
        }
        return sb.toString();
    }
    
    
    /**
     * This abstract class represents any type of node in the tree
     * This class is a super class of the LeafNode and InternalNode types.
     * 
     * @author sapan
     */
    private abstract class Node {
        
        // List of keys
        List<K> keys;
        
        /**
         * Package constructor
         */
        Node() {
        	keys = new LinkedList<>();
        }
        
        /**
         * Inserts key and value in the appropriate leaf node 
         * and balances the tree if required by splitting
         *  
         * @param key
         * @param value
         * @return node
         */
        abstract InternalNode insert(K key, V value);

        /**
         * Gets the first leaf key of the tree
         * 
         * @return key
         */
        abstract K getFirstLeafKey();
        
        /**
         * Gets the new sibling created after splitting the node
         * 
         * @return Node
         */
        abstract Node split();
        
        /**
         * range search implemented by the Bplus tree
         * @param key
         * @param comparator
         * @return the list of value that is within the specified range
         */
        abstract List<V> rangeSearch(K key, String comparator);

        /**
         * 
         * @return boolean
         */
        abstract boolean isOverflow();
        
        public String toString() {
            return keys.toString();
        }
    
    } // End of abstract class Node
    
    /**
     * This class represents an internal node of the tree.
     * This class is a concrete sub class of the abstract Node class
     * and provides implementation of the operations
     * required for internal (non-leaf) nodes.
     * 
     * @author sapan
     */
    private class InternalNode extends Node {

        // List of children nodes
        List<Node> children;
        
        /**
         * Package constructor
         */
        InternalNode() {
            super();
            children = new LinkedList<>();
        }
        
        /**
         * Gets the first leaf key of the tree
         * 
         * @return key
         */
        K getFirstLeafKey() {
            if(keys.isEmpty()) return null;
            return keys.get(keys.size()-1);
        }
        
        /**
         * judge if the node contains more keys than allowed
         * 
         * @return true if node contains more keys than allowed
         */
        boolean isOverflow() {
            return keys.size()==branchingFactor;
        }
        
        /**
         * insert a key-value pair into the node and its subtree, split if needed
         * @return null if the node doesn't need to be split, else the node to be pushed up
         */
        InternalNode insert(K key, V value) {
            int cur = 0;
            while(cur<keys.size()&&keys.get(cur).compareTo(key)<0)cur++;
            InternalNode up = children.get(cur).insert(key, value);
            if(up==null) return null;
            children.remove(cur);
            children.addAll(cur, up.children);        
            keys.addAll(cur, up.keys);
            if(this.isOverflow()) {
                return this.split();
            }else return null;
        }
        
        /**
         * split the node, return the new node generated
         * 
         * @return an internal node that is the parent of two split children
         */
        InternalNode split() {
        	InternalNode left = new InternalNode();
        	InternalNode right = new InternalNode();
        	InternalNode top = new InternalNode();
        	left.keys.addAll(keys.subList(0, keys.size()/2));
        	left.children.addAll(children.subList(0, keys.size()/2+1));
            right.children.addAll(children.subList(keys.size()/2+1, children.size()));
        	right.keys.addAll(keys.subList(keys.size()/2+1, keys.size()));
        	top.children.add(left);
        	top.children.add(right);
        	top.keys.add(keys.get(keys.size()/2));
            return top;
        }
        
        /**
         * range search for the key in the node and its subtree
         * 
         * @return a list contains everything in the range
         */
        List<V> rangeSearch(K key, String comparator) {
        	List<V> ret = new LinkedList<>();
        	switch(comparator) {
        	case "==":
        		Iterator<K> it = keys.iterator();
        		Iterator<Node> itc = this.children.iterator();
        		K cur = null;
        		Node nn = null;
        		while(it.hasNext()) {
        			cur = it.next(); nn = itc.next();
        			if(key.compareTo(cur)<=0)break;        			
        		}
        		while(cur.compareTo(key)==0) {
        			ret.addAll(nn.rangeSearch(key, comparator));
        			if(!it.hasNext())break;
        			nn = itc.next();
        			cur = it.next();
        		}
        		if(cur.compareTo(key)==0) {
        			if(itc.hasNext()) {
        				nn = itc.next();
        				ret.addAll(nn.rangeSearch(key, comparator));
        			}
        		}else {
        		    ret.addAll(nn.rangeSearch(key, comparator)); nn = itc.next();
        		    ret.addAll(nn.rangeSearch(key, comparator));
        		} 
        		return ret;
        	case "<=":
        	    it = keys.iterator();
                itc = this.children.iterator();
                cur = null;
                nn = null;
                while(it.hasNext()) {
                    cur = it.next(); nn = itc.next();
                    if(cur.compareTo(key)>0)break;                 
                }                
                if(cur.compareTo(key)<=0) {
                    nn = itc.next();
                    ret.addAll(nn.rangeSearch(key, comparator));
                }else ret.addAll(nn.rangeSearch(key, comparator));
                return ret;
        	case ">=":
        	    it = keys.iterator();
                itc = this.children.iterator();
                cur = null;
                nn = null;
                while(it.hasNext()) {
                    cur = it.next(); nn = itc.next();
                    if(cur.compareTo(key)>=0)break;                 
                }
                if(cur.compareTo(key)<0) {
                    nn = itc.next();
                    ret.addAll(nn.rangeSearch(key, comparator));                    
                }else ret.addAll(nn.rangeSearch(key, comparator));
                return ret;        		
        	}
        	
            return null;
        }
    
    } // End of class InternalNode
    
    
    /**
     * This class represents a leaf node of the tree.
     * This class is a concrete sub class of the abstract Node class
     * and provides implementation of the operations that
     * required for leaf nodes.
     * 
     * @author sapan
     */
    private class LeafNode extends Node {
        
        // List of values
        List<V> values;
        
        // Reference to the next leaf node
        LeafNode next;
        
        // Reference to the previous leaf node
        LeafNode previous;
        
        /**
         * Package constructor
         */
        LeafNode() {
            super();
            this.values = new LinkedList<>();
        }
        
        
        /**
         * Gets the first leaf key of the tree
         * 
         * @return key
         */
        K getFirstLeafKey() {
        	if(keys.isEmpty())return null;
            return keys.get(keys.size()-1);
        }
        
        /**
         * judge if the node contains more keys than allowed
         * 
         * @return true if node contains more keys than allowed
         */
        boolean isOverflow() {
            return keys.size()==branchingFactor;
        }
        
        /**
         * insert a key-value pair into the node and its subtree, split if needed
         * 
         * @return null if the node doesn't need to be split, else the 
         *                                                  node to be pushed up
         */
        InternalNode insert(K key, V value) {
        	int ind;
        	for(ind = 0; ind<=keys.size(); ind++) {
        		if(ind<keys.size()&&key.compareTo(this.keys.get(ind))<=0) {
        			keys.add(ind, key);
        			values.add(ind, value);
        			break;
        		}else if(ind==keys.size()) {
        		    keys.add(ind, key);
                    values.add(ind, value);
                    break;
        		}
        	}        
        	if(this.isOverflow()) {
        	    return this.split();
        	}else return null;
        }
        
        /**
         * split the node, return the new node generated
         * 
         * @return an internal node that is the parent of two split children
         */
        InternalNode split() {
        	LeafNode left = new LeafNode();
        	LeafNode right = new LeafNode();
        	InternalNode top = new InternalNode();   
        	left.keys.addAll(keys.subList(0, keys.size()/2));
        	left.values.addAll(values.subList(0, values.size()/2));
        	right.values.addAll(values.subList(values.size()/2, values.size()));
        	right.keys.addAll(keys.subList(keys.size()/2, keys.size()));
        	top.children.add(left);
        	top.children.add(right);
        	top.keys.add(left.keys.get(left.keys.size()-1));
        	left.next = right;
        	right.previous = left;
        	left.previous = previous;
        	right.next = next;   
        	if(previous!=null)previous.next = left;
        	if(next!=null)next.previous = right;
            return top;
        }
        
        /**
         * range search for the key in the node and its subtree
         * 
         * @return a list contains everything in the range
         */
        List<V> rangeSearch(K key, String comparator) {
            List<V> ret = new LinkedList<>();
            switch(comparator) {
                case "==":
                    Iterator<K> it = keys.iterator();
                    Iterator<V> itv = values.iterator();
                    while(it.hasNext()) {
                        K cur = it.next();
                        if(cur.compareTo(key)==0)ret.add(itv.next());
                        else itv.next();
                    }
                    return ret;
                case ">=":
                    it = keys.iterator();
                    itv = values.iterator();
                    while(it.hasNext()) {
                        K cur = it.next();
                        if(cur.compareTo(key)>=0)ret.add(itv.next());
                        else itv.next();
                    }
                    LeafNode curNode = next;                    
                    while(curNode!=null) {
                        ret.addAll(curNode.values);
                        curNode = curNode.next;
                    }                    
                    return ret;
                case "<=":
                    it = keys.iterator();
                    itv = values.iterator();
                    while(it.hasNext()) {
                        K cur = it.next();
                        if(cur.compareTo(key)<=0)ret.add(itv.next());
                        else itv.next();
                    }
                    curNode = previous;                    
                    while(curNode!=null) {
                        ret.addAll(curNode.values);
                        curNode = curNode.previous;
                    }          
                    return ret;
                
            }
            return null;
        }
        
    } // End of class LeafNode
    
    
    /**
     * Contains a basic test scenario for a BPTree instance.
     * It shows a simple example of the use of this class
     * and its related types.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // create empty BPTree with branching factor of 3
//        BPTree<Integer, Integer> bpt = new BPTree<>(3);
//        bpt.insert(1, 1);
//        bpt.insert(2, 2);
//        bpt.insert(3, 3);
//        bpt.insert(4, 4);
//        bpt.insert(5, 5);
//        bpt.insert(6, 6);
//        bpt.insert(7, 7);
//        bpt.insert(4, 8);
//        bpt.insert(4, 9);
//        bpt.insert(4, 10);
//        bpt.insert(4, 11);
//        System.out.println(bpt.rangeSearch(4, "<=").toString());
//        
//        System.out.println("success");
     // create empty BPTree with branching factor of 3
        BPTree<Double, Double> bpTree = new BPTree<>(3);

        // create a pseudo random number generator
        Random rnd1 = new Random();

        // some value to add to the BPTree
        Double[] dd = {0.0d, 0.5d, 0.2d, 0.8d};

        // build an ArrayList of those value and add to BPTree also
        // allows for comparing the contents of the ArrayList 
        // against the contents and functionality of the BPTree
        // does not ensure BPTree is implemented correctly
        // just that it functions as a data structure with
        // insert, rangeSearch, and toString() working.
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            Double j = dd[rnd1.nextInt(4)];
            list.add(j);
            bpTree.insert(j, j);
            System.out.println("\n\nTree structure:\n" + bpTree.toString());
        }
        int ans = 0;
        for(Double a : list) {if(a==0.2d)ans++;}
        List<Double> filteredValues = bpTree.rangeSearch(0.2d, "==");
        System.out.println("Filtered values: " + filteredValues.toString());
        System.out.println(ans+" "+filteredValues.size());
    }

} // End of class BPTree
