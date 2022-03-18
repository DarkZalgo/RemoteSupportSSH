package com.johnc.remotesupportssh;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import jdk.internal.net.http.common.Log;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.ConsoleKnownHostsVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MainController {

    SSHClient  sshClient = new SSHClient();

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
            if(!uniqueCodes.contains(code.getReaderName())) {

                uniqueCodes.add(code.getReaderName());
                codeDict.put(code.getReaderName(), code);
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm a");
//                checkboxes.add(new JCheckBox(code.getReaderName()+" --- "+sdf.format(code.getDate()) + " MST"));

            }
        }
    }

    public List<ClockCode> getClockCodes(Path path) throws IOException
    {
        List<ClockCode> result;
        try (Stream<Path> walk = Files.walk(path)){


            result = walk.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().split("-").length >1)
                    .map(file ->{
                        ClockCode code = new ClockCode();
                        code.setPath(file.toAbsolutePath());
                        String [] fileNameSplit = new String (file.getFileName().toString().getBytes(), StandardCharsets.ISO_8859_1).split("-");
                        try {
                            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                            code.setDate(new Date(attributes.creationTime().toMillis()));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        String fileNameRes = "";
                        if (fileNameSplit.length > 4)
                        {
                            for (int i = 0; i < fileNameSplit.length; i++)
                            {
                                if (i < fileNameSplit.length-3) {
                                    fileNameRes+=fileNameSplit[i];
                                    if (i+1 < fileNameSplit.length-3 )
                                    {
                                        fileNameRes+="-";
                                    }
                                } else if (i == fileNameSplit.length-2)
                                {
                                    code.setSshCode(fileNameSplit[i]);
                                } else if (i == fileNameSplit.length-1)
                                {
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

    public void createPTYConnection(String ip) throws IOException {
        sshClient.addHostKeyVerifier(new ConsoleKnownHostsVerifier(new File(""),System.console()));

        sshClient.connect(ip);
        try {
            sshClient.authPublickey(System.getProperty("user.name"));
            final Session session = sshClient.startSession();
            try {
                session.allocateDefaultPTY();
                final Session.Shell shell = session.startShell();

                new StreamCopier(shell.getInputStream(), System.out, LoggerFactory.DEFAULT)
                        .bufSize(shell.getLocalMaxPacketSize())
                        .spawn("stdout");

                new StreamCopier(shell.getErrorStream(), System.err, LoggerFactory.DEFAULT)
                        .bufSize(shell.getLocalMaxPacketSize())
                        .spawn("stderr");


                new StreamCopier(System.in, shell.getOutputStream(), LoggerFactory.DEFAULT)
                        .bufSize(shell.getLocalMaxPacketSize())
                        .copy();

            } catch (Exception e) {

            } finally {
                session.close();
            }
        } catch(Exception e){

        } finally {
            sshClient.disconnect();
        }
    }

}