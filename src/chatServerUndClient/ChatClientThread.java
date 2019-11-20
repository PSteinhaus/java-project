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

   public ChatClientThread(ChatClient _client, Socket _socket, ClientGUI _gui) {
      client   = _client;
      socket   = _socket;
      gui      = _gui;
      open();  
      start();
   }

   public void open() {
      try {
         streamIn  = new DataInputStream(socket.getInputStream());
      }
      catch(IOException ioe) {
         System.out.println("Error getting input stream: " + ioe);
         client.stop();
      }
   }

   public void stopThread() {
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
         case 0:  // just a normal message
            String msg = "";
            try {
               msg = streamIn.readUTF();
            }
            catch(IOException ioe) {
               System.out.println("Listening error: " + ioe.getMessage());
               client.stop();
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
               System.out.println("Listening error: " + ioe.getMessage());
               client.stop();
            }
            catch (ClassNotFoundException cnfe) {
               System.out.println(cnfe);
            }
            if( gui != null ) {
               // if there is a GUI hand the list over to it
               gui.updateUserlist( userlist );
            }
            break;
      }

   }

}
