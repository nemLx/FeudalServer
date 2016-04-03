package tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Test;

import server.*;

public class serverTest {
	
	private boolean serverUp = true;
	private final String HOST = "127.0.0.1";
	private final int PORT = 7891;
	
	private int[][] terrainResponse;
	private int[][] gamePlayResponse;

	

	@Test
	public void testServer() {
		
		terrainResponse = new int[2][4];
		gamePlayResponse = new int[2][13];
		
		
		ServerThread testServer = new ServerThread();
		ClientThread testClientA = new ClientThread(0);
		ClientThread testClientB = new ClientThread(1);
		
		
		testServer.start();
		testClientA.start();
		testClientB.start();
		
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("sleep failed");
			System.out.println(e);
		}
		
		assertEquals(true, serverUp);
		
		setTerrainTest();
		
		gamePlayTest();
	}

	
	
	private void gamePlayTest(){
		
		if (gamePlayResponse[0][0] == Data.PROT_SETUP_A){
			assertEquals(Data.PROT_WAIT_FOR_A, gamePlayResponse[1][0]);
			assertEquals(101, gamePlayResponse[1][1]);
			assertEquals(201, gamePlayResponse[1][2]);
			assertEquals(Data.PROT_SETUP_B, gamePlayResponse[1][3]);
			assertEquals(Data.PROT_START_SECOND, gamePlayResponse[1][4]);
			assertEquals(103, gamePlayResponse[1][5]);
			assertEquals(203, gamePlayResponse[1][6]);
			assertEquals(Data.PROT_TOGGLE_TURN, gamePlayResponse[1][7]);
			assertEquals(104, gamePlayResponse[1][8]);
			assertEquals(204, gamePlayResponse[1][9]);
			assertEquals(Data.PROT_TOGGLE_TURN, gamePlayResponse[1][10]);
		}else{
			assertEquals(Data.PROT_WAIT_FOR_A, gamePlayResponse[0][0]);
			assertEquals(101, gamePlayResponse[0][1]);
			assertEquals(201, gamePlayResponse[0][2]);
			assertEquals(Data.PROT_SETUP_B, gamePlayResponse[0][3]);
			assertEquals(Data.PROT_START_SECOND, gamePlayResponse[0][4]);
			assertEquals(103, gamePlayResponse[0][5]);
			assertEquals(203, gamePlayResponse[0][6]);
			assertEquals(Data.PROT_TOGGLE_TURN, gamePlayResponse[0][7]);
			assertEquals(104, gamePlayResponse[0][8]);
			assertEquals(204, gamePlayResponse[0][9]);
			assertEquals(Data.PROT_TOGGLE_TURN, gamePlayResponse[0][10]);
		}
		
	}
	
	
	
	private void setTerrainTest() {
		if (terrainResponse[0][0] == Data.PROT_ACTIVE){
			assertEquals(Data.PROT_ACTIVE, terrainResponse[0][0]);
			assertEquals(Data.PROT_PASSIVE, terrainResponse[1][0]);
			assertEquals(100, terrainResponse[1][2]);
			assertEquals(200, terrainResponse[1][3]);
		}else{
			assertEquals(Data.PROT_ACTIVE, terrainResponse[1][0]);
			assertEquals(Data.PROT_PASSIVE, terrainResponse[0][0]);
			assertEquals(100, terrainResponse[0][2]);
			assertEquals(200, terrainResponse[0][3]);
		}
		
		assertEquals(Data.PROT_SET_TERRAIN, terrainResponse[0][1]);
		assertEquals(Data.PROT_SET_TERRAIN, terrainResponse[1][1]);
	}
	
	
	
	public class ClientThread extends Thread{
		
		Socket client;
		BufferedReader in;
		PrintWriter out;
		int id;
		int readCount = 0;
		boolean active;
		boolean bothTested;
		
		public ClientThread(int id){
			this.id = id;
		}
		
		public void run(){
			
			try {
				client = new Socket(HOST, PORT);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);
			} catch (Exception e) {
				System.out.println("client "+id+" failed to start");
				System.out.println(e);
				return;
			}
			
			int action = -1;
			
			
			while (true){
				
				action = readInt();
				
				if (action == -1){
					return;
				}else if (action == -2){
					return;
				}
				
				if (readCount < 4){
					terrainResponse[id][readCount] = action;
				}else if (readCount < 17){
					gamePlayResponse[id][readCount-4] = action;
				}else{
					return;
				}
				readCount++;
				respond(action);
			}
			
		}
		
		
		private void respond(int action){
			
			if (action == Data.PROT_ACTIVE){
				active = true;
			}else if (action == Data.PROT_SET_TERRAIN){
				if (!active){
					return;
				}
				out.println(100);
				out.println(200);
				out.println(Data.PROT_DONE_SET_TERRAIN);
				readCount+=2;
			}else if (action == Data.PROT_PASSIVE){
				active = false;
			}else if (action == Data.PROT_SETUP_A){
				out.println(101);
				out.println(201);
				out.println(Data.PROT_DONE_SET_PIECE);
			}else if (action == Data.PROT_SETUP_B){
				out.println(102);
				out.println(202);
				out.println(Data.PROT_DONE_SET_PIECE);
			}else if (action == Data.PROT_START_FIRST){
				out.println(103);
				out.println(203);
				out.println(Data.PROT_TOGGLE_TURN);
			}else if (action == Data.PROT_START_SECOND){
				return;// do nothing
			}else if (action == Data.PROT_TOGGLE_TURN){
				out.println(104);
				out.println(204);
				out.println(Data.PROT_TOGGLE_TURN);
			}
			
		}
		
		
		public int readInt(){
			String line = null;
			
			try{
				line = in.readLine();
			} catch (Exception e) {
				System.out.println("client failed to read");
				System.out.println(e);
				return -1;
			}
			
			if (line == null){
				return -2;
			}
			
			return Integer.parseInt(line);
		}
		
	}
	
	
	
	public class ServerThread extends Thread{
		public void run(){
			try {
				new Server(PORT, 100);
			} catch (Exception e) {
				serverUp = false;
				System.out.println("server failed to start");
				System.out.println(e);
				return;
			}
		}
		
	}

}
