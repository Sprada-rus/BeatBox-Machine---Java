package bin;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.*;


public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList;
    Sequencer sequencer;
    Sequence seq;
    Track track;
    JFrame theFrame;

    String[] instrumentName = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom","High Bongo",
    "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instrument = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }

    public void buildGui(){
        theFrame = new JFrame("BeatBox Machine");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Старт");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Остановить");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTemp = new JButton("+ Темп");
        upTemp.addActionListener(new MyUpTempListener());
        buttonBox.add(upTemp);

        JButton downTemp = new JButton("- Темп");
        downTemp.addActionListener(new MyDownTempListener());
        buttonBox.add(downTemp);

        JButton clearCheckBox = new JButton("Очистить");
        clearCheckBox.addActionListener(new MyClearCheckBoxListener());
        buttonBox.add(clearCheckBox);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem saveFile = new JMenuItem("Сохранить");
        JMenuItem loadFile = new JMenuItem("Загрузить");

        saveFile.addActionListener(new SaveFileListener());
        loadFile.addActionListener(new LoadFileListener());

        fileMenu.add(saveFile);
        fileMenu.add(loadFile);
        menuBar.add(fileMenu);

        theFrame.setJMenuBar(menuBar);
        theFrame.getContentPane().add(BorderLayout.NORTH, background);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++){
            nameBox.add(new Label(instrumentName[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++){
            JCheckBox check = new JCheckBox();
            check.setSelected(false);
            checkBoxList.add(check);
            mainPanel.add(check);
        }

        setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    public void setUpMidi(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            seq = new Sequence(Sequence.PPQ, 4);
            track = seq.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart(){
//        int[] trackList = null;

        seq.deleteTrack(track);
        track = seq.createTrack();

        for (int i = 0; i < 16; i++){
            int[] trackList = new int[16];

            int key = instrument[i];

            for (int j = 0; j < 16; j++){
                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16 * i));
                if (jc.isSelected()){
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            makeTrack(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }

        try {
            sequencer.setSequence(seq);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {e.printStackTrace();}
    }

    private class SaveFileListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
        JFileChooser fileSave = new JFileChooser();
        fileSave.showSaveDialog(theFrame);
        saveFile(fileSave.getSelectedFile());

        sequencer.stop();
        }
    }

    private class LoadFileListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fileOpen = new JFileChooser();
            fileOpen.showOpenDialog(theFrame);
            loadFile(fileOpen.getSelectedFile());

            sequencer.stop();
            buildTrackAndStart();
        }
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }

    public class MyUpTempListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }

    public class MyClearCheckBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            for (int i = 0; i < 256; i++){
                JCheckBox check = checkBoxList.get(i);
                check.setSelected(false);
                checkBoxList.set(i,check);
            }
        }
    }

    public void loadFile(File nameFile){
        boolean[] checkState = null;

        try{
            FileInputStream fileStream = new FileInputStream(new File(String.valueOf(nameFile)));
            ObjectInputStream is = new ObjectInputStream(fileStream);
            checkState = (boolean[]) is.readObject();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        for (int i = 0; i < 256; i++){
            JCheckBox checkBox = (JCheckBox) checkBoxList.get(i);
            if (checkState[i]){
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
        }
    }

    public void saveFile(File nameFile){
        boolean[] checkState = new boolean[256];

        for (int i = 0; i < 256; i++){
            JCheckBox checkBox = (JCheckBox) checkBoxList.get(i);
            if (checkBox.isSelected()){
                checkState[i] = true;
            }
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(new File(String.valueOf(nameFile)));
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(checkState);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void makeTrack(int[] list){
        for (int i = 0; i < 16; i++){
            int key = list[i];

            if (key != 0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}
