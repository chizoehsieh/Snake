package Snake_test1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;



public class Snake_Server extends JFrame{
	private JTextArea txt = new JTextArea("Snake server run.\nNumber of player: 0 \n");         // text show on server
	private JButton close_server = new JButton("close_server");           // button to close server
	private ServerSocket serverSocket = null;              // save server's socket port
	private ExecutorService exec = null;                   // to generate a newCachedThreadPool
	private static Map<Socket, Integer> list = new LinkedHashMap<Socket, Integer>();   // to save socket and port
	private static Map<Socket, String> name = new LinkedHashMap<Socket,String>();      // to save socket and player's name
	private static Map<Socket, List<Integer>> body_x = new LinkedHashMap<Socket,List<Integer>>();   // to save socket and the value of x-axis on coordinate of snake's body
	private static Map<Socket, List<Integer>> body_y = new LinkedHashMap<Socket,List<Integer>>();   // to save socket and the value of y-axis on coordinate of snake's body
	private static Map<Socket, Integer> score = new LinkedHashMap<Socket, Integer>();  // to save socket and player's score
	private static Map<Socket, Integer> state = new LinkedHashMap<Socket, Integer>();  // to save socket and player's state (1 is playing,2 is game over,3 is in pause)
	int appleX , appleY;  // save the coordinate of apple
	static final int SCREEN_WIDTH = 800;  // save the width of window
	static final int SCREEN_HEIGHT = 800; // save the height of window
	static final int UNIT_SIZE = 25 ;     // save the size of unit
	static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT)/UNIT_SIZE;  // save the amount of units in game
	static final int DELAY = 70; //Speed of snake
	Random random;  // to generate random int
	int userNum = 0;  // save the number of online player
	
	
	public Snake_Server() throws IOException{
		setLayout(new BorderLayout());          // set layout type
		this.add(new JScrollPane(txt),BorderLayout.CENTER);    // add a new scrollable pane in center of layout
		this.add(close_server,BorderLayout.SOUTH);             // add a button to close server on the bottom of layout
		close_server.addActionListener(new ActionListener() {  // add action listener of button

			@Override
			public void actionPerformed(ActionEvent e) {
				Iterator it = list.entrySet().iterator();      // to loop through the map "list"
				DataOutputStream toClient_close = null;        // generate a new dataOutputStream
				while(it.hasNext()) {                          // when there are data in "it"
					Entry entry = (Entry) it.next();           // save next data
					Socket client = (Socket) entry.getKey();   // save the socket of the data
					try {
						toClient_close = new DataOutputStream(client.getOutputStream());  // set up the getOutputStream to the client
						toClient_close.writeUTF("Server close.");         // send the data to the client
						client.close();                        // close the socket
						userNum--;                             // minus the amount of online player
						txt.append("Number of player: " + userNum + "\n");   // show on the server
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();                   // show the exception
					}
					
					
				}
				score.clear();                        // clear the data of the score of player
				state.clear();                        // clear the data of the state of player
				body_x.clear();                       // clear the data of the coordinate of snake
				body_y.clear();                       // clear the data of the coordinate of snake
				list.clear();                         // clear the data of the port of player
				txt.append("Server is closed. \n");   // show on the server
			}
			
		});
		setSize(500,300);             // set the window size of server
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // specify one of several options for the close button
		setVisible(true);             // show to user
		random = new Random();        // generate a new random seed
		newApple();                   // generate new apple
		
		serverSocket = new ServerSocket(54321);    // establish the server on port 54321
		exec = Executors.newCachedThreadPool();    // use newCachedThreadPool to run
		
		while(true) {
			Socket socket = null;     // generate a new socket
			socket = serverSocket.accept();        // save the new connection user 
			DataInputStream fromClient = new DataInputStream(socket.getInputStream());     // generate a new dataInputStream
			list.put(socket,socket.getPort());     // save the socket and port number to the map "list"
			String n = fromClient.readUTF();       // save the string frome client which is the name of player
			name.put(socket, n);                   // save the name of player to the map "name"
			userNum++;                             // plus the amount of online player
			txt.append("Number of player: " + userNum + "\n");     // show on the server
			exec.execute(new Snake_Communication(socket));         // use newCachedThreadPool to run new communication with client
		}
	}
	
	public void newApple() {
		appleX = (random.nextInt((int)(SCREEN_WIDTH/UNIT_SIZE) - 4) + 1)*UNIT_SIZE;      // random from 1 to 22 to make apple visible
		appleY = (random.nextInt((int)(SCREEN_HEIGHT/UNIT_SIZE) - 4) + 1)*UNIT_SIZE;     // random from 1 to 22 to make apple visible 
	}
	
	public class Snake_Communication implements Runnable, ActionListener{
		private Socket socket;                 // generate a new Socket variable
		private DataInputStream fromClient = null;   // generate a new dataInputStream
		private DataOutputStream toClient = null;    // generate a new dataOutputStream
		private String msg;                   // generate a new String variable to save data from client
		char direction = 'R';                 // generate a new char variable to save the direction of snake
		Timer timer;                          // generate a new time
		
		public Snake_Communication(Socket socket) throws IOException{
			this.socket = socket;             // save socket with player to the variable
			fromClient = new DataInputStream(socket.getInputStream());  // establish dataInputStream with player
			List<Integer> init = new ArrayList<Integer>();    // generate a new list to save initial coordinate of snake
			for(int i=0;i<4;i++) {            // initial length of the snake is 4
				init.add(0);                  // initial value is 0
			}			
			body_x.put(socket,init);          // save socket and x-axis coordinate
			init = new ArrayList<Integer>();  // reset the list "init" to avoid point the same memory with x
			for(int i=0;i<4;i++) {            // initial length of the snake is 4
				init.add(25);                 // initial value is 25
			}
			body_y.put(socket,init);          // save socket and y-axis coordinate
			score.put(socket,0);              // save socket and score which is 0 at the beginning
			state.put(socket,1);              // save socket and state which is 1 (playing) at the beginning
			timer = new Timer(DELAY,this);    // generate a new time to schedule task
			timer.start();                    // start the timer
			direction = 'R';                  // set the direction to right
		}

		@Override
		public void run() {
			try {
				while((msg = fromClient.readUTF()) != null) {    // continually to receive data from client
					if(state.get(socket) == 1) {                // if player playing the game
						if(msg.equals("U")) {                   // if the data received is U
							if(direction != 'D') {              // if the direction of snake isn't D now
								direction = 'U';                // make the direction to U
							}
						}
						if(msg.equals("D")) {                   // if the data received is D
							if(direction != 'U') {              // if the direction of snake isn't U now
								direction = 'D';                // make the direction to D
							}
						}
						if(msg.equals("R")) {                   // if the data received is R
							if(direction != 'L') {              // if the direction of snake isn't L now
								direction = 'R';                // make the direction to R
							}
						}
						if(msg.equals("L")) {                   // if the data received is L
							if(direction != 'R') {              // if the direction of snake isn't R now
								direction = 'L';                // make the direction to L
							}
						}
						if(msg.equals("pause")) {               // if the data received is pause
							toClient.writeUTF("pause");         // tell to client that game pause
							timer.stop();                       // stop the timer
							state.replace(socket, 3);           // refresh the state to 3(pause)
							continue;                           // wait to receive new data from client
						}
						
					}
					if(state.get(socket) == 3) {                // if the game is pause now
						if(msg.equals("pause")) {               // if the data received is pause
								state.replace(socket, 1);       // refresh the state to 1(playing)
								timer.start();                  // start timer which control game
								toClient.writeUTF("pause");     // tell to client that game restart
						}
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();                            // show the exception
			}
			
			
		}
		
		public void broadcast() {                                // the function tell to client
			try {
				toClient = new DataOutputStream(socket.getOutputStream());   // establish dataOutputStream with player
				if(state.get(socket) == 1) {                     // if player is playing
					toClient.writeUTF("player");                 // tell to the client the data below is the player
					toClient.writeInt(list.get(socket));         // tell to the client the port number to identify different player
					toClient.writeInt(score.get(socket));        // tell to the client the score of the player now
					toClient.writeInt(body_x.get(socket).size());  // tell to the client the length of snake now
					for(int i=0;i<body_x.get(socket).size();i++) {  // to read every point of snake
						toClient.writeInt(body_x.get(socket).get(i));   // tell to the client the x-axis coordinate of every point of snake body
					}
					for(int i=0;i<body_y.get(socket).size();i++) {      // to read every point of snake
						toClient.writeInt(body_y.get(socket).get(i));   // tell to the client the y-axis coordinate of every point of snake body
					}
					
					toClient.writeUTF("other");                  // tell to the client the data below is about other player online now
					toClient.writeInt(list.size() - 1);          // tell to the client the number of other player
					Iterator it = list.entrySet().iterator();    // to loop through the map "list"
					while(it.hasNext()) {                        // when there are data in "it"
						Entry entry = (Entry) it.next();         // save next data
						Socket client = (Socket) entry.getKey();  // save the socket of the data
						if(client != socket) {                    // avoid to send the player data again
							if(state.get(client) == 1) {          // if this player playing now
								toClient.writeInt(body_x.get(client).size());   // tell to the client the length of snake now
								toClient.writeInt(list.get(client));            // tell to the client the port number
								toClient.writeUTF(name.get(client));            // tell to the client the name of this player
								for(int i=0;i<body_x.get(client).size();i++) {  // to read every point of snake
									toClient.writeInt(body_x.get(client).get(i));  // tell to the client the x-axis coordinate of every point of snake body
								}
								for(int i=0;i<body_y.get(client).size();i++) {  // to read every point of snake
									toClient.writeInt(body_y.get(client).get(i));  // tell to the client the y-axis coordinate of every point of snake body
								}
								toClient.writeInt(score.get(client));           // tell to the client the score of this player now
							}
							else {
								toClient.writeInt(0);                           // tell to the client that this player isn't playing now
								toClient.writeInt(list.get(socket));            // tell to the client the port number
								toClient.writeUTF(name.get(client));            // tell to the client the name of this player
								toClient.writeInt(score.get(client));           // tell to the client the score of this player now
							}
						}
					}
					toClient.writeInt(appleX);                                  // tell to the client the x-axis coordinate of apple
					toClient.writeInt(appleY);                                  // tell to the client the y-axis coordinate of apple
				}
				else if(state.get(socket) == 2){                                // if player game over
					toClient.writeUTF("game over");                             // tell to client the game is over
					score.remove(socket);                                       // remove score data from map
					state.remove(socket);                                       // remove state data from map
					body_x.remove(socket);                                      // remove x-axis data from map
					body_y.remove(socket);                                      // remove y-axis data from map
					list.remove(socket);                                        // remove port number data from map
					userNum--;                                                  // minus the amount of online player
					txt.append("Number of player: " + userNum + "\n");          // show on the server
					socket.close();                                             // close the connection with client
				}
			}
			catch (IOException e) {
				e.printStackTrace();                                            // show the exception
			}
		}
		
		public void move() {                               // the function move the snake
			for(int i = body_x.get(socket).size() - 1; i > 0; i--) {            // move the snake body except for head(list[0])
				body_x.get(socket).set(i,body_x.get(socket).get(i-1));          // Move bodyparts of snake one place up e.g. x[3] goes to x[2], x[2] goes to x[1] place, x[1] goes to x[0] place (which is the head of the snake) and x[0] changes with the switch direction
				body_y.get(socket).set(i,body_y.get(socket).get(i-1));          // Same for y[]
			}
			switch(direction) { // Move head of snake
				case 'U':                                                       // if direction is U
					body_y.get(socket).set(0, body_y.get(socket).get(0) - UNIT_SIZE); // Move bodypart y[0] (head) of snake up
					break;
				case 'D':                                                       // if direction is D
					body_y.get(socket).set(0, body_y.get(socket).get(0) + UNIT_SIZE); // Move bodypart y[0] (head) of snake down
					break;
				case 'L':                                                       // if direction is L
					body_x.get(socket).set(0, body_x.get(socket).get(0) - UNIT_SIZE); // Move bodypart x[0] (head) of snake left
					break;
				case 'R':                                                       // if direction is R
					body_x.get(socket).set(0, body_x.get(socket).get(0) + UNIT_SIZE); // Move bodypart x[0] (head) of snake right
					break;
			}
		}
		
		public void checkApple() {                                              // the function check if snake eats apple
			if((body_x.get(socket).get(0) == appleX) && (body_y.get(socket).get(0) == appleY)) { // If head eats apple
				body_x.get(socket).add(0);                                      // add a body on x-axis data
				body_y.get(socket).add(0);                                      // add a body on y-axis data
				score.put(socket,score.get(socket) + 1);                        // put will remove same key data, so use this to add score
				newApple(); // Create new apple
			}
		}
		
		public void checkCollisions() {                                         // the function check if snake collides with something
			// check if head collides with other snake or the body or itself
			Iterator it = list.entrySet().iterator();                           // to loop through the map "list"
			while(it.hasNext()) {                                               // when there are data in "it"
				Entry entry = (Entry) it.next();                                // save next data
				Socket client = (Socket) entry.getKey();                        // save the socket of the data
				if(state.get(client) == 1 && client != socket) {                // if this player is playing, and this player isn't the player
					for(int i = body_x.get(client).size() - 1;i >= 0; i--) {    // check with of every point of snake of this player
						if((body_x.get(socket).get(0).equals(body_x.get(client).get(i))) && (body_y.get(socket).get(0).equals( body_y.get(client).get(i)))) {   // if x-axis and y-axis coordinate of two point is same
							state.replace(socket, 2);                           // the player is game over
						}
					}
				}
				else if(state.get(client) == 1) {                               // if this player is the player
					for(int i = body_x.get(client).size() - 1; i > 0; i--) {    // check the body
						if((body_x.get(socket).get(0).equals(body_x.get(client).get(i))) && (body_y.get(socket).get(0).equals(body_y.get(client).get(i)))) {   // if x-axis and y-axis coordinate of two point is same
							state.replace(socket, 2);                           // the player is game over
						}
					}
				}
			}
			
			if(body_x.get(socket).get(0) < 0) {          // if snake head exceed left boundary
				state.replace(socket, 2);                // the player is game over
			}
			
			if(body_x.get(socket).get(0) > SCREEN_WIDTH) {  // if snake head exceed right boundary
				state.replace(socket, 2);                // the player is game over
			}
			
			if(body_y.get(socket).get(0) < 25) {         // if snake head exceed up boundary
				state.replace(socket, 2);                // the player is game over
			}
			
			if(body_y.get(socket).get(0) > SCREEN_HEIGHT) {  // if snake head exceed down boundary
				state.replace(socket, 2);                // the player is game over
			}
			
			if(state.get(socket) != 1) {                 // if the player is game over
				timer.stop();                            // stop schedule risk
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(list.get(socket) == null) {          // if socket disconnect
				timer.stop();                       // stop schedule risk
			}
			move();                                 // call the function to move the snake
			checkApple();                           // call the function to check if snake eats apple
			checkCollisions();                      // call the function to check if snake collides something
			broadcast();                            // send data to client
		}
		
	}
	
	public static void main(String[] args)  throws Exception{
		Snake_Server server = new Snake_Server();    // run the whole program
	}
}
