package rsachat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * This class is the server class in a chat program. A daemon thread listerns for incoming requests at server port
 * given as arguments when the server starts. Every time it receives a client connection request, enqueues it 
 * into a blocking queue, and continue listening. 
 * A thread pool of threads (5 threads as macro defined) deal with every request in blocking queue from head of 
 * the queue. In this way, socket connections of one server with multi-clients are developed.
 * On each socket connection, use two threads, one for listening from and one for speaking to client. This ensure 
 * asynchronous communication with the client.
 * Server receives messages from each client (ServerListener.java), and sends messages to clients (ServerSpeaker.java).
 * @author minpan, yangkang, fangpengliu
 *
 */
public class ChatServer {
    private int count;
	private ServerSocket serverSocket = null;
	private static Scanner sc;
	private String port;
	private int thread_num = 5;
	private int[] enkeys;
	private ArrayList<OutputStream> outline;
	private ArrayList<String> infoline;
	private ArrayList<Integer> numbers;
	private ArrayBlockingQueue<ArrayList<Integer>> keys = new ArrayBlockingQueue<ArrayList<Integer>>(2);
	volatile boolean hasEnter = false;
	
	/**
	 * Constructor for ChatServer
	 * @param port to be set to this ChatServer's port number
	 */
	public ChatServer(String port) {
		this.port = port;
		enkeys = new int[2];
		
		try{
	        System.out.println("**********Before you start, please enter your private key: **********");
	        for (int i = 0; i < enkeys.length; i++) {
	            enkeys[i] = sc.nextInt();
	        }		    
		} catch (InputMismatchException e) {
            System.out.println("Your key is not valid, server terminated");
            sc.close();
            System.exit(0);
        }
        
        
        outline = new ArrayList<OutputStream>();
        infoline = new ArrayList<String>();
        numbers = new ArrayList<Integer>();
        count = 0;
	}
	
	/**
	 * Main Method, takes the argument and starts the chatserver program
	 * @param args	to be used to set port number of the new ChatServer object
	 */
	public static void main(String args[]) {
	    sc = new Scanner(System.in);
		try {
			System.out.println("host IP address: " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		if (args.length == 1) {
			try {
				new ChatServer(args[0]).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("RSAChat program usage: binding port");
		} 
		sc.close();
	}
	/**
	 * A daemon thread listerns for incoming requests at server port given as arguments when the server starts. 
	 * Every time it receives a client connection request, enqueues it into a blocking queue, and continue listening. 
	 * A WokerThread will deal with the request in blocking queue from head of the queue. On each socket connection, 
	 * use two threads, one for listening from and one for speaking to client. This ensures asynchronous communication
	 * with the client. 
	 * @throws IOException 
	 */
	public void start() throws IOException {
		System.out.println("Server started: ServerSocket[addr=" + 
							InetAddress.getLocalHost().getHostAddress() + 
							", port=" + 0 + ", localport=" + port + "]");
		BlockingQueue<Runnable> bq = new ArrayBlockingQueue<Runnable>(1024);
		ThreadPoolExecutor tp = new ThreadPoolExecutor(thread_num, 2 * thread_num, 10, TimeUnit.SECONDS, bq);
		Socket clientSocket;
		
		serverSocket = new ServerSocket(Integer.parseInt(port));
		System.out.println("Waiting for a client ...");
		tp.execute(new ServerSpeaker());
		
		while(true) {
			clientSocket = serverSocket.accept(); 
			hasEnter = true;
			InputStream in = clientSocket.getInputStream();
			OutputStream out = clientSocket.getOutputStream();
			String info = clientSocket.getInetAddress().getHostAddress();
			infoline.add(info);
			System.out.println("Client accepted: Socket[addr=" +
							   clientSocket.getInetAddress().getHostAddress() + 
							   ", port=" + clientSocket.getPort() + 
							   ", localport=" + clientSocket.getLocalPort() + "]");
            outline.add(out);
            numbers.add(count);
			tp.execute(new ServerListener(in, info, count));
            count++;
		}
	}
	
	/**
	 * This listener class gets an input socket connection, and decrypts what it listens from this connection
	 * and output to the console.
	 * @author minpan yangkang fangpengliu
	 *
	 */
	private class ServerListener implements Runnable {
		private InputStream in;
		private int out;
		private String endMessage = ".bye";
		private String socketInfo;
		private int[] dekeys;
		private RSAio reader;
		/**
		 * Constructs and initializes the new ServerListener instance with input socket
		 * @param cs the socket to be used as clientSocket
		 */
		public ServerListener(InputStream inn, String info, int o) {
			in = inn;
			out = o;
			dekeys = new int[2];

            System.out.println("**********Please enter your chatting buddy's public key: **********");
            System.out.println("in 'xxxx xxxx' format: ");
			try {
                ArrayList<Integer> tempkey = keys.take();
                dekeys[0] = tempkey.get(0);
                dekeys[1] = tempkey.get(1);
            } catch (InterruptedException e) {
                System.out.println("problem occur in accepting new client");
            }
			
			socketInfo = info;
            reader = new RSAio(0, dekeys);
	        //sc.close();
		}
		
		/**
		 * The run method of this Runnable class. Reads in from the socket inputstream and
		 * output to the console.
		 */
		public void run() {
			String message = ""; 
            while (!endMessage.equals(message)) {
            	//read in the message from wrapper
            	message = reader.read(in);
                if (message == null) break;
                System.out.println("[From chatter " + socketInfo + "]");
                System.out.println("\tBefore decryption: " + reader.getOriginal() +
                        "\n\tAfter decryption: " + message);
            }
            
            for(int i = 0; i < numbers.size(); i++) {
                if (numbers.get(i) == out) {
                    numbers.remove(i);
                    outline.remove(i);
                    infoline.remove(i);
                    break;
                }
            }
            
            System.out.println("Client " + socketInfo + " said bye.");
			
		}
	}

	/**
	 * 
	 * This speaker class gets an output socket connection, and reads from the console, encrypt the 
	 * message and send it throught the connection.
	 * @author minpan yangkang fangpengliu
	 *
	 */
	private class ServerSpeaker implements Runnable {
		private String endMessage = ".bye";
		private RSAio writer = new RSAio(1, enkeys);
		/**
		 * Constructs and initializes the new ServerSpeaker instance with input socket
		 * @param cs the socket to be used as clientSocket
		 */
		public ServerSpeaker() {}
		
		/**
		 * The run method of this Runnable class. Reads in from the console, encrypts the 
		 * message and output to the console.
		 */
		public void run() {
		    
            String message = ""; 
            
            while (true) {
                
            	message = sc.nextLine(); 
            	
            	if (hasEnter) {
            	    message = message.trim();
                    try {
                    ArrayList<Integer> dk = new ArrayList<Integer>();
                    int a = Integer.parseInt(message.substring(0, message.indexOf(' ')));
                    dk.add(a);
                    a = Integer.parseInt(message.substring(message.indexOf(' ') + 1, message.length()));
                    dk.add(a);
                        keys.put(dk);
                    } catch (InterruptedException e) {
                        System.out.println("client interrupted");
                    } catch (NumberFormatException e) {
                        System.out.println("in 'xxxx xxxx' format: ");
                        continue;
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("in 'xxxx xxxx' format: ");
                        continue;
                    }
                    hasEnter = false;
                    System.out.println("Type, enter .bye to quit");
                    continue;
                }
            	
                if (endMessage.equals(message)) {
                    
                    if (outline.size() == 0) {
                        System.out.println("There is no client connected to say bye to");
                        continue;
                    }
                    
                    while(outline.size() != 0) {
                        try {
                            outline.get(0).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        outline.remove(0);
                        infoline.remove(0);
                        numbers.remove(0);
                    }

                    System.out.println("You said bye to all the client ");
                    continue;
                }
            	
            	for (int i = 0; i < outline.size(); i++) {
            	    OutputStream out = outline.get(i);
            	    if (!writer.write(message, out)) {
                        try {
                            outline.remove(i).close();
                        } catch (IOException e) {
                            System.out.println("close end problem");
                        }
            	    }
                    System.out.println("[To the chatter " + infoline.get(i) + "]");
                    System.out.println("\tBefore encryption: " + message +
                            "\n\tAfter encryption: " + writer.translate(message));
            	}
                System.out.println("Type, enter .bye to quit");            	
                
            }
            
		}
	}
}
