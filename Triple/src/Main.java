import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
	GameEstimator ge;
	public static void main(String[] args) throws IOException {

		
//		GameEstimator ge = new GameEstimator();
//		Node resultNode = ge.getNode2file();
//		ge.writeNode2file(resultNode);

		int runTime = 20;
		runGame(runTime);
	}
	
	
	public static void runGame(int runTime) throws IOException{
		int avg = 0;
		int round_avg = 0;
		Game [] g = new Game[runTime];
		GameEstimator ge = new GameEstimator();
		for(int i=0;i<runTime;i++ ){
			g[i] = new Game();
			System.out.print( (i+1) +"\t");
			g[i].goOver(1000);
//			System.out.println("step: #"+g[i].totalRound + "\t max = " +  g[i].findMax() + "  score: " + g[i].getGameScore());
			round_avg += g[i].totalRound;
			avg += g[i].gameScore;
		}
		avg = avg/runTime;
		round_avg = round_avg/runTime;
		System.out.println("Avg. Score = " + avg);
		System.out.println("Avg. Round = " + round_avg);
	}
	
}