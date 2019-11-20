package chatServerUndClient;

import chatServerUndClientGUI.ServerGUI;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ChatServer implements Runnable {
   private ChatServerThread[] clients = new ChatServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private Map<String,User> knownUsers = Collections.unmodifiableMap(new HashMap<String, User>());
   private final Path knownUsersPath = Paths.get("users.data");
   private ServerGUI gui = null;
   private String[] userList = new String[0];

   public ChatServer(int port, ServerGUI _gui) {
      gui = _gui;
      try {
         try {
            // load known Users
            knownUsers = (HashMap<String,User>) Helper.deserialize(Files.readAllBytes(knownUsersPath));
         } catch(IOException ioe) {
            writeServerOutput("User-file not found: " + ioe);
         } catch(ClassNotFoundException cnfe) {
            System.out.println(cnfe);
         }

         writeServerOutput("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);
         writeServerOutput("Server started: " + server);
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
            writeServerOutput("Waiting for a client ...");
            addThread(server.accept());
         }
         catch(IOException ie)
         {  writeServerOutput("Acceptance Error: " + ie); }
      }
      close();
   }

   private int findClient(ChatServerThread userThread) {
      for (int i = 0; i < clientCount; i++)
         if (clients[i] == userThread)
            return i;
      return -1;
   }

   void addToUserlist(String name) {
      String[] newUserList = new String[userList.length+1];
      System.arraycopy(userList, 0, newUserList, 0, newUserList.length-1);
      newUserList[newUserList.length-1] = name;
      userList = newUserList;
   }

   private void removeFromUserlist(String username) {
      for(int i=0; i<userList.length; i++) {
         if( userList[i].equals(username) ) {
            if (userList.length - 1 - i >= 0) System.arraycopy(userList, i + 1, userList, i, userList.length - 1 - i);
            break;
         }
      }
      //String[] oldUserList = copy(userList);
      userList = Arrays.copyOf(userList, userList.length-1);
      //System.arraycopy(oldUserList, 0, userList, 0, userList.length-1);
   }

    void sendUserlist() {
      for (ChatServerThread client : clients) {
         if (client != null) {
            client.sendUserlist(userList);
         }
      }
      // also tell the server-GUI if you have one
      if(gui != null)
         gui.updateUserlist(userList);
   }

   public void close() {
      writeServerOutput("Closing ...");
      for(int i=0; i<clients.length; i++) {
         if(clients[i]!=null) {
            clients[i].send("Server closing...");
            clients[i].stopThread();
         }
      }
      try {
         // save known users
         Files.write(knownUsersPath, Helper.serialize(knownUsers) );
      } catch(IOException ioe) {
         writeServerOutput("Error writing User-file: " + ioe);
      }
      System.exit(0);
   }

   void handleMessage(ChatServerThread userThread, String input) {
      switch (input) {
         case "!bye":
            // remove this user
            userThread.send("byebye");
            remove(userThread);
            break;
         case "!online":
            // tell THIS USER who's online
            userThread.send("online now:");
            for (int i = 0; i < clientCount; i++) {
               String name = clients[i].getUsername();
               if (name != null)
                  userThread.send(name);
            }
            break;
         case "!joined":
            // tell the others that you're online now
            for (int i = 0; i < clientCount; i++)
               clients[i].send(userThread.getUsername() + " is now online.");
            break;
         default:
            for (int i = 0; i < clientCount; i++)
               clients[i].send(userThread.getUsername() + ": " + input);
            break;
      }
   }

    synchronized void handle(ChatServerThread userThread, int signal) {
      switch(signal) {

         case 0:  // just a normal message
            handleMessage(userThread, userThread.readString());
            break;

         case 1:  // the user-list (should never happen, because only the server itself creates and sends these
            break;
      }

   }

   private void handleServerControl(String input) {
      if (input.equals("stop")) 
         close();
   }

    void writeServerOutput(String input) {
      if(gui!=null)
         gui.writeMessage(input);
      else
         System.out.println(input);
   }

   private boolean running() {
      return thread != null;
   }

   private synchronized void addThread(Socket socket) {
      if (clientCount < clients.length) {
         writeServerOutput("Client accepted: " + socket);
         clients[clientCount] = new ChatServerThread(this, socket, gui);
         try {
            clients[clientCount].open();
            clients[clientCount].start();
            clientCount++;
         }
         catch(IOException ioe) {
            writeServerOutput("Error opening thread: " + ioe);
         }
      }
      else
         writeServerOutput("Client refused: maximum " + clients.length + " reached.");
   }

    synchronized void remove(ChatServerThread userThread) {
      int pos = findClient(userThread);
      if (pos >= 0) {
         ChatServerThread toTerminate = clients[pos];
         writeServerOutput("Removing client thread at " + pos);
         if (pos < clientCount-1)
            if (clientCount - pos + 1 >= 0)
               System.arraycopy(clients, pos + 1, clients, pos, clientCount - pos -1);
         clients[clientCount-1] = null;
         clientCount--;
         toTerminate.stopThread();
      }
      // tell the others
      for (int i = 0; i < clientCount; i++)
         clients[i].send(userThread.getUsername() + " has quit.");
      removeFromUserlist(userThread.getUsername());
      sendUserlist();
   }

   public static void main(String[] args) {
      ChatServer server = null;
      if (args.length != 1)
         System.out.println("Usage: java ChatServer port");
      else {
         server = new ChatServer(Integer.parseInt(args[0]), null);
         Scanner scan = new Scanner(System.in);
         while( server.running() )
            server.handleServerControl(scan.next());
      }
   }

   // USER STUFF

    User getUser(String username) {
      return knownUsers.get(username);
   }

    void addNewUser(User user) {
      knownUsers.put(user.getName(), user);
   }

}
