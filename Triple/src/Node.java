import java.util.ArrayList;


public class Node implements Comparable{
	Integer [] index;
	ArrayList<Node> Neighbors;
	int level;
	double estScore;
	int nodeNum=0;
	int spaceSize=-1;
	public Node(int x , int y){
		index = new Integer[2];
		index[0] = x;
		index[1] = y;
		level = 0;
		estScore = 0.0;
		spaceSize = -1;
	}
	public Node(int x , int y, int nodeNum){
		index = new Integer[2];
		index[0] = x;
		index[1] = y;
		level = 0;
		estScore =0.0;
		this.nodeNum = nodeNum;
	}
	public boolean sameLocal(Node n ){
		if( (this.index[0] == n.index[0]) && (this.index[1]==n.index[1]) )
			return true;
		else
			return false;
	}
	public Integer[] getIndex(){
		return index;
	}
	public ArrayList<Node> getNeighbor(){
		return Neighbors;
	}
	public String toString(){
		String str = this.level +"  #" + nodeNum +"("+index[0] + "," +index[1] + ")" + " spa="+spaceSize;
//		String str = ""+ estScore;
		return str;
	}
	public int getLevel(){
		return this.level;
	}
	public void setLevel(int l){
		this.level = l;
	}
	public void upgrade(){
		this.level +=1;
	}
	@Override
	public int compareTo(Object obj) {
		return (int)( this.nodeNum - ((Node)obj).nodeNum );
	}
}
