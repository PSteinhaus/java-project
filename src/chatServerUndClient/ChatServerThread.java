package chatServerUndClient;

import chatServerUndClientGUI.ServerGUI;

import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread {
   private Socket           socket    = null;
   private ChatServer       server    = null;
   private ServerGUI        gui       = null;
   private User             user      = null;
   private DataInputStream  streamIn  = null;
   private DataOutputStream streamOut = null;
   private boolean          stopped   = false;

    ChatServerThread(ChatServer _server, Socket _socket, ServerGUI _gui) {
      server = _server;  
      socket = _socket;
      gui    = _gui;
   }

    void send(String msg) {
      try {
         streamOut.writeInt(0);  // signals that the following is a message
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

    void stopThread() {
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

    String getUsername() {
      if (user!=null)
         return user.getName();

      return null;
   }

   String readString() {
      try {
         return streamIn.readUTF();
      }
      catch(IOException ioe) {
         server.writeServerOutput("ERROR reading: " + ioe.getMessage());
         server.remove(this);
         stopThread();
      }
      return null;
   }

   public void run() {
      server.writeServerOutput("Server Thread " + socket.getPort() + " running.");
      // login user
      login();
      // handle further input
      while (!stopped) {
         try {
            server.handle(this, streamIn.readInt());
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
            if(streamIn.readInt()==0)  // check if the next thing is really a message
               username = streamIn.readUTF();
            else continue;
            send("Password: ");
            if(streamIn.readInt()==0) // same
               password = streamIn.readUTF();
            else continue;
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
      if( gui == null ) {
         server.handleMessage(this, "!online");
      }
      server.addToUserlist(getUsername());
      server.sendUserlist();
      // tell the others that someone new has logged on
      server.handleMessage(this,"!joined");
   }

    void open() throws IOException {
      streamIn = new DataInputStream( new BufferedInputStream(socket.getInputStream()) );
      streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
   }

    void sendUserlist(String[] userlist) {
      try {
         streamOut.writeInt(1);  // signals that the following are a number of bytes and then a userlist
         byte[] asBytes = Helper.serialize(userlist);
         streamOut.writeInt( asBytes.length );  // length of the byteArray of the list
         streamOut.write( asBytes );
         streamOut.flush();
      }
      catch(IOException ioe) {
         System.out.println(getUsername() + " ERROR sending: " + ioe.getMessage());
         server.remove(this);
         stopThread();
      }
   }

}