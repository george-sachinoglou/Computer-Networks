import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Client {

    Socket connection;
    Socket listenerFollow;
    DataOutputStream out;
    DataInputStream in;
    DataInputStream follow_in;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    String clientID;
    String accepted;
    String imgFound;
    static String projectDir = Paths.get("").toAbsolutePath().toString();
    File clientDir;
    ArrayList<String> followers = new ArrayList<>();
    ArrayList<String> following = new ArrayList<>();

    public static void main(String[] args){
        Client client = new Client();
        client.loginGUI();
        client.listenForFollow();

    }

    /*CLIENT CONSTRUCTOR*/
    public Client(){
        connect();
    }

    /*CLIENT GUI - CONTAINS: ACCESS PROFILE, ADD PICTURE, DOWNLOAD PICTURE, FOLLOW, UNFOLLOW FUNCTIONS*/
    public void clientGUI(){
        JFrame jFrame = new JFrame("SN Client");
        jFrame.setSize(350, 350);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.EXIT_ON_CLOSE));

        JLabel jlTitle = new JLabel("SN "+clientID);
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0 , 10 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtonsRowOne = new JPanel();
        jpButtonsRowOne.setBorder(new EmptyBorder(40, 0 , 0 ,0));

        JPanel jpButtonsRowTwo = new JPanel();
        jpButtonsRowTwo.setBorder(new EmptyBorder(0, 0 , 0 ,0));

        JPanel jpButtonsRowThree = new JPanel();
        jpButtonsRowThree.setBorder(new EmptyBorder(0, 0 , 0 ,0));

        JButton jbAccessProfile = new JButton("Access Profile");
        jbAccessProfile.setPreferredSize(new Dimension(150, 50));

        JButton jbAddPic = new JButton("Add Picture");
        jbAddPic.setPreferredSize(new Dimension(150, 50));

        JButton jbSearchPic = new JButton("Search Picture");
        jbSearchPic.setPreferredSize(new Dimension(150, 50));

        JButton jbDownloadPic = new JButton("Download Picture");
        jbDownloadPic.setPreferredSize(new Dimension(150, 50));

        JButton jbFollow = new JButton("Follow");
        jbFollow.setPreferredSize(new Dimension(100, 30));

        JButton jbUnfollow = new JButton("Unfollow");
        jbUnfollow.setPreferredSize(new Dimension(100, 30));

        jFrame.add(jlTitle);

        jpButtonsRowOne.add(jbAccessProfile);
        jpButtonsRowOne.add(jbAddPic);

        jpButtonsRowTwo.add(jbSearchPic);
        jpButtonsRowTwo.add(jbDownloadPic);

        jpButtonsRowThree.add(jbFollow);
        jpButtonsRowThree.add(jbUnfollow);

        jFrame.add(jpButtonsRowOne);
        jFrame.add(jpButtonsRowTwo);
        jFrame.add(jpButtonsRowThree);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        jbAccessProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accessProfileGUI();
            }
        });
        jbAddPic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageUploadGUI();

            }
        });

        jbSearchPic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchGUI();
            }
        });

        jbDownloadPic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadGUI();
            }
        });

        jbFollow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                followGUI();
            }
        });

        jbUnfollow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unfollowGUI();
            }
        });

        jFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try{
                    out.writeUTF("Disconnected");
                    e.getWindow().dispose();
                }catch(IOException error){
                    error.printStackTrace();
                }
            }
        });
    }

    /*GUI FOR ADDING AN IMAGE TO PROFILE*/
    public void imageUploadGUI(){
        final File[] imageToSend = new File[1];
        final File[] txtToSend = new File[1];

        JFrame jFrame = new JFrame("SN ImageUpload");
        jFrame.setSize(500, 500);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("SN Uploader");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0 , 10 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlFileName = new JLabel("Choose an image");
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        jlFileName.setBorder(new EmptyBorder(15, 0 , 0 ,0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlImageDesc = new JLabel("Image Description:");
        jlImageDesc.setFont(new Font("Arial", Font.BOLD, 15));
        jlImageDesc.setBorder(new EmptyBorder(20, 0 , 10 ,0));
        jlImageDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea textDesc = new JTextArea(13, 15);
        textDesc.setPreferredSize(new Dimension(100, 50));
        JScrollPane areaScrollPane = new JScrollPane(textDesc);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(10, 0 , 10 ,0));

        JButton jbSendFile = new JButton("Upload Image");
        jbSendFile.setPreferredSize(new Dimension(150, 75));

        JButton jbChooseFile = new JButton("Choose Image");
        jbChooseFile.setPreferredSize(new Dimension(150, 75));

        jFrame.add(jlTitle);
        jFrame.add(jlImageDesc);
        jFrame.add(areaScrollPane);
        textDesc.setLineWrap(true);
        textDesc.setWrapStyleWord( true );
        jFrame.add(jlFileName);
        jFrame.setResizable(false);
        jpButton.add(jbSendFile);
        jpButton.add(jbChooseFile);
        jFrame.add(jpButton);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        jbChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose an image to send");
                jFileChooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter1 = new FileNameExtensionFilter("png", "png");
                FileNameExtensionFilter filter2 = new FileNameExtensionFilter("jpg", "jpg");
                FileNameExtensionFilter filter3 = new FileNameExtensionFilter("jpeg", "jpeg");
                jFileChooser.addChoosableFileFilter(filter1);
                jFileChooser.addChoosableFileFilter(filter2);
                jFileChooser.addChoosableFileFilter(filter3);

                if(jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    imageToSend[0] = jFileChooser.getSelectedFile();
                    jlFileName.setText("The image you want to send is: " + imageToSend[0].getName());
                }
            }
        });

        jbSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imageToSend[0] == null){
                    jlFileName.setText("Please choose a file first");
                } else {
                    try{
                        out.writeUTF("imageUpload");
                        out.flush();
                    }catch(IOException error){
                        error.printStackTrace();
                    }
                    try {
                        String text= textDesc.getText();
                        String rawName = removeExtension(imageToSend);
                        txtToSend[0] = new File(projectDir+"\\ClientDirectory\\" +clientID+"Dir\\"+rawName+".txt");
                        FileWriter writer = new FileWriter(txtToSend[0]);
                        writer.write(text);
                        writer.close();
                    }catch(IOException error){
                        error.printStackTrace();
                    }
                    sendImage(imageToSend,txtToSend);
                    jFrame.dispose();
                };
            }
        });
    };

    /*LOGIN GUI - CONTAINS: LOGIN, REGISTER FUNCTIONS*/
    public void loginGUI(){
        JFrame jFrame = new JFrame("SN Client");
        jFrame.setSize(450, 450);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.EXIT_ON_CLOSE));

        JLabel jlTitle = new JLabel("Enter Login Info");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(20, 0 , 10 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlUser = new JLabel("Username");
        jlUser.setBorder(new EmptyBorder(0, 0 , 0 ,0));
        jlUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField textUser = new JTextField(20);

        JLabel jlPass = new JLabel("Password");
        jlPass.setBorder(new EmptyBorder(0, 0 , 0 ,0));
        jlPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField textPass= new JTextField(20);

        JPanel jpTextUser = new JPanel();
        jpTextUser.setBorder(new EmptyBorder(50, 0 , 0,0));

        JPanel jpTextPass = new JPanel();
        jpTextPass.setBorder(new EmptyBorder(0, 0 , 10 ,0));

        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(50, 0 , 10 ,0));

        JButton jbRegister = new JButton("Register");
        jbRegister.setPreferredSize(new Dimension(100, 25));

        JButton jbLogin = new JButton("Login");
        jbLogin.setPreferredSize(new Dimension(100, 25));

        jFrame.add(jlTitle);
        jpTextUser.add(jlUser);
        jpTextUser.add(textUser);
        jpTextPass.add(jlPass);
        jpTextPass.add(textPass);
        jpButton.add(jbLogin);
        jpButton.add(jbRegister);
        jFrame.add(jpTextUser);
        jFrame.add(jpTextPass);
        jFrame.add(jpButton);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.setResizable(false);

        jbLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    out.writeUTF("login");
                    out.flush();
                }catch (IOException error){
                    error.printStackTrace();
                }
                login(connection, textUser.getText(), textPass.getText());
                try{
                    accepted = in.readUTF();
                }catch(IOException error){
                    error.printStackTrace();
                }
                if(accepted.equals("true")){
                    setFollowers();
                    setFollowing();
                    clientDir = new File(projectDir + "\\ClientDirectory\\" +clientID+"dir");
                    jFrame.dispose();
                    clientGUI();
                }else if(accepted.equals("online")){
                    jlTitle.setText("Already online");
                }else {
                    jlTitle.setText("Wrong Info");
                }
            }
        });

        jbRegister.addActionListener((new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textUser.getText().equals("") || textPass.getText().equals("")){
                    jlTitle.setText("Register Field Empty");
                }else {
                    try {
                        out.writeUTF("register");
                        out.flush();
                    }catch (IOException error){
                        error.printStackTrace();
                    }
                    register(connection, textUser.getText(), textPass.getText());
                    try{
                        accepted = in.readUTF();
                    }catch(IOException error){
                        error.printStackTrace();
                    }
                    if (accepted.equals("true")){
                        clientDir = new File(projectDir+"\\ClientDirectory\\" +clientID+"dir");
                        clientDir.mkdirs();
                        try {
                            File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
                            FileWriter w = new FileWriter(socialGraph, true);
                            if (socialGraph.length() == 0) {
                                w.write(clientID);
                                w.close();
                            } else {
                                w.write("\n" + clientID);
                                w.close();
                            }
                        } catch (IOException error) {
                            error.printStackTrace();
                        }
                        jFrame.dispose();
                        clientGUI();
                    }else{
                        jlTitle.setText("User Already Registered");
                    }
                }
            }
        }));
    }

    /*GUI USED FOR INPUT IN ACCESS PROFILE BUTTON*/
    public void accessProfileGUI(){
        JFrame jFrame = new JFrame("Profile Finder");
        jFrame.setSize(250, 160);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("Insert Client Name");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpPanel = new JPanel();
        jpPanel.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpPanel.setSize(new Dimension(250, 50));


        JTextField text = new JTextField(20);

        JButton jbFindClient = new JButton("Find Profile");
        jbFindClient.setPreferredSize(new Dimension(40, 20));
        jbFindClient.setAlignmentX(Component.CENTER_ALIGNMENT);

        jbFindClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clientToFind = text.getText();
                if(!access_profile(clientToFind)){
                    jlTitle.setText("Client not found/not following");
                }else{
                    jFrame.dispose();
                }

            }
        });



        jFrame.add(jlTitle);
        jFrame.add(jpPanel);
        jpPanel.add(text);
        jFrame.add(jbFindClient);
        jFrame.setVisible(true);
        jFrame.pack();
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);



    }


    /*FINDS PROFILE.TXT AND CALLS DOWNLOAD FUNCTION*/
    /**
     * @param clientToFind
     * @return
     */
    public Boolean access_profile(String clientToFind){

        try {
            File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
            Scanner socialGraphReader = new Scanner(socialGraph);
            boolean followerFound = false;
            while(socialGraphReader.hasNextLine()){
                ArrayList<String> line = new ArrayList<>(Arrays.asList(socialGraphReader.nextLine().split(" ")));
                if (!line.get(0).equals(clientID)) continue;
                for (String follower : line) {
                    if (follower.equals(clientToFind)) {
                        followerFound = true;
                        break;
                    }
                }
                socialGraphReader.close();
            }

            if(!followerFound) return false;

            //if follower was found
            File profileTxt = null;
            out.writeUTF("accessProfile");
            out.flush();

            out.writeUTF(clientToFind);
            out.flush();

            int txtFileNameLength = in.readInt();
            String txtFileName = null;
            if (txtFileNameLength > 0) {
                byte[] imageFileNameBytes = new byte[txtFileNameLength];
                in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                txtFileName = new String(imageFileNameBytes);

                int txtFileContentLength = in.readInt();

                if (txtFileContentLength > 0) {
                    byte[] txtFileContentBytes = new byte[txtFileContentLength];
                    in.readFully(txtFileContentBytes, 0, txtFileContentLength);
                    /* READ TEXT FILE*/

                    profileTxt = new File(projectDir + "\\ClientDirectory\\" + clientID + "dir\\" + clientToFind + "Profile.txt");
                    FileOutputStream fileOutputStream = new FileOutputStream(profileTxt);

                    fileOutputStream.write(txtFileContentBytes);
                    fileOutputStream.close();
                }

            }
            ArrayList<String> files = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(profileTxt));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] splitted = line.split("\\s+");
                    files.add(splitted[2]);
                }


            for (String file : files) {
                download(clientToFind, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // display profile contents
        // when user deletes the display window those profile files are deleted

        return true;
    }

    /*ESTABLISHES CONNECTION WITH THE SERVER*/
    public void connect(){
        try {
            connection = new Socket("127.0.0.1", 1337);
            listenerFollow = new Socket("127.0.0.1", 1337);
            System.out.println("Connected to Server.");
            out = new DataOutputStream(connection.getOutputStream());
            in = new DataInputStream(connection.getInputStream());
            follow_in = new DataInputStream(listenerFollow.getInputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectInputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*STORES AN IMAGE LOCALLY, THEN SENDS IT TO THE SERVER FOR SYNCHRONIZATION*/
    public void sendImage(File[] imageFile,File[] txtFile){
        try {
            try{
                Files.copy(imageFile[0].toPath(),(new File(clientDir.toPath() +"\\" + imageFile[0].getName())).toPath());
            }catch(IOException error){
                error.printStackTrace();
            }
            /*SEND IMAGE FILE*/
            FileInputStream imageFileInputStream = new FileInputStream(imageFile[0].getAbsoluteFile());

            String imageFileName = imageFile[0].getName();
            byte[] imageFileNameBytes = imageFileName.getBytes();

            byte[] imageFileContentBytes = new byte[(int) imageFile[0].length()];
            imageFileInputStream.read(imageFileContentBytes);

            out.writeInt(imageFileNameBytes.length);
            out.flush();
            out.write(imageFileNameBytes);
            out.flush();

            out.writeInt(imageFileContentBytes.length);
            out.flush();
            out.write(imageFileContentBytes);
            out.flush();

            /*SEND TEXT FILE*/
            FileInputStream txtFileInputStreamTxt = new FileInputStream(txtFile[0].getAbsoluteFile());

            String txtFileName = txtFile[0].getName();
            byte[] txtFileNameBytes = txtFileName.getBytes();

            byte[] txtFileContentBytes = new byte[(int) txtFile[0].length()];
            txtFileInputStreamTxt.read(txtFileContentBytes);

            out.writeInt(txtFileNameBytes.length);
            out.flush();
            out.write(txtFileNameBytes);
            out.flush();

            out.writeInt(txtFileContentBytes.length);
            out.flush();
            out.write(txtFileContentBytes);
            out.flush();
        }catch (IOException error) {
            error.printStackTrace();
        }

        File profile_txt = new File(projectDir + "\\ClientDirectory\\"+clientID+"dir\\Profile_1000"+clientID+".txt");
        try{
            FileWriter w = new FileWriter(profile_txt, true);
            w.write(clientID + " posted " +txtFile[0].getName() + "\n");
            w.write(clientID + " posted " +imageFile[0].getName() + "\n");
            w.close();

            objectOutputStream.writeObject(followers);
        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    /*SENDS CLIENT DATA TO SERVER FOR LOGIN*/
    public void login(Socket connection, String username, String password){

        try{
            out.writeUTF(username);
            out.flush();
            clientID = username;
            System.out.println(clientID+ "'s terminal");

            out.writeUTF(password);
            out.flush();

        }catch(IOException error){
            error.printStackTrace();
        }

    }

    /*SENDS CLIENT DATA TO SERVER FOR REGISTER*/
    public void register(Socket connection, String username, String password){

        try{
            out.writeUTF(username);
            out.flush();
            clientID = username;
            System.out.println(clientID+ "'s terminal");

            out.writeUTF(password);
            out.flush();

        }catch(IOException error){
            error.printStackTrace();
        }

    }

    public void downloadGUI(){
        JFrame jFrame = new JFrame("Image Downloader");
        jFrame.setSize(250, 160);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("Insert Image Name");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpPanel = new JPanel();
        jpPanel.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpPanel.setSize(new Dimension(250, 50));


        JTextField text = new JTextField(20);

        JButton jbDownloadImage = new JButton("Download");
        jbDownloadImage.setPreferredSize(new Dimension(40, 20));
        jbDownloadImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jpPanel);
        jpPanel.add(text);
        jFrame.add(jbDownloadImage);
        jFrame.setVisible(true);
        jFrame.pack();
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);

        jbDownloadImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(text.getText().equals("")){
                    jlTitle.setText("Empty Field");
                }else{
                    try {
                        out.writeUTF("imageSearch");
                        out.flush();
                    }catch(IOException error){
                        error.printStackTrace();
                    }
                    String imageToDownload = text.getText();
                    ArrayList<String> clientsWithImage = search(imageToDownload);
                    if(imgFound.equals("Image not found")){
                        jlTitle.setText("Image not found");
                    }else {
                        jFrame.dispose();

                        if(clientsWithImage.size() == 1){
                            download(clientsWithImage.get(0), imageToDownload);
                        }
                        else{
                            int random = new Random().nextInt(clientsWithImage.size()-1);
                            download(clientsWithImage.get(random), imageToDownload);
                        }
                    }
                }
            }
        });

    }

    /*RETURNS CLIENTS WITH A SPECIFIC IMAGE IN THEIR DIRECTORY*/
    public ArrayList<String> search(String Photo_name){
        ArrayList<String> resultSearch = new ArrayList<>();
        try {
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            out.writeUTF(Photo_name);
            out.flush();
            objectOutputStream.writeObject(following);
            objectOutputStream.flush();
            imgFound = in.readUTF();
            if(imgFound.equals("Found")){
                resultSearch = (ArrayList<String>)objectInputStream.readObject();
            }
        }catch(IOException error){
            error.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return resultSearch;
    }

    /*GUI USED FOR INPUT IN DOWNLOAD BUTTON*/
    public void searchGUI(){
        JFrame jFrame = new JFrame("Image Search");
        jFrame.setSize(250, 200);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("Insert Image Name");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpPanel = new JPanel();
        jpPanel.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpPanel.setSize(new Dimension(250, 50));

        JTextArea textNonEdit = new JTextArea();

        JTextField text = new JTextField(20);

        JButton jbSearchImage = new JButton("Search");
        jbSearchImage.setPreferredSize(new Dimension(40, 20));
        jbSearchImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(textNonEdit);
        jFrame.add(jpPanel);
        jpPanel.add(text);
        jFrame.add(jbSearchImage);
        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);
        textNonEdit.setEditable(false);

        jbSearchImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(text.getText().equals("")){
                    jlTitle.setText("Empty Field");
                }else{
                    try {
                        out.writeUTF("imageSearch");
                        out.flush();
                    }catch(IOException error){
                        error.printStackTrace();
                    }
                    String imageToSearch = text.getText();
                    ArrayList<String> clientsWithImage = search(imageToSearch);
                    if(clientsWithImage.size() == 0){
                        jlTitle.setText("Image not found");
                    }else {
                        for(String s: clientsWithImage){
                            textNonEdit.append(s + " ");
                        }
                    }
                }
            }
        });

    }

    /*DOWNLOADS AN IMAGE AFTER FINDING THE CLIENTS THAT HAVE IT*/
    public void download(String client, String imageToDownload){
        try{
            // 3-Way handshake
            int SEQy;
            int SEQx = new Random().nextInt(2000);

            out.writeUTF("download");
            out.flush();

            out.writeInt(1);
            out.flush();
            out.writeInt(SEQx);
            out.flush();

            System.out.println("SYN=" + in.readInt() + ",ACK=" + in.readInt());
            System.out.println("-----------------");
            int ACKx = in.readInt();
            SEQy = in.readInt();
            System.out.println("ACKx=" + ACKx + ",SEQy=" +SEQy);
            System.out.println("-----------------");
            out.writeInt(++SEQy);
            out.flush();
            out.writeInt(1);
            out.flush();

            out.writeUTF(client);
            out.flush();
            out.writeUTF(imageToDownload);
            out.flush();
            // End of 3-Way handshake

            try {
                int imageFileNameLength = in.readInt();
                String imageFileName = null;
                if (imageFileNameLength > 0) {
                    byte[] imageFileNameBytes = new byte[imageFileNameLength];
                    in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                    imageFileName = new String(imageFileNameBytes);

                    ArrayList<Byte> constructImage = new ArrayList<Byte>();

                    for(int i = 1; i <= 10; i++){
                        int chunkLength = in.readInt();
                        System.out.println(chunkLength);
                        if (chunkLength > 0) {
                            byte[] chunkContentBytes = new byte[chunkLength];
                            in.readFully(chunkContentBytes, 0, chunkLength);
                            for(int j = 0; j < chunkContentBytes.length; j++){
                                constructImage.add(chunkContentBytes[j]);
                            }
                        }
                    }
                    System.out.println(constructImage.size());
                    byte[] finishedImage = new byte[constructImage.size()];
                    for(int j = 0; j < constructImage.size(); j++){
                        finishedImage[j] = constructImage.get(j);
                    }
                    File imageFile = new File(projectDir + "\\ClientDirectory\\" + clientID + "dir\\" + imageFileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

                   fileOutputStream.write(finishedImage);
                   fileOutputStream.close();
                }
                int txtFileNameLength = in.readInt();
                String txtFileName = null;
                if (txtFileNameLength > 0) {
                    byte[] imageFileNameBytes = new byte[txtFileNameLength];
                    in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                    txtFileName = new String(imageFileNameBytes);

                    int txtFileContentLength = in.readInt();

                    if (txtFileContentLength > 0) {
                        byte[] txtFileContentBytes = new byte[txtFileContentLength];
                        in.readFully(txtFileContentBytes, 0, txtFileContentLength);

                        File txtFile = new File(projectDir + "\\ClientDirectory\\" + clientID + "dir\\" + txtFileName);
                        FileOutputStream fileOutputStream = new FileOutputStream(txtFile);

                        fileOutputStream.write(txtFileContentBytes);
                        fileOutputStream.close();

                    }else{
                        File txtFile = new File(projectDir + "\\ClientDirectory\\" + clientID + "dir\\" + txtFileName);
                        txtFile.createNewFile();
                    }
                }

            } catch (IOException error) {
                error.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*USES THE SOCIAL GRAPH TO DETERMINE CLIENTS FOLLOWERS*/
    public void setFollowers(){
        try {
            File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
            BufferedReader br = new BufferedReader(new FileReader(socialGraph));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] splitted = line.split("\\s+");
                if (splitted[0].equals(clientID)){
                    for(int i = 1; i < splitted.length; i++){
                        followers.add(splitted[i]);
                    }
                }
            }
        }catch (IOException error){
            error.printStackTrace();
        }
    }

    /*USES THE SOCIAL GRAPH TO DETERMINE CLIENTS FOLLOWING*/
    public void setFollowing(){
        try {
            File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
            BufferedReader br = new BufferedReader(new FileReader(socialGraph));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] splitted = line.split("\\s+");
                for(int i = 1; i < splitted.length; i++){
                    if(splitted[i].equals(clientID)){
                        following.add(splitted[0]);
                    }
                }
            }
        }catch (IOException error){
            error.printStackTrace();
        }
    }

    /*REMOVES EXTENSION FROM A FILE*/
    public String removeExtension(File[] file){
        String fileName = file[0].getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1)) { // If '.' is not the first or last character.
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    /*GUI USED FOR INPUT IN UNFOLLOW BUTTON*/
    public void unfollowGUI(){
        JFrame jFrame = new JFrame("Unfollow CLient");
        jFrame.setSize(250, 350);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("Insert Client to Unfollow");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpFollowing = new JPanel();
        jpFollowing.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpFollowing.setSize(new Dimension(250, 50));

        JPanel jpPanel = new JPanel();
        jpPanel.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpPanel.setSize(new Dimension(250, 50));

        JTextArea clientsText = new JTextArea(20, 20);
        for(String f: following){
            clientsText.append(f + " ");
        }
        JTextField text = new JTextField(20);

        JButton jbUnfollow = new JButton("Unfollow");
        jbUnfollow.setPreferredSize(new Dimension(40, 20));
        jbUnfollow.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jpFollowing);
        jFrame.add(jpPanel);
        jpPanel.add(text);
        jpFollowing.add(clientsText);
        jFrame.add(jbUnfollow);
        clientsText.setEditable(false);
        clientsText.setLineWrap(true);
        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);

        jbUnfollow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<ArrayList<String>> rawFile;
                String clientToUnfollow = text.getText();
                boolean flag = false;
                for(String f: following){
                    if(f.equals(clientToUnfollow)){
                        following.remove(clientToUnfollow);
                        System.out.println("Unfollowed "+clientToUnfollow+".");
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    jlTitle.setText("Wrong Client");
                }
                if(flag){
                    try {
                        out.writeUTF("unfollow");
                        out.flush();
                        rawFile = new ArrayList<>();
                        File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
                        Scanner s = new Scanner(socialGraph);
                        while(s.hasNextLine()) {
                            ArrayList<String> splitted = new ArrayList<>(Arrays.asList(s.nextLine().split("\\s+")));
                            if(splitted.get(0).equals(clientToUnfollow)){
                                for(int i = 1; i < splitted.size(); i++){
                                    if(clientID.equals(splitted.get(i))){
                                        splitted.remove(i);
                                        break;
                                    }
                                }
                            }
                            rawFile.add(splitted);
                        }
                        System.out.println(rawFile);
                        objectOutputStream.writeObject(rawFile);
                        objectOutputStream.flush();
                    }catch (IOException error){
                        error.printStackTrace();
                    } catch (ConcurrentModificationException error){
                        error.printStackTrace();
                    }
                    jlTitle.setText("Unfollowed");
                }
            }
        });
    }

    /*GUI USED FOR INPUT IN FOLLOW BUTTON*/
    public void followGUI(){
        JFrame jFrame = new JFrame("F "+clientID);
        jFrame.setSize(250, 160);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel("Insert Client to Follow");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpPanel = new JPanel();
        jpPanel.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpPanel.setSize(new Dimension(250, 50));

        JTextField text = new JTextField(20);

        JButton jbFollow = new JButton("Follow");
        jbFollow.setPreferredSize(new Dimension(40, 20));
        jbFollow.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jpPanel);
        jpPanel.add(text);
        jFrame.add(jbFollow);
        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);

        jbFollow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clientToFollow = text.getText();
                boolean flag = false;
                for(String  f: following){
                    if(f.equals(clientToFollow)){
                        jlTitle.setText("Already Following");
                        flag = true;
                        break;
                    }
                }
                if(clientToFollow.equals(clientID)){
                    flag = true;
                    jlTitle.setText("Bro that's you. Are you drunk?");
                }
                if(!flag){
                    try{
                        out.writeUTF("follow");
                        out.flush();
                        out.writeUTF(clientToFollow);
                        out.flush();
                        String result = in.readUTF();
                        if(result.equals("User offline")){
                            jlTitle.setText(result);
                        }else{
                            jlTitle.setText(result);
                        }

                    }catch(IOException error){
                        error.printStackTrace();
                    }
                }
            }
        });
    }

    /*GUI THAT POPS UP WHEN WE GET A FOLLOW REQUEST*/
    public void acceptFollowGUI(String client){
        JFrame jFrame = new JFrame(clientID+" accept follow");
        jFrame.setSize(350, 150);
        jFrame.setLayout((new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS)));
        jFrame.setDefaultCloseOperation((JFrame.DISPOSE_ON_CLOSE));

        JLabel jlTitle = new JLabel(client + " wants to follow you");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        jlTitle.setBorder(new EmptyBorder(5, 0 , 5 ,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(10, 10 , 10 ,10));
        jpButtons.setSize(new Dimension(150, 50));

        JButton jbAccept = new JButton("Accept");
        jbAccept.setPreferredSize(new Dimension(80, 40));
        jbAccept.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jbDecline = new JButton("Decline");
        jbDecline.setPreferredSize(new Dimension(80, 40));
        jbDecline.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jpButtons);
        jpButtons.add(jbAccept);
        jpButtons.add(jbDecline);
        jFrame.setVisible(true);
        jFrame.setResizable(false);
        jFrame.setLocationRelativeTo(null);

        jbAccept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<ArrayList<String>> rawFile;
                try {
                    System.out.println("Allowed "+client+" to follow you.");
                    followers.add(client);
                    out.writeUTF("acceptedFollow");
                    out.flush();
                    out.writeUTF(client);
                    out.flush();
                    jFrame.dispose();
                    rawFile = new ArrayList<>();
                    File socialGraph = new File(projectDir + "\\src\\SocialGraph.txt");
                    Scanner s = new Scanner(socialGraph);
                    while(s.hasNextLine()) {
                        ArrayList<String> splitted = new ArrayList<>(Arrays.asList(s.nextLine().split("\\s+")));
                        if(splitted.get(0).equals(clientID)){
                            splitted.add(client);
                        }
                        rawFile.add(splitted);
                    }
                    objectOutputStream.writeObject(rawFile);
                    objectOutputStream.flush();
                }catch (IOException error){
                    error.printStackTrace();
                } catch (ConcurrentModificationException error){
                    error.printStackTrace();
                }
            }
        });

        jbDecline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rejected " + client + "'s request to follow you.");
                jFrame.dispose();
            }
        });
    }

    /*THREAD THAT ALWAYS LISTENS FOR A FOLLOW*/
    public void listenForFollow(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(listenerFollow.isConnected()) {
                    try {
                        String msg = follow_in.readUTF();
                        if (msg.equals("Requesting follow")) {
                            String client = follow_in.readUTF();
                            acceptFollowGUI(client);
                        } else if (msg.equals("update")) {
                            System.out.println("updating...");
                            String clientIfollowed = follow_in.readUTF();
                            System.out.println("You are now following "+clientIfollowed+"!");
                            following.add(clientIfollowed);
                        }
                    } catch (IOException error) {
                        error.printStackTrace();
                    }
                }
            }
        }).start();
    }
}





