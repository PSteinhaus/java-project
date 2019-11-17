package chatServerUndClient;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ChatServer implements Runnable {
   private ChatServerThread clients[] = new ChatServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private Map<String,User> knownUsers = new HashMap<String,User>();
   private final Path knownUsersPath = Paths.get("users.data");

   public ChatServer(int port) {
      try {
         try {
            // load known Users
            knownUsers = (HashMap<String,User>) Helper.deserialize(Files.readAllBytes(knownUsersPath));
         } catch(IOException ioe) {
            System.out.println("User-file not found: " + ioe);
         } catch(ClassNotFoundException cnfe) {
            System.out.println(cnfe);
         }

         System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         if (thread == null) {
            thread = new Thread(this); 
            thread.start();
         }
      }
      catch(IOException ioe)
      {  System.out.println(ioe); }
   }

   public void run() {
      while (running()) {
         try {
            System.out.println("Waiting for a client ..."); 
            addThread(server.accept());
         }
         catch(IOException ie)
         {  System.out.println("Acceptance Error: " + ie); }
      }
      close();
   }

   private int findClient(ChatServerThread userThread) {
      for (int i = 0; i < clientCount; i++)
         if (clients[i] == userThread)
            return i;
      return -1;
   }

   private void close() {
      System.out.println("Closing ...");
      for(int i=0; i<clients.length; i++) {
         if(clients[i]!=null)
            clients[i].stopThread();
      }
      try {
         // save known users
         Files.write(knownUsersPath, Helper.serialize(knownUsers) );
      } catch(IOException ioe) {
         System.out.println("Error writing User-file: " + ioe);
      }
      System.exit(0);
   }

   public synchronized void handle(ChatServerThread userThread, String input) {
      if (input.equals("!bye")) {
         // remove this user
         userThread.send("byebye");
         remove(userThread);
      } else if (input.equals("!online")) {
         // tell THIS USER who's online
         userThread.send("online now:");
         for (int i = 0; i < clientCount; i++) {
            String name = clients[i].getUsername();
            if (name!=null)
               userThread.send(name);
         }
      } else if (input.equals("!joined")) {
         // tell the others that you're online now
         for (int i = 0; i < clientCount; i++)
            clients[i].send(userThread.getUsername() + " is now online.");
      }
      else
         for (int i = 0; i < clientCount; i++)
            clients[i].send(userThread.getUsername() + ": " + input);   
   }

   public void handleServerControl(String input) {
      if (input.equals("stop")) 
         close();
   }

   public boolean running() {
      return thread != null;
   }

   public synchronized void addThread(Socket socket) {
      if (clientCount < clients.length) {
         System.out.println("Client accepted: " + socket);
         clients[clientCount] = new ChatServerThread(this, socket);
         try {
            clients[clientCount].open();
            clients[clientCount].start();  
            clientCount++;
         }
         catch(IOException ioe) {
            System.out.println("Error opening thread: " + ioe);
         }
      }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached.");
   }

   public synchronized void remove(ChatServerThread userThread) {
      int pos = findClient(userThread);
      if (pos >= 0) {
         ChatServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread at " + pos);
         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;
         toTerminate.stopThread();
      }
      // tell the others
      for (int i = 0; i < clientCount; i++)
         clients[i].send(userThread.getUsername() + " has quit.");   
   }

   public static void main(String args[]) {
      ChatServer server = null;
      if (args.length != 1)
         System.out.println("Usage: java ChatServer port");
      else {
         server = new ChatServer(Integer.parseInt(args[0]));
         Scanner scan = new Scanner(System.in);
         while( server.running() )
            server.handleServerControl(scan.next());
      }
   }

   // USER STUFF

   public User getUser(String username) {
      return knownUsers.get(username);
   }

   public void addNewUser(User user) {
      knownUsers.put(user.getName(), user);
   }

}
