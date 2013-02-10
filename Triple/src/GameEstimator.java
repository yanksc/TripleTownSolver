import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class GameEstimator{
	Node[][] inputMatrix;
	static int BOUNDARY;
	static int ROBOT_LEVEL = -100;
	static int VACUUM_LEVEL = 0, GRASS_LEVEL =1, BUSH_LEVEL =2, TREE_LEVEL=3, HUT_LEVEL=4, HOUSE_LEVEL=5, MANSION_LEVEL = 6, CASTLE_LEVEL = 7 ;
	static int BEAR_LEVEL = -10,  TOMB_LEVEL = -9, CHURCH_LEVEL=-8, CATHEDRAL_LEVEL=-7,TREASURE_LEVEL=-6;
	static int CRYSTAL_LEVEL = 100, ROCK_LEVEL = 101, MOUNTAIN_LEVEL = 102;
	static double alpha = 0.5;
	static double beta = 0.1;
	static double P_GRASS = 0.75, P_BUSH = 0.15, P_TREE =0.04, P_CRYSTAL= 0.04, P_HUT = 0.02;
	int changeflag=0;
	HashMap<Integer, Integer> level2score;
	String readstr;
	Game oldGame, newGame;
	int nodeNum=0;
	Algorithm algo;
	public GameEstimator() throws IOException{
		this.newGame = new Game();
		this.BOUNDARY = newGame.BOUNDARY;
		level2score = getLevel2Score();
		Algorithm algo = new Algorithm();
		
	}
	
	public void writeNode2file(Node n) throws IOException{
		BufferedWriter fwriter = new BufferedWriter(new FileWriter("./put.txt"));
//		fwriter.write(changeflag +"\n" + n.index[0] + "\t" + n.index[1] );
		fwriter.write(changeflag +"\n" + n.index[1] + "\t" + n.index[0] );
		fwriter.close();
	}
	
	public Node getNode2file() throws IOException{
		Game g1 = new Game();
		Game g2 = new Game();
		
		Node selectNode, setNode, holdNode;
		Node[][] readMatrix = getMapfromfile();
		double setScore=0, holdScore=0;
		g1.setMatrix(readMatrix);
		g2.setMatrix(readMatrix);
		int setlevel = getLevelfromfile();
		int holdlevel = readMatrix[0][0].getLevel();
		int selectLevel = 0;
		Algorithm algo = new Algorithm();
		
//		System.out.println(printGameSpec(newGame));
		System.out.println(g1);
		
		System.out.println("=============== SET lv." + setlevel+" ===============");
		printScoreMap( g1.getMDPscore(setlevel) );
		g1.updateSpaceSize(); // space area 
		updateAll(g1, setlevel); // 1-way look ahead 
		
		System.out.println("--------------After update---------------");
		printScoreMap( g1.judgeMap );
		
		System.out.println("\n=============== HOLD lv." + holdlevel+" ===============");
		printScoreMap( g2.getMDPscore(holdlevel) );
		g2.updateSpaceSize();
		updateAll(g2, holdlevel);
		
		System.out.println("--------------After update---------------");
		printScoreMap( g2.judgeMap );
		
		// if is ROBOT, remove first
		if(setlevel == ROBOT_LEVEL){
			selectNode = algo.pickRemove(g1);
		}else{
			// pickMax will read g.scoreMap
			setNode = algo.pickMax(setlevel, g1);
			setScore = g1.judgeMap[setNode.index[0]][setNode.index[1]];
			holdNode = algo.pickMax(holdlevel, g2);
			holdScore = g2.judgeMap[holdNode.index[0]][holdNode.index[1]];
			
			// compare HOLD and CURRENT to decide change or not
			System.out.println("--------------RESULT---------------");
			System.out.println("SET: $" + setScore + "\tHOLD: $" + holdScore);
			if(setScore >= holdScore){
				selectNode = setNode;
				selectLevel = setlevel;
				System.out.println("(" + selectNode.index[1]+","+ selectNode.index[0] + ") lv." + selectLevel);
				changeflag = 0;
			}
			else{ 
				selectNode = holdNode;
				selectLevel = holdlevel;
				System.out.println("CHANGE (" + selectNode.index[0]+","+ selectNode.index[1] + ") lv." + selectLevel);
				changeflag = 1;
			}
		}
		return selectNode;
	}
	
	public void updateAll(Game curGame, int level){
		
		double[][] jMap = new double[BOUNDARY][BOUNDARY];
		for(int i=0; i<curGame.BOUNDARY; i++){
			for(int j=0; j<curGame.BOUNDARY; j++){
				if(curGame.scoreMap[i][j]!=-1){
					jMap[i][j] = curGame.scoreMap[i][j] + 
							alpha * fgScore(curGame.nodeMatrix[i][j], level, curGame)+ 
							beta * curGame.nodeMatrix[i][j].spaceSize; 
				}else
					jMap[i][j] = -1;
			}
		}
		curGame.judgeMap = jMap;
		curGame.updateEstScore();

	}
	// use future information add to current game
	public double fgScore(Node n, int level, Game curGame){
		Game fg = futureGame(curGame, n, level);
		int setLevel =0;
		double scoreSum =0;
		scoreSum = P_GRASS * extractLevelScore(fg, GRASS_LEVEL) +
				P_BUSH * extractLevelScore(fg, BUSH_LEVEL) +
				P_TREE * extractLevelScore(fg, TREE_LEVEL) +
				P_CRYSTAL * extractLevelScore(fg, CRYSTAL_LEVEL) +
				P_HUT * extractLevelScore(fg, HUT_LEVEL);
		
//		System.out.println("$1\t" + extractLevelScore(fg, 1));
//		System.out.println("$2\t"+ extractLevelScore(fg, 2));
//		System.out.println("$3\t"+ extractLevelScore(fg, 3));
//		System.out.println("$100\t"+ extractLevelScore(fg, 100));
//		System.out.println("SUM:\t" + curGame.scoreMap[n.index[0]][n.index[1]]+ "+" + scoreSum);
		return scoreSum;
		
	}
	
	public double extractLevelScore(Game g, int level){
		double gScore = 0.1;
		g.getMDPscore(level);
		for(int i=0; i<g.BOUNDARY; i++){
			for(int j=0; j<g.BOUNDARY; j++){
				if(g.scoreMap[i][j] > gScore)
					gScore = g.scoreMap[i][j]; 
			}
		}
		return gScore;
	}
	
	public Game futureGame(Game oldg, Node inNode, int curLevel){
		int futureScore = 0;
		Game fgame = new Game();
		
		copyGameState(oldg, fgame);
		
		Node putNode = fgame.getNewNode(inNode, curLevel);
		fgame.refreshNeighbor();
		futureScore = fgame.putAction(putNode,  curLevel);
//		fgame.getMDPscore(1);
//		System.out.println("得到" +futureScore);
		return fgame;
	}
	
	
	
	public void copyGameState(Game fromG, Game toG){
		int getLevel=0;
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				getLevel = fromG.nodeMatrix[i][j].getLevel();
				toG.nodeMatrix[i][j].setLevel(getLevel);
			}
		}
	}
	public String printGameSpec(Game g){
		String str= "";
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				str = str + g.nodeMatrix[i][j] +"\t";
			}
			str = str + "\n";
		}
		return str;
	}
	
	
	public void printScoreMap(double[][] scoreMap){
		String str = "";
		for(double[] scoreRow : scoreMap){
			for(double score : scoreRow){
				java.text.DecimalFormat df = new   java.text.DecimalFormat("0.00");
				if(score == -1){ 
					System.out.print("X"+"\t");
				}else{ 
					System.out.print(df.format(score)+"\t");
				}
			}
			System.out.println();
		}
	}
	
	public int computeScore(Game g){
		int score = 0;
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				if(level2score.containsKey(g.nodeMatrix[i][j].getLevel())){
//					System.out.println("add" + g.nodeMatrix[i][j] + " by score #" + level2score.get( g.nodeMatrix[i][j].getLevel() ));
					score += level2score.get( g.nodeMatrix[i][j].getLevel() );
				}
			}
		}
		return score;
	}
	
	public Node[][] getMapfromfile() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader("./map.txt"));
		Node[][] nMatrix = new Node[BOUNDARY][BOUNDARY];
		freader.readLine(); // skip the first line
		int row = 0;
		while( (readstr = freader.readLine()) != null ){
			if( readstr.split("\t").length < BOUNDARY) {
				System.out.println(readstr);
				break;
			}
			for(int cul=0; cul<BOUNDARY; cul++){
				String str = readstr.split("\t")[cul];
				nMatrix[row][cul] = new Node(row , cul, nodeNum++);
				nMatrix[row][cul].setLevel(Integer.parseInt(str));
			}
			row++;
		}
		return nMatrix;
	}
	public int getLevelfromfile() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader("./map.txt")); 
		int level = 0;
		if( (readstr = freader.readLine()) != null ){
			String str = readstr.split("\t")[0];
			level = Integer.parseInt(str);
		}
		return level;
	}
	
	public Game deriveGame(Game newGame, Game oldGame){
		for(int i=0; i<BOUNDARY; i++){
			for(int j=0; j<BOUNDARY; j++){
				// some upgrade node is generate
				if(newGame.nodeMatrix[i][j].getLevel() > oldGame.nodeMatrix[i][j].getLevel() ){
					Node upNode = new Node(i,j,nodeNum++);
					newGame.setNeighbor(upNode);
					newGame.nodeMatrix[i][j] = upNode;
				}
			}
		}

		return newGame;
	}
	

	public HashMap<Integer, Integer> getLevel2Score(){
		HashMap<Integer, Integer> level2score = new HashMap<Integer, Integer>();
//		 =2, TREE_LEVEL=3, HUT_LEVEL=4, HOUSE_LEVEL=5, MANSION_LEVEL = 6, CASTLE_LEVEL = 7 ;
//		 static int BEAR_LEVEL = -10,  TOMB_LEVEL = -9, CHURCH_LEVEL=-8, CATHEDRAL_LEVEL=-7,TREASURE_LEVEL=-6;
		
//		level2score.put(GRASS_LEVEL, 5);
//		level2score.put(BUSH_LEVEL, 20);
//		level2score.put(TREE_LEVEL, 100);
//		level2score.put(HUT_LEVEL, 500);
//		level2score.put(HOUSE_LEVEL, 1500);
//		level2score.put(MANSION_LEVEL, 5000);
//		level2score.put(CASTLE_LEVEL, 20000);
		
		
		// the enhance expected value
		level2score.put(GRASS_LEVEL, 5);
		level2score.put(BUSH_LEVEL, 30);
		level2score.put(TREE_LEVEL, 160);
		level2score.put(HUT_LEVEL, 820);
		level2score.put(HOUSE_LEVEL, 3100);
		level2score.put(MANSION_LEVEL, 12000);
		
//		level2score.put(TOMB_LEVEL, 0);
//		level2score.put(CHURCH_LEVEL, 1000);
//		level2score.put(CATHEDRAL_LEVEL, 5000);
//		level2score.put(TREASURE_LEVEL, 10000);

		level2score.put(TOMB_LEVEL, 0);
		level2score.put(CHURCH_LEVEL, 1000);
		level2score.put(CATHEDRAL_LEVEL, 7000);
		level2score.put(TREASURE_LEVEL, 24000);
		
		level2score.put(MOUNTAIN_LEVEL, 1000);
		
		return level2score;
	}
	/*
	public double[][] getEXPscore(int level, Game g){
	double[][] scoreMatrix = new double[BOUNDARY][BOUNDARY];
	double score=0.0;
	for(int i=0; i<BOUNDARY; i++){
		for(int j=0; j<BOUNDARY; j++){
			if(g.nodeMatrix[i][j].getLevel()==0){
				
				Node putNode = new Node(i,j); // add a fake node to estimate
				
				if(level==CRYSTAL_LEVEL) putNode.setLevel( g.decideCrystal(g.nodeMatrix[i][j]) ); 
				else putNode.setLevel(level);
				
				putNode.setLevel(level);
				score = g.getUpScore(putNode); // level up the fake node
				scoreMatrix[i][j] = score;
			}else
				scoreMatrix[i][j] = 0;
		}
	}
	return scoreMatrix;
	}
	*/
}
