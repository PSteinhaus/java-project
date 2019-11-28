package chatServerUndClient;

import java.net.*;
import java.io.*;
import chatServerUndClientGUI.ClientGUI;

// this thread mainly listens
public class ChatClientThread extends Thread {
   private Socket           socket   = null;
   private ChatClient       client   = null;
   private ClientGUI        gui      = null;
   private DataInputStream  streamIn = null;
   private boolean          stopped  = false;

   ChatClientThread(ChatClient _client, Socket _socket, ClientGUI _gui) {
      client   = _client;
      socket   = _socket;
      gui      = _gui;
      open();  
      start();
   }

   private void open() {
      try {
         streamIn  = new DataInputStream(socket.getInputStream());
      }
      catch(IOException ioe) {
         System.out.println("Error getting input stream: " + ioe);
         client.stop();
      }
   }

   void stopThread() {
      stopped = true;
      try {
         if (streamIn != null) streamIn.close();
      }
      catch(IOException ioe) {
         System.out.println("Error closing input stream: " + ioe);
      }
   }

   public void run() {
      while (!stopped) {
         try {
            handle(streamIn.readInt());
         }
         catch(IOException ioe) {
            System.out.println("Listening error: " + ioe.getMessage());
            client.stop();
         }
      }
   }

   private void handle(int signal) {
      switch(signal) {
         case -1: // special case for received game updates
         {
            try {
               // first get the number of bytes to read
               int numberOfBytes = streamIn.readInt();
               byte[] asBytes = new byte[numberOfBytes];
               streamIn.read(asBytes, 0, numberOfBytes);
               client.receiveGameUpdate(asBytes);
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            break;
         }

         case 0:  // just a normal message
            String msg = "";
            try {
               msg = streamIn.readUTF();
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            if( gui == null ) {
               if (msg.equals("!bye")) {
                  System.out.println("Good bye. Press RETURN to exit ...");
                  client.stop();
               }
               else System.out.println(msg);
            }
            else {
               gui.writeMessage(msg);
            }
            break;

         case 1:  // the user-list
            String[] userlist = null;
            try {
               // first get the number of bytes to read
               int numberOfBytes = streamIn.readInt();
               byte[] asBytes = new byte[numberOfBytes];
               streamIn.read(asBytes, 0, numberOfBytes);
               userlist = (String[]) Helper.deserialize(asBytes);
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            catch (ClassNotFoundException cnfe) {
               System.out.println(cnfe);
            }
            if( gui != null ) {
               // if there is a GUI hand the list over to it
               gui.updateUserlist( userlist );
            }
            break;

         case 2: // the server tells you your username (necessary because the login procedure is handled on the server-side)
            try {
               client.setUsername(streamIn.readUTF());
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            break;

         case 3: // someone wants to host a new game (only received by server so unnecessary here)
            break;
         case 4: // the game you wanted to host has been accepted and here is your gameId
            try {
               int id = streamIn.readInt();
               client.setGameId(id);
               client.joinGame(id);
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            break;

         case 5: // a reply to you wanting to join a game (an int; -1 as a decline, else an id that you just joined)
            try {
               int id = streamIn.readInt();
               if(id==-1) {
                  client.joinDeclined();
               } else {
                  client.joinedGame(id);
               }
            }
            catch(IOException ioe) {
               listenError(ioe);
            }
            break;

         case 6: // the session you joined has been disbanded
            client.gameSessionDisbanded();
            break;

         case 7: // you've been invited to join a game!
         {
            try {
               String host = streamIn.readUTF();      // who invites
               String gameName = streamIn.readUTF();  // what game
               int gameId = streamIn.readInt();       // which id
               client.reactToInvitation(host, gameName, gameId);
            } catch (IOException ioe) {
               listenError(ioe);
            }
            break;
         }

         case 8: // a new user joined your game session
         {
            try {
               String newcomer = streamIn.readUTF();           // who came
               boolean gameIsReady = streamIn.readBoolean();   // whether the game is now ready to be started
               client.addPlayerToList(newcomer);
               client.writeChatOutput(newcomer+" joined the game.");
               client.setReady(gameIsReady);
            } catch (IOException ioe) {
               listenError(ioe);
            }
            break;
         }

         case 9: // a user left your game session
         {
            try {
               String leaving = streamIn.readUTF();            // who left
               boolean gameIsReady = streamIn.readBoolean();   // whether the game is now ready to be started
               client.removePlayerFromList(leaving);
               client.writeChatOutput(leaving+" left the game.");
               client.setReady(gameIsReady);
            } catch (IOException ioe) {
               listenError(ioe);
            }
            break;
         }

         case 10: // your game session is actually starting the game!
         {
            try {
               String nameOfGame = streamIn.readUTF(); // which game
               client.startGameProgram(nameOfGame);
               client.writeChatOutput("Starting "+nameOfGame+"!");
            } catch (IOException ioe) {
               listenError(ioe);
            }
            break;
         }
      }

   }

   private void listenError(IOException ioe) {
      System.out.println("Listening error: " + ioe.getMessage());
      client.stop();
   }

}
