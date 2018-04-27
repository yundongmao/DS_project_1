package activitystreamer.client;

import activitystreamer.Client;
import activitystreamer.messages.LoginFailedMsg;
import activitystreamer.messages.LoginMsg;
import activitystreamer.util.Settings;
import activitystreamer.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;


@SuppressWarnings("serial")
public class TextFrame extends JFrame implements ActionListener {
    private static final Logger log = LogManager.getLogger(TextFrame.class);
    private JTextArea inputText;
    private JTextField usernameRegText;
    private JTextField secretRegText;
    private JTextField remoteHostText;
    private JTextField remotePortText;
    private JTextField remoteHostTextR;
    private JTextField remotePortTextR;
    private JTextField usernameLoginText;
    private JTextField secretLoginText;

    private JTextArea outputText;
    private JButton sendButton;
    private JButton disconnectButton;
    private JButton registerButton;
    private JButton loginButton;
    private JButton clearButton;
//	private JSONParser parser = new JSONParser();

    public TextFrame() {
        setTitle("ActivityStreamer Text I/O");
        //total
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));

        //left
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1));

        //left top
        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setLayout(new GridLayout(2, 1));

        //left top top
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new FlowLayout(0));
        remoteHostText = new JTextField(16);
        remotePortText = new JTextField(16);
        usernameLoginText = new JTextField(16);
        secretLoginText = new JTextField(16);
        JLabel remoteHostJL = new JLabel("remote host");
        JLabel remotePortJL = new JLabel("remote port");
        JLabel usernameLoginJL = new JLabel("username");
        JLabel secretLoginJL = new JLabel("secret");
        loginButton = new JButton("login");
        loginButton.addActionListener(this);
        Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "login");
        loginPanel.setBorder(lineBorder);
        loginPanel.add(remoteHostJL);
        loginPanel.add(remoteHostText);
        loginPanel.add(remotePortJL);
        loginPanel.add(remotePortText);
        loginPanel.add(usernameLoginJL);
        loginPanel.add(usernameLoginText);
        loginPanel.add(secretLoginJL);
        loginPanel.add(secretLoginText);
        loginPanel.add(loginButton);


        //left mid
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(new FlowLayout(0));
        usernameRegText = new JTextField(14);
        secretRegText = new JTextField(14);
        JLabel usernameLabel = new JLabel("username");
        JLabel secretLabel = new JLabel("secret");
        lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "register");
        registerPanel.setBorder(lineBorder);
        registerButton = new JButton("Register");
        registerButton.addActionListener(this);
        remoteHostTextR = new JTextField(16);
        remotePortTextR = new JTextField(16);
        JLabel remoteHostJR = new JLabel("remote host");
        JLabel remotePortJR = new JLabel("remote port");

        registerPanel.add(remoteHostJR);
        registerPanel.add(remoteHostTextR);
        registerPanel.add(remotePortJR);
        registerPanel.add(remotePortTextR);
        registerPanel.add(usernameLabel);
        registerPanel.add(usernameRegText);
        registerPanel.add(secretLabel);
        registerPanel.add(secretRegText);
        registerPanel.add(registerButton);


        //left bottom
        JPanel leftInputAreaPanel = new JPanel();
        leftInputAreaPanel.setLayout(new BorderLayout());
        lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "message send to server");
        leftInputAreaPanel.setBorder(lineBorder);
        inputText = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(inputText);
        leftInputAreaPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonGroup = new JPanel();
        sendButton = new JButton("Send");
        disconnectButton = new JButton("Disconnect");
        buttonGroup.add(sendButton);
        buttonGroup.add(disconnectButton);
        leftInputAreaPanel.add(buttonGroup, BorderLayout.SOUTH);
        sendButton.addActionListener(this);
        disconnectButton.addActionListener(this);

        //right
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "JSON output, received from server");
        outputPanel.setBorder(lineBorder);
        outputPanel.setName("Text output");
        outputText = new JTextArea();
        scrollPane = new JScrollPane(outputText);
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        outputPanel.add(clearButton,BorderLayout.SOUTH);



        leftTopPanel.add(loginPanel);
        leftTopPanel.add(registerPanel);
        leftPanel.add(leftTopPanel);
        leftPanel.add(leftInputAreaPanel);
        mainPanel.add(leftPanel);
        mainPanel.add(outputPanel);
        add(mainPanel);


        setLocationRelativeTo(null);
        setSize(1280, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        remoteHostText.setText(Settings.getRemoteHostname());
        remotePortText.setText(String.valueOf( Settings.getRemotePort()));
        remoteHostTextR.setText(Settings.getRemoteHostname());
        remotePortTextR.setText(String.valueOf( Settings.getRemotePort()));

    }


    public void setOutputText(final JSONObject obj) {
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		JsonParser jp = new JsonParser();
//		JsonElement je = jp.parse(obj.toJSONString());
//        String prettyJsonString = gson.toJson(obj.toJSONString());
//        outputText.setText(obj.toJSONString());
        outputText.append(obj.toJSONString()+"\n");
//        outputText.revalidate();
//        outputText.repaint();
    }

    public void setOutputText(final String text) {
        outputText.append(text+"\n");
        outputText.revalidate();
        outputText.repaint();
    }

    public void setNoLogin() {
        loginButton.setEnabled(false);
        remoteHostText.setEnabled(false);
        remotePortText.setEnabled(false);
        usernameLoginText.setEnabled(false);
        secretLoginText.setEnabled(false);
    }

    public void setLogin() {
        loginButton.setEnabled(true);
        remoteHostText.setEnabled(true);
        remotePortText.setEnabled(true);
        usernameLoginText.setEnabled(true);
        secretLoginText.setEnabled(true);
    }


    public void setRegister() {
        registerButton.setEnabled(true);
        usernameRegText.setEnabled(true);
        secretRegText.setEnabled(true);
        remoteHostTextR.setEnabled(true);
        remotePortTextR.setEnabled(true);
    }

    public void setNoRegister() {
        registerButton.setEnabled(false);
        usernameRegText.setEnabled(false);
        secretRegText.setEnabled(false);
        remoteHostTextR.setEnabled(false);
        remotePortTextR.setEnabled(false);
    }


        //	@Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String msg = inputText.getText();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg",msg);

            ClientSkeleton.getInstance().activetyMessage(jsonObject);
        } else if (e.getSource() == disconnectButton) {
            ClientSkeleton.getInstance().logout();
            setLogin();
            setRegister();
        } else if (e.getSource() == registerButton) {
            log.info("send register");
            ClientSkeleton.getInstance().register(usernameRegText.getText(), secretRegText.getText());
        } else if (e.getSource() == loginButton) {
            String username = usernameLoginText.getText();
            String secret = secretLoginText.getText();
            String remoteHost = remoteHostText.getText();
            String portStr = remotePortText.getText();
            if (StringUtils.isNullorEmpty(remoteHost) || StringUtils.isNullorEmpty(portStr)) {
                this.setOutputText("remote host or port is null");
                return;
            }

            int port = 8888;
            try {
                port = Integer.valueOf(portStr);
            } catch (Exception pe) {
                this.setOutputText("invalid port");
                log.info("invalid port");
                return;
            }

            Settings.setRemoteHostname(remoteHost);
            Settings.setRemotePort(port);
            if(StringUtils.isNullorEmpty(username)){
                Settings.setUsername("anonymous");
            }else{
                Settings.setUsername(username);
                if(StringUtils.isNullorEmpty(secret)){
                    this.setOutputText("secret is null");
                    return;
                }
            }


            Settings.setSecret(secret);
            ClientSkeleton.getInstance().login();

        }else if(e.getSource()==clearButton){
            outputText.setText("");
            outputText.revalidate();
            outputText.repaint();
        }
    }
}
