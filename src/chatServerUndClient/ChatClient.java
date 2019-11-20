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
   }

   public void sendMassage (String s) throws IOException {
      streamOut.writeInt(0);  // signals that the following is a message
      streamOut.writeUTF(s);
      streamOut.flush();
   }

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
      if (thread != null) { 
         thread = null;
      }
      try {
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
}