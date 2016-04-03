package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Nemo Li
 * 
 * Worker thread for the server, handles a game
 */
public class WorkerThread extends Thread{
	
	private PrintWriter out[];
	private BufferedReader in[];
	private int playerA;
	private int playerB;
	
	
	
	/**
	 * @param A - socket of one player
	 * @param B - socket of another player
	 * @throws IOException
	 * 
	 * create a thread and sets up the in/out put streams
	 * also determines the order of the game randomly
	 */
	public WorkerThread(Socket A, Socket B) throws IOException{
		in = new BufferedReader[2];
		out = new PrintWriter[2];
		
		in[0] = new BufferedReader(new InputStreamReader(A.getInputStream()));
		in[1] = new BufferedReader(new InputStreamReader(B.getInputStream()));
		out[0] = new PrintWriter(A.getOutputStream(), true);
		out[1] = new PrintWriter(B.getOutputStream(), true);
		
		playerA = (int) (Math.random() * 2); // determines who goes first
		playerB = 1 - playerA;
	}
	

	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		if (!setTerrain()){		// coordinate two players to setup terrain
			return;				// and pieces, return if anything goes wrong
		}else if (!setPiece()){
			return;
		}
		
		int turn = playerA;
		int actionCode = -1;
		
		out[playerA].println(Data.PROT_START_FIRST);  // send start signals
		out[playerB].println(Data.PROT_START_SECOND);
		
		while (true) {	// loop drives the turns of the game
			actionCode = -1;
			actionCode = readAction(turn);

			if (isBoardClick(actionCode)) {
				out[1 - turn].println(actionCode);
			} else if (actionCode == Data.PROT_TOGGLE_TURN) {
				out[1 - turn].println(Data.PROT_TOGGLE_TURN);
				turn = 1 - turn;
			} else if (actionCode == -1){
				return;	// if the stream end, return
			}
		}
	}
	
	
	
	/**
	 * @return true if successfully set terrain
	 */
	private boolean setTerrain() {
		int setter = (int) (Math.random() * 2);
		
		out[setter].println(Data.PROT_ACTIVE);
		out[setter].println(Data.PROT_SET_TERRAIN);
		out[1 - setter].println(Data.PROT_PASSIVE);
		out[1 - setter].println(Data.PROT_SET_TERRAIN);
		
		int click = 0;

		while (true) {
			click = -1;
			click = readAction(setter);

			if (isBoardClick(click)) {
				out[1 - setter].println(click);	// relay a board click
			} else if (click == Data.PROT_DONE_SET_TERRAIN) {
				return true;
			} else if (click == -1){
				return false; // stream closed
			}
		}
	}

	
	
	/**
	 * @return true if successfully setup pieces
	 */
	private boolean setPiece() {
		out[playerA].println(Data.PROT_SETUP_A);	// let A setup and B wait
		out[playerB].println(Data.PROT_WAIT_FOR_A); 

		if (!setPiece(playerA)){
			return false;
		}
		
		out[playerB].println(Data.PROT_SETUP_B);	// let B setup and A wait
		out[playerA].println(Data.PROT_WAIT_FOR_B);
		
		if (!setPiece(playerB)){
			return false;
		}
		
		return true;
	}

	
	
	/**
	 * @param player - the player whom the server is setting piece for
	 * @return true if nothing goes wrong
	 */
	private boolean setPiece(int player) {
		int click = -1;

		while (true) {
			click = -1;
			click = readAction(player);

			if (isBoardClick(click)) {
				out[1-player].println(click);
			} else if (click == Data.PROT_DONE_SET_PIECE) {
				return true;
			} else if (click == -1){
				return false; // stream closed
			}
		}
	}
	
	
	
	/**
	 * @param player - player reading from
	 * @return action code sent by player
	 */
	private int readAction(int player){
		String line = null;
		
		try {
			line = in[player].readLine();
		} catch (IOException e) {
			return -1;
		}

		if (line == null) {
			return -1;
		}
		
		return Integer.parseInt(line);
	}
	

	
	/**
	 * @param id - action code id
	 * @return if this is a click on board
	 */
	private boolean isBoardClick(int id) {
		return id > -1 && id < Data.PROT_R_CLICK_LIMIT;
	}

}
