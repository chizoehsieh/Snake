package Snake_test1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;





public class Snake_Client extends JFrame  implements ActionListener{
	static final int SCREEN_WIDTH = 800;                // save the width of window
	static final int SCREEN_HEIGHT = 800;               // save the width of window
	static final int UNIT_SIZE = 25 ;                   // Size of unit => 25 pixels
	static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT)/UNIT_SIZE; // How many units can be at the same time in the game, to fill the whole screen
	static final int DELAY = 90;                        // Speed of snake
	
	List<Integer> x = new ArrayList<Integer>();       // x-axis coordinates of body of snake in single game
	List<Integer> y = new ArrayList<Integer>();       // y-axis coordinates of body of snake in single game
	
	int bodyParts = 3;                   // Start with 3 bodyparts in single game
	int applesEaten;                     // Score in single game
	int appleX , appleY;                 // save apple coordinates in all game
	char direction = 'R';                // Start by moving right in all geame
	
	boolean running = false;             // If game is running it's true
	boolean started = false;             // False so the game doesn't start by itself. True only after the user clicks the screen
	boolean pause = false;               // if game is pause it's true
	Timer timer;                         // generate a new timer
	Random random;                       // to generate random int
	static boolean gameOn = false;       // [Space] to change to false or true. Pausing and resuming the game. 
	int pageNum = 0;                     // to decide hoe to draw the screen
	
	private ExecutorService exec = null;  // to generate a newCachedThreadPool
	private Socket clientSocket = null;   // generate a new socket to connect to server
	private DataInputStream fromServer;   // a DataInputStream variable to save data from server
	private DataOutputStream toServer;    // a DataOutputStream variable to save data deliver to server
	int port = 0;
	private String serverIp = "localhost";
	private int serverPort = 54321;
	private static Map<Integer, Integer> score = new LinkedHashMap<Integer, Integer>();               // save score of all online player in online game
	private static Map<Integer, List<Integer>> body_x = new LinkedHashMap<Integer, List<Integer>>();  // save x-axis coordinate of snake of all online player in online game
	private static Map<Integer, List<Integer>> body_y = new LinkedHashMap<Integer, List<Integer>>();  // save y-axis coordinate of snake of all online player in online game
	private static Map<Integer, String> name = new LinkedHashMap<Integer, String>();                  // save name of all online player in online game
	
	private JFrame frame = new JFrame("Snake");                    // JFrame of start page
	private JFrame endpage = new JFrame("Snake");                  // JFrame of end page of online game
	private JFrame endpage2 = new JFrame("Snake");                 // JFrame of end page of single game
	
	public Snake_Client() {
		random = new Random();           // generate a new random seed
		startPage();                     // call to draw start page
		
	}
	
	public void startPage() {            // to draw the start page
		frame = new JFrame("Snake");     // set frame title is "Snake"
		frame.setLayout(null);           // set don't use the layout manager, components are positioned by coordinate
		frame.setSize(800,800);          // set window size
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // specify one of several options for the close button 
		frame.getContentPane().setBackground(Color.black);      // set back ground color
		frame.setFocusable(true);        // make focus on the window
		frame.setLocationRelativeTo(null);   // make window show on the center of screen
		JLabel title = new JLabel("S N A K E");     // set game title
		title.setFont(new Font("SAN_SERIF", Font.BOLD, 45));  // set font of game title
		title.setBounds(280,100,240,100);           // set coordinate of game title
		title.setForeground(Color.lightGray);       // set color of game title
		frame.add(title);                // put game title on the frame
		JLabel ruleTitle = new JLabel("Game Rule"); // set game rule title
		ruleTitle.setFont(new Font("SAN_SERIF", Font.BOLD, 28));  // set font of game rule title
		FontMetrics metrics1 = getFontMetrics(ruleTitle.getFont());  // get font data of game rule title
		ruleTitle.setBounds(((SCREEN_WIDTH - metrics1.stringWidth("Game Rule"))/2),200, metrics1.stringWidth("Game Rule"),100);   // set coordinate of game rule
		ruleTitle.setForeground(Color.lightGray);    // set color of game rule title
		frame.add(ruleTitle);            // put game rule title on the frame
		JLabel rule1 = new JLabel("Use the arrow keys to control the movement of the snake.");  // set the string of first rule
		rule1.setFont(new Font("SAN_SERIF", Font.BOLD, 16));   // set the font of rule1
		rule1.setBounds(170, 300, 460, 50);                    // set the coordinate of rule1
		rule1.setForeground(Color.lightGray);                  // set the color of rule1
		frame.add(rule1);                // put rule1 on the frame
		JLabel rule2 = new JLabel("If you eat an apple, you will gain points and increase snake's body length.");  // set the string of second rule
		rule2.setFont(new Font("SAN_SERIF", Font.BOLD, 16));   // set the font of rule2
		rule2.setBounds(100, 350, 600, 50);                    // set the coordinate of rule2
		rule2.setForeground(Color.lightGray);                  // set the color of rule2
		frame.add(rule2);                // put rule2 on the frame
		JLabel rule3 = new JLabel("If the snake head exceeds the screen or touches an object other than the apple, the game is over.");   // set the string of third rule
		rule3.setFont(new Font("SAN_SERIF", Font.BOLD, 14));   // set the font of rule3
		rule3.setBounds(50, 400, 700, 50);                     // set the coordinate of rule3
		rule3.setForeground(Color.lightGray);                  // set the color of rule3
		frame.add(rule3);                // put rule3 on the frame
		JLabel rule4 = new JLabel("Single games can only be played by yourself.");   // set the string of fourth rule
		rule4.setFont(new Font("SAN_SERIF", Font.BOLD, 18));   // set the font of rule4
		rule4.setBounds(200, 450, 400, 50);                    // set the coordinate of rule4
		rule4.setForeground(Color.lightGray);                  // set the color of rule4
		frame.add(rule4);                // put rule4 on the frame
		JLabel rule5 = new JLabel("Online games are connected to the server, and multiple players can play.");    // set the string of fifth rule
		rule5.setFont(new Font("SAN_SERIF", Font.BOLD, 18));   // set the font of rule5
		rule5.setBounds(80, 500, 640, 50);                    // set the coordinate of rule5
		rule5.setForeground(Color.lightGray);                  // set the color of rule5
		frame.add(rule5);                // put rule5 on the frame
		JButton single = new JButton("Single");     // set a new button about starting single game
		single.setBounds(100,600,100,50);           // set the coordinate of single button
		single.setFont(new Font("SAN_SERIF", Font.BOLD, 20));   // set the font of single button
		frame.add(single);               // put single button on the frame
		single.addActionListener(new ActionListener() {          // set the listener of single button

			@Override
			public void actionPerformed(ActionEvent e) {
				started = true;                                  // make state is playing
				startGame();                                     // call function to draw the screen and start the game
				frame.setVisible(false);                         // set the visible of start page is false, make player only focus on the game
			}
			
		});
		JTextField nameEdt = new JTextField("Please enter your name.");   // set the text field with hint text
		nameEdt.addFocusListener(new FocusListener() {           // add focus listener of nameEdt to make hint text disappear

			@Override
			public void focusGained(FocusEvent e) {            // if click the text field
				nameEdt.setText("");                           // make the text disappear
			}

			@Override
			public void focusLost(FocusEvent e) {              // when focus lost 
				nameEdt.setText(nameEdt.getText());            // no action
			}
			
		});
		nameEdt.setBounds(250, 600, 300, 50);                  // set the coordinate of nameEdt
		frame.add(nameEdt);             // put nameEdt on the frame
		JButton online = new JButton("Online");                // set a new button about starting online game
		online.setBounds(600, 600, 100, 50);                   // set the coordinate of online button
		online.setFont(new Font("SAN_SERIF", Font.BOLD, 20));  // set the font of online button
		frame.add(online);              // put online button on the frame
		online.addActionListener(new ActionListener() {        // set the listener of online button

			@Override
			public void actionPerformed(ActionEvent e) {
				if(!nameEdt.getText().equals("") && !nameEdt.getText().equals("Please enter your name.")) {   // if player doesn't keyin his or her name, online game doesn't start
					exec = Executors.newCachedThreadPool();    // use newCachedThreadPool to run
					exec.execute(new ClientSocket(nameEdt.getText()));   // use newCachedThreadPool to run the connection with server
					nameEdt.setText("");              // make nameEdt with nothing
					pageNum = 11;                     // set page number to draw on the screen
					frame.setVisible(false);          // set the visible of start page is false, make player only focus on the game
					gamePage();                       // call to open game frame
					repaint();                        // repaint the frame
				}
			}
			
		});
		
		frame.setVisible(true);                       // set start page frame is visible
		frame.setResizable(false);                    // set the size of start page is stable
		
	}
	
	public void gamePage() {                          // the function open the frame of game page
		setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));    // set the window size
		getContentPane().setBackground(Color.black);  // set the background color of frame
		setFocusable(true);                           // make focus on the window
		addKeyListener(new MyKeyAdapter());           // add key listener to liste to the user's operation
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // specify one of several options for the close button 
		pack();                // sizes the frame so that all its contents are at or above their preferred sizes
		setVisible(true);      // make window show to the player
		setLocationRelativeTo(null);   // make window show on the center of screen
		setResizable(false);   // set the size of game page is stable
	}
	
	public void endPage1(String titleStr) {         // the function to draw the end page of online game
		endpage = new JFrame("Snake");              // set a new frame which title is "Snake"
		endpage.setSize(800,800);                   // set window size
		endpage.getContentPane().setBackground(Color.black);  // set background color of frame
		endpage.setFocusable(true);                 // make focus on the window
		endpage.setLayout(null);                    // doesn't use the layout manager, components are positioned by coordinate
		endpage.setDefaultCloseOperation(EXIT_ON_CLOSE);      // set the window size
		endpage.setVisible(true);                   // set start page frame is visible
		endpage.setResizable(false);                // set the size of start page is stable
		endpage.setLocationRelativeTo(null);        // make window show on the center of screen
		FontMetrics metrics1 = getFontMetrics(getFont());     // get font data of the text font now
		if(score.size() > 1) {                      // if there are other player online now
			JLabel other = new JLabel("Other Player ");   // set the title of other player
			other.setFont(new Font("SAN_SERIF", Font.BOLD, 38));     // set the font of other player title
			metrics1 = getFontMetrics(other.getFont());   // get font data of other player title
			other.setBounds((SCREEN_WIDTH - metrics1.stringWidth("Other Player: "))/2, 450, 400, metrics1.getHeight());   // set the coordinate of other player title
			other.setForeground(Color.red);         // set the color of other player title
			endpage.add(other);                     // put other player title on the frame
			JTextArea allScore = new JTextArea("");  // set a new text area to contain the score of all other player
			JScrollPane scorePane = new JScrollPane(allScore);  // set a new scrollable pane to show all score in a stable size
			scorePane.setBounds(270, 500, 250, 700-(500 + metrics1.getHeight()));   // set the coordinate of scorePane
			scorePane.setBackground(Color.black);    // set background color of scorePane
			scorePane.setBorder(null);               // set no border on the scorePane
			allScore.setFont(new Font("SAN_SERIF", Font.BOLD, 32));   // set the font of all score
			allScore.setForeground(Color.red);                        // set the color of all score
			allScore.setBackground(Color.black);                      // set the background color of all score
			JLabel title = new JLabel(titleStr);                      // set the string show the reason of end, which deliver from server 
			title.setFont(new Font("SAN_SERIF", Font.BOLD, 75));      // set the font of title string
			metrics1 = getFontMetrics(title.getFont());               // get the font data of title string
			title.setBounds((SCREEN_WIDTH - metrics1.stringWidth(titleStr))/2, SCREEN_HEIGHT/4, metrics1.stringWidth(titleStr) + 50, metrics1.getHeight());  // set the coordinate of title string
			title.setForeground(Color.red);                           // set the color of title string
			JLabel yourScore = new JLabel();                          // set a label to save the score of player
			Iterator it = score.entrySet().iterator();                // to loop through the map "score"
			while(it.hasNext()) {                                     // when there are data in "it"
				Entry entry = (Entry) it.next();                      // save next data
				int portNum = (int) entry.getKey();                   // save the port number of the data
				if(portNum == port) {                                 // if this player is the player
					yourScore.setText("Your score is: " + score.get(portNum));     // set the string of score label 
					yourScore.setFont(new Font("SAN_SERIF", Font.BOLD, 45));       // set the font of score label
					yourScore.setForeground(Color.red);                            // set color of score label
					metrics1 = getFontMetrics(yourScore.getFont());                // get font data of score label
					yourScore.setBounds((SCREEN_WIDTH - metrics1.stringWidth("Your score is: " + applesEaten))/2, (SCREEN_HEIGHT/4) + 100, metrics1.stringWidth("Your score is: " + score.get(portNum)), metrics1.getHeight());  // set the coordinate of score label
				}
				else {
					allScore.append(name.get(portNum) +"'s score: " + score.get(portNum) + "\n");   // append player's name and score to allScore text area
				}
			}
			endpage.add(scorePane);         // put scorePane on the frame
			endpage.add(title);             // put title on the frame
			endpage.add(yourScore);         // put score on the frame
			
			
			
		}
		else {
			JLabel title = new JLabel(titleStr);       // set the string show the reason of end, which deliver from server 
			title.setFont(new Font("SAN_SERIF", Font.BOLD, 75));    // set the font of title
			metrics1 = getFontMetrics(title.getFont());             // get the font data of title
			title.setBounds((SCREEN_WIDTH - metrics1.stringWidth(titleStr))/2, SCREEN_HEIGHT/4, metrics1.stringWidth(titleStr) + 50, metrics1.getHeight());   // set coordinate of title
			title.setForeground(Color.red);            // set color of title
			JLabel yourScore = new JLabel("Your score is: " + score.get(port));   // set a label to show the score of player
			yourScore.setFont(new Font("SAN_SERIF", Font.BOLD, 45));   // set the font of score
			yourScore.setForeground(Color.red);        // set color of score
			metrics1 = getFontMetrics(yourScore.getFont());            // get font data of score
			yourScore.setBounds((SCREEN_WIDTH - metrics1.stringWidth("Your score is: " + applesEaten))/2, (SCREEN_HEIGHT/2) + 50, metrics1.stringWidth("Your score is: " + score.get(port)), metrics1.getHeight());   // set coordinate of score
			endpage.add(title);                       // put title on the frame
			endpage.add(yourScore);                   // put score on the frame
			
		}
		JButton backHome = new JButton("Home");       // add a button about back to start page
		backHome.setBounds(150,550,100,50);           // set coordinate of home button
		backHome.setFont(new Font("SAN_SERIF", Font.BOLD, 20));   // set the font of text on home button
		endpage.add(backHome);                        // put home button on the frame
		backHome.addActionListener(new ActionListener() {      // set listener of home button 

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);               // start page show on the screen
				endpage.setVisible(false);            // end page hide
			}
			
		});
		JButton exit = new JButton("Exit");           // add a button about close the game
		exit.setBounds(550,550,100,50);               // set coordinate of exit button
		exit.setFont(new Font("SAN_SERIF", Font.BOLD, 20));  // set font of exit button
		endpage.add(exit);           // put exit button on the frame
		exit.addActionListener(new ActionListener() {        // set listener of exit button

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);                       // close the program
			}
			
		});
	}
	
	public void endPage2() {                    // the function draw the end page of single game
		endpage2 = new JFrame("Snake");         // set a new frame which title is "Snake"
		endpage2.setSize(800,800);              // set size of window
		endpage2.getContentPane().setBackground(Color.black);  // set the background color of frame
		endpage2.setFocusable(true);            // make focus on the window
		endpage2.setLayout(null);               // doesn't use the layout manager, components are positioned by coordinate
		endpage2.setDefaultCloseOperation(EXIT_ON_CLOSE);      // set the window size
		endpage2.setVisible(true);              // set start page frame is visible
		endpage2.setResizable(false);           // set the size of start page is stable
		endpage2.setLocationRelativeTo(null);   // make window show on the center of screen
		FontMetrics metrics1 = getFontMetrics(getFont());    // get font data of the text font now
		JLabel title = new JLabel("Game Over");              // set string show end page title
		title.setFont(new Font("SAN_SERIF", Font.BOLD, 75)); // set font of title
		metrics1 = getFontMetrics(title.getFont());          // get font data of title
		title.setBounds((SCREEN_WIDTH - metrics1.stringWidth("Game Over"))/2, SCREEN_HEIGHT/4, metrics1.stringWidth("Game Over") + 50, metrics1.getHeight());   // set coordinate of page title
		title.setForeground(Color.red);         // set color of title
		JLabel yourScore = new JLabel("Your score is: " + applesEaten);     // set string show the score
		yourScore.setFont(new Font("SAN_SERIF", Font.BOLD, 45));  // set font of score string
		metrics1 = getFontMetrics(yourScore.getFont());           // get font data of score string
		yourScore.setBounds((SCREEN_WIDTH - metrics1.stringWidth("Your score is: " + applesEaten))/2, (SCREEN_HEIGHT/2) + 50, metrics1.stringWidth("Your score is: " + applesEaten), metrics1.getHeight());   // set coordinate of score string
		yourScore.setForeground(Color.red);     // set color of score string
		endpage2.add(title);                    // put title on the frame
		endpage2.add(yourScore);                // put score string on the frame
		JButton backHome = new JButton("Home");  // add a button about back to start page
		backHome.setBounds(200,550,100,50);     // set coordinate of home button
		backHome.setFont(new Font("SAN_SERIF", Font.BOLD, 20));   // set the font of text on home button
		endpage2.add(backHome);                 // put home button on the frame
		backHome.addActionListener(new ActionListener() {         // set listener of home button 
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);         // start page show on the screen
				endpage2.setVisible(false);     // end page hide
			}
			
		});
		JButton exit = new JButton("Exit");     // add a button about close the game
		exit.setBounds(500,550,100,50);         // set coordinate of exit button
		exit.setFont(new Font("SAN_SERIF", Font.BOLD, 20));   // set font of exit button
		endpage2.add(exit);                     // put exit button on the frame
		exit.addActionListener(new ActionListener() {         // set listener of exit button
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);                 // close the program
			}
			
		});
	}
	
	
	public void draw(Graphics g) {            // function to draw game image
		g.setColor(Color.black);              // set draw color
		g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);  // set background
		if(pageNum == 11) {               // if it is online game playing
			int n = 2;                    // to count score string coordinate
			g.setColor(Color.RED);        // set draw color
			g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);  // draw the apple
			Iterator it = body_x.entrySet().iterator();        // to loop through the map "body_x"
			while(it.hasNext()) {         // when there are data in "it"
				Entry entry = (Entry) it.next();        // save next data
				int portNum = (int) entry.getKey();     // save the port number of the data
				for(int i = 0; i<body_x.get(portNum).size(); i++) {     // to read every data in body_x of the data
					if(portNum != port) {               // if it's other player
						if(i == 0) {         // if it's the head of snake
							g.setColor(Color.white);    // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of snake
						}
						else {
							g.setColor(Color.gray);     // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of snake
						}
					}
					else if(portNum == port) {          // if it's the player
						if(i == 0) {         // if it's the head of snake
							g.setColor(Color.blue);    // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of snake
						}
						else {
							g.setColor(Color.green);     // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of snake
						}
					}
				}
				if(portNum == port) {   // if it's the player
					g.setColor(Color.cyan);   // set color of score
					g.setFont(new Font("Ink Free", Font.BOLD,30));   // set font of score
					g.drawString("Score: "+score.get(portNum),50,g.getFont().getSize() + 25);    // draw the score
				}
				else {                   // if it's other player
					g.setColor(Color.gray);   // set color of score
					g.setFont(new Font("Ink Free", Font.BOLD,24));   // set font of score
					g.drawString(name.get(portNum) + "'s score: "+score.get(portNum),50,g.getFont().getSize() * n + 40 );    // draw the score
					n++;                 // plus an other player
				}
			}
		}
		if(pageNum == 12) {              // if it's online game in pause
			int n = 2;                    // to count score string coordinate
			g.setColor(Color.RED);        // set draw color
			g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);  // draw the apple
			Iterator it = body_x.entrySet().iterator();        // to loop through the map "body_x"
			while(it.hasNext()) {         // when there are data in "it"
				Entry entry = (Entry) it.next();        // save next data
				int portNum = (int) entry.getKey();     // save the port number of the data
				for(int i = 0; i<body_x.get(portNum).size(); i++) {     // to read every data in body_x of the data
					if(portNum != port) {               // if it's other player
						if(i == 0) {         // if it's the head of snake
							g.setColor(Color.white);    // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of snake
						}
						else {
							g.setColor(Color.gray);     // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of snake
						}
					}
					else if(portNum == port) {          // if it's the player
						if(i == 0) {         // if it's the head of snake
							g.setColor(Color.blue);    // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of snake
						}
						else {
							g.setColor(Color.green);     // set draw color
							g.fillRect(body_x.get(portNum).get(i), body_y.get(portNum).get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of snake
						}
					}
				}
				if(portNum == port) {   // if it's the player
					g.setColor(Color.cyan);   // set color of score
					g.setFont(new Font("Ink Free", Font.BOLD,30));   // set font of score
					g.drawString("Score: "+score.get(portNum),50,g.getFont().getSize() + 25);    // draw the score
				}
				else {                   // if it's other player
					g.setColor(Color.gray);   // set color of score
					g.setFont(new Font("Ink Free", Font.BOLD,24));   // set font of score
					g.drawString(name.get(portNum) + "'s score: "+score.get(portNum),50,g.getFont().getSize() * n + 40 );    // draw the score
					n++;                 // plus an other player
				}
			}
			g.setColor(new Color(0,0,0,60));       // set an translucent color
			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);    // set a rectangle between game image and pause string
			g.setColor(Color.red);       // set draw color
			g.setFont(new Font("SAN_SERIF", Font.BOLD, 75));  // set the font of pause string
			FontMetrics metrics = getFontMetrics(g.getFont());  // get font data of pause string
			g.drawString("Pause", (SCREEN_WIDTH - metrics.stringWidth("Pause"))/2, SCREEN_HEIGHT/2);    // draw the pause string
		}
		if(pageNum == 21) {
			g.setColor(Color.RED);    // set draw color
			g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);   // draw the apple
			
			for(int i = 0; i < x.size(); i++) {     // read all data in x
				if(i == 0) {                        // Make the head of the snake blue
					g.setColor(Color.BLUE);         // set draw color
					g.fillRect(x.get(i), y.get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of the snake
				}
				else {                              // Make the body of the snake green
					g.setColor(Color.GREEN);        // set draw color
					g.fillRect(x.get(i), y.get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of the snake
				}
			}
			//Score text
			g.setColor(Color.CYAN);   // set draw color
			g.setFont(new Font("Ink Free", Font.BOLD, 30));     // set the font of score string
			FontMetrics metrics = getFontMetrics(g.getFont());  // get the font data of score string
			g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+ applesEaten))/2, g.getFont().getSize() + 25);   // draw the score
			
		}
		
		if(pageNum == 22) {
			g.setColor(Color.RED);    // set draw color
			g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);   // draw the apple
			
			for(int i = 0; i < x.size(); i++) {     // read all data in x
				if(i == 0) {                        // Make the head of the snake blue
					g.setColor(Color.BLUE);         // set draw color
					g.fillRect(x.get(i), y.get(i), UNIT_SIZE, UNIT_SIZE);    // draw the head of the snake
				}
				else {                              // Make the body of the snake green
					g.setColor(Color.GREEN);        // set draw color
					g.fillRect(x.get(i), y.get(i), UNIT_SIZE, UNIT_SIZE);    // draw the body of the snake
				}
			}
			//Score text
			g.setColor(Color.CYAN);   // set draw color
			g.setFont(new Font("Ink Free", Font.BOLD, 30));     // set the font of score string
			FontMetrics metrics = getFontMetrics(g.getFont());  // get the font data of score string
			g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+ applesEaten))/2, g.getFont().getSize() + 25);   // draw the score
			
			g.setColor(new Color(0,0,0,60));       // set an translucent color
			g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);    // set a rectangle between game image and pause string
			g.setColor(Color.red);       // set draw color
			g.setFont(new Font("SAN_SERIF", Font.BOLD, 75));  // set the font of pause string
			metrics = getFontMetrics(g.getFont());  // get font data of pause string
			g.drawString("Pause", (SCREEN_WIDTH - metrics.stringWidth("Pause"))/2, SCREEN_HEIGHT/2);    // draw the pause string
		}
	}
	
	public void newApple() {
		appleX = random.nextInt(((int)(SCREEN_WIDTH/UNIT_SIZE) - 3) + 1)*UNIT_SIZE;        // random from 1 to 23 to make apple visible
		appleY = random.nextInt(((int)(SCREEN_WIDTH/UNIT_SIZE) - 3) + 1)*UNIT_SIZE;        // random from 1 to 23 to make apple visible
	}
	
	public void startGame() {     // set the initial data of single game
		// Start the game only after the user clicks the screen
		if(started) {
			gamePage();           // call function to draw game image
			newApple();           // Create an apple
			running = true;       // declare the game is started
			timer = new Timer(DELAY,this);   // generate a new time to schedule task
			timer.start();        // start the timer
			pageNum = 21;         // set the page number to draw game image
			x.clear();            // clear the data in x of snake
			y.clear();            // clear the data in y of snake
			applesEaten = 0;      // zero the score
			direction = 'R';      // set the direction to right
			for(int i=0;i<4;i++) {  // initial length of the snake is 4
				x.add(0);         // initial x-axis value is 0
				y.add(25);        // initial y-axis value is 25
			}
		}
		
	}
	
	public void pause() {    // function to set pause option
		pause = true;        // set pause is true
		pageNum = 22;        // set page number to draw pause
		repaint();           // To display message "Game Paused"
		timer.stop();        // Stop game
	}

	public void resume() {   // function to back game
		pause = false;       // set pause is false
		pageNum = 21;        // set page number to draw game image
		timer.start();       // Restart game from where it was paused
	}
	
	public class MyKeyAdapter extends KeyAdapter{         // listen to the keyboard
		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT:                        // if press the left arrow key
				if(pageNum == 21 && !pause) {             // if it's playing single game
					if(direction != 'R') {                // if the snake isn't facing to the right
						direction = 'L';                  // set the direction to the left
					}
				}
				if(pageNum == 11) {                       // if it's playing online game
					try {
						toServer.writeUTF("L");           // tell to server that player press the left arrow key
					} catch (IOException e1) {
						e1.printStackTrace();             // show the exception
					}
				}
				break;
				
			case KeyEvent.VK_RIGHT:                        // if press the right arrow key
				if(pageNum == 21 && !pause) {              // if it's playing single game
					if(direction != 'L') {                 // if the snake isn't facing to the left
						direction = 'R';                  // set the direction to the right
					}
				}
				if(pageNum == 11) {                       // if it's playing online game
					try {
						toServer.writeUTF("R");           // tell to server that player press the right arrow key
					} catch (IOException e1) {
						e1.printStackTrace();             // show the exception
					}
				}
				break;
				
			case KeyEvent.VK_UP:                          // if press the up arrow key
				if(pageNum == 21 && !pause) {             // if it's playing single game
					if(direction != 'D') {                // if the snake isn't facing to the down
						direction = 'U';                  // set the direction to the up
					}
				}
				if(pageNum == 11) {                       // if it's playing online game
					try {
						toServer.writeUTF("U");           // tell to server that player press the up arrow key
					} catch (IOException e1) {
						e1.printStackTrace();             // show the exception
					}
				}
				break;
				
			case KeyEvent.VK_DOWN:                        // if press the up arrow key
				if(pageNum == 21 && !pause) {             // if it's playing single game
					if(direction != 'U') {                // if the snake isn't facing to the up
						direction = 'D';                  // set the direction to the down
					}
				}
				if(pageNum == 11) {                       // if it's playing online game
					try {
						toServer.writeUTF("D");           // tell to server that player press the down arrow key
					} catch (IOException e1) {
						e1.printStackTrace();             // show the exception
					}
				}
				break;
				
			case KeyEvent.VK_SPACE:                       // if press the space key
				if(pageNum == 21) {                       // if it's playing single game
					pause();                              // call the pause function to pause game
				}
				else if(pageNum == 22) {                  // if it's in pause of single game
					resume();                             // call the resume function to back to the game
				}
				if(pageNum == 11 || pageNum == 12) {      // if it's in online game
					try {
						toServer.writeUTF("pause");       // tell to server that player press the space key
					} catch (IOException e1) {
						e1.printStackTrace();             // show the exception
					}
				}
				break;
			}
			
		}
	}
	
	class ClientSocket implements Runnable{               // function connect to server
		String playerName;              // set a string variable to save the name which is entered by player
		
		public ClientSocket(String pn) {   // the construction of the class
			playerName = pn;           // put the name into the variable
		}

		@Override
		public void run() {
			try {
				clientSocket = new Socket(serverIp, serverPort);      //  set a connection with server
				fromServer = new DataInputStream(clientSocket.getInputStream());   // set a dataInputStream to receive data from server
				toServer = new DataOutputStream(clientSocket.getOutputStream());   // set a dataOutputStream to deliver data to server
				toServer.writeUTF(playerName);              // tell the server about the name of player
				toServer.flush();                           // deliver the data
				while(true) {                               // continually to receive data from server
					String msg = fromServer.readUTF();      // set a string variable to save data from server
					if(msg.equals("game over")) {           // if the data is "game over"
						JComponent comp = (JComponent) getContentPane();      // get the game frame
						Window win = SwingUtilities.getWindowAncestor(comp);  // get the window of game
						win.setVisible(false);              // hide game page
						endPage1("Game Over");              // call end page to draw the result with game over
						break;                              // stop receive data from server
					}
					if(msg.equals("Server close.")) {       // if the data is "server close"
						JComponent comp = (JComponent) getContentPane();      // get the game frame
						Window win = SwingUtilities.getWindowAncestor(comp);  // get the window of game
						win.setVisible(false);              // hide game page
						endPage1("Server closed");          // call end page to draw the result with server close
						break;                              // stop receive data from server
					}
					if(msg.equals("pause")) {              // if the data is "pause"
						if(pageNum == 11) {                // if game is playing
							pageNum = 12;                  // set page number to draw pause page
							repaint();                     // repaint the frame
							continue;                      // wait for next data
						}
						else if(pageNum == 12) {           // if game is in pause
							pageNum = 11;                  // set page number to back game
							repaint();                     // repaint the frame
							continue;                      // wait for next data
						}					
					}
					if(msg.equals("player")) {             // if the data is "player"
						score.clear();                     // clear all score data
						body_x.clear();                    // clear all x-axis data
						body_y.clear();                    // clear all y-axis data
						port = fromServer.readInt();       // receive the port number from server to identify players
						score.put(port, fromServer.readInt());  // receive the score from server and put into map "score"
						int msg_int = fromServer.readInt();     // receive the body length
						List<Integer> body = new ArrayList<Integer>();  // generate a list to temporary storage the body coordinate
						for(int i = 0 ; i < msg_int; i++) {     // receive the body length times
							body.add(fromServer.readInt());     // add the data received from server to temporary list
						}
						body_x.put(port, body);                 // put the temporary list into map with port number
						body = new ArrayList<Integer>();        // renew the temporary list
						for(int i = 0; i < msg_int; i++) {      // receive the body length times
							body.add(fromServer.readInt());     // add the data received from server to temporary list
						}
						body_y.put(port,body);                  // put the temporary list into map with port number
					}
					msg = fromServer.readUTF();                 // receive the string data from server
					if(msg.equals("other")) {             // if the data is "other"
						name.clear();                     // clear all name data
						int length = fromServer.readInt();      // receive the amount of other player
						for(int i = 0; i < length; i++) {  // loop length times to process all player
							int size = fromServer.readInt();    // receive the body length of this player
							if(size != 0) {                // if it has length
									int port_ = fromServer.readInt();   // receive the port number of this player from server
									name.put(port_, fromServer.readUTF());   // put the name into map with port number
									List<Integer> body = new ArrayList<Integer>();  // generate a list to temporary storage the body coordinate
									for(int j=0; j<size; j++) {         // receive size times
										body.add(fromServer.readInt());      // add the data received from server to temporary list
									}
									body_x.put(port_,body);     // put the temporary list into map with port number
									body = new ArrayList<Integer>();    // renew the temporary list
									for(int j=0; j<size; j++) {         // receive size times
										body.add(fromServer.readInt());// add the data received from server to temporary list
									}
									body_y.put(port_,body);            // put the temporary list into map with port number
									score.put(port_, fromServer.readInt());  // receive the score of this player
								}
							
							else {
								int port_ = fromServer.readInt();      // receive the port number from server
								name.put(port_,fromServer.readUTF());  // receive the name from server
								score.put(port_,fromServer.readInt()); // receive the score form server
							}
						}
						appleX = fromServer.readInt();                 // receive the x-axis of apple
						appleY = fromServer.readInt();                 // receive the y-axis of apple
					}
					repaint();                // repaint the frame
				}
			}
			catch(UnknownHostException e) {
				e.printStackTrace();          // show the exception
			}
			catch(IOException e) {
				e.printStackTrace();          // show the exception
			}
			
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {   // the action done in timer
		if(running) {            // if it's playing
			move();              // call the function to move the snake
			checkApple();        // call the function to check if snake eats apple
			checkCollisions();   // call the function to check if snake collides something
		}
		repaint();               // refresh the frame image
		
	}
	
	
	public void move() {         // the function move the snake
		for(int i = x.size() - 1; i > 0; i--) {    // move the snake body except for head(list[0])
			x.set(i, x.get(i-1));                  // Move bodyparts of snake one place up e.g. x[3] goes to x[2], x[2] goes to x[1] place, x[1] goes to x[0] place (which is the head of the snake) and x[0] changes with the switch direction
			y.set(i, y.get(i-1));                  // same with x
		}
		switch(direction) {       // Move head of snake
			case 'U':             // if direction is U
				y.set(0, y.get(0) - UNIT_SIZE);   // Move bodypart y[0] (head) of snake up
				break;
			case 'D':             // if direction is D
				y.set(0, y.get(0) + UNIT_SIZE);   // Move bodypart y[0] (head) of snake down
				break;
			case 'L':             // if direction is L
				x.set(0, x.get(0) - UNIT_SIZE);   // Move bodypart x[0] (head) of snake left
				break;
			case 'R':             // if direction is R
				x.set(0, x.get(0) + UNIT_SIZE);   // Move bodypart x[0] (head) of snake right
				break;
		}
		
	}
	
	public void checkApple() {    // the function check if snake eats apple
		if((x.get(0) == appleX) && (y.get(0) == appleY)) { //If head eats apple
			x.add(0);             // add a body on x-axis data
			y.add(0);             // add a body on y-axis data
			applesEaten++;        // Add to score
			newApple();           // Create new apple
		}			
	}
	
	public void checkCollisions() {   // the function check if snake collides with something
		// check if head collides with body
		for(int i = bodyParts; i > 0; i--) {  // check every point of body of snake
			if((x.get(0) == x.get(i)) && (y.get(0) == y.get(i))) {    // if head collides body
				running = false;             // game is over
			}			
		}
		// check if head touches left border
		if(x.get(0) < 0) {                   // if snake head exceed left boundary
			running = false;                 // game is over
		}	
		// check if head touches right border
		if(x.get(0) > SCREEN_WIDTH - 25) {   // if snake head exceed right boundary
			running = false;                 // game is over
		}	
		// check if head touches top border
		if(y.get(0) < 25) {                  // if snake head exceed up boundary
			running = false;                 // game is over
		}
		// check if head touches bottom border
		if(y.get(0) > SCREEN_HEIGHT - 25) {  // if snake head exceed bottom boundary
			running = false;                 // game is over
		}
		
		if(!running) {                       // if game is over
			timer.stop();                    // stop schedule risk
			JComponent comp = (JComponent) getContentPane();      // get the game frame
			Window win = SwingUtilities.getWindowAncestor(comp);  // get the window
			win.setVisible(false);  // hide game page
			endPage2();             // call function to draw end page
		}
	}
	
	@Override
	public void paint(Graphics g) {           // the function to draw
		super.paintComponents(g);             // get the component
		draw(g);                              // draw on the graphics
	}
	
	public static void main(String[] args) {
		Snake_Client client = new Snake_Client();   // run the whole program
	}
}
