import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


public class Game {
	Node[][] nodeMatrix;
	static int BOUNDARY = 6;
	static int ROBOT_LEVEL = -100;
	static int HOLD_LEVEL = 56;
	static int VACUUM_LEVEL = 0, GRASS_LEVEL =1, BUSH_LEVEL =2, TREE_LEVEL=3, HUT_LEVEL=4, HOUSE_LEVEL=5, MANSION_LEVEL = 6, CASTLE_LEVEL = 7 ;
	static int BEAR_LEVEL = -10,  TOMB_LEVEL = -9, CHURCH_LEVEL=-8, CATHEDRAL_LEVEL=-7,TREASURE_LEVEL=-6;
	static int CRYSTAL_LEVEL = 100, ROCK_LEVEL = 101, MOUNTAIN_LEVEL = 102;
	static double P_GRASS = 0.75, P_BUSH = 0.15, P_TREE =0.04, P_CRYSTAL= 0.04, P_HUT = 0.02;
	static double P_BEAR = 0.1, P_ROBOT = 0.1;
	int gameScore;
	int totalRound;
	int nextLevel;
	int nodeNum;
	double[][] scoreMap;
	double[][] judgeMap;
	public Game(){
		gameScore = 0;
		totalRound = 0;
		nodeNum = 0;
		nodeMatrix = new Node[BOUNDARY][BOUNDARY];
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				nodeMatrix[i][j] = new Node(i, j, nodeNum);
				nodeNum++;
			}
		}
		nodeMatrix[0][0].setLevel(HOLD_LEVEL);
		// for every node initialization.
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				setNeighbor(nodeMatrix[i][j]);
			}
		}		
	}

	
	public int getUpScore(Node curNode){
		int curLevel = curNode.getLevel();
		ArrayList<Node> upList = getThreeList(curNode);
		if(upList.size() >= 3){
			Node uplevelNode = getNewNode(curNode, curNode.getLevel()+1);
			if(upList.size() > 3)
				return getPoint(curLevel+1, true) + getUpScore(uplevelNode);
			else 	
				return getPoint(curLevel+1, false) + getUpScore(uplevelNode);
		}
		return 0;
	}
		
	public ArrayList<Node> getThreeList(Node curNode){
		boolean isExtend = true;
		int curLevel = curNode.getLevel();
		ArrayList<Node> upList = new ArrayList<Node>();		
		upList.add(curNode);
		//find all the up grade node by bfs
		while(isExtend){
			isExtend = false;
			ArrayList<Node> goList = new ArrayList<Node>(upList);
			for(Node checkNode : goList){
//				System.out.println("now check" + checkNode +" in " + goList);
				for(Node nearNode : checkNode.Neighbors){
//					System.out.println("Neighbor check" + upNode +" in " + checkNode.Neighbors);
					if(nearNode.getLevel() == curLevel ){
						if(!upList.contains(nearNode) && !containPlace(upList, nearNode)){
							upList.add(nearNode);
							isExtend = true;
//							System.err.println("combine List" + upList);
						}
					}
				}
			}
			goList.clear();
		}
		return upList;
	}
	
	// put the node into the map, 
	public int upgrade(Node curNode){	
		int curLevel = curNode.getLevel();
		Node newNode;
		if(curLevel == BEAR_LEVEL) return 0;
		int upScore = 0;
		ArrayList<Node> upList = getThreeList(curNode);
//		System.err.println(curNode + "\t seq: " + upList);
		if(upList.size() >= 3){
			for(Node upNode : upList){
				upNode.setLevel(VACUUM_LEVEL);
			}
//			curNode.setLevel(curLevel+1);
			newNode = getNewNode(curNode, curLevel+1);
			nodeMatrix[curNode.index[0]][curNode.index[1]] = newNode;
			refreshNeighbor();
			if(upList.size() > 3) 
				upScore += this.getPoint(curLevel+1, true) + upgrade(newNode);
			else 
				upScore += this.getPoint(curLevel+1, false) + upgrade(newNode);
			upList.clear();
//			upList = getThreeList(curNode);
		}
		return upScore;
	}
	
	public int decideCrystal(Node curNode){
		int maxScore = 0, tmpScore;
		int upLevel = 0;
		HashSet<Integer> levelSet = new HashSet<Integer>();
		
		// get all possible level in the map
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if(nodeMatrix[i][j].getLevel() != 0)
					levelSet.add(nodeMatrix[i][j].getLevel());
			}
		}
		// try which level can get the max of score.
		for(Integer level : levelSet){
			Node tNode = getNewNode(curNode, level);
			tmpScore = getUpScore(tNode); 
			if( tmpScore > maxScore){
				maxScore = tmpScore;
				upLevel = level;
			}
		}
		
		if(maxScore==0) return ROCK_LEVEL;
		return upLevel;
	}
	
//	public int putAction(Node upNode, int putlevel){
//		if(putlevel == CRYSTAL_LEVEL) {
//			int dLevel = decideCrystal(upNode);
//			upNode.setLevel(dLevel );
//		}else{
//			upNode.setLevel( putlevel);
//		}
//		
//		return upgrade(upNode);
//	}
	public int putAction(Node upNode, int putlevel){
		int dLevel = 0;
		if( putlevel == CRYSTAL_LEVEL) {
			dLevel = decideCrystal(upNode);
		}else{
			dLevel = putlevel;
		}
		upNode.setLevel( dLevel );
		Node curNode = getNewNode(upNode, dLevel);
		nodeMatrix[curNode.index[0]][curNode.index[1]] = curNode;
		refreshNeighbor();
//		System.err.println("put " + curNode);
		return upgrade(upNode);
	}
	
	
	public Node getNewNode(Node n, int level){
		Node tNode = new Node(n.index[0], n.index[1], nodeNum++);
		setNeighbor(tNode);
		tNode.setLevel(level);
		return tNode;
	}
	
	// to get next output  
	// P_GRASS = 0.75, P_BUSH = 0.15, P_TREE =0.04, P_CRYSTAL= 0.04, P_HUT = 0.02;
	// P_BEAR = 
	public int getNextLevel(){
		double r = Math.random();
		r = r*1.1;
		if(r < P_GRASS) 	return GRASS_LEVEL; // 1
		else if(r < P_GRASS + P_BUSH) 	return BUSH_LEVEL; //100
		else if(r < P_GRASS + P_BUSH + P_TREE) 	return TREE_LEVEL; 
		else if(r < P_GRASS + P_BUSH + P_TREE + P_HUT) 	return HUT_LEVEL;
		else if(r < P_GRASS + P_BUSH + P_TREE + P_HUT + P_CRYSTAL) 	return CRYSTAL_LEVEL;
		else return BEAR_LEVEL;
	}
	
	public int goOver(int count) throws IOException{
		int round = 0;
		while(!isFinish()){
//			System.out.println("=============== ROUND #"+ round + " =====================");
			goRound();
			if(round++ >= count) break;
		}
		
		totalRound = round;
		System.out.println(totalRound  + "\t" + gameScore);
		return round;
	}
	
	public void bearMoveAll(){
		int bearScore=0;
		ArrayList<Node> bearList = new ArrayList<Node>();
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if( nodeMatrix[i][j].getLevel() == BEAR_LEVEL ){
					bearList.add(nodeMatrix[i][j]);
				}
			}
		}
		// from the lastest bear to move to tomb
		Collections.sort( bearList );
		Collections.reverse(bearList);
		for(Node bearNode : bearList){
			changeTomb(bearNode);
			bearScore += bearMove(bearNode);
		}
	}
	
	public int changeTomb(Node bearNode){
		if(bearNode.getLevel()!=BEAR_LEVEL) 
			return -1;
		int bearScore = 0;
		boolean isChange = true;
		ArrayList<Node> bearSeq = getThreeList(bearNode);
		ArrayList<Node> nearList = new ArrayList<Node>();
		
		for(Node n : bearSeq){
			for(Node nearNode : n.Neighbors){
				if( nearNode.getLevel() == VACUUM_LEVEL){
					isChange = false;
				}
			}
		}
		// from the lastest to check move to lastest
		Collections.sort( bearSeq );
		Collections.reverse(bearSeq);
		if(isChange){
			for(Node n : bearSeq){
				n.setLevel(TOMB_LEVEL);
				bearScore += upgrade(n);
//				gameScore += upgrade(n);
			}
		}
		return bearScore;
	}
	
	public int bearMove(Node bearNode){
		if(bearNode.getLevel()!=BEAR_LEVEL) 
			return 0;
		int moveIndex ;
		Node selectNode;
		ArrayList<Node> moveSet = new ArrayList<Node>();
		for(Node nearNode : bearNode.Neighbors){
			if(nearNode.getLevel() == VACUUM_LEVEL){
				moveSet.add(nearNode);
			}	
		}
		// if there is no place to go, to be tomb!
		// or random select a direction to move
		if(!moveSet.isEmpty()){
			moveIndex = (int) ( (Math.random()*100) % moveSet.size() );
			moveSet.get(moveIndex).setLevel(bearNode.getLevel());
			bearNode.setLevel(VACUUM_LEVEL);
//			System.out.println("bear:"+ bearNode + " move to" + moveSet.get(moveIndex) );
		}
		return 0;
	}
//	public int gameRound(int level){
//		
//	}
	
	public int goRound() throws IOException{
		int curLevel = getNextLevel();
		int getScore = 0;
		Algorithm algo = new Algorithm();
		GameEstimator ge = new GameEstimator();
//		System.out.println("Output Node: Lv."+ curLevel);
		scoreMap = getMDPscore(curLevel);
		judgeMap = getMDPscore(curLevel);
		updateSpaceSize();
		ge.updateAll(this, curLevel);
		updateEstScore();
		
		
//		ge.updateAll(this, curLevel);

//		Node putNode = algo.manualPick(this);
//		Node putNode = algo.pickRandom(this);
		Node putNode = algo.pickMax(curLevel, this);
//		Node putNode = algo.pickNeighbor(curLevel, this);
		
//		printGameSpec();
//		System.out.println(this);
//		System.out.println("pick:\t" + putNode);
		
		getScore = putAction( putNode,  curLevel );
		bearMoveAll();
		gameScore += getScore;
//		gameScore += 5;
		return getScore;		
	}
	public void updateJudgeMap(){
		
	}
	
	// calculate the score for all the map 
	public double[][] getMDPscore(int level){
		double[][] scoreMatrix = new double[BOUNDARY][BOUNDARY];
		double score=0.0;
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if(nodeMatrix[i][j].getLevel()==0){
					
					Node putNode = new Node(i,j); // add a fake node to estimate
					setNeighbor(putNode);
					
					if(level==CRYSTAL_LEVEL) putNode.setLevel( decideCrystal(nodeMatrix[i][j]) ); 
					else putNode.setLevel(level);
					
					score = getUpScore(putNode); // level up the fake node
					scoreMatrix[i][j] = score;
//					nodeMatrix[i][j].estScore = score;
				}else{
					scoreMatrix[i][j] = -1;
//					nodeMatrix[i][j].estScore = -1;
				}
			}
		}
		scoreMap = scoreMatrix;
		updateEstScore();
		return scoreMatrix;
	}
	
	public void updateSpaceSize(){
		boolean isExtend = true;
		int curLevel = VACUUM_LEVEL;
		ArrayList<Node> okList = new ArrayList<Node>();
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				Node curNode = nodeMatrix[i][j];
				if( curNode.getLevel() != VACUUM_LEVEL ){
					curNode.spaceSize = -1;
				}else{
					isExtend = true;
					ArrayList<Node> spaceList = new ArrayList<Node>();		
					spaceList.add(curNode);
					//find all the up grade node by bfs
						while(isExtend){
							isExtend = false;
							ArrayList<Node> goList = new ArrayList<Node>(spaceList);
							for(Node checkNode : goList){
		//						System.out.println("now check" + checkNode +" in " + goList);
								for(Node nearNode : checkNode.Neighbors){
		//							System.out.println("Neighbor check " + nearNode +" in " + checkNode.Neighbors);
									if(nearNode.getLevel() == VACUUM_LEVEL ){
										if(!spaceList.contains(nearNode) && !containPlace(spaceList, nearNode)){
											spaceList.add(nearNode);
											okList.add(nearNode);
//											System.out.println(curNode + " add " + nearNode);
											isExtend = true;
										}
									}
								}
							}
							goList.clear();
						}
						int sSize = spaceList.size();
						curNode.spaceSize = sSize;
						for(Node sNode : spaceList){
							sNode.spaceSize = sSize;
						}
					}
			}
		}
	}
	
	
	public void updateEstScore(){
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
			nodeMatrix[i][j].estScore = scoreMap[i][j];
			}
		}
	}
	public ArrayList<Node> getAvList(){
		ArrayList<Node> avList = new ArrayList<Node>();
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				 if(nodeMatrix[i][j].getLevel()==0){
					 if( !(i==0 && j ==0) )
						 avList.add(nodeMatrix[i][j]);
				 }
			}
		}
		return avList;
	}
	
	public void setNeighbor(Node n){
		ArrayList a = new ArrayList();
		int x = n.getIndex()[0];
		int y = n.getIndex()[1];
		if( x != BOUNDARY-1 ){
			a.add(nodeMatrix[x+1][y]);
		}
		if( x != 0 ){
			a.add(nodeMatrix[x-1][y]);
		}
		if( y != BOUNDARY-1 ){
			a.add(nodeMatrix[x][y+1]);
		}
		if( y != 0 ){
			a.add(nodeMatrix[x][y-1]);
		}
		if(a.contains(nodeMatrix[0][0]))
			a.remove(nodeMatrix[0][0]);
		n.Neighbors = a;
	}
	public boolean containPlace(ArrayList<Node> checklist, Node checkNode){
		for(Node n : checklist){
			if(n.sameLocal(checkNode)) return true;
		}
		return false;
	}
	// print out the game status (level) map
	public String toString(){
		String str = "";
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				str = str + nodeMatrix[i][j].level +"\t";
			}
			str = str +  "\n";
		}
		return str;
	}
	
	public String printGameSpec(){
		String str= "";
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				str = str + nodeMatrix[i][j] +"\t";
			}
			str = str + "\n";
		}
		System.out.println(str);
		return str;
	}
	
	// check if there is no space for new coming node.
	public boolean isFinish(){
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if(  nodeMatrix[i][j].getLevel() == 0  )
					return false;
			}
		}
		return true;
	}
	
	// get the max level of the board 
	public int findMax(){
		int max =0;
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if(  nodeMatrix[i][j].getLevel() > max  )
					max = nodeMatrix[i][j].getLevel();
			}
		}
		return max;
	}
	
	public void setMatrix(Node[][] matrix){
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				this.nodeMatrix[i][j] = matrix[i][j];
			}
		}
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				this.setNeighbor(nodeMatrix[i][j]);
			}
		}
	}
	
	public void refreshNeighbor(){
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				setNeighbor(nodeMatrix[i][j]);
			}
		}
	}
	
	public int getPoint(int level, boolean bonus){
		int score =0;
		
		if(level == GRASS_LEVEL) score = 5;
		if(level == BUSH_LEVEL) score = 20;
		if(level == TREE_LEVEL)score = 100;
		if(level == HUT_LEVEL)score = 500;
		if(level == HOUSE_LEVEL)score = 1500;
		if(level == MANSION_LEVEL)score = 5000;
		if(level == CASTLE_LEVEL)score = 20000;
		
		if(level == CHURCH_LEVEL)score = 1000;
		if(level == CATHEDRAL_LEVEL)score = 5000;
		if(level == MOUNTAIN_LEVEL)score = 1000;
		
		if(bonus)
			score *= 2;
		return score;
	}
}