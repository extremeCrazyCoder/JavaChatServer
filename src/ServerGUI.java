import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


@SuppressWarnings("serial")
public class ServerGUI extends JFrame {

    private JPanel contentPane;
    private ServerRoom parent;
    private JTextArea messagesArea;
    private JTextArea userTextInput;
    private JButton btnSend;
    private JScrollPane scrollMessages;
    private JScrollPane scrolluserTextInput;
    private JLabel lblRoomName;
    
    /**
     * Create the frame.
     * @param serverClient 
     */
    public ServerGUI(ServerRoom parent) {
        this.parent = parent;
        
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setLineWrap(true);
        scrollMessages = new JScrollPane(messagesArea);
        scrollMessages.setBounds(0, 30, 434, 270);
        scrollMessages.setBorder(BorderFactory.createLineBorder(Color.black));
        scrollMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollMessages);

        userTextInput = new JTextArea();
        userTextInput.setLineWrap(true);
        userTextInput.addKeyListener(new KeyListener() {
            @Override public void keyReleased(KeyEvent e) {};
            @Override public void keyPressed(KeyEvent e) {};
            @Override
            public void keyTyped(KeyEvent e) {
                if(((int) e.getKeyChar() == 10) || ((int) e.getKeyChar() == 13)) {
                    if(!e.isShiftDown()) {
                        sendText();
                    }
                    else {
                        userTextInput.setText(userTextInput.getText() + "\n");
                    }
                }
            }
        });
        scrolluserTextInput = new JScrollPane(userTextInput);
        scrolluserTextInput.setBounds(0, 300, 434, 79);
        scrolluserTextInput.setBorder(BorderFactory.createLineBorder(Color.black));
        scrolluserTextInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrolluserTextInput);
        
        btnSend = new JButton("Send");
        btnSend.setBounds(0, 379, 434, 40);
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                sendText();
            }
        });
        contentPane.add(btnSend);
        
        JButton btnCloseStream = new JButton("Close Room");
        btnCloseStream.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                closeRoom();
            }
        });
        btnCloseStream.setBounds(0, 422, 434, 40);
        contentPane.add(btnCloseStream);
        
        lblRoomName = new JLabel(parent.getName());
        lblRoomName.setHorizontalAlignment(SwingConstants.CENTER);
        lblRoomName.setBounds(0, 0, 434, 30);
        contentPane.add(lblRoomName);
    }

    protected void closeRoom() {
        parent.closeRoom("server", "admin");
    }

    protected void sendText() {
        String text = userTextInput.getText();
        
        text = text.replaceAll("\r", "");
        while(text.endsWith("\n"))
            text = text.substring(0, text.length() - 1);
        
        parent.sendSystemMessage(text, null);
        userTextInput.setText("");
    }

    public void addLine(Message toAdd) {
        messagesArea.setText(messagesArea.getText() + toAdd.getUserName() 
                + ": " + toAdd.getMessage() + "\n");
    }
}