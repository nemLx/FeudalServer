package server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Nemo Li
 * 
 */
public class Server {

	public static void main(String[] args){
		int port = Data.PORT;
		int capacity = Data.SERVER_CAPACITY;
		
		if (args.length > 1){
			port = Integer.parseInt(args[0]);
			capacity = Integer.parseInt(args[1]);
		}
		
		try {
			new Server(port, capacity);
		} catch (Exception e) {
			System.out.println("server failed to start");
			System.out.println(e);
		}
	}
	
	
	
	/**
	 * @param p - port
	 * @throws Exception
	 */
	public Server(int p, int capacity) throws Exception{
		int clientIndex = 0;
		int port;
		Socket[] clientSockets = new Socket[2];
		
		if (p == -1){
			port = Data.PORT;
		}else{
			port = p;
		}
		
		ServerSocket listener = new ServerSocket(port);
		Data.loadProtocal();

		while (clientIndex < capacity) {
			clientSockets[clientIndex%2] = listener.accept();
			clientIndex ++;
			
			if (clientIndex != 0 && (clientIndex%2 == 0)){
				WorkerThread manager = new WorkerThread(clientSockets[0], clientSockets[1]);
				manager.start();	// once have two players ready, start worker thread to handle game
			}
		}
	}

}
