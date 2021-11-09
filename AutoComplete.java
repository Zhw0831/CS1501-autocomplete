//TO-DO Add necessary imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Comparator;

public class AutoComplete{

  //TO-DO: Add instance variable: you should have at least the tree root
    private DLBNode root;

  public AutoComplete(String dictFile) throws java.io.IOException {
    //TO-DO Initialize the instance variables
    //root = new DLBNode(null, 0);

    Scanner fileScan = new Scanner(new FileInputStream(dictFile));
    while(fileScan.hasNextLine()){
      StringBuilder word = new StringBuilder(fileScan.nextLine());
      //TO-DO call the public add method or the private helper method if you have one
      add(word);
    }
    fileScan.close();
  }

  /**
   * Part 1: add, increment score, and get score
   */

  //add word to the tree
  public void add(StringBuilder word){
    	//TO-DO Implement this method
  		if (word == null) throw new IllegalArgumentException("calls add() with a null key");
    	root = addHelper(root, word, 0);
  }

  private DLBNode addHelper(DLBNode x, StringBuilder word, int pos){
  		DLBNode result = x;
        if (x == null){
            result = new DLBNode(null, 0);
            result.data = word.charAt(pos);
            // the case that we haven't finished inserting every char in the word
            if(pos < word.length()-1){
              result.child = addHelper(result.child, word, pos+1);
            } else {
              result.data = word.charAt(pos);
              // when we insert the whole word, isWord is true in the node containing the last char of the word
              result.isWord = true;
            }
        } else if(x.data == word.charAt(pos)) {
            // if the node's data matches our char, we should go to its child
            if(pos < word.length()-1){
              result.child = addHelper(result.child, word, pos+1);
            } else {
              result.data = word.charAt(pos);
              result.isWord = true;
            }
        } else {
            // the data is not the same as our char, so we should go to the next node's place in the linkedList
          result.sibling = addHelper(result.sibling, word, pos);
        }
        return result;
  }

  //increment the score of word
  public void notifyWordSelected(StringBuilder word){
    //TO-DO Implement this method
      // find the node containing the last char in the word
    DLBNode findNode = getNode(root, word.toString(), 0);
    if(findNode==null){
    	System.out.println("No such word in the trie.");
    	return;
    }
    // if the node is found, increment the score
    findNode.score++;
  }

  //get the score of word
  public int getScore(StringBuilder word){
    //TO-DO Implement this method
    DLBNode target = getNode(root, word.toString(), 0);
    return target.score;
  }
 
  /**
   * Part 2: retrieve word suggestions in sorted order.
   */
  
  //retrieve a sorted list of autocomplete words for word. The list should be sorted in descending order based on score.
  public ArrayList<Suggestion> retrieveWords(StringBuilder word){
    //TO-DO Implement this method
    ArrayList<Suggestion> predictions = new ArrayList<Suggestion> ();
    // find the node containing the last char of the word
    DLBNode start = getNode(root, word.toString(), 0);
    // store the word and its score in case the prefix is a word itself
    StringBuilder wordCopy = new StringBuilder();
    int startScore = start.score;
    wordCopy.append(word);
    if(start==null)
      return predictions;
    searchWord(start.child, word, predictions);
    // if the prefix is a word itself, add it to our predictions as well
    if(start.isWord) {
      StringBuilder startData = new StringBuilder();
      predictions.add(new Suggestion(startData.append(wordCopy), startScore));
    }

    // sort the ArrayList (ascending) based on the score
    Collections.sort(predictions, new Comparator<Suggestion>() {
    	public int compare (Suggestion c1, Suggestion c2) {
        	return c1.compareTo(c2);
    		}
		}
	);
    // reverse the order to make it descending
    Collections.sort(predictions, Collections.reverseOrder());

    return predictions;
  }

  // search the words starting with the prefix and add them to the prediction ArrayList
  private void searchWord(DLBNode start, StringBuilder word, ArrayList<Suggestion> predictions){
      // update the string builder every time to avoid being overwritten
      StringBuilder wordy = new StringBuilder();
      wordy.append(word);
      if(start==null)
        return;
      // append the char to our existing string
      wordy.append(start.data);
      // if our string is a word, store it into the predictions
      if(start.isWord && !predictions.contains(new Suggestion(wordy,start.score))){
        predictions.add(new Suggestion(wordy, start.score));
      }
      // search its child if its child is not null
      if(start.child!=null){
        searchWord(start.child, wordy, predictions);
      }
      // search its sibling if its child is not null
      if(start.sibling!=null){
        StringBuilder noLastLetter = new StringBuilder();
        // if we return from checking child recursion, go back one level to check the next node in the parent's linkedList
          // so we get the substring from the beginning to the state before adding the parent's data
          // so we can add the parent's sibling's data instead
        // if start.child is null and we go straight into this block, we go to the current node's sibling
        int lastIndex = wordy.lastIndexOf(String.valueOf(start.data));
        noLastLetter.append(wordy.substring(0,lastIndex));
        searchWord(start.sibling, noLastLetter, predictions);
      }

      return;
  }

  /**
   * Helper methods for debugging.
   */

  //Print the subtree after the start string
  public void printTree(String start){
    System.out.println("==================== START: DLB Tree Starting from "+ start + " ====================");
    DLBNode startNode = getNode(root, start, 0);
    if(startNode != null){
      printTree(startNode.child, 0);
    }
    System.out.println("==================== END: DLB Tree Starting from "+ start + " ====================");
  }

  //A helper method for printing the tree
  private void printTree(DLBNode node, int depth){
    if(node != null){
      for(int i=0; i<depth; i++){
        System.out.print(" ");
      }
      System.out.print(node.data);
      if(node.isWord){
        System.out.print(" *");
      }
        System.out.println(" (" + node.score + ")");
      printTree(node.child, depth+1);
      printTree(node.sibling, depth);
    }
  }

  //return a pointer to the node at the end of the start string. Called from printTree.
  private DLBNode getNode(DLBNode node, String start, int index){
    DLBNode result = node;
    if(node != null){
      if((index < start.length()-1) && (node.data.equals(start.charAt(index)))) {
          result = getNode(node.child, start, index+1);
      } else if((index == start.length()-1) && (node.data.equals(start.charAt(index)))) {
          result = node;
      } else {
          result = getNode(node.sibling, start, index);
      }
    }
    return result;
  }


  //A helper class to hold suggestions. Each suggestion is a (word, score) pair. 
  //This class should be Comparable to itself.
  public class Suggestion implements Comparable<Suggestion> {
    //TO-DO Fill in the fields and methods for this class. Make sure to have them public as they will be accessed from the test program A2Test.java.
    public StringBuilder word;
    public int score;

    // constructor
    public Suggestion(StringBuilder word, int score){
    	this.word = word;
    	this.score = score;
    }

    // overwrite the compareTo function so it compares based on the score
    // if the two words have the same score, we put the word with shorter length first
    public int compareTo(Suggestion other){
      if(this.score==other.score)
        return other.word.length() - this.word.length();
      return this.score - other.score;
    }

  }

  //The node class.
  private class DLBNode{
    private Character data;
    private int score;
    private boolean isWord;
    private DLBNode sibling;
    private DLBNode child;

    private DLBNode(Character data, int score){
        this.data = data;
        this.score = score;
        isWord = false;
        sibling = child = null;
    }
  }
}
