package com.prog3.mailserver;

import com.prog3.common.model.Email;
import com.prog3.common.model.MailBox;
import com.prog3.mailserver.controller.ControllerPrimary;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static final int port = 4445;
    private ServerSocket s = null;
    private ControllerPrimary controllerPrimary = null;
    private List<MailBox> account = new ArrayList<>();
    public static int idCounter = 0;
    public ArrayList<String> fileNames = new ArrayList<>();
    private static ArrayList<ServerThread> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(3);

    public void setControllerPrimary(ControllerPrimary c) {
        controllerPrimary = c;
    }

    /**
     * This method increments idCounter, a shared counter variable that allows each email to have a different ID.
     */
    public synchronized void incrementIdCounter(){
        idCounter++;
    }

    /**
     * Method to read the emails present in the email.txt file
     */
    private void readFile() {
        InputStream ioStream = Main.class.getResourceAsStream("data/email.txt");
        try (InputStreamReader isr = new InputStreamReader(ioStream); BufferedReader br = new BufferedReader(isr);) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                String name = line.substring(0, line.indexOf(","));
                String email = line.substring(line.indexOf(",") + 1);
                MailBox accountToInsert = new MailBox();
                accountToInsert.setName(name);
                accountToInsert.setEmail(email);
                account.add(i,accountToInsert);
                fileNames.add(i,email);
                i++;
            }
            ioStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method allows you to read from files in mutual exclusion. To do this, we use an ArrayList of strings called
     * "fileName" which stores the three email addresses inside. By cycling with a for and using the keyword
     * "synchronized" you access the file writing in mutual exclusion.
     */
    private void readIncomingEmail() {
        for (MailBox mailbox : account) {
            mailbox.getListReceivedEmails().clear();
            int i;
            for(i = 0; i<fileNames.size(); i++){
                if(mailbox.getEmail().equals(fileNames.get(i))){
                    break;
                }
            }
            try {
                synchronized (fileNames.get(i)){
                    File emailFile = new File("incoming_emails/" + fileNames.get(i) + ".txt");
                    if(emailFile.exists()){
                        BufferedReader reader = new BufferedReader(new FileReader(emailFile));
                        String line = reader.readLine();
                        while (line != null && line != "" && line!="\n" && line!=" ") {
                            Email e = Email.fromString(line);
                            mailbox.getListReceivedEmails().add(e);
                            line = reader.readLine();
                            idCounter = Math.max(e.getID(),idCounter);
                        }
                        reader.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method allows you to write to files in mutual exclusion. To do this, we use an ArrayList of strings called
     * "fileName" which stores the three email addresses inside. By cycling with a for and using the keyword
     * "synchronized" you access the file writing in mutual exclusion.
     *
     * @param e
     * @param email
     * @throws FileNotFoundException
     */
    public void writeToFile(String e, Email email) throws FileNotFoundException {
        int i;
        for(i = 0; i<fileNames.size(); i++){
            if(e.equals(fileNames.get(i))){
                break;
            }
        }
        synchronized (fileNames.get(i)){
            PrintWriter writer = new PrintWriter(new FileOutputStream("incoming_emails/" + fileNames.get(i) + ".txt", true));
            writer.println(email.toString());
            writer.flush();
            writer.close();
        }
    }

    /**
     * This method allows you to perform the delete in mutual exclusion. To do this, we use an ArrayList of strings called
     * "fileName" which stores the three email addresses inside. By cycling with a for and using the keyword
     * "synchronized" you access the file writing in mutual exclusion.
     *
     * @param email
     * @param id
     */
    public void deleteEmail(String email, int id) {
        int i;
        for(i = 0; i<fileNames.size(); i++){
            if(email.equals(fileNames.get(i))){
                break;
            }
        }
        synchronized (fileNames.get(i)){
            try {
                List<String> lines = new ArrayList<>();
                File emailFile = new File("incoming_emails/" + fileNames.get(i) + ".txt");
                BufferedReader reader = new BufferedReader(new FileReader(emailFile));
                String line = reader.readLine();
                while (line != null) {
                    Email e = Email.fromString(line);
                    if (e.getID() != id) {
                        lines.add(line);
                    }
                    line = reader.readLine();
                }
                reader.close();
                FileWriter writer = new FileWriter(emailFile);
                for (String l : lines) {
                    writer.write(l + System.lineSeparator());
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method waits for one or more clients to connect and connects when a client wakes up.
     * @throws IOException
     */
    synchronized public void activate() throws IOException {
        readFile();
        readIncomingEmail();
        new Thread(() -> {
            try {
                s = new ServerSocket(port);
                while (true) {
                    Socket s1 = s.accept();
                    ServerThread st1 = new ServerThread(s1,controllerPrimary,account,this);
                    pool.execute(st1);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
