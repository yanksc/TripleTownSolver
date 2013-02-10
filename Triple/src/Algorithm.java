import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Algorithm {
	static int VACUUM_LEVEL = 0, GRASS_LEVEL =1, BUSH_LEVEL =2, TREE_LEVEL=3, HUT_LEVEL=4, HOUSE_LEVEL=5, MANSION_LEVEL = 6, CASTLE_LEVEL = 7 ;
	static int BEAR_LEVEL = -10, TOMB_LEVEL = -9, CHURCH_LEVEL=-8, CATHEDRAL_LEVEL=-7,TREASURE_LEVEL=-6;
	static int CRYSTAL_LEVEL = 100, ROCK_LEVEL = 101, MOUNTAIN_LEVEL = 102;
	public Algorithm(){
	}
	
	public Node pickRandom(Game g){
		int index = 0;
		ArrayList<Node> avList = g.getAvList();
		return pickfromList(avList);
	}
	
	public Node pickRemove(Game g){
		return pickRemove(g, TOMB_LEVEL);
	}
	
	public Node pickRemove(Game g, int removelevel){
		ArrayList<Node> removeList = new ArrayList<Node>();
		ArrayList<Node> allList = new ArrayList<Node>();
		for(int i=0; i<g.BOUNDARY; i++){
			for(int j=0; j<g.BOUNDARY; j++){
				if(g.nodeMatrix[i][j].getLevel() == removelevel){
					removeList.add(g.nodeMatrix[i][j]);
				}
				if(g.nodeMatrix[i][j].getLevel() != 0){
					allList.add(g.nodeMatrix[i][j]);
				}
			}
		}
		
		if(allList.contains(g.nodeMatrix[0][0]))
			allList.remove(g.nodeMatrix[0][0]);
		
		if(removeList.isEmpty()){ 
			if(removelevel == TOMB_LEVEL)
				return pickRemove(g, ROCK_LEVEL);
			if(removelevel == ROCK_LEVEL)
				return pickRemove(g, GRASS_LEVEL);
			if(removelevel == GRASS_LEVEL)
				return pickRemove(g, BUSH_LEVEL);
			else
				return pickfromList(allList);
		}
		else
			return pickfromList(removeList);
		 
	}
	
	public Node pickMax(int level, Game g){
		int index=0;
		double maxScore = 0.1;
		double nodeScore = 0.0; 
		Node maxNode;
		ArrayList<Node> avlist = g.getAvList();
		ArrayList<Node> pickList = new ArrayList<Node>();
		
		for(Node n : avlist){
			nodeScore = g.judgeMap[n.index[0]][n.index[1]];
//			nodeScore = n.estScore;
			if(nodeScore >= maxScore){
				maxScore = nodeScore;
			}
		}
//		System.out.println("find maxScore: " + maxScore);
		// add the node with maxScore as a list
		for(Node n : avlist){
			nodeScore = g.judgeMap[n.index[0]][n.index[1]];
//			nodeScore = n.estScore;
			if(nodeScore == maxScore){
				pickList.add(n);
			}
		}
		// random select the list
		if(pickList.isEmpty()) {
//			System.err.println("no point get QQ ");
			return pickNeighbor(level, g);
		}else{
			return pickfromList(pickList);
		}
			
	}
	
	public Node pickNeighbor(int level, Game g){
		ArrayList<Node> avList = g.getAvList();
		ArrayList<Node> pickList = new ArrayList<Node>();
		int index=0;
		int curlevel = level;
		if(level == CRYSTAL_LEVEL) curlevel = ROCK_LEVEL;
		
		for(Node n : avList){
			for(Node nearNode : n.Neighbors){
				if(nearNode.getLevel() == curlevel ){
					pickList.add(n);
				}
			}
		}
		
		if( pickList.isEmpty() ){
			if(curlevel != CASTLE_LEVEL && 
				curlevel != TREASURE_LEVEL &&
				curlevel != MOUNTAIN_LEVEL ) {
					return pickNeighbor(curlevel+1, g);
			}else{
				return pickRandom(g);
			}
		}
//		System.out.println("select Neighbor lv." + curlevel + "\t" + pickList);
		return pickfromList(pickList);
	}
	
	public Node pickfromList(ArrayList<Node> pickList){
		int index =0;
		for(Node n : pickList){
			index = (int) (Math.random() * pickList.size()*10);
			index = index % pickList.size();
		}
		return pickList.get(index);
	}
	

	
	public Node manualPick(Game g) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = br.readLine();
		int x = Integer.valueOf(input.split(" ")[0]);
		int y = Integer.valueOf(input.split(" ")[1]);
		return g.nodeMatrix[x][y];
	}	
}
