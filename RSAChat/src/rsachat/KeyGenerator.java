package rsachat;

import java.util.Scanner;

/**
 * KeyGenerator provides user input options for: 
1: generate key. Based on two prime numbers input, generates public key and private key.
2: crack key. Based on inputs of key and c, crack key and get d, then decrypts input code.
3: encrypt. Based on inputs of key and c, encrypt input code.
4: decrypt. Based on inputs of key and c, decrypt input code.
It works closely with functions in RSA: generate_key(), crack_key(), decrypt(), and endecrypt()
 * @author fangpengliu yangkang minpan
 *
 */
public class KeyGenerator {
    
    public static RSA r = new RSA();
    public static int key = 0;
    public static int c = 0;
    /**
     * The main function of KeyGenerator, prompting users with options to use. Collecting user's
     * option and works according to the user's selection.
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("1. generate key \n2. crack key \n3.encrypt \n4.decrypt:");
        Scanner sc = new Scanner(System.in);
        String in = sc.nextLine();
        if (in.startsWith("1")) {
        
            System.out.println("Please enter two prime number");
            int a = sc.nextInt();
            int b = sc.nextInt();
            int k[] = r.generate_key(a, b);
            System.out.println("Public key: (" + k[1] + ", " + k[0] + ")");
            System.out.println("Private key: (" + k[2] + ", " + k[0] + ")");
            int x = r.endecrypt(688, k[1], k[0]);
            System.out.println("key test if it is 688 then key is successfully generated:\n" + r.endecrypt(x, k[2], k[0]));
            System.out.println("Notice: check whether both your inputs are prime numbers if output above is incorrect");
            
        } else if (in.startsWith("2")) {
            System.out.println("Please enter key and c to crack: ");
            int a = sc.nextInt();
            int b = sc.nextInt();
            System.out.println("C, E, D are: " + b + ", " + a + ", " + (key = r.crack_key(a, b)));
            c = b;
            System.out.println("enter code to decrypt, 'quit' to stop");
            String msg = sc.nextLine();
            msg = sc.nextLine();
            while (!msg.equals("quit")) {
                System.out.println("this is letter: " + decrypt(msg));
                msg = sc.nextLine();
            }
        } else if (in.startsWith("3")) {
            System.out.println("Please enter key and c to encrypt: ");
            key = sc.nextInt();
            c = sc.nextInt();
            System.out.println("enter code to encrypt");
            String msg = sc.nextLine();
            msg = sc.nextLine();
            System.out.println(encrypt(msg));
        } else if (in.startsWith("4")) {
            System.out.println("Please enter key and c to decrypt: ");
            key = sc.nextInt();
            c = sc.nextInt();
            System.out.println("enter code to decrypt, 'quit' to stop");
            String msg = sc.nextLine();
            msg = sc.nextLine();
            while (!msg.equals("quit")) {
                System.out.println("this is letter: " + decrypt(msg));
                msg = sc.nextLine();
            }
        }
        System.out.println("bye~");
        sc.close();
    }
    /**
     * Given a string, encrypts it using the private key, and returns the encrypted
     * results
     * @param s the string needs encryption
     * @return encrypted message of s
     */
    private static String encrypt(String s) {
        String result = "";
        for(int i = 0; i < s.length(); i++) {
            int a = s.charAt(i) + 0;
            result += r.endecrypt(a, key, c) + "\n";
        }
        return result;
    }
    /**
     * Given a string, decrypts it using the public key, and returns the decrypted
     * message
     * @param s the string needs decryption
     * @return decrypted message of s
     */
    private static char decrypt(String s) {
        try{
            int code = Integer.parseInt(s);
            return (char)r.endecrypt(code, key, c);
        } catch(NumberFormatException e) {
            System.out.println("this is no numbers");
            return 0;
        }
    }

}
