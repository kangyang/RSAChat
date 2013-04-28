This project is done by Fangpeng Liu, Min Pan and Yang Kang. 

Files included:
KeyGenerator.java
ChatServer.java
ChatClient.java
RSA.java
RSAio.java

---------------------------------------------------------------------------------------

Feature Introduction:
This Chat program implements communications between server and multiple clients. 
When server sends message, all clients receive the same message. In this way, we implemented broadcasting.
When client sends message to server, server receives the message. Server can receive messages from different clients simultaneously.
When client type ".bye", this client exits chatting, and server displays information accordingly. 
When server type ".bye", all alive clients exit chatting. But server never exits. 
Server is always available to be connected to chat with new coming clients.

---------------------------------------------------------------------------------------

If run on Eclipse:
1, Go to Run Configurations, put <port number> as argument, run ChatServer.java
2, Go to Run Configurations, put <server's IP address> <port number> as argument, run ChatClient.java

If run on Terminals under Linux Operating System:
1, change directory to which contains all java files as above
2, Compile: javac KeyGenerator.java ChatServer.java ChatClient.java RSA.java RSAio.java
   (It will generate a .class file for each .java file)
3, cd .. (goes to directory of rsachat)
4, Open another Terminal (will be working as Client)
5, On first Terminal, run: java rsachat.ChatServer <port number>
(run: java rsachat.ChatServer  this will output the server's IP address to console)
6, On second Terminal, run: java rsachat.ChatClient <server IP> <port number>

---------------------------------------------------------------------------------------

Run the program:
1, Open server, input server's private key, and wait for a client
2, Open client, input client's private key. Then input server (chatting buddy)'s public key
3, Go back to server, input this new client's public key. 
4, They can start chatting!
5, To open new clients, repeat 2 and 3.
6, If client says .bye, this client terminates.
7, If server says .bye, all alive clients terminate.


##################################################################
                              Introduction to all classes:
##################################################################

KeyGenerator provides user input options for 
1: generate key. Based on two prime numbers input, generates public key and private key.
2: crack key. Based on inputs of key and c, crack key and get d, then decrypt input code.
3: encrypt. Based on inputs of key and c, encrypt input code.
4: decrypt. Based on inputs of key and c, decrypt input code.
It works closely with functions in RSA: generate_key(), crack_key(), decrypt(), and endecrypt()

---------------------------------------------------------------------------------------

RSA includes functions for encryption and decryption, etc.
endecrypt(): Given an integer representing an ASCII value, encrypt it.
crack_key(): Given key and c, crack key and get d.
coprime(): Gets a random integer that is coprime to the given input parameter.
generate_key(): Generates c, e, d and returns them as an integer array. 
key[0] = c = a * b, key[1] = e = coprime of (a - 1)*(b - 1), key[2] = d = mod_inverse of e%m

---------------------------------------------------------------------------------------

RSAio mainly provides data wrapping methods to cooperates with data input/output at both server and client sockets. When read message in, it decrypts it. When write message out, it encrypts it.
read(): Takes an InputStream as parameter. It works when either server or client receives message, decrypts the message and converts the original message into a String.
write(): Takes an String s as message to be sent, encrypts the message, and send it out through an OutputStream.

---------------------------------------------------------------------------------------

ChatServer: Includes three classes: 
1, ChatServer.java: Contains main methods of server. 
This class is the server class in a chat program. A daemon thread listerns for incoming requests at server port given as arguments when the server starts. Every time it receives a client connection request, enqueues it into a blocking queue, and continue listening. A thread pool of threads (5 threads as macro defined) deal with every request in blocking queue from head of the queue. In this way, socket connections of one server with multi-clients are developed.
On each socket connection, use two threads, one for listening from and one for speaking to client. This ensures asynchronous communication with the client. Server receives messages from each client (ServerListener.java), and sends messages to clients (ServerSpeaker.java).
The server will keeping running, even it sends or receives a .bye message, it only closes the connection with that single client and continues listening for other connections.

2, ServerListener.java: 
Gets InputStream from socket, and decrypts it and converts it into String (cooperates with functions in RSAio). Then prints out the decrypted message.
If client send ".bye", print out message accordingly, this thead ends.

3, ServerSpeaker.java:
Gets console input message, converts it into String. Then encrypt the input message and write it out through socket (cooperates with functions in RSAio).
Print out message before encryption (original input) and after encryption.
If client send ".bye", print out message accordingly, this thread ends.

---------------------------------------------------------------------------------------

ChatClient: Includes three classes:
1, ChatClient.java: Contains main methods of client.
Connects to the user-defined IP address and port number(where chatServer is running). Use two threads one for listening from and one for speaking to server. Client receives messages from server (ClientListener.java), and sends messages to server(ClientSpeaker.java).

2, ClientListener.java:
Gets InputStream from socket, and decrypts it and converts it into String (cooperates with functions in RSAio). Then prints out the decrypted message.
If server sends ".bye", print out message accordingly, this thread ends. 

3, ClientSpeaker.java:
Gets console input message, converts it into String. Then encrypt the input message and write it out through socket (cooperates with functions in RSAio).
Print out message before encryption (original input) and encrypted message.
If server send ".bye", client replies ".bye", print out message accordingly and this thread ends.

##################################################################
                                             TEST CASE (Chatting Part)
##################################################################

host IP address: 192.168.222.1
**********Before you start, please enter your private key: **********
701 2491
Server started: ServerSocket[addr=192.168.222.1, port=0, localport=8000]
Waiting for a client ...
Type, enter .bye to quit
Client accepted: Socket[addr=192.168.222.1, port=21165, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
i'm server
[To all chatter 192.168.222.1]
	Before decryption: i'm server
	After decryption: 2013 1185 2261 2486 1225 1728 1252 1135 1728 1252 
Type, enter .bye to quit
Client accepted: Socket[addr=192.168.222.1, port=21169, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
dajia hao
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
dajiahao
[To all chatter 192.168.222.1]
	Before decryption: dajiahao
	After decryption: 2270 1743 848 2013 1743 398 1743 1134 
[To all chatter 192.168.222.1]
	Before decryption: dajiahao
	After decryption: 2270 1743 848 2013 1743 398 1743 1134 
Type, enter .bye to quit
[From chatter 192.168.222.1]
	Before decryption: 4433 2165 1163 3510 1233 4
	After decryption: client 1
[From chatter 192.168.222.1]
	Before decryption: 4433 2165 1163 3510 1233 180
	After decryption: client 2
[From chatter 192.168.222.1]
	Before decryption: 4433 2165 1163 3510 1233 1804 6327 2511 16
	After decryption: .bye
Client 192.168.222.1 said bye.
[From chatter 192.168.222.1]
	Before decryption: 4433 2165 1163 3510 1233 4463 2725 1116 
	After decryption: .bye
Client 192.168.222.1 said bye.
.bye
There is no client connected to say bye to
Client accepted: Socket[addr=192.168.222.1, port=21184, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
[From chatter 192.168.222.1]
	Before decryption: 4433 2165 1163 3510 1181 
	After decryption: client3
hi 333
[To all chatter 192.168.222.1]
	Before decryption: hi 333
	After decryption: 398 2013 2486 1246 1246 1246 
Type, enter .bye to quit
Client accepted: Socket[addr=192.168.222.1, port=21190, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
[From chatter 192.168.222.1]
	Before decryption: 2341 6523 3165 2643 3423 3153 1311 8816 9
	After decryption: hi i'm four
.bye
You said bye to all the client 
Client 192.168.222.1 said bye.
Client 192.168.222.1 said bye.
Client accepted: Socket[addr=192.168.222.1, port=21193, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
i'm server la
[To all chatter 192.168.222.1]
	Before decryption: i'm server la
	After decryption: 2013 1185 2261 2486 1225 1728 1252 1135 1728 1252 2486 79 1743 
Type, enter .bye to quit
Client accepted: Socket[addr=192.168.222.1, port=21201, localport=8000]
**********Please enter your chatting buddy's public key: **********
in 'xxxx xxxx' format: 
251 355
Type, enter .bye to quit
nimen hao
[To all chatter 192.168.222.1]
	Before decryption: nimen hao
	After decryption: 623 2013 2261 1728 623 2486 398 1743 1134 
[To all chatter 192.168.222.1]
	Before decryption: nimen hao
	After decryption: 623 2013 2261 1728 623 2486 398 1743 1134 
Type, enter .bye to quit





**********Please enter your private key: **********
251 355
**********Please enter your chatting buddy's public key: **********
389 2491
Type, enter .bye to quit
[From server: ]
	Before decryption: 2013 1185 2261 2486 1225 1728 1252 1135 1728 1252 2486 7917 43
	After decryption: i'm server la
[From server: ]
	Before decryption: 2013 1185 2261 2486 1225 1728 1252 1135 1728 1252 2486 7917 4362 3201 3226 1172 8623 2486 3981 7431 134
	After decryption: nimen hao





**********Please enter your private key: **********
251 355
**********Please enter your chatting buddy's public key: **********
389 2491
Type, enter .bye to quit
[From server: ]
	Before decryption: 6232 0132 2611 7286 2324 8639 8174 3113 4
	After decryption: nimen hao



##################################################################
                                             TEST CASE (Key-Generation Part)
##################################################################

*****************************************
Test Case 1
*****************************************

Please select one of the following: 
1. generate key 
2. crack key 
3.encrypt 
4.decrypt:
NOTICE: big numbers may take 1-5 seconds to run
1
Please enter two prime number
9973 8839
Public key: (51002005, 88151347)
Private key: (75524029, 88151347)
key test if you see 688 below then key is successfully generated:
688
Notice: check whether both your inputs are prime numbers if output above is incorrect
bye~

*****************************************
Test Case 2
*****************************************

Please select one of the following: 
1. generate key 
2. crack key 
3.encrypt 
4.decrypt:
NOTICE: big numbers may take 1-5 seconds to run
2
Please enter key and c to crack: 
51002005 88151347
C, E, D are: 88151347, 51002005, 75524029
enter code to decrypt, 'quit' to stop
quit
bye~


*****************************************
Test Case 3
*****************************************

Please select one of the following: 
1. generate key 
2. crack key 
3.encrypt 
4.decrypt:
NOTICE: big numbers may take 1-5 seconds to run
3
Please enter key and c to encrypt: 
51002005 88151347
enter code to encrypt
Hello!
18578979
40350840
15800392
15800392
51352018
81073645

bye~


*****************************************
Test Case 4
*****************************************

Please select one of the following: 
1. generate key 
2. crack key 
3.encrypt 
4.decrypt:
NOTICE: big numbers may take 1-5 seconds to run
4
Please enter key and c to decrypt: 
75524029 88151347
enter code to decrypt, 'quit' to stop
18578979
this is letter: H
40350840
this is letter: e
15800392
this is letter: l
51352018
this is letter: o
81073645
this is letter: !
quit
bye~


