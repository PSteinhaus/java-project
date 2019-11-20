package chatServerUndClientGUI;

import chatServerUndClient.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientGUI implements ActionListener {
    private JMenuItem menuItemConnect = null;
    private JMenuItem menuItemDisconnect = null;
    private JTextField tf = null;
    private JTextArea ta = null;
    private JFrame frame;
    private ChatClient chatClient = null;

    public static void main(String[] args) {

        ClientGUI clientGui = new ClientGUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( e.getSource() == menuItemConnect ) {
            // CONNECT TO SERVER
            // choose server ip and port
            JTextField ip = new JTextField();
            JTextField port = new JPasswordField();
            Object[] message = {
                    "Server IP:", ip,
                    "Port:", port
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Choose server", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // first disconnect
                if(chatClient != null) {
                    chatClient.stop();
                    chatClient = null;
                }
                // start the ChatClient (workhorse)
                try { chatClient = new ChatClient( ip.getText(), Integer.parseInt(port.getText()), this ); }
                catch(NumberFormatException nfe) {  }
            } else {
                System.out.println("Login canceled");
            }
        } else if( e.getSource() == menuItemDisconnect ) {
            // DISCONNECT
            if(chatClient != null) {
                chatClient.stop();
                chatClient = null;
                writeMessage("DISCONNECTED FROM SERVER");
            }
        } else if( e.getSource() == tf ) {
            // SEND MESSAGE
            if( chatClient != null )
                try { chatClient.sendMassage(tf.getText()); }
                catch(IOException ioe) {
                    writeMessage("Sending error: " + ioe.getMessage());
                }
            tf.setText("");
        }
    }

    public void writeMessage(String message) {
        ta.append(message+"\n");
        ta.setCaretPosition(ta.getDocument().getLength()); // auto-scrolling
    }

    public ClientGUI() {
        // Creating the Frame
        frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("Connection");
        mb.add(m1);
        menuItemConnect = new JMenuItem("Connect to server");
        menuItemConnect.addActionListener(this);
        menuItemDisconnect = new JMenuItem("Disconnect");
        menuItemDisconnect.addActionListener(this);
        m1.add(menuItemConnect);
        m1.add(menuItemDisconnect);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter Text");
        tf = new JTextField(20);
        tf.addActionListener(this);
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);

        // Text Area at the Center
        ta = new JTextArea();
        ta.setMinimumSize(new Dimension(200, 500));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        JScrollPane scrollTa = new JScrollPane(ta);

        // User list at the Center
        JPanel userPanel = new JPanel(); // the panel is not visible in output
        JList list = new JList();
        JLabel userListLabel = new JLabel("Online:");
        JScrollPane userList = new JScrollPane(list);
        userPanel.add(userListLabel); // Components Added using Flow Layout
        userPanel.add(userList);
        userPanel.setMinimumSize(new Dimension(110, 500));

        // Erzeugung eines JSplitPane-Objektes mit vertikaler Trennung
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitpane.setLeftComponent(userPanel);
        splitpane.setRightComponent(scrollTa);

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.PAGE_START, mb);
        frame.getContentPane().add(BorderLayout.CENTER, splitpane);
        frame.getContentPane().add(BorderLayout.PAGE_END, panel);

        frame.setVisible(true);
    }
}