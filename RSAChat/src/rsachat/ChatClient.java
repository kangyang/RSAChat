package rsachat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 * Contains main methods of client.Connects to the user-defined IP address and port number 
 * (where chatServer is running). Use two threads one for listening from and one for speaking 
 * to server.
 * Client receives messages from server (ClientListener.java), and sends messages to server 
 * (ClientSpeaker.java).
 * @author yangkang fangpengliu minpan
 *
 */
public class ChatClient{
    private static String ipAddress;
    private static String port;
    private static Socket socket;
    private static ExecutorService chatClient;
    /**
     * The main method of the ChatClient program. Reads in the command line arguments, and 
     * Starts a chatClient.
     * @param args <server address> <server port>
     */
    public static void main(String args[]){
    	int[] dekeys = new int[2]; 
    	int[] enkeys = new int[2]; 
        Scanner sc = new Scanner(System.in);
        
        try {
        	if (args.length == 2) {
        		ipAddress = args[0];
    			port = args[1];
    		} else {
    			System.out.println("RSAChat program usage: <server address> <server port>");
    			sc.close();
    			return;
    		} 
        	try{
                System.out.println("**********Please enter your private key: **********");
        	    for (int i = 0; i < enkeys.length; i++) {
        	        enkeys[i] = sc.nextInt();
        	    }

                System.out.println("**********Please enter your chatting buddy's public key: **********");
                for (int i = 0; i < dekeys.length; i++) {
                    dekeys[i] = sc.nextInt();
                }
        	} catch (InputMismatchException e) {
        	    System.out.println("Your key is not valid, client terminated");
        	    sc.close();
        	    return;
        	}
            socket = new Socket(ipAddress, Integer.parseInt(port));
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            chatClient = Executors.newFixedThreadPool(2);
            chatClient.execute(new ClientListener(in, dekeys));
            chatClient.execute(new ClientSpeaker(out, enkeys));
            chatClient.shutdown();
            chatClient.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            chatClient.shutdownNow();
            socket.close();
            sc.close();
        } catch (UnknownHostException e1) {
            System.out.println("unknown host");
        } catch (IOException e1) {
            System.out.println("server not started yet!");
        } catch (InterruptedException e) {
            System.out.println("client interruptted");
		}
    }
    /**
     * Gets InputStream from socket, and decrypts it and converts it into String 
     * (cooperates with functions in RSAio). Then prints out the decrypted message.
     * If server sends ".bye", print out message accordingly, this thread ends. 
     * @author yangkang fangpengliu minpan
     *
     */
    private static class ClientListener implements Runnable{
        RSAio reader;
        InputStream in;
        /**
         * Constructs and initializes the ClientListener instance with socket inputstream
         * and decryption keys.
         * @param i socket inputstream
         * @param keys the client's private key
         */
        public ClientListener(InputStream i, int[] keys){
            reader = new RSAio(0, keys);
            in = i;
        }
        /**
         * The run method of this Runnable class.
         * Keeps receives encrypted messages, decrypts it, and output it to the console.
         * If the the message is .bye, print out message accordingly, this thread ends. 
         */
        public void run() {
        	String message = "";
            String endMessage = ".bye";
            
            while (!message.equals(endMessage)) {
                message = reader.read(in);
                if (message == null) break;
                System.out.println("[From server: ]");
                System.out.println("\tBefore decryption: " + reader.getOriginal() +
                        "\n\tAfter decryption: " + message);
            }
            System.out.println("Server said goodbye.");
            chatClient.shutdownNow();
			try {
				if (!chatClient.awaitTermination(100, TimeUnit.MICROSECONDS)) {
				    System.out.println("Still waiting...");
				    System.exit(0);
				}
			} catch (InterruptedException e) {
				System.out.println("Client Exits...");
				System.exit(0);
			}
        }
    }
    /**
     * Gets console input message, converts it into String. Then encrypt the input 
     * message and write it out through socket (cooperates with functions in RSAio).
     * Prints out message before encryption (original input) and encrypted message.
     * If server send ".bye", client replies ".bye", print out message accordingly 
     * and this thread ends.
     * @author yangkang fangpengliu minpan
     *
     */
    private static class ClientSpeaker implements Runnable{
        RSAio writer;
        OutputStream out;
        /**
         * Constructs and initializes the ClientSpeaker instance with socket outputstream
         * and encryption keys.
         * @param o socket outputstream
         * @param keys the server's public key
         */
        public ClientSpeaker (OutputStream o, int[] keys) {
            writer = new RSAio(1, keys);
            out = o;
        }
        /**
         * The run method of this Runnable class.
         * Reads in message as string from console, encrypts it and sends it throught the
         * outputstream.
         * If the the message is .bye, print out message accordingly, this thread ends. 
         */
        public void run() {
        	System.out.println("Type, enter .bye to quit");
            Scanner sc = new Scanner(System.in);
            String endMessage = ".bye";
            String message = "";

            while (!message.equals(endMessage)) {
                message = sc.nextLine();
                if (message.length() == 0 || !writer.write(message, out)) break;
                System.out.println("[To server: ]");
                System.out.println("\tBefore encryption: " + message +
                        "\n\tAfter encryption: " + writer.translate(message));
            }
            System.out.println("Cliet ends chat.");
            sc.close();
            chatClient.shutdownNow();
			try {
				if (!chatClient.awaitTermination(100, TimeUnit.MICROSECONDS)) {
				    System.out.println("Still waiting...");
				}
			} catch (InterruptedException e) {
				System.out.println("Client Exits...");
				System.exit(0);
			}
        }
    }
}
