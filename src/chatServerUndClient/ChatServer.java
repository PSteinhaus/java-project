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
   private ArrayList<GameSession> sessions = new ArrayList<>(20); // Create an ArrayList object
   private int lastGameId = 0;

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

   private ChatServerThread findClientThread(String username) {
      for (int i = 0; i < clientCount; i++)
         if (clients[i] != null && clients[i].getUsername().equals(username) )
               return clients[i];
      return null;
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
      int newLength = userList.length-1;
      if(newLength<0) newLength=0;        // dirty fix
      userList = Arrays.copyOf(userList, newLength);
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

   void handle(ChatServerThread userThread, int signal) {
      switch(signal) {

         case -1: // special case for game data
         {
            // first get the session id
            int gameId = userThread.readInt();
            GameSession session = getGameSession(gameId);
            if (session != null)
               session.takePlayerInput(userThread);
            break;
         }

         case 0:  // just a normal message
            handleMessage(userThread, userThread.readString());
            break;

         case 1:  // the user-list (should never happen, because only the server itself creates and sends these)
            break;
         case 2: // a new username (should never happen, because only the server itself creates and sends these)
            break;
         case 3: // a user wants to host a new game
         {
            String nameOfGame = userThread.readString();
            int minNumberOfPlayers = userThread.readInt();
            int maxNumberOfPlayers = userThread.readInt();
            int id = addGameSession(nameOfGame, minNumberOfPlayers, maxNumberOfPlayers);
            userThread.sendGameId(id);
            break;
         }
         case 4: // a gameId is sent as a message that the game is now open (should never happen, because only the server itself creates and sends these)
            break;
         case 5: // a user wants to join a game with the following id
         {
            int id = userThread.readInt();
            boolean accepted = addUserToGame(userThread, id);
            if (accepted) {
               System.out.println("accepted into group");
               userThread.acceptPlayer(id);
               if(gui!=null) gui.updateSessionList();
            } else {
               System.out.println("declined");
               userThread.declinePlayer();
            }
            break;
         }

         case 6: // a user wants to disband a game-session he hosts
         {
            System.out.println("removing");
            int id = userThread.readInt();
            removeGameSession(id);
            break;
         }

         case 7: // a user wants to invite someone to the game he hosts
         {
            System.out.println("invited");
            String invited = userThread.readString();
            int gameId = userThread.readInt();
            ChatServerThread invitedThread = findClientThread(invited);
            invitedThread.invitePlayer(userThread.getUsername(),getGameSession(gameId).getNameOfGame() ,gameId);
            break;
         }

         case 8: // a user wants to start the game he hosts
         {
            int gameId = userThread.readInt();
            int width = userThread.readInt();
            int height = userThread.readInt();
            GameSession session = getGameSession(gameId);
            if(session!=null && !session.started()) session.startGame(width, height);
            break;
         }

      }

   }

   private int addGameSession(String nameOfGame, int minNumberOfPlayers, int maxNumberOfPlayers) {
      int id = ++lastGameId;
      GameSession newSession = new GameSession(nameOfGame,minNumberOfPlayers, maxNumberOfPlayers,id);
      sessions.add(newSession);
      return id;
   }

   public ArrayList<GameSession> getGameSessions() { return sessions; }

   private void removeGameSession(int id) {
      GameSession session = getGameSession(id);
      if(session!=null) session.stop();
      sessions.remove(session);
      if(gui!=null) gui.updateSessionList();
   }

   private GameSession getGameSession(int id) {
      for (GameSession session: sessions) {
         if(session.getId()==id) return session;
      }
      return null;
   }

   private boolean addUserToGame(ChatServerThread userThread, int id) {
      GameSession session = getGameSession(id);
      if(session!=null)
         return session.addPlayer(userThread);
      else
         return false;
   }

   private void handleServerControl(String input) {
      if (input.equals("stop")) 
         close();
   }

    synchronized void writeServerOutput(String input) {
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
         writeServerOutput("Removing client thread at " + pos);
         if (pos < clientCount-1)
            if (clientCount - pos + 1 >= 0)
               System.arraycopy(clients, pos + 1, clients, pos, clientCount - pos -1);
         clients[clientCount-1] = null;
         clientCount--;
      }
      // also remove the user from his game session if he has one
       for (GameSession session: sessions) {
          if(session.hasPlayer(userThread.getUsername()))
             if(session.removePlayer(userThread)) // if it's the host
             {
                removeGameSession(session.getId());
                break;
             }
       }
      // tell the others
       if(userThread.getUsername()!=null) {
          for (int i = 0; i < clientCount; i++)
             clients[i].send(userThread.getUsername() + " has quit.");
          removeFromUserlist(userThread.getUsername());
          sendUserlist();
       }
       userThread.stopThread();
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

   boolean isOnline(String username) {
      for (String name: userList) {
         if( username.equals(name) ) return true;
      }
      return false;
   }
}
