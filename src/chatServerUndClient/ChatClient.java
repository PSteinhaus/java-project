package chatServerUndClient;

import java.net.*;
import java.io.*;
import java.util.*;
import chatServerUndClientGUI.ClientGUI;

public class ChatClient implements Runnable {
   private Socket socket              = null;
   private Thread thread              = null;
   private DataInputStream  console   = null;
   private DataOutputStream streamOut = null;
   private ChatClientThread clientThread    = null;
   private ClientGUI gui              = null;
   private String username            = null;
   private String hostedGame          = null;
   private int hostedGameId           = -1;
   private int joinedGameId           = -1;
   private ArrayList<String> gameSessionNames  = new ArrayList<>(6);;

   public ChatClient(String serverName, int serverPort, ClientGUI gui) {
      this.gui = gui;
      // setup
      writeChatOutput("Establishing connection. Please wait ...");
      try {
         socket = new Socket(serverName, serverPort);
         System.out.println("Connected: " + socket);
         start(gui);
      }
      catch(UnknownHostException uhe) {
         writeChatOutput("Host unknown: " + uhe.getMessage());
      }
      catch(IOException ioe) {
         writeChatOutput("Unexpected exception: " + ioe.getMessage());
      }
      catch(IllegalArgumentException iae) {
         writeChatOutput("Illegal argument: " + iae.getMessage());
      }
   }

   public void run() {
      while (thread != null) {
         try {
            Scanner scan = new Scanner(System.in);
            sendMassage(scan.nextLine());
         }
         catch(IOException ioe) {
            writeChatOutput("Sending error: " + ioe.getMessage());
            stop();
         }
      }
      // tell the gui you're dead
      if(gui!=null) {
         gui.killClient(false);
      }
   }

   public void sendMassage (String s) throws IOException {
      streamOut.writeInt(0);  // signals that the following is a message
      streamOut.writeUTF(s);
      streamOut.flush();
   }

   void setUsername(String _username) {
      username = _username;
      // also tell the gui (if there is one) that you've joined a server
      if(gui!=null) gui.joinedServer();
   }

   public String getUsername() { return username; }

   void writeChatOutput(String input) {
      if(gui!=null)
         gui.writeMessage(input);
      else
         System.out.println(input);
   }

   private void start(ClientGUI gui) throws IOException {
      if(gui==null) console = new DataInputStream(System.in);
      streamOut = new DataOutputStream(socket.getOutputStream());
      if (thread == null) {
         clientThread = new ChatClientThread(this, socket, gui);
         thread = new Thread(this);                   
         thread.start();
      }
   }

   public void stop() {
      if (thread != null) { 
         thread = null;
      }
      try {
         if (hostedGameId != -1) stopHosting();
         if (gui       != null)  gui.killClient(false);
         if (console   != null)  console.close();
         if (streamOut != null)  streamOut.close();
         if (socket    != null)  socket.close();
         if (clientThread != null) clientThread.stopThread();
      }
      catch(IOException ioe) {
         System.out.println("Error closing ...");
      }
   }

   public static void main(String[] args) {
      ChatClient client = null;
      if (args.length != 2)
         System.out.println("Usage: java ChatClient host port");
      else
         client = new ChatClient(args[0], Integer.parseInt(args[1]), null);
   }

   public String getHostedGame() {
      return hostedGame;
   }

   public void hostGame(String option) {
      // first stop hosting, if you're already hosting
      if(hostedGameId!=-1)
         stopHosting();
      hostedGame = option;
      // tell the server to register a new game session waiting for players
      try {
         streamOut.writeInt(3);  // signals that the following is a new game
         streamOut.writeUTF(option);
         streamOut.writeInt(2);  // number of players (for now always 2)
         streamOut.flush();
      } catch(IOException ioe) {
         sendError(ioe);
      }
   }

   void setGameId(int id) {
      hostedGameId = id;
   }

   private void sendError(IOException ioe) {
      writeChatOutput("Sending error: " + ioe.getMessage());
      stop();
   }

   void joinGame(int id) {
      // tell the server you want to join a game
      // first stop hosting, except you want to join your own game
      if(hostedGameId!=-1 && id!=hostedGameId)
         stopHosting();
      try {
         streamOut.writeInt(5);  // signals that you want to join a game with the following id
         streamOut.writeInt(id);
         streamOut.flush();
      } catch(IOException ioe) {
         sendError(ioe);
      }
   }

   private void stopHosting() {
      // tell the server to disband you game-session
      try {
         streamOut.writeInt(6);  // signals that you want to disband the session you host
         streamOut.writeInt(hostedGameId);
         streamOut.flush();
         hostedGameId = -1;
         hostedGame = null;
      } catch(IOException ioe) {
         sendError(ioe);
      }
   }

   void joinedGame(int id) {
      // the server just told you that the game with the following id welcomes you
      joinedGameId = id;
   }

   void joinDeclined() {
      // the server just told you that a game you wanted to join is already full
      writeChatOutput("Sry, the game is already full :(");
   }

   void gameSessionDisbanded() {
      // your game session has been disbanded
      // TODO: stop the game, if it's already running
      writeChatOutput("Your game session has been disbanded.");
      joinedGameId = -1;
   }

   void reactToInvitation(String host, String gameName, int gameId) {
      // someone invited you to join his game!
      if(gui!=null) {
         boolean accepted = gui.reactToInvitation(host, gameName);
         if(accepted) {
            joinGame(gameId);
         } else {
            gui.writeMessage("Invitation declined");
         }
      } else {
         // TODO: maybe create a way to let the client answer without a gui
      }
   }

   public void invite(String invited) {
      // invite someone to your game!
      if(getHostedGame()!=null) {
         // tell the server to invite him
         try {
            streamOut.writeInt(7);  // signals that you want to invite someone
            streamOut.writeUTF(invited);
            streamOut.writeInt(hostedGameId);
            streamOut.flush();
            System.out.println("invitation send");
         } catch(IOException ioe) {
            sendError(ioe);
         }
      }
   }

   void addPlayerToList(String username) {
      if(hostedGame==null) return;
      gameSessionNames.add(username);
   }

   void removePlayerFromList(String username) {
      if(hostedGame==null) return;
      gameSessionNames.remove(username);
   }

   public boolean inYourGame(String username) {
      // WORKS ONLY IF YOU ARE THE HOST!
      if(hostedGame==null) return false;
      for (String player: gameSessionNames) {
         if(player.equals(username)) return true;
      }
      return false;
   }

}