import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Filename:   AVLTree.java
 * Project:    p2
 * Authors:    Debra Deppeler, Ruoyu He, Shirui Chen
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Lecture:    002
 * 
 * Due Date:   Oct, 1
 * Version:    1.0
 * 
 * Credits:    Implement rotation methods according to Prof. Deppler's lecture notes
 * 
 * Bugs:       no known bugs, but not complete either
 */

/** 
 * AVLTree is class that represents a balanced binary search tree data structure, with its nested
 * class BSTNode to represent each individual node
 * @param <K> 
 */
public class AVLTree<K extends Comparable<K>> implements AVLTreeADT<K> {
     
    /** 
     * BSTNode is a nested class in AVLTree.
     *  Represents a tree node
     *  Has a field as a root node,
     *  Each node has a left and a right child
     *  Each node has its own height that starts from 1
     * @param <K> generic type extending comparable whose value is stored in each node
     */
    class BSTNode<K> {
        /* fields */
        private K key;  // the key stored in the node
        private int height; // the height of the node
        private BSTNode<K> left, right; // left child and right child of the node
        
        /**
         * Constructor for a BST node.
         * @param key
         */
        BSTNode(K key) {
            this.key = key;
			this.height = 1;
        }
        
        BSTNode(K key, BSTNode<K> left,BSTNode<K> right) {
            this.key = key;
            this.left = left;
            this.right = right;  
        }
        
        //accessor
        int getHeight(){
            return height;
        }
        
        K getKey(){
            return key;
        }
        
        BSTNode<K> getLeft() {
            return left;
        }
        
        BSTNode<K> getRight() {
            return right;
        }
        
        //mutator
        void setKey(K key){
            this.key = key;
        }
        
        void setLeft(BSTNode<K> left) {
            this.left = left;
        }
        
        void setRight(BSTNode<K> right) {
            this.right = right;
        }
		
		void setHeight(int height){
			this.height = height;
		}
	}
	
    private BSTNode<K> root; // root of the AVL tree
    
	//calculate the height
	/**
	 * Calculate the height at current node positions recursively, root has height 1
	 * @param node
	 * @return the height at current position
	 */
	private int calHeight(BSTNode<K> node){
		if(node==null) return 0;
		int rightHeight = node.getRight()==null ? 0 : node.getRight().getHeight();
		int leftHeight = node.getLeft()==null ? 0 : node.getLeft().getHeight();
		node.setHeight(Math.max(leftHeight, rightHeight)+1);
		return node.getHeight();
	}
	
	
    /**
     * Calculating branching factor of a specific node 
     * @param node the node to be calculate the branching factor
     * @return the branching factor = LeftChildHeight - RightChildHeight
     */
    private int getBranchingFactor(BSTNode<K> node){
        int leftHeight = node.getLeft()==null ? 0 : node.getLeft().getHeight();
        int rightHeight = node.getRight()==null ? 0 : node.getRight().getHeight();
        int branchingFactor = leftHeight-rightHeight;
        return branchingFactor;
    }
    
    
    /**
     * Checks for an empty AVL tree.
     * @return true if AVL tree contains 0 items
     */
    @Override
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Adds key to the AVL tree
     * @param key
     * @throws IllegalArgumentException if key is already in the AVL tree or null value inserted
     */
    @Override
    public void insert(K key) throws DuplicateKeyException, IllegalArgumentException {
		if (key == null) 
            throw new IllegalArgumentException("Key is null");
		
		root = insert(key, root);
        
 
    }
    
    /**
     * Helper method to insert the keys and balance the tree recursively 
     * @param key to delete
     * @param node Current node position
     * @return the current balanced node using rotation
     * @throws DuplicateKeyException
     */
    private BSTNode<K> insert(K key, BSTNode<K> node) throws DuplicateKeyException{
        if (node == null) return new BSTNode<K>(key);
        if (node.getKey().compareTo(key)==0)
            throw new DuplicateKeyException("Duplicate items inserted to the tree");
        if (key.compareTo(node.key)>0)
            node.setRight(insert(key, node.getRight()));
        else
            node.setLeft(insert(key, node.getLeft()));
        
		calHeight(node);// update the height of current node
		int branchingFactor = getBranchingFactor(node);
		
		//left rotate
		if(branchingFactor < -1 && key.compareTo(node.getRight().getKey())>0){
			return leftRotate(node);
		}
		//right left rotate
		if(branchingFactor < -1 && key.compareTo(node.getRight().getKey())<0){
			node.setRight(rightRotate(node.getRight()));
			return leftRotate(node);
		}
		//left right rotate
		if(branchingFactor > 1 && key.compareTo(node.getLeft().getKey())>0){
			node.setLeft(leftRotate(node.getLeft()));
			return rightRotate(node);
		}
		//right rotate
		if(branchingFactor > 1 && key.compareTo(node.getLeft().getKey())<0){
			return rightRotate(node);
		}
	    return node;
	}
    
    /**
     * Implements leftRotate at grandparent node
     * @param n grandparent node
     * @return parent node after rotation
     */
    private BSTNode<K> leftRotate(BSTNode<K> n) {
        BSTNode<K> G, P, K, temp;
        G = n;
        P = G.getRight();
        temp = P.getLeft();
        G.setRight(temp);
        P.setLeft(G);
		calHeight(G);//update the height
		calHeight(P);
        
        return P;
    }
    /**
     * Implements rightRotate at grandparent node
     * @param n grandparent node
     * @return parent node after rotation
     */
    private BSTNode<K> rightRotate(BSTNode<K> n){
        BSTNode<K> G, P, K, temp;
        G = n;
        P = G.getLeft();
        temp = P.getRight();
        G.setLeft(temp);
        P.setRight(G);
		calHeight(G);//update the height
		calHeight(P);
        
        return P;
    }

    /**
     * Deletes key from the AVL tree
     * @param key
     * @throws IllegalArgumentException if try to delete null
     */
    @Override
    public void delete(K key) throws IllegalArgumentException {
		if(key==null) throw new IllegalArgumentException("Key is null");
		try{
			root = delete(key, root);
		}catch(IllegalArgumentException e){
			System.out.println("This element is not in the tree.");
		}
        
    }

    /**
     * Helper method to delete nodes and balance the AVL tree recursively using rotation, returns
     * null if key is not found in the AVl tree
     * @param key 
     * @param node Current node position
     * @return node that deletes the value in the subtree and is balanced
     * @throws IllegalArgumentException if the node is null
     */
    private BSTNode<K> delete(K key, BSTNode<K> node) throws IllegalArgumentException{
        if (node == null)//if key is not found
            throw new IllegalArgumentException();
        if(key.compareTo(node.key)==0) {
            if (node.getLeft()==null&&node.getRight() == null) {
                return null;// deleted node is a leave
            }else if(node.getLeft() == null&& node.getRight() !=null){
                return node.getRight();//deleted node has only right child
            }else if(node.getLeft() != null && node.getRight() == null ) {
                return node.getLeft();//deleted node has only left child
            }else {
                //deleted node has two children
				BSTNode<K> cur = node.getLeft();
				while(cur.getRight()!=null) {
				    // finding the in-order predecessor
					cur = cur.getRight();
				}
				node.setKey(cur.getKey());// replace the key from the in-order predecessor
				node.setLeft(delete(cur.getKey(), node.getLeft()));// delete the predecessor from the old subtree 
				
				return node;
            }
        }
		
        if (key.compareTo(node.getKey())>0) {
            node.setRight(delete(key, node.getRight()));
        }else {
            node.setLeft(delete(key, node.getLeft()));
        }
		
		calHeight(node);// check for balance
		int branchingFactor = getBranchingFactor(node);
		
		// left rotate
		if(branchingFactor < -1 && getBranchingFactor(node.getRight())<=0){
			node = leftRotate(node);
			return node;
		}
		// right left rotate
		if(branchingFactor < -1 && getBranchingFactor(node.getRight())>0){
			node.setRight(rightRotate(node.getRight()));
			node = leftRotate(node);
			return node;
		}
		// left right rotate
		if(branchingFactor > 1 && getBranchingFactor(node.getLeft())<0){
			node.setLeft(leftRotate(node.getLeft()));
			node = rightRotate(node);
			return node;
		}
		// right rotate
		if(branchingFactor > 1 && getBranchingFactor(node.getLeft())>=0){
			node = rightRotate(node);
			return node;
		}
	
        return node;
    }

    /**
     * Search for a key in AVL tree
     * @param key
     * @return true if AVL tree contains that key
     * @throws IllegalArgumentException if searching for a null value
     */
    @Override
    public boolean search(K key) throws IllegalArgumentException {
        return search(key,root);
    }
    /**
     * Helper method to search for a given key recursively
     * @param key 
     * @param node 
     * @return true if the value is found in the AVL tree
     * @throws IllegalArgumentException
     */
    private boolean search(K key, BSTNode<K> node) throws IllegalArgumentException{
        if (key == null)
            throw new IllegalArgumentException("Key is null");
        if (node == null) return false;
        if (key.compareTo(node.getKey())==0) return true;
        if (key.compareTo(node.getKey())>0) return search(key, node.getRight());
        else return search(key, node.getLeft());
    }

    /**
     * Prints AVL tree in in-order traversal.
     */
    @Override
    public String print() {
		if(root==null)
			return "It is an empty tree";	
        return print(root);
    }
    
    /**
     * Helper method to recursively print out the key values in every node of AVL trees using in-order traversal
     * @param node Current node position
     * @return String of keys in every node of AVL tree 
     */
    private String print(BSTNode<K> node) {
        String s ="";
        if(node.getLeft() != null) s+=print(node.getLeft());
        s+=node.getKey()+" ";
        if (node.getRight() != null) s+=print(node.getRight());
        return s;
    }

    /**
     * Checks for the Balanced Search Tree.
     * @return true if AVL tree is balanced tree
     */
    @Override
    public boolean checkForBalancedTree() {
		return checkForBalancedTree(root);
    }
    
    /**
     * Private helper method to recursively check balance for AVL tree
     * @param node Current node position 
     * @return true if the absolute value of balanced factor at every position is no more than 1
     */
    private boolean checkForBalancedTree(BSTNode<K> node){
		if(node == null) return true;
		int branchingFactor = getBranchingFactor(node);
		if(branchingFactor>1 || branchingFactor<-1) return false;
		return checkForBalancedTree(node.getLeft()) && checkForBalancedTree(node.getRight());//recursively call this method at each node
    }
    
    /**
     * Checks for Binary Search Tree.
     * @return true if AVL tree is binary search tree.
     */
    @Override
    public boolean checkForBinarySearchTree() {
        return checkForBinarySearchTree(root);
    }
    
	/**
	 * Helper method to check a binary search tree
	 * @param node
	 * @return true the AVL tree is a binary search tree
	 */
	private boolean checkForBinarySearchTree(BSTNode<K> node){
		if(node == null) return true;
		boolean leftCorrect = node.getLeft()==null ? true : node.getLeft().getKey().compareTo(node.getKey())<0;//check if the left is smaller than right
		boolean rightCorrect = node.getRight()==null ? true : node.getRight().getKey().compareTo(node.getKey())>0;// check if the right is larger than the left
		return leftCorrect && rightCorrect && checkForBalancedTree(node.getLeft()) && checkForBalancedTree(node.getRight());
	}
	
    public static void main (String args[]) throws IllegalArgumentException, DuplicateKeyException {
        AVLTree<Integer> tree = new AVLTree<>();
        int[] in = {8,3,11,2,5,9,12,1,4,6,10,7};
        for(int i = 0; i < 12; i++) {
            tree.insert(in[i]);
        }
        tree.delete(10);
        return;
    }
}
