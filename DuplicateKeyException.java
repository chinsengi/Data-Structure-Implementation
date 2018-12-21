/** 
 *  Thrown when trying to add a duplicate key to the BST.
 *     
 *  DO NOT EDIT THIS CLASS
 *  
 */  
class DuplicateKeyException extends Exception {
        private static final long serialVersionUID = 1L; 
 
        public DuplicateKeyException() {
		super("Duplicate Key is not permitted");
        }   
 
        public DuplicateKeyException(String message) {
                super(message);
        }   
}
