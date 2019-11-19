package chatServerUndClientGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI implements ActionListener {
    private JMenuItem menuItemConnect = null;
    private JFrame frame;

    public static void main(String[] args) {

        ClientGUI clientGui = new ClientGUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( e.getSource() == menuItemConnect ) {
            // choose server ip and port
            JTextField ip = new JTextField();
            JTextField port = new JPasswordField();
            Object[] message = {
                    "Server IP:", ip,
                    "Port:", port
            };

            int option = JOptionPane.showConfirmDialog(frame, message, "Choose server", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                // ToDo : give this action actual functionality (connect to server)
                //if (ip.getText().equals("h") && port.getText().equals("h")) {
            } else {
                System.out.println("Login canceled");
            }
        }
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
        JMenuItem m22 = new JMenuItem("Disconnect");
        m1.add(menuItemConnect);
        m1.add(m22);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter Text");
        JTextField tf = new JTextField(18); // displays up tp 18 characters
        JButton send = new JButton("Send");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);

        // Text Area at the Center
        JTextArea ta = new JTextArea();

        // User list at the Center
        JPanel userPanel = new JPanel(); // the panel is not visible in output
        JList list = new JList();
        JLabel userListLabel = new JLabel("Online:");
        JScrollPane userList = new JScrollPane(list);
        userPanel.add(userListLabel); // Components Added using Flow Layout
        userPanel.add(userList);

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.EAST, ta);
        frame.getContentPane().add(BorderLayout.WEST, userPanel);
        frame.setVisible(true);
    }
}