package chatServerUndClientGUI;

import javax.swing.*;
import java.awt.*;

public class ClientGui {

    public static void main(String[] args) {

        //Creating the Frame
        JFrame frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("Connection");
        mb.add(m1);
        JMenuItem m11 = new JMenuItem("Connect to server");
        JMenuItem m22 = new JMenuItem("Disconnect");
        m1.add(m11);
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