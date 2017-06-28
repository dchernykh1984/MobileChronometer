package hello;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class HelloMIDlet extends MIDlet implements CommandListener {

    private Display display;
    private Form formResults;
    private Command commandNextCompetitorID;
    private TextField textFieldNextCompetitor;
    private TextField textFieldResultsLogList;
    private FileConnection fileConnectionResults;
    private OutputStream outputStreamResults;

    private Form formGroups;
    private Command commandStartGroup;
    private TextField textFieldGroup;
    private TextField textFieldGroupsLogList;
    private FileConnection fileConnectionGroup;
    private OutputStream outputStreamGroup;

    private Form formSelectFiles;
    private StringItem stringItemFilesPath;
    private ChoiceGroup choiceGroupNextFolder;
    private Command commandNextFolderSelect;
    private Command commandSaveFilesPaths;
    private TextField textFieldResultsFileName;
    private TextField textFieldGroupFileName;

    private Form formOtherSettings;
    private ChoiceGroup choiceGroupFinishCompetitors;
    private ChoiceGroup choiceGroupUseSummerTimeShifting;
    private TextField textFieldDSQ;
    private TextField textFieldGroupsStartDeliminer;
    private TextField textFieldNumberOfElementsInLog;
    private Command commandDSQ;

    private Command commandShowResults;
    private Command commandShowGroup;
    private Command commandShowFilesSelection;
    private Command commandShowOtherSettings;
    private Command commandExit; // The exit command

    private Form formConfirmation;
    private ChoiceGroup choiceGroupConfirmation;
    private Command commandConfirm;
    private Command commandToConfirm;
    private Form formBeforeConfirmation;

    private void addLogEvent(String eventDescription, TextField logField, OutputStream outputStream) {
        logField.setString(eventDescription + "\n" + logField.getString());
        checkLengthOfLog(logField, Integer.parseInt(textFieldNumberOfElementsInLog.getString()),outputStream);
    }
    private void checkLengthOfLog(TextField logField, int maxLinesNumber, OutputStream outputStream) {
        int i = 0;
        if (maxLinesNumber > 100) {
            maxLinesNumber = 100;
            textFieldNumberOfElementsInLog.setString("100");
        }
        for(i = 0;i<logField.getString().length();i++) {
            if(maxLinesNumber == 0) {
                break;
            }
            if(logField.getString().charAt(i) == '\n') {
                maxLinesNumber--;
            }
        }
        if(!writeToFile(logField.getString().substring(i), outputStream)) {
            Alert myAlert = new Alert("Unable to write data to file", "Possibly one of output files was not opened", null, AlertType.ERROR);
            myAlert.setTimeout(2000);
            Displayable currentDisplayable = display.getCurrent();
            display.setCurrent(myAlert, currentDisplayable);

        }
        logField.setString(logField.getString().substring(0, i));
   }

    public HelloMIDlet() {
        display = Display.getDisplay(this);
    }

    private void initializeSTCForms() {
        commandExit = new Command("Exit", Command.EXIT, 0);
        commandShowResults = new Command("Results", Command.EXIT, 0);
        commandShowOtherSettings = new Command("Settings", Command.EXIT, 0);

        commandShowResults = new Command("Results", Command.EXIT, 0);
        commandShowGroup = new Command("Groups", Command.EXIT, 0);
        commandShowFilesSelection = new Command("Select Files", Command.EXIT, 0);
        commandShowOtherSettings = new Command("Settings", Command.EXIT, 0);
        commandExit = new Command("Exit", Command.EXIT, 0);

        formResults = new Form("Results");
        commandNextCompetitorID = new Command("Next", Command.OK, 1);
        textFieldNextCompetitor = new TextField("Current ID", "", 256, TextField.NUMERIC);
        textFieldResultsLogList = new TextField("Actions Log", "", 9999, TextField.ANY);
        formResults.append(textFieldNextCompetitor);
        formResults.append(textFieldResultsLogList);
        formResults.addCommand(commandNextCompetitorID);
        formResults.addCommand(commandExit);
        formResults.addCommand(commandShowOtherSettings);
        formResults.addCommand(commandShowGroup);
        formResults.addCommand(commandShowResults);
        formResults.addCommand(commandShowFilesSelection);
        formResults.setCommandListener(this);

        formGroups = new Form("Group");
        commandStartGroup = new Command("Start", Command.OK, 1);
        textFieldGroup = new TextField("Group", "", 256, TextField.ANY);
        textFieldGroupsLogList = new TextField("Actions Log", "", 9999, TextField.ANY);
        formGroups.append(textFieldGroup);
        formGroups.append(textFieldGroupsLogList);
        formGroups.addCommand(commandStartGroup);
        formGroups.addCommand(commandExit);
        formGroups.addCommand(commandShowOtherSettings);
        formGroups.addCommand(commandShowGroup);
        formGroups.addCommand(commandShowResults);
        formGroups.addCommand(commandShowFilesSelection);
        formGroups.setCommandListener(this);

        formSelectFiles = new Form("Select Files");
        stringItemFilesPath = new StringItem("File Path", "file:///");
        textFieldResultsFileName = new TextField("Results Times File", "results.txt", 255, TextField.ANY);
        textFieldGroupFileName = new TextField("Groups Times File", "group.txt", 255, TextField.ANY);
        choiceGroupNextFolder = new ChoiceGroup("Next Folder", Choice.EXCLUSIVE);
        commandNextFolderSelect = new Command("Set Folder", Command.OK, 0);
        commandSaveFilesPaths = new Command("Set Paths", Command.OK, 0);
        formSelectFiles.addCommand(commandNextFolderSelect);
        formSelectFiles.addCommand(commandSaveFilesPaths);
        formSelectFiles.addCommand(commandExit);
        formSelectFiles.addCommand(commandShowOtherSettings);
        formSelectFiles.addCommand(commandShowGroup);
        formSelectFiles.addCommand(commandShowResults);
        formSelectFiles.addCommand(commandShowFilesSelection);
        formSelectFiles.append(choiceGroupNextFolder);
        formSelectFiles.append(stringItemFilesPath);
        formSelectFiles.append(textFieldResultsFileName);
        formSelectFiles.append(textFieldGroupFileName);
        formSelectFiles.setCommandListener(this);


        Enumeration e = FileSystemRegistry.listRoots();
        while (e.hasMoreElements()) {
            String nextString = (String) e.nextElement();
            choiceGroupNextFolder.append(nextString, null);
        }


        formOtherSettings = new Form("Settings");
        commandDSQ = new Command("DSQ", Command.OK, 1);
        textFieldDSQ = new TextField("DSQ", "", 255, TextField.NUMERIC);
        textFieldGroupsStartDeliminer = new TextField("Simultaneous groups start deliminer", ",", 255, TextField.ANY);
        textFieldNumberOfElementsInLog = new TextField("Max Number of Elements in Log(less than 100)", "50", 255, TextField.NUMERIC);
        choiceGroupFinishCompetitors = new ChoiceGroup("Finish Competitors", Choice.EXCLUSIVE);
        choiceGroupFinishCompetitors.append("Yes", null);
        choiceGroupFinishCompetitors.append("No", null);
        choiceGroupFinishCompetitors.setSelectedIndex(1, true);
        choiceGroupUseSummerTimeShifting = new ChoiceGroup("Use Summer Time Shifting", Choice.EXCLUSIVE);
        choiceGroupUseSummerTimeShifting.append("Yes", null);
        choiceGroupUseSummerTimeShifting.append("No", null);
        choiceGroupUseSummerTimeShifting.setSelectedIndex(1, true);
        formOtherSettings.append(choiceGroupFinishCompetitors);
        formOtherSettings.append(choiceGroupUseSummerTimeShifting);
        formOtherSettings.append(textFieldDSQ);
        formOtherSettings.append(textFieldGroupsStartDeliminer);
        formOtherSettings.append(textFieldNumberOfElementsInLog);
        formOtherSettings.addCommand(commandDSQ);
        formOtherSettings.addCommand(commandExit);
        formOtherSettings.addCommand(commandShowOtherSettings);
        formOtherSettings.addCommand(commandShowGroup);
        formOtherSettings.addCommand(commandShowResults);
        formOtherSettings.addCommand(commandShowFilesSelection);
        formOtherSettings.setCommandListener(this);

        formConfirmation = new Form("Are you sure?");
        commandConfirm = new Command("Confirm", Command.OK, 0);
        choiceGroupConfirmation = new ChoiceGroup("Are you sure", Choice.EXCLUSIVE);
        choiceGroupConfirmation.append("Yes", null);
        choiceGroupConfirmation.append("No", null);
        formConfirmation.addCommand(commandConfirm);
        formConfirmation.append(choiceGroupConfirmation);
        formConfirmation.setCommandListener(this);

    }

    private String getCurrentTime() {
        Calendar calend = Calendar.getInstance();
        long millisecondsInit = calend.getTime().getTime() + calend.getTimeZone().getRawOffset();
        if(choiceGroupUseSummerTimeShifting.isSelected(0) && calend.getTimeZone().useDaylightTime()) {
            millisecondsInit+=3600000;
        }
        long additionalVar = millisecondsInit / 1000;
        long millisec = millisecondsInit - additionalVar * 1000;
        millisecondsInit = additionalVar;
        additionalVar = millisecondsInit / 60;
        long seconds = millisecondsInit - additionalVar * 60;
        millisecondsInit = additionalVar;
        additionalVar = millisecondsInit / 60;
        long minutes = millisecondsInit - additionalVar * 60;
        millisecondsInit = additionalVar;
        additionalVar = millisecondsInit / 24;
        long hours = millisecondsInit - additionalVar * 24;
        return additionalVar + " " + hours + ":" + minutes + ":" + seconds + "." + millisec;

    }

    public void startApp() {
        initializeSTCForms();
        display.setCurrent(formSelectFiles);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
        if (c == commandExit) {
            formBeforeConfirmation = (Form)display.getCurrent();
            commandToConfirm = commandExit;
            choiceGroupConfirmation.setSelectedIndex(1, true);
            display.setCurrent(formConfirmation);
        } else if (c == commandConfirm) {
            if(commandToConfirm == commandExit) {
                if(choiceGroupConfirmation.getSelectedIndex() == 0) {
                    closeFileStreams();
                    destroyApp(false);
                    notifyDestroyed();
                } else {
                    display.setCurrent(formBeforeConfirmation);
                }
            } else {
                display.setCurrent(formBeforeConfirmation);
            }

        } else if (c == commandShowGroup) {
            display.setCurrent(formGroups);
        } else if (c == commandShowResults) {
            display.setCurrent(formResults);
        } else if (c == commandShowFilesSelection) {
            display.setCurrent(formSelectFiles);
        } else if (c == commandShowOtherSettings) {
            display.setCurrent(formOtherSettings);

        } else if (c == commandNextCompetitorID) {
            String stringToWrite = getCurrentTime();
            if(choiceGroupFinishCompetitors.isSelected(0)) {
                stringToWrite = textFieldNextCompetitor.getString() + "#" + stringToWrite + "#finish#";
            } else {
                stringToWrite = textFieldNextCompetitor.getString() + "#" + stringToWrite + "#nextLap#";
            }
            textFieldNextCompetitor.setString("");
            addLogEvent(stringToWrite, textFieldResultsLogList, outputStreamResults);
        } else if (c == commandStartGroup) {
            String stringToWrite;
            String groupsStartTime = getCurrentTime();
            while(textFieldGroup.getString().indexOf(textFieldGroupsStartDeliminer.getString())>=0) {
                stringToWrite = textFieldGroup.getString().substring(0, textFieldGroup.getString().indexOf(textFieldGroupsStartDeliminer.getString()))+ "#" + groupsStartTime + "#";
                addLogEvent(stringToWrite, textFieldGroupsLogList, outputStreamGroup);
                textFieldGroup.setString(textFieldGroup.getString().substring(textFieldGroup.getString().indexOf(textFieldGroupsStartDeliminer.getString())+textFieldGroupsStartDeliminer.getString().length()));
            }
            stringToWrite = textFieldGroup.getString() + "#" + groupsStartTime + "#";
            addLogEvent(stringToWrite, textFieldGroupsLogList, outputStreamGroup);
            textFieldGroup.setString("");
        } else if (c == commandNextFolderSelect) {
            selectNextFolder();
        } else if (c == commandSaveFilesPaths) {
            openFiles();
        } else if (c == commandDSQ) {
            String stringToWrite = getCurrentTime();
                stringToWrite = textFieldDSQ.getString() + "#" + stringToWrite + "#DSQ#";
                textFieldDSQ.setString("");
                addLogEvent(stringToWrite, textFieldResultsLogList, outputStreamResults);
        }
    }

    private boolean writeToFile(String stringsToWrite, OutputStream outputStream) {
        if (outputStream == null) {
            return false;
        }
        try {
            if(stringsToWrite==null) {
                return true;
            }
            while(stringsToWrite.indexOf('\n')>0) {
                String nextStringToWrite = stringsToWrite.substring(stringsToWrite.lastIndexOf('\n')).replace('\n', ' ').trim();
                stringsToWrite = stringsToWrite.substring(0, stringsToWrite.lastIndexOf('\n'));
                if(!nextStringToWrite.equalsIgnoreCase("")) {
                    nextStringToWrite = nextStringToWrite + "\n";
                    outputStream.write(nextStringToWrite.getBytes());
                }
            }
            if(!stringsToWrite.equalsIgnoreCase("")) {
                stringsToWrite = stringsToWrite + "\n";
                outputStream.write(stringsToWrite.getBytes());
            }
        } catch (IOException ex) {
            Alert myAlert = new Alert("Error", "IOException: \"" + ex.getMessage() + "\"", null, AlertType.ERROR);
            myAlert.setTimeout(5000);
            Displayable currentDisplayable = display.getCurrent();
            display.setCurrent(myAlert, currentDisplayable);
            return false;
        }
        return true;
    }

    private void selectNextFolder() {
        if (choiceGroupNextFolder.getSelectedIndex() == -1) {
            Alert myAlert = new Alert("Error","Next folder not selected",null,AlertType.ALARM);
            myAlert.setTimeout(2000);
            Displayable currentElement = display.getCurrent();
            display.setCurrent(myAlert, currentElement);
            return;
        }
        String nextFolder = choiceGroupNextFolder.getString(choiceGroupNextFolder.getSelectedIndex());
        if (nextFolder.equalsIgnoreCase("..")) {
            stringItemFilesPath.setText(stringItemFilesPath.getText().substring(0, stringItemFilesPath.getText().lastIndexOf('/',stringItemFilesPath.getText().length()-2))+"/");
        } else if(!nextFolder.endsWith("/")) {
            return;
        } else {
            stringItemFilesPath.setText(stringItemFilesPath.getText() + nextFolder);
        }
        int numberOfChoiceElements = choiceGroupNextFolder.size();
        for (int i = 0; i < numberOfChoiceElements; i++) {
            choiceGroupNextFolder.delete(0);
        }
        if (stringItemFilesPath.getText().equalsIgnoreCase("file:///")) {
            Enumeration e = FileSystemRegistry.listRoots();
            while (e.hasMoreElements()) {
                String nextString = (String) e.nextElement();
                choiceGroupNextFolder.append(nextString, null);
            }
        } else {
            Enumeration tempEnum;
            try {
                FileConnection tempFC = (FileConnection) Connector.open(stringItemFilesPath.getText());
                tempEnum = tempFC.list();
            } catch (IOException ex) {
                Alert myAlert = new Alert("Error", "IOException: \"" + ex.getMessage() + "\"", null, AlertType.ERROR);
                myAlert.setTimeout(5000);
                Displayable currentDisplayable = display.getCurrent();
                display.setCurrent(myAlert, currentDisplayable);
                return;
            }
            choiceGroupNextFolder.append("..", null);
            while (tempEnum.hasMoreElements()) {
                choiceGroupNextFolder.append((String) tempEnum.nextElement(), null);
            }
        }

    }

    private void openFiles() {
        try {
            closeFileStreams();
            fileConnectionResults = (FileConnection) Connector.open(stringItemFilesPath.getText() + textFieldResultsFileName.getString());
            if (!fileConnectionResults.exists()) {
                fileConnectionResults.create();
            }
            outputStreamResults = fileConnectionResults.openOutputStream();
            fileConnectionGroup = (FileConnection) Connector.open(stringItemFilesPath.getText() + textFieldGroupFileName.getString());
            if (!fileConnectionGroup.exists()) {
                fileConnectionGroup.create();
            }
            outputStreamGroup = fileConnectionGroup.openOutputStream();
            writeToFile(textFieldGroupsLogList.getString(), outputStreamGroup);
        } catch (IOException ex) {
            Alert myAlert = new Alert("Error", "IOException: \"" + ex.getMessage() + "\"", null, AlertType.ERROR);
            myAlert.setTimeout(5000);
            Displayable currentDisplayable = display.getCurrent();
            display.setCurrent(myAlert, currentDisplayable);
        }
   }

     private void closeFileStreams() {
        try {
            if (fileConnectionResults != null) {
                checkLengthOfLog(textFieldResultsLogList, 0, outputStreamResults);
                outputStreamResults.close();
                fileConnectionResults.close();
            }
            if (fileConnectionGroup != null) {
                checkLengthOfLog(textFieldGroupsLogList, 0, outputStreamGroup);
                outputStreamGroup.close();
                fileConnectionGroup.close();
            }

        } catch (IOException ex) {
            Alert myAlert = new Alert("Error", "IOException: \"" + ex.getMessage() + "\"", null, AlertType.ERROR);
            myAlert.setTimeout(5000);
            Displayable currentDisplayable = display.getCurrent();
            display.setCurrent(myAlert, currentDisplayable);
        }
    }
}
