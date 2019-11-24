package chatServerUndClientGUI;

import chatServerUndClient.ChatServer;
import chatServerUndClient.GameSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI implements ActionListener {
    private ChatServer chatServer = null;
    private JTextArea ta         = null;
    private JList<String> list = null;
    private DefaultListModel<String> listModel = null;

    private ServerGUI() {
        // Creating the Frame
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);

        Integer port = null;
        while(port == null) {
            // get the port
            JTextField portField = new JPasswordField();
            Object[] message = {
                    "Server port:", portField
            };

            int option = JOptionPane.showConfirmDialog(
                    frame,
                    message,
                    "",
                    JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try { port = Integer.parseInt(portField.getText()); }
                catch (NumberFormatException nfe) { port = null; }
            }
            else { return; }
        }

        // Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JButton send = new JButton("Stop Server");
        send.addActionListener(this);
        panel.add(send);

        // Text Area at the Center
        ta = new JTextArea();
        ta.setMinimumSize(new Dimension(200, 300));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        JScrollPane scrollTa = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // User list at the Center
        JPanel userPanel = new JPanel(); // the panel is not visible in output
        list = new JList<>();
        list.setFixedCellWidth(200);
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel userListLabel = new JLabel("Online:");
        JScrollPane userList = new JScrollPane(list);
        userPanel.add(userListLabel, BorderLayout.BEFORE_FIRST_LINE);
        userPanel.add(userList, BorderLayout.AFTER_LINE_ENDS);
        userPanel.setMinimumSize(new Dimension(200, 200));
        userPanel.setPreferredSize(new Dimension(200, 200));

        // session list below
        JPanel sessionPanel = new JPanel(); // the panel is not visible in output
        listModel = new DefaultListModel<String>();
        JList<String> sessionList = new JList<String>(listModel);
        //sessionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        sessionList.setFixedCellWidth(200);
        DefaultListCellRenderer sessionRenderer = (DefaultListCellRenderer) sessionList.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel sessionListLabel = new JLabel("Hosted games:");
        JScrollPane scrollingSessionList = new JScrollPane(sessionList);
        sessionPanel.add(sessionListLabel, BorderLayout.BEFORE_FIRST_LINE);
        sessionPanel.add(scrollingSessionList, BorderLayout.AFTER_LINE_ENDS);
        sessionPanel.setMinimumSize(new Dimension(200, 200));
        sessionPanel.setPreferredSize(new Dimension(200, 200));

        // Erzeugung eines JSplitPane-Objektes mit hotizontaler Trennung
        JSplitPane splitpaneLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitpaneLeft.setTopComponent(userPanel);
        splitpaneLeft.setBottomComponent(sessionPanel);

        // Erzeugung eines JSplitPane-Objektes mit vertikaler Trennung
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitpane.setLeftComponent(splitpaneLeft);
        splitpane.setRightComponent(scrollTa);

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.CENTER, splitpane);
        frame.getContentPane().add(BorderLayout.PAGE_END, panel);
        frame.setVisible(true);

        // start the server (workhorse)
        chatServer = new ChatServer(port, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // stop the server
        if(chatServer!=null) chatServer.close();
        System.exit(0);
    }

    public void updateSessionList() {
        System.out.println("updating");
        // update the list of hosted games
        listModel.removeAllElements();
        // add sessions one after another
        for (GameSession session: chatServer.getGameSessions()) {
            System.out.println("session added");
            listModel.addElement(session.getNameOfGame()+", hosted by "+session.getHost());
        }
    }

    public void updateUserlist( String[] listData ) {
        list.setListData(listData);
    }

    public void writeMessage(String message) {
        ta.append(message+"\n");
        ta.setCaretPosition(ta.getDocument().getLength()); // auto-scrolling
    }

    public static void main(String[] args) {
        ServerGUI serverGUI = new ServerGUI();
    }
}
