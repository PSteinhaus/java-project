package chatServerUndClient;

import java.net.*;
import java.io.*;
import java.util.*;
import chatServerUndClientGUI.ClientGUI;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tests.DoubleClickTest;
import vierGewinntUndChomp.FutternGraphical;
import vierGewinntUndChomp.Spiel;
import vierGewinntUndChomp.Turn;
import vierGewinntUndChomp.VierGewinntGraphical;

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
   private boolean gameIsReady = false;
   private Spiel game                 = null;

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
      System.out.println("stopping...");
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

   public void writeChatOutput(String input) {
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
      System.out.println("Stopping the client");
      if (thread != null) { 
         thread = null;
      }
      try {
         if (hostedGameId != -1) stopHosting();
         if (game      != null)  game.stop();
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
         streamOut.writeInt(2);  // minimum number of players (for now always 2)
         streamOut.writeInt(2);  // maximum number of players (for now always 2)
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
      System.out.println("Sending error: " + ioe.getMessage());
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
         System.out.println("I want to disband");
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
      if(game!=null) {
         game.stopGame();
         game = null;
      }
      writeChatOutput("Your game session has been disbanded.");
      // clear the gameSessionPlayer list
      gameSessionNames.clear();
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
            writeChatOutput("You invited "+invited+" to your game of "+getHostedGame()+".");
         } catch(IOException ioe) {
            sendError(ioe);
         }
      }
   }

   void addPlayerToList(String username) {
      if(hostedGame==null) return;
      gameSessionNames.add(username);
      if(gui!=null) gui.checkListSelection();
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

   void setReady(boolean _gameIsReady) {
      gameIsReady = _gameIsReady;
      if(gui!=null) gui.setReadyForGame(gameIsReady);
   }

   public void startGame(int width, int height) {
      if(getHostedGame()!=null) {
         // tell the server to start the game
         try {
            streamOut.writeInt(8);  // signals that you want to start the game
            streamOut.writeInt(hostedGameId);   // this game
            streamOut.writeInt(width);
            streamOut.writeInt(height);
            streamOut.flush();
            writeChatOutput("GAME START");
         } catch(IOException ioe) {
            sendError(ioe);
         }
      }
   }

   void startGameProgram(String nameOfGame, int width, int height, String[] playernames) {
      // start the real program that actually runs the game
      // but first make sure no second game can be started
      setReady(false);
      switch (nameOfGame) {
         case "Chomp": {
            game = new FutternGraphical(width,height,playernames[0],playernames[1]);
            break;
         }
         case "Vier gewinnt": {
            game = new VierGewinntGraphical(width,height,playernames[0],playernames[1], this);
            break;
         }
      }
   }

    void receiveGameUpdate(byte[] asBytes) {
        // the server sent an update for the state of the game
        // make sure it gets to the game
        if(game!=null) game.receiveUpdate(asBytes);
    }

    public void sendGameUpdate(Turn turn) {
       // tell the server that you did something in the game
       try {
          System.out.println("sending a turn");
          byte[] data;
          data = Helper.serialize(turn);
          streamOut.writeInt(-1);  // signals that the following is a move
          if(hostedGameId!=-1)
             streamOut.writeInt(hostedGameId);
          else
             streamOut.writeInt(joinedGameId);
          streamOut.writeInt(data.length);  // how many bytes
          streamOut.write(data);
          streamOut.flush();
       } catch(IOException ioe) {
          sendError(ioe);
          System.out.println(ioe.getMessage());
          System.out.println(Arrays.toString(ioe.getStackTrace()));
       }
    }
}