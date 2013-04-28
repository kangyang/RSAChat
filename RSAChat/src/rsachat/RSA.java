package rsachat;

import java.util.Random;

/**
 * RSA library
 * RSA includes functions for encryption and decryption, etc.
 * endecrypt(): Given an integer representing an ASCII value, encrypt it.
 * crack_key(): Given key and c, crack key and get d.
 * coprime(): Gets a random integer that is coprime to the given input parameter.
 * generate_key(): Generates c, e, d and returns them as an integer array. 
 * key[0] = c = a * b, key[1] = e = coprime of (a - 1)*(b - 1), key[2] = d = mod_inverse of e%m
 * @author fangpengliu yangkang minpan
 */
public class RSA {
    
    private Random rd;
    /*private int[] primeNumbers = {2, 3, 5,7, 11,13, 17, 19, 
            23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 
            71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 
            113, 127, 131, 137, 139, 149, 151, 157, 163, 
            167, 173, 179, 181, 191, 193, 197, 199, 211, 
            223, 227, 229, 233, 239, 241, 251, 257, 263, 
            269, 271, 277, 281, 283, 293, 307, 311, 313, 
            317, 331, 337, 347, 349, 353, 359, 367, 373, 
            379, 383, 389, 397, 401, 409, 419, 421, 431, 
            433, 439, 443, 449, 457, 461, 463, 467, 479, 
            487, 491, 499, 503, 509, 521, 523, 541, 547, 
            557, 563, 569, 571, 577, 587, 593, 599, 601, 
            607, 613, 617, 619, 631, 641, 643, 647, 653, 
            659, 661, 673, 677, 683, 691, 701, 709, 719, 
            727, 733, 739, 743, 751, 757, 761, 769, 773, 
            787, 797, 809, 811, 821, 823, 827, 829, 839, 
            853, 857, 859, 863, 877, 881, 883, 887, 907, 
            911, 919, 929, 937, 941, 947, 953, 967, 971, 
            977, 983, 991, 997};*/
    /**
     * Constructs and initializes the new RSA instance
     */
    public RSA(){
        rd = new Random();
    }
    /**
     * Given an integer representing an ASCII value, encrypt it.
     * @param msg the message to be encrypted
     * @param key the key to use to do encryption
     * @param c 
     * @return the encrypted message
     */
    public int endecrypt(int msg, int key, int c){
        return modulo(msg, key, c);
    }
    /**
     * Generates c, e, d and returns them as an integer array. 
     * @param a	input interger for generating key
     * @param b input interger for generating key
     * @return keys generated from inputs
     */
    public int[] generate_key(int a, int b) {
        int[] keys = new int[3];
        keys[0] = a * b;
        int m = (a - 1) * (b - 1);
        keys[1] = coprime(m);
        keys[2] = mod_inverse(keys[1], m);
        return keys;
    }
    /**
     * Given key and c, crack key and get d.
     * @param key public key
     * @param c
     * @return the cracked private key
     */
    public int crack_key(int key, int c) {
        for (int i = 2; i < c; i++) {
            if (isPrime(i) && c % i == 0 && isPrime(c / i)) {
                int m = (i - 1) * (c / i - 1);
                for (int j = 2; j < m; j++){
                    if (GCD(m, j) == 1 && j == key) {
                        return mod_inverse(j, m);
                    }
                }
            }
        }
        return 0;
    }
    /**
     * Checks if the input argument is a prime number
     * @param n the number to check
     * @return true if the input is a prime, false otherwise
     */
    private static boolean isPrime(int n) {
        if (n == 1) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0) return false;
        int limit = (int)(Math.sqrt(n)+ 0.5);
        for (int i = 3; i <= limit; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
    /**
     * Gets a random integer that is coprime to the given input parameter.
     * @param x the given integer to find coprime for
     * @return a coprime number with the given integer
     */
    private int coprime(int x){
        if (x <= 2) throw new IllegalArgumentException("number smaller than 3 has no coprime number.");
        boolean k = true;
        int coprimeNumber = 0;
        while (k) {
            coprimeNumber = rd.nextInt(x - 2) + 2;
            k = (GCD(x, coprimeNumber) != 1);
        }
        return coprimeNumber;
    }
    /**
     * Finds the GCD of two input integers
     * @param a one input integer
     * @param b another input integer
     * @return the GCD of the two input integers
     */
    private int GCD(int a, int b){
        if (b == 0) return a;
        return GCD(b, a % b);
    }
    /**
     * Computes and returns the modular inverse of the given arguments
     * base^-1 % m
     * @param base the number to use as base
     * @param m the number to use as m
     * @return the mod_inverse of the given arguments
     */
    private int mod_inverse(int base, int m){
        return modulo(base, totient(m) - 1, m);
    }
    /**
     * Computes the Math modulo mod(Math.pow(a, b), c) for large values
     * @param a
     * @param b
     * @param c
     * @return the mod results
     */
    private int modulo(int a, int b, int c) {
        long mod = 1;
        for (int i = 0; i < b; i++) {
            mod = (mod * a) % c;
        }
        return ltoi(mod);
    }
    /**
     * Computes the Euler's Totient
     * @param n the input number for computing Euler's Totient
     * @return the euler's totient of the given number
     */
    private int totient(int n){
        int count = 0;
        for (int i = 1; i < n; i++){
            if (GCD(n, i) == 1) count++;
        }
        return count;
    }
    /**
     * 
     * @param l
     * @return
     */
    private static int ltoi(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
