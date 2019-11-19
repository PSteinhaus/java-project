package chatServerUndClient;

import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread {
   private Socket           socket    = null;
   private ChatServer       server    = null;
   private User             user      = null;
   private DataInputStream  streamIn  = null;
   private DataOutputStream streamOut = null;
   private boolean          stopped   = false;

   public ChatServerThread(ChatServer _server, Socket _socket) {
      server = _server;  
      socket = _socket;
   }

   public void send(String msg) {
      try {
         streamOut.writeUTF(msg);
         streamOut.flush();
         server.writeServerOutput(msg);
      }
      catch(IOException ioe) {
         System.out.println(getUsername() + " ERROR sending: " + ioe.getMessage());
         server.remove(this);
         stopThread();
      }
   }

   public void stopThread() {
      try {
         stopped = true;
         if (socket != null)    socket.close();
         if (streamIn != null)  streamIn.close();
         if (streamOut != null) streamOut.close();
      }
      catch(IOException ioe) {
         System.out.println("Error closing thread: " + ioe);
      }
   }

   public String getUsername() {
      if (user!=null)
         return user.getName();
      else
         return null;
   }

   public void run() {
      server.writeServerOutput("Server Thread " + socket.getPort() + " running.");
      // login user
      login();
      // handle further input
      while (!stopped) {
         try {
            server.handle(this, streamIn.readUTF());
         }
         catch(IOException ioe) {
            server.writeServerOutput("ERROR reading: " + ioe.getMessage());
            server.remove(this);
            stopThread();
         }
      }
   }

   private void login() {
      boolean accepted = false;
      while(!accepted) {
         String feedback, username = null, password = null;
         try {
            send("Username: ");
            username = streamIn.readUTF();
            send("Password: ");
            password = streamIn.readUTF();
         } catch(IOException ioe) {
            server.writeServerOutput("Error logging in: " + ioe);
         }
         user = server.getUser(username);
         if(user != null) {
            if(user.tryPassword(password)) {
               // accept user
               feedback = "Welcome back "+username+".";
               accepted = true;
            } else {
               // password wrong
               feedback = "Wrong password, please try again.";
            }
         } else {
            // new user
            user = new User(username,password);
            server.addNewUser(user);
            feedback = "Welcome "+username+".";
            accepted = true;
         }
         // feedback for user
         send(feedback);
      }
      // tell him who's online
      server.handle(this,"!online");
      // tell the others that someone new has logged on
      server.handle(this,"!joined");
   }

   public void open() throws IOException {
      streamIn = new DataInputStream( new BufferedInputStream(socket.getInputStream()) );
      streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
   }

}