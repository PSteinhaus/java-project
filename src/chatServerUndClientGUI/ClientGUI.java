package chatServerUndClientGUI;

import chatServerUndClient.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.IOException;

public class ClientGUI implements ActionListener {
    private JMenuItem menuItemConnect = null;
    private JMenuItem menuItemDisconnect = null;
    private JTextField tf = null;
    private JTextArea ta = null;
    private JFrame frame;
    private ChatClient chatClient = null;
    private JList<String> list = null;
    private JButton buttonInvite = null;
    private JButton buttonHost = null;
    private JButton buttonStart = null;

    public static void main(String[] args) {
        ClientGUI clientGui = new ClientGUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var source = e.getSource();
        if( source == menuItemConnect ) {
            // CONNECT TO SERVER
            // choose server ip and port
            JTextField ip = new JTextField();
            JTextField port = new JTextField();
            Object[] message = {
                    "Server IP:", ip,
                    "Port:", port
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Choose server", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // first disconnect
                killClient(true);
                // start the ChatClient (workhorse)
                try {
                    chatClient = new ChatClient( ip.getText(), Integer.parseInt(port.getText()), this );
                }
                catch(NumberFormatException nfe) {  }
            } else {
                System.out.println("Login canceled");
            }
        } else if( source == menuItemDisconnect ) {
            // DISCONNECT
            killClient(true);
        } else if( source == tf ) {
            // SEND MESSAGE
            if( chatClient != null )
                try { chatClient.sendMassage(tf.getText()); }
                catch(IOException ioe) {
                    writeMessage("Sending error: " + ioe.getMessage());
                }
            tf.setText("");
        } else if( source == buttonHost ) {
            // HOST A GAME
            // open a dialog
            Object[] options = {"Vier gewinnt",
                    "Chomp"};
            int n = JOptionPane.showOptionDialog(frame,
                    "Choose a game",
                    "Host new session",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            chatClient.hostGame((String) options[n]);
        } else if( source == buttonInvite ) {
            // INVITE USER TO GAME
            String invited = list.getSelectedValue();
            chatClient.invite(invited);
        } else if( source == buttonStart ) {
            // START THE GAME
            // TODO: ask the user to input the dimensions of the game board first
            int width, height;
            // let the user input width and height
            JTextField wField = new JTextField();
            JTextField hField = new JTextField();
            Object[] message = {
                    "Width:", wField,
                    "Height:", hField
            };
            int option = JOptionPane.showConfirmDialog(frame, message, "Choose board dimensions", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    width = Integer.parseInt(wField.getText());
                    height = Integer.parseInt(hField.getText());
                    // check if the board is big enough and start the game
                    if( width >= 4 && height >= 4 )
                        chatClient.startGame(width, height);
                    else
                        writeMessage("Invalid dimensions, please choose a larger game board");
                }
                catch(NumberFormatException nfe) { writeMessage("Can't read this number"); }
            }
        }
    }

    public void writeMessage(String message) {
        ta.append(message+"\n");
        ta.setCaretPosition(ta.getDocument().getLength()); // auto-scrolling
    }

    public void killClient(boolean stopFirst) {
        if(chatClient!=null) {
            if (stopFirst)
                chatClient.stop();
            chatClient = null;
            list.setListData(new String[0]);
            writeMessage("Disconnected");
        }
        buttonHost.setVisible(false);
        buttonInvite.setVisible(false);
        buttonStart.setVisible(false);
    }

    public void updateUserlist( String[] listData ) {
        list.setListData(listData);
    }

    public boolean reactToInvitation(String host, String gameName) {
        // show the invitation and prompt a response
        System.out.println("reacting");
        Object[] options = {"Accept",
                            "Refuse"};
        int n = JOptionPane.showOptionDialog(frame,
                host+" invited you to a game of "+gameName,
                "Invitation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        return n == 0;
    }

    private ClientGUI() {
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

        // User list at the left
        JPanel userPanel = new JPanel();
        list = new JList<>(new String[0]);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setFixedCellWidth(100);
        // get the selection and react to it
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // create a button for hosting games
        buttonHost = new JButton("Host game");
        buttonHost.addActionListener(this);
        buttonHost.setVisible(false);

        // create an initially invisible button for inviting other users to your game
        buttonInvite = new JButton("Invite");
        buttonInvite.addActionListener(this);
        buttonInvite.setVisible(false);

        // create an initially invisible button for starting games you're hosting
        buttonStart = new JButton("Start game");
        buttonStart.addActionListener(this);
        buttonStart.setVisible(false);

        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent le) {
                int idx = list.getSelectedIndex();
                // if something is selected and you are hosting and you didn't select yourself show "Invite"
                if (idx != -1 && chatClient.getHostedGame()!=null && !list.getModel().getElementAt(idx).equals(chatClient.getUsername()) && !chatClient.inYourGame(list.getModel().getElementAt(idx)) )
                    buttonInvite.setVisible(true);
                else
                    buttonInvite.setVisible(false);
            }
        });
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel userListLabel = new JLabel("Online:");
        JScrollPane userList = new JScrollPane(list);
        userPanel.add(userListLabel, BorderLayout.PAGE_START);
        userPanel.add(userList, BorderLayout.CENTER);
        userPanel.add(buttonHost, BorderLayout.SOUTH);
        userPanel.add(buttonInvite, BorderLayout.SOUTH);
        userPanel.add(buttonStart, BorderLayout.SOUTH);
        userPanel.setMinimumSize(new Dimension(100, 500));
        userPanel.setPreferredSize(new Dimension(100, 500));

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

    public void joinedServer() {    // called by client whenever you join a server (and are authenticated)
        buttonHost.setVisible(true);
    }

    public void checkListSelection() {
        // just manually check whether "Invite" should be displayed
        int idx = list.getSelectedIndex();
        // if something is selected and you are hosting and you didn't select yourself show "Invite"
        if (idx != -1 && chatClient.getHostedGame()!=null && !list.getModel().getElementAt(idx).equals(chatClient.getUsername()) && !chatClient.inYourGame(list.getModel().getElementAt(idx)) )
            buttonInvite.setVisible(true);
        else
            buttonInvite.setVisible(false);
    }

    public void setReadyForGame(boolean gameIsReady) {
        if(gameIsReady && chatClient.getHostedGame()!=null)
            buttonStart.setVisible(true);
        else
            buttonStart.setVisible(false);
    }
}