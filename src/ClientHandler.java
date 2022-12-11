import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread{
    Socket connection;
    Socket listenerFollow;
    Scanner s;
    FileWriter w;
    String clientID;
    String password;
    String action;
    DataInputStream in;
    DataOutputStream out;
    DataOutputStream follow_out;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    int imageFileNameLength;
    int txtFileNameLength;
    String projectDir = Paths.get("").toAbsolutePath().toString();
    File loginInfoFile = new File(projectDir + "\\ServerDirectory\\LoginInfo.txt");
    File serverDir;

    ConcurrentHashMap<String, String> logininfo;

    public ClientHandler(Socket connection, Socket listenerFollow) {
        this.connection = connection;
        this.listenerFollow = listenerFollow;
        try {
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            follow_out = new DataOutputStream(listenerFollow.getOutputStream());
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public void run() {
        while (connection.isConnected()) {
            try {
                action = in.readUTF();
            } catch (IOException error) {
                error.printStackTrace();
            }
            if (action.equals("login")) {
                try {
                    clientID = in.readUTF();
                    password = in.readUTF();
                } catch (IOException error) {
                    error.printStackTrace();
                }
                login();
            } else if (action.equals("register")) {
                try {
                    clientID = in.readUTF();
                    password = in.readUTF();
                } catch (IOException error) {
                    error.printStackTrace();
                }
                register();
            } else if (action.equals("imageUpload")) {
                imageSynchronization();
            } else if (action.equals("imageSearch")) {
                imageSearch();
            }else if(action.equals("accessProfile")){
                getProfile();
                //wait for specific pictures
            } else if(action.equals("download")){
                answerDownload();
            }else if(action.equals("unfollow")){
                unfollow();
            } else if (action.equals(("follow"))) {
                follow();
            } else if (action.equals("acceptedFollow")){
                acceptedFollow();
            } else if (action.equals("Disconnected")){
                Server.activeClients.remove(clientID);
            }
            action = null;

        }
    }

    /*HANDLES LOGIN REQUEST*/
    public void login() {
        String accepted = "false";
        boolean flagOnline = false;
        boolean flagWrong = true;
        try {
            s = new Scanner(loginInfoFile);
        } catch (java.io.FileNotFoundException error) {
            error.printStackTrace();
        }

        logininfo = new ConcurrentHashMap<String, String>();
        while (s.hasNext()) {
            String c = s.next();
            String p = s.next();
            logininfo.put(c, p);
        }

        for (Map.Entry<String, String> entry : logininfo.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equals(clientID) && value.equals(password)) {
                if(!(Server.activeClients.isEmpty())){
                    for (Map.Entry<String,ClientHandler> entry1 : Server.activeClients.entrySet()) {
                        String key1 = entry1.getKey();
                        if (key1.equals(clientID)) {
                            try {
                                accepted = "online";
                                System.out.println("Request handled for " + clientID +": ALREADY ONLINE");
                                out.writeUTF(accepted);
                                out.flush();
                            } catch (IOException error) {
                                error.printStackTrace();
                            }
                            flagOnline = true;
                            break;
                        }
                    }
                    if(!flagOnline){
                        System.out.println("Request handled for " + clientID +": NOT ONLINE");
                        System.out.println("Welcome client " + clientID);
                        Server.activeClients.put(clientID,this);
                        try {
                            accepted = "true";
                            out.writeUTF(accepted);
                            out.flush();
                        } catch (IOException error) {
                            error.printStackTrace();
                        }
                    }
                }else {
                    System.out.println("Welcome client " + clientID);
                    Server.activeClients.put(clientID,this);
                    System.out.println("Request handled for " + clientID +": NO ONE ONLINE");
                    try {
                        accepted = "true";
                        out.writeUTF(accepted);
                        out.flush();
                    } catch (IOException error) {
                        error.printStackTrace();
                    }
                }
                flagWrong = false;
                break;
            }
            }
        if(flagWrong){
            try {
                System.out.println("Request handled for " + clientID +": WRONG INFO");
                accepted = "false";
                out.writeUTF(accepted);
                out.flush();
            } catch (IOException error) {
                error.printStackTrace();
            }

        }
    }

    /*HANDLES REGISTER REQUEST*/
    public void register () {
            String accepted = "true";
            try {
                s = new Scanner(loginInfoFile);
            } catch (java.io.FileNotFoundException error) {
                error.printStackTrace();
            }
            logininfo = new ConcurrentHashMap<String, String>();
            while (s.hasNext()) {
                String c = s.next();
                String p = s.next();
                logininfo.put(c, p);
            }

            for (Map.Entry<String, String> entry : logininfo.entrySet()) {
                String key = entry.getKey();
                if (key.equals(clientID)) {
                    accepted = "false";
                    break;
                }
            }
            if (accepted == "true") {
                try {
                    w = new FileWriter(loginInfoFile, true);
                    if (loginInfoFile.length() == 0) {
                        w.write(clientID + " " + password);
                        w.close();
                    } else {
                        w.write("\n" + clientID + " " + password);
                        w.close();
                    }
                    System.out.println("Registration of " + clientID + " succesful");
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
            try {
                out.writeUTF(accepted);
                out.flush();
            } catch (IOException error) {
                error.printStackTrace();
            }

            serverDir = new File(projectDir + "\\ServerDirectory\\" + clientID + "ServerDir");
            serverDir.mkdirs();
        }

    /*RECEIVES IMAGE AND STORES IT IN SERVER DIRECTORY*/
    public void imageSynchronization () {
        String imageFileName = null;
        String txtFileName = null;
        try {
            imageFileNameLength = in.readInt();
            if (imageFileNameLength > 0) {
                byte[] imageFileNameBytes = new byte[imageFileNameLength];
                in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                imageFileName = new String(imageFileNameBytes);

                int imageFileContentLength = in.readInt();

                if (imageFileContentLength > 0) {
                    byte[] imageFileContentBytes = new byte[imageFileContentLength];
                    in.readFully(imageFileContentBytes, 0, imageFileContentLength);
                    /* READ TEXT FILE*/

                    File imageFile = new File(projectDir + "\\ServerDirectory\\" + clientID + "ServerDir\\" + imageFileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

                    fileOutputStream.write(imageFileContentBytes);
                    fileOutputStream.close();


                }
            }
            txtFileNameLength = in.readInt();
            if (txtFileNameLength > 0) {
                byte[] imageFileNameBytes = new byte[txtFileNameLength];
                in.readFully(imageFileNameBytes, 0, imageFileNameBytes.length);
                txtFileName = new String(imageFileNameBytes);

                int txtFileContentLength = in.readInt();

                if (txtFileContentLength > 0) {
                    byte[] txtFileContentBytes = new byte[txtFileContentLength];
                    in.readFully(txtFileContentBytes, 0, txtFileContentLength);
                    /* READ TEXT FILE*/

                    File txtFile = new File(projectDir + "\\ServerDirectory\\" + clientID + "ServerDir\\" + txtFileName);
                    FileOutputStream fileOutputStream = new FileOutputStream(txtFile);

                    fileOutputStream.write(txtFileContentBytes);
                    fileOutputStream.close();


                }else{
                    File txtFile = new File(projectDir + "\\ServerDirectory\\" + clientID + "ServerDir\\" + txtFileName);
                    txtFile.createNewFile();
                }
            }

        } catch (IOException error) {
            error.printStackTrace();
        }

        File profile_txt = new File(projectDir + "\\ServerDirectory\\"+clientID+"ServerDir\\Profile_1000"+clientID+".txt");
        try{
            w = new FileWriter(profile_txt, true);
            w.write(clientID + " posted " +txtFileName + "\n" );
            w.write(clientID + " posted " +imageFileName + "\n" );
            w.close();

            ArrayList<String> clientfollowers = (ArrayList) objectInputStream.readObject();
            for (String c: clientfollowers){
                File others_profile = new File(projectDir + "\\ServerDirectory\\"+c+"ServerDir\\Others_1000"+c+".txt");
                w = new FileWriter(others_profile, true);
                w.write(clientID + " posted " +txtFileName + "\n" );
                w.write(clientID + " posted " +imageFileName + "\n" );
                w.close();

                File others_profile_clientside = new File(projectDir + "\\ClientDirectory\\"+c+"dir\\Others_1000"+c+".txt");
                w = new FileWriter(others_profile_clientside, true);
                w.write(clientID + " posted " +txtFileName + "\n" );
                w.write(clientID + " posted " +imageFileName + "\n" );
                w.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*SEARCHES FOR CLIENTS WITH SPECIFIC IMAGE IN THEIR DIRECTORY*/
    public void imageSearch () {
        try {
            System.out.println("Searching for Image");
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            String Photo_name = in.readUTF();
            ArrayList<String> following = (ArrayList<String>) objectInputStream.readObject();
            ArrayList<String> resultSearch = new ArrayList<>();
            boolean exists;
            for (String client : following) {
                exists = new File(projectDir + "\\ServerDirectory\\" + client + "ServerDir", Photo_name).exists();
                if (exists) {
                    resultSearch.add(client);
                }
            }

            if (resultSearch.isEmpty()) {
                out.writeUTF("Image not found");
                out.flush();
            } else {
                out.writeUTF("Found");
                out.flush();
                objectOutputStream.writeObject(resultSearch);
                objectOutputStream.flush();
            }

        } catch (IOException | ClassNotFoundException error) {
            error.printStackTrace();
        }


    }

    public void getProfile(){
        try {
            String clientToFind = in.readUTF();
            File clientFile = new File(projectDir + "\\ServerDirectory\\" + clientToFind + "ServerDir\\Profile_1000" + clientToFind + ".txt");
            final File[] txtFile = new File[1];
            txtFile[0] = clientFile;

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void answerDownload(){
        try{
            // 3-Way handshake
            int synRequest = in.readInt();
            int SEQx = in.readInt();

            System.out.println("SYN=" + synRequest + ",SEQx=" + SEQx);
            System.out.println("-----------------");

            out.writeInt(1);
            out.writeInt(1);
            out.writeInt(++SEQx);
            int SEQy = new Random().nextInt(2000);
            out.writeInt(SEQy);
            int ACKy = in.readInt();
            System.out.println("ACK="+in.readInt()+",ACKy="+ ACKy);
            System.out.println("-----------------");

            String clientName = in.readUTF();
            String fileName = in.readUTF();

            // End of 3-Way handshake

            String filePath = projectDir + "\\ServerDirectory\\" + clientName + "ServerDir\\" + fileName;
            File[] imageFile = new File[1];
            imageFile[0] = new File(filePath);
            File[] imageText = new File[1];
            imageText[0] = new File(projectDir + "\\ServerDirectory\\" + clientName + "ServerDir\\" + removeExtension(imageFile) + ".txt");

            File MyServerDir = new File( projectDir + "\\ServerDirectory\\" + clientID + "ServerDir");

            try {
                try{
                    Files.copy(imageFile[0].toPath(),(new File(MyServerDir.toPath() +"\\" + imageFile[0].getName())).toPath());
                }catch(IOException error){
                    error.printStackTrace();
                }
                //SEND IMAGE FILE/
                FileInputStream imageFileInputStream = new FileInputStream(imageFile[0].getAbsoluteFile());

                String imageFileName = imageFile[0].getName();
                byte[] imageFileNameBytes = imageFileName.getBytes();

                byte[] imageFileContentBytes = new byte[(int) imageFile[0].length()];
                imageFileInputStream.read(imageFileContentBytes);

                out.writeInt(imageFileNameBytes.length);
                out.flush();
                out.write(imageFileNameBytes);
                out.flush();
                //out.write(imageFileContentBytes.length);
                //out.flush();

                byte[] chunk;
                for(int i = 1; i <= 10; i++){

                    int first = ((i-1)*imageFileContentBytes.length)/10;
                    System.out.println(first);
                    int last =  ((i)*imageFileContentBytes.length)/10;
                    System.out.println(last);
                    chunk = new byte[(last-first)];
                    int counter = 0;
                    for(int j = first; j < last; j++){
                        chunk[counter] = imageFileContentBytes[j];
                        counter++;
                    }
                    out.writeInt(chunk.length);
                    out.flush();
                    out.write(chunk);
                    out.flush();
                }
                //SEND TEXT FILE/
                try{
                    Files.copy(imageText[0].toPath(),(new File(MyServerDir.toPath() +"\\" + imageText[0].getName())).toPath());
                }catch(IOException error){
                    error.printStackTrace();
                }

                FileInputStream txtFileInputStreamTxt = new FileInputStream(imageText[0].getAbsoluteFile());

                String txtFileName = imageText[0].getName();
                byte[] txtFileNameBytes = txtFileName.getBytes();

                byte[] txtFileContentBytes = new byte[(int) imageText[0].length()];
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


        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /*CONFIGURES SOCIAL GRAPH FOR UNFOLLOW*/
    public void unfollow(){
        ArrayList<ArrayList<String>> rawfile;
        try {
            rawfile = (ArrayList<ArrayList<String>>) objectInputStream.readObject();
            File socialGraph= new File(projectDir + "\\src\\SocialGraph.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(socialGraph));
            for(ArrayList<String> a: rawfile){
                for(String s: a){
                    writer.write(s + " ");
                }
                writer.write("\n");
            }
            writer.close();

        }catch(IOException | ClassNotFoundException error){
            error.printStackTrace();
        }
    }

    /*FINDS CLIENT'S CONNECTION WE WANT TO FOLLOW*/
    public void follow(){
        try{
            String clientToFollow = in.readUTF();
            boolean flag = false;
            for (Map.Entry<String, ClientHandler> entry : Server.activeClients.entrySet()) {
                String key = entry.getKey();
                ClientHandler value = entry.getValue();
                if(key.equals(clientToFollow)){
                    out.writeUTF("Sent request to follow");
                    out.flush();
                    Socket socketClientToRequest = value.listenerFollow;
                    DataOutputStream follow_out_other = new DataOutputStream(socketClientToRequest.getOutputStream());
                    follow_out_other.writeUTF("Requesting follow");
                    follow_out_other.flush();
                    follow_out_other.writeUTF(clientID);
                    follow_out_other.flush();
                    flag = true;
                    break;
                }
            }
            if(!flag){
                out.writeUTF("User offline");
                out.flush();
            }
        }catch (IOException error){
            error.printStackTrace();
        }
    }

    /*CONFIGURES SOCIAL GRAPH FOR FOLLOW*/
    public void acceptedFollow(){

            ArrayList<ArrayList<String>> rawfile;
            try {
                String clientIaccepted = in.readUTF();
                for (Map.Entry<String, ClientHandler> entry : Server.activeClients.entrySet()) {
                    String key = entry.getKey();
                    ClientHandler value = entry.getValue();
                    if(key.equals(clientIaccepted)){
                        Socket socketClientToRequest = value.listenerFollow;
                        DataOutputStream follow_out_other = new DataOutputStream(socketClientToRequest.getOutputStream());
                        follow_out_other.writeUTF("update");
                        follow_out_other.flush();
                        follow_out_other.writeUTF(clientID);
                        follow_out_other.flush();
                    }
                }
                rawfile = (ArrayList<ArrayList<String>>) objectInputStream.readObject();
                File socialGraph= new File(projectDir + "\\src\\SocialGraph.txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(socialGraph));
                for(ArrayList<String> a: rawfile){
                    for(String s: a){
                        writer.write(s + " ");
                    }
                    writer.write("\n");
                }
                writer.close();

            }catch(IOException | ClassNotFoundException error){
                error.printStackTrace();
            }
    }

    public String removeExtension(File[] file){
        String fileName = file[0].getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0 && pos < (fileName.length() - 1)) { // If '.' is not the first or last character.
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }
}
