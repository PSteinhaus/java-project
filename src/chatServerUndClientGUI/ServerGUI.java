package chatServerUndClientGUI;

import javax.swing.*;
import java.awt.*;

public class ServerGUI {

    public static void main(String[] args) {

        // Creating the Frame
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

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
        panel.add(send);

        // Text Area at the Center
        JTextArea ta = new JTextArea();

        // User list at the Center
        JList list = new JList();
        JScrollPane userList = new JScrollPane(list);

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.EAST, ta);
        frame.getContentPane().add(BorderLayout.WEST, userList);
        frame.setVisible(true);
    }
}
