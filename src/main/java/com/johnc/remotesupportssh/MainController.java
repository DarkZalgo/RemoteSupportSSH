package com.johnc.remotesupportssh;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
//import jdk.internal.net.http.common.Log;
import javafx.scene.control.TextArea;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MainController {

    private static InputStream ptyInput;

    private static OutputStream ptyOut;

    SSHClient sshClient = new SSHClient();

    @FXML
    private TextArea curTextArea, sendTextArea;

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void displayClockCodes() throws IOException {
        Path path = Paths.get("C:\\ClockCodes");
        Set<String> uniqueCodes = new HashSet<>();
        Dictionary<String, ClockCode> codeDict = new Hashtable<>();
        List<ClockCode> clockCodes = getClockCodes(path);
//        List<JCheckBox> checkboxes = new ArrayList<>();
        clockCodes.sort((code1, code2) -> code2.getDate().compareTo(code1.getDate()));
        for (ClockCode code : clockCodes) {
//            if ( !krXtBox.isSelected() && code.getReaderName().matches("0012E5[0-9A-Fa-f]{6}|80E26660[0-9A-Fa-f]{4}"))
//            {
//                continue;
//            } else if (krXtBox.isSelected() && !code.getReaderName().matches("0012E5[0-9A-Fa-f]{6}|80E26660[0-9A-Fa-f]{4}")) {
//                continue;
//            }
            if (!uniqueCodes.contains(code.getReaderName())) {

                uniqueCodes.add(code.getReaderName());
                codeDict.put(code.getReaderName(), code);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm a");
//                checkboxes.add(new JCheckBox(code.getReaderName()+" --- "+sdf.format(code.getDate()) + " MST"));

            }
        }
    }

    public List<ClockCode> getClockCodes(Path path) throws IOException {
        List<ClockCode> result;
        try (Stream<Path> walk = Files.walk(path)) {


            result = walk.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().split("-").length > 1)
                    .map(file -> {
                        ClockCode code = new ClockCode();
                        code.setPath(file.toAbsolutePath());
                        String[] fileNameSplit = new String(file.getFileName().toString().getBytes(), StandardCharsets.ISO_8859_1).split("-");
                        try {
                            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                            code.setDate(new Date(attributes.creationTime().toMillis()));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        String fileNameRes = "";
                        if (fileNameSplit.length > 4) {
                            for (int i = 0; i < fileNameSplit.length; i++) {
                                if (i < fileNameSplit.length - 3) {
                                    fileNameRes += fileNameSplit[i];
                                    if (i + 1 < fileNameSplit.length - 3) {
                                        fileNameRes += "-";
                                    }
                                } else if (i == fileNameSplit.length - 2) {
                                    code.setSshCode(fileNameSplit[i]);
                                } else if (i == fileNameSplit.length - 1) {
                                    code.setVncCode(fileNameSplit[i]);
                                }

                            }
                        } else {
                            fileNameRes = fileNameSplit[0];
                            code.setSshCode(fileNameSplit[2]);
                            code.setVncCode(fileNameSplit[3]);

                        }
                        code.setReaderName(fileNameRes);
                        return code;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

    @FXML
    private void createPTYConnection(ActionEvent event) throws IOException {
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {


//        sshClient.addHostKeyVerifier(new ConsoleKnownHostsVerifier(new File(""),System.console()));
               sshClient.addHostKeyVerifier(new PromiscuousVerifier());
               sshClient.connect("192.168.4.52", 3735);


                try {
                    sshClient.authPassword("root", "$ynEL401515".toCharArray());
//            sshClient.authPublickey(System.getProperty("user.name"));
                    final Session session = sshClient.startSession();
                    try {
                        session.allocateDefaultPTY();
                        final Session.Shell shell = session.startShell();
                        ptyInput = shell.getInputStream();
                        ptyOut = shell.getOutputStream();

//                        new StreamCopier( ptyInput, ptyOut, LoggerFactory.DEFAULT)
//                                .bufSize(shell.getLocalMaxPacketSize())
//                                .spawn("stdout");
//
//                        new StreamCopier(shell.getErrorStream(), System.err, LoggerFactory.DEFAULT)
//                                .bufSize(shell.getLocalMaxPacketSize())
//                                .spawn("stderr");
//
//
//                        new StreamCopier(ptyInput, ptyOut, LoggerFactory.DEFAULT)
//                                .bufSize(shell.getLocalMaxPacketSize())
//                                .copy();

                        bgTask.run();

//                        new StreamCopier( ptyInput, System.out, LoggerFactory.DEFAULT)
//                                .bufSize(shell.getLocalMaxPacketSize())
//                                .spawn("stdout");
//
//                        new StreamCopier(shell.getErrorStream(), System.err, LoggerFactory.DEFAULT)
//                                .bufSize(shell.getLocalMaxPacketSize())
//                                .spawn("stderr");
//
//
                        new StreamCopier(System.in, ptyOut, LoggerFactory.DEFAULT)
                                .bufSize(shell.getLocalMaxPacketSize())
                                .copy();

                        StringProperty textProperty = curTextArea.textProperty();
                        textProperty.addListener((observable,oldValue, newValue)-> {
                            curTextArea.selectPositionCaret(curTextArea.getLength());
                            curTextArea.deselect();
                        });
                      bgTask.messageProperty().addListener((observable,oldValue, newValue)-> {
                            curTextArea.selectPositionCaret(curTextArea.getLength());
                            curTextArea.deselect();
                        });
                    } catch (Exception e) {

                    } finally {
                        session.close();
                    }
                } catch (
                        Exception e) {

                } finally {
                    MainController.this.sshClient.disconnect();
                }
                return null;
            }
        };
        Executors.newSingleThreadExecutor().execute(task);
    }

    private void doStuff(){
        bgTask.messageProperty().addListener((observable,oldValue, newValue)->{
            curTextArea.selectPositionCaret(curTextArea.getLength());
            curTextArea.deselect();
        });
    }

    @FXML
    private void sendMsg(ActionEvent event) throws IOException {
        System.out.println("sending: "+sendTextArea
                .getText() + "\n");
        PrintWriter pw = new PrintWriter(ptyOut);
       pw.write(
                sendTextArea
                        .getText() + "\n");
       pw.flush();
    }
    Task<Void> bgTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            boolean notGoingToHappen = false;
//            InputStream inputStream = process.getInputStream();
            while(true) {
                StringBuilder consoleContent = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(ptyInput))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        consoleContent
                                .append(line)
                                .append("\n");
                        updateMessage(consoleContent.toString());
                        System.out.println(consoleContent.toString().replace("[\\d;\\d\\dm",""));
                        System.out.println(consoleContent.toString());
                        Platform.runLater(() -> {
                            curTextArea.appendText(consoleContent.toString().replaceAll("\\[\\d;\\d{1,2}m|\\[\\dm",""));
                        });

                        if(notGoingToHappen)
                            return null;
                        // trigger ChangeListener

                    }
                } catch (IOException e) {
                    System.out.println("");
                }
            }

        }

    };
}