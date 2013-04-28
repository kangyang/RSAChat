package rsachat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * RSAio
 * RSAio mainly provides data wrapping methods to cooperates with data input/output at both server and client sockets. When read message in, it decrypts it. When write message out, it encrypts it.
 * read(): Takes an InputStream as parameter. It works when either server or client receives message, decrypts the message and converts the original message into a String.
 * write(): Takes an String s as message to be sent, encrypts the message, and send it out through an OutputStream.
 * @author fangpengliu yangkang minpan
 */
public class RSAio {
    
    private RSA r = new RSA();
    private int flag;
    private int[] enkey;
    private int[] dekey;
    private String original = null;
    
    /**
     * Constructor for specific uses set by flag
     * @param flag 0 for reader, 1 for writer
     * @param keys if flag is 0 enter the private key, if 1 enter the public key
     */
    public RSAio(int flag, int[] keys){
        if (flag != 0 && flag != 1) throw new IllegalArgumentException("invalid flag");
        this.flag = flag;
        if (flag == 0) {
            dekey = keys;
        } else {
            enkey = keys;
        }
        original = "";
    }
    
    public String getOriginal() {
    	String temp = "";
    	for (int i = 0; i < original.length(); i++) {
    		if ((i + 1) % 4 == 0) {
    			temp += original.substring(i, i+1) + " ";
    		} else {
    			temp += original.substring(i, i+1);
    		}
    	}
    	return temp;
    }
    
    /**
     * Constructor for both reader and writer
     * @param keyForWrite public key
     * @param keyForRead private key
     */
    public RSAio(int[] keyForWrite, int[] keyForRead){
        flag = 2;
        enkey = keyForWrite;
        dekey = keyForRead;
    }

    /**
     * read one line
     * @param is inputstream to be linked with
     * @return a string read in, return null if the stream is closed
     */
    public String read(InputStream is){
        if (flag == 1) throw new UnsupportedOperationException();
        byte[] bytes = new byte[4];
        String s = "";
        try {
            while (is.read(bytes, 0, 4) > -1) {
                ByteBuffer wrapped = ByteBuffer.wrap(bytes);
                int data = wrapped.getInt();
                original += data + "";
                s += decrypt(data);
                if (is.available() == 0) break;
            }
        } catch (IOException e) {
            return null;
        }
        if (s.length() < 1) return null;
        return s;
    }
    
    /**
     * write one line
     * @param s the string to be write
     * @param os the output stream
     */
    public boolean write(String s, OutputStream os){
        if (flag == 0) throw new UnsupportedOperationException();
        int[] send = encrypt(s);
        ByteBuffer byteBuffer = ByteBuffer.allocate(send.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(send);
        byte[] bytes = byteBuffer.array();
        try {
            os.write(bytes);
            os.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    /**
     * 
     * @param s
     * @return
     */
    public String translate(String s) {
        int key = enkey[0];
        int c = enkey[1];
        String result = "";
        for(int i = 0; i < s.length(); i++) {
            int a = s.charAt(i) + 0;
            result += r.endecrypt(a, key, c) + " ";
        }
        return result;
    }
    /**
     * Given a string input, encrypts it using the private key
     * @param s the string to be encrypted
     * @return the encrypted integer array
     */
    private int[] encrypt(String s){
        int key = enkey[0];
        int c = enkey[1];
        int len = s.length();
        int[] code = new int[len];
        for (int i = 0; i < len; i++) {
            int a = s.charAt(i) + 0;
            code[i] = r.endecrypt(a, key, c);
        }
        return code;
    }
    /**
     * Given a message input, decrypts it using the public key
     * @param msg the message to be decrypted
     * @return a char of the decrypted msg
     */
    private char decrypt(int msg){
        int key = dekey[0];
        int c = dekey[1];
        return (char)r.endecrypt(msg, key, c);
    }
}
