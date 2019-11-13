package chatServerUndClient;

import java.net.*;
import java.io.*;

// this thread mainly listens
public class ChatClientThread extends Thread {
   private Socket           socket   = null;
   private ChatClient       client   = null;
   private DataInputStream  streamIn = null;
   private boolean          stopped  = false;

   public ChatClientThread(ChatClient _client, Socket _socket) { 
      client   = _client;
      socket   = _socket;
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
            handle(streamIn.readUTF());
         }
         catch(IOException ioe) {
            System.out.println("Listening error: " + ioe.getMessage());
            client.stop();
         }
      }
   }

   public void handle(String msg) {
      if (msg.equals(".bye")) {
         System.out.println("Good bye. Press RETURN to exit ...");
         client.stop();
      }
      else
         System.out.println(msg);
   }

}
