package com.prog3.mailserver;

import com.prog3.common.model.*;
import com.prog3.mailserver.controller.ControllerPrimary;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ServerThread extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ControllerPrimary controllerPrimary;
    private List<MailBox> accounts;
    private Server server;

    /**
     * This method initializes threads.
     *
     * @param socket
     * @param controllerPrimary
     * @param accounts
     * @param server
     * @throws IOException
     */
    public ServerThread(Socket socket, ControllerPrimary controllerPrimary, List<MailBox> accounts, Server server) throws IOException {
        this.socket = socket;
        this.controllerPrimary = controllerPrimary;
        this.accounts = accounts;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.server = server;

    }

    /**
     * This method allows connection between client and server.
     */
    @Override
    public void run() {
        System.out.println("Connected");
        while (true) {
            try {
                // Receive the request from the client
                Object request = in.readObject();
                // Manage the request
                Object response = handleRequest(request);
                // Send the response to the client
                out.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                break;
            }
            // Close connection and input/output streams
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method manages the logfile in such a way that every consistent action of the client is printed in the
     * server view. The actions that are reported in the log file are the following: login, sending an email,
     * deleting an email, reporting the receipt of a new email, verifying a new email, closing a connection.
     *
     * @param request
     * @return
     * @throws IOException
     */
    private Object handleRequest(Object request) throws IOException {
        if (request instanceof LoginRequest) {
            return getMailBox((LoginRequest) request);
        } else if (request instanceof SendEmailRequest) {
            return sendEmail((SendEmailRequest) request);
        } else if (request instanceof ReceivedEmailRequest) {
            return receivedEmail((ReceivedEmailRequest) request);
        } else if (request instanceof DeleteEmailRequest) {
            return deleteEmail((DeleteEmailRequest) request);
        }  else if (request instanceof CheckNewEmailsRequest) {
            return getNewEmails((CheckNewEmailsRequest) request);
        } else if (request instanceof String) {
            controllerPrimary.printLog("Connection closed for " + (String) request);
        }
        return null;
    }

    private MailBox getMailBox(LoginRequest request) {
        LoginRequest loginRequest = request;
        String email = loginRequest.getEmail();
        // Check if the email exists
        for (MailBox mailBox : accounts) {
            if (mailBox.getEmail().equals(email)) {
                controllerPrimary.printLog("Connection opened for " + email);
                return mailBox;
            }
        }
        // The email address was not found: returns null
        return null;
    }

    private boolean sendEmail(SendEmailRequest request) {
        SendEmailRequest sendEmailRequest = request;
        server.incrementIdCounter();
        Email email = sendEmailRequest.getEmail();
        email.setID(Server.idCounter);

        // Find all the recipients of the email
        List<MailBox> recipients = findRecipients(email.getReceiver());
        // Find the wrong (non-existing) recipients of the email
        List<String> nonExistingRecipients = findNonExistingRecipients(email.getReceiver());

        if (!recipients.isEmpty() || !nonExistingRecipients.isEmpty()) {
            // For every non-existing email send to the sender a failure email
            for (String e : nonExistingRecipients){
                List<String> receiver = new ArrayList<>();
                receiver.add(email.getSender());
                Email errorSenderEmail = new Email("MailSystem@prog3.com",
                        receiver,
                        server.idCounter,
                        "Delivery Status Notification (Failure)",
                        "The response was 550 5.1.1 The email account that you tried to reach (" + e + ") does not exist.",
                        LocalDateTime.now());

                // Write the failure email to the file
                try {
                    server.writeToFile(email.getSender(),errorSenderEmail);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                // Add the failure email to the mailbox
                for(int i = 0; i < accounts.size(); i++) {
                    if(accounts.get(i).getEmail().equals(email.getSender())){
                        synchronized (accounts.get(i)){
                            accounts.get(i).getListReceivedEmails().add(errorSenderEmail);
                        }
                    }
                }
            }

            // Print log
            List<String> receivers = email.getReceiver();
            for (MailBox account : accounts) {
                if (account.getEmail().equals(email.getSender())) {
                    if(!nonExistingRecipients.isEmpty()) {
                        controllerPrimary.printLog("Email sent from " + email.getSender() + " to " + nonExistingRecipients.toString() + ": failed");
                    }
                    for(String x : nonExistingRecipients){
                        receivers.remove(x);
                    }
                    if(receivers!=null && receivers.size()>=1)
                        controllerPrimary.printLog("Email sent from " + email.getSender() + " to " + receivers);

                }

            }

            // Add the email to each recipient's mailbox and write the email to the file
            for (MailBox recipient : recipients) {
                email.setReceiver(receivers);
                recipient.getListReceivedEmails().add(email);
                try {
                    server.writeToFile(recipient.getEmail(),email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private Object receivedEmail(ReceivedEmailRequest request) {
        ReceivedEmailRequest forwardEmailRequest = request;
        String email = forwardEmailRequest.getEmail();
        controllerPrimary.printLog("Email received by " + email);
        return null;
    }

    private Object deleteEmail(DeleteEmailRequest request) {
        DeleteEmailRequest deleteEmailRequest = request;
        String email = deleteEmailRequest.getEmail();
        int id = deleteEmailRequest.getIdEmail();
        server.deleteEmail(email, id);
        for (MailBox account : accounts) {
            if (account.getEmail().equals(email)) {
                for (int i = 0; i < account.getListReceivedEmails().size(); i++) {
                    if (account.getListReceivedEmails().get(i).getID() == id) {
                        account.getListReceivedEmails().remove(i);
                    }
                }
                controllerPrimary.printLog("Email deleted from " + deleteEmailRequest.getEmail());
                return true;
            }
        }
        return false;
    }

    private List<Email> getNewEmails(CheckNewEmailsRequest request) {
        CheckNewEmailsRequest checkNewEmailsRequest = request;
        String email = checkNewEmailsRequest.getEmail();
        int lastEmailId = checkNewEmailsRequest.getLastEmailId();
        for (MailBox account : accounts) {
            synchronized (account){
                if (account.getEmail().equals(email)) {
                    boolean x = false;
                    List<Email> newEmails = new ArrayList<>();
                    for (int i = 0; i < account.getListReceivedEmails().size(); i++) {
                        if (account.getListReceivedEmails().get(i).getID() > lastEmailId) {
                            newEmails.add(account.getListReceivedEmails().get(i));
                            x = true;
                        }
                    }
                    if (x == false) return null;
                    return newEmails;
                }
            }
        }
        return null;
    }

    /**
     * This method is used to find the email recipients while sending the email to the recipient.
     *
     * @param emails
     * @return
     */
    private List<MailBox> findRecipients(List<String> emails) {
        List<MailBox> recipients = new ArrayList<>();
        for (String email : emails) {
            MailBox recipient = findRecipient(email);
            if (recipient != null) {
                recipients.add(recipient);
            }
        }
        return recipients;
    }

    private MailBox findRecipient(String email) {
        for (MailBox mailBox : accounts) {
            if (mailBox.getEmail().equals(email)) {
                return mailBox;
            }
        }
        return null;
    }

    /**
     * This method is used to find the non-existing email recipients while sending the email to the recipient.
     *
     * @param emails
     * @return
     */
    private List<String> findNonExistingRecipients(List<String> emails){
        List<String> recipients = new ArrayList<>();
        for (String email : emails) {
            String recipient = findNonExistingRecipient(email);
            if (recipient != null) {
                recipients.add(recipient);
            }
        }
        return recipients;
    }

    private String findNonExistingRecipient(String email){
        for (MailBox mailBox : accounts) {
            if (mailBox.getEmail().equals(email)) {
                return null;
            }
        }
        return email;
    }
}
