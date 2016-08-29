package hr.tvz.diplomski.gui;

import hr.tvz.diplomski.level.Level;
import hr.tvz.diplomski.level.Room;
import hr.tvz.diplomski.level.WallPositions;
import hr.tvz.diplomski.neural.LevelrMicrophones;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.PersistBasicNetwork;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Filip on 28.1.2016..
 */
public class Levelr extends JFrame implements ActionListener, MouseListener, Runnable {

    //Constants
    final static int TIMER_DIV = 1000000;
    private final JLabel netLoc;
    private final JLabel sndLoc;
    //private final JComboBox<String> dropDown;
   // private final JButton train;
   // private final JButton move;
    private JButton chooseDir;

    private   JButton resetButton;
    Canvas canvas;

    JSplitPane pane;
    JTextField xRooms, yRooms, wallNumber;
    JLabel roomGrid, numberOfWalls;
    JPanel levelPanel;
    JButton generateLevel, placeNet, placeSource;
    LevelrMicrophones network;
    Level level;

    int networkLocationX, networkLocationY, soundSourceX, soundSourceY, fps, delay, width, height, hwidth, hheight, riprad = 3, size, i, a, b;

    Image levelr, soundSource, baseLevel, image, offImage;
    MemoryImageSource source;
    boolean frozen = true;
    Graphics offGraphics;
    private int cellSize;
    private boolean sourceFound, placeNetwork, placeSoundSource;
    private boolean netFound;
    private boolean[][] walls;
    short ripplemap[], wave[][];
    int texture[];
    int ripple[];
    int oldind, newind, mapind;
    Thread animatorThread;
    int maxX = 0, maxY = 0, maxWave = 0, prevMaxX = 20, prevMaxY = 20;
    double[] networkInput;
    private Room networkCell;
    int whereTo;
    private int prevMaxWave;
    private BufferedImage copyImage;
    private Image backupLevel;
    PrintWriter writer ;

    public Levelr() {
        this.setTitle("Levelr");
        this.setLayout(new FlowLayout());
        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        /*try {
            writer = new PrintWriter("inputDirections2.in", "UTF-8");
            writer.println("createtraining:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        canvas = new Canvas();
        try {



            String netContent = "levelr" + ".cont";
            String netFile ="levelr"+ ".net";
            PersistBasicNetwork netPersistor = new PersistBasicNetwork();



           /* network = new LevelrMicrophones("inputDirections.in");
            FileOutputStream fos = new FileOutputStream(netFile);
            netPersistor.save(fos, network.getNetwork());
            BufferedWriter bw = new BufferedWriter(new FileWriter(
                    netContent));

            for (int i = 0; i < network.getNeuron2identity().size(); i++) {
                bw.write(i + ", " + network.getNeuron2identity().get(i) + " ");
                bw.newLine();
            }
            fos.close();
            bw.close();*/





            FileInputStream fis = new FileInputStream(new File(netFile));
            network = new LevelrMicrophones((BasicNetwork) netPersistor.read(fis));
            Map<Integer, Integer> neuron2identity = new HashMap<Integer, Integer>();
            final FileInputStream fstream = new FileInputStream(netContent);
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(in));
            String line;
            int key;
            int character;
            int pos = 0;
            while ((line = br.readLine()) != null) {
                pos = line.indexOf(",");
                key = Integer.parseInt(line.substring(0, pos));
                character = Character.getNumericValue(line.charAt(pos + 2));
                network.getNeuron2identity().put(key, character);
                neuron2identity.put(key, character);

            }

            in.close();


            //network = new LevelrNetwork();
            //System.out.println(network.getNetwork().getLayerCount());
            canvas.setImage(ImageIO.read(new File("levelBackground.png")));
            levelr = ImageIO.read(new File("network.png"));
            soundSource = ImageIO.read(new File("crosshair.png"));
//            SerializeObject.save(new File("levelr.net"),network.getNetwork());
        } catch (IOException e) {
            e.printStackTrace();
        }
        canvas.addMouseListener(this);
        pane.setTopComponent(canvas);

        levelPanel = new JPanel(new GridLayout(7, 2));

        roomGrid = new JLabel("The level is a 10x10 grid of rooms.");
        levelPanel.add(roomGrid);
        levelPanel.add(new JLabel(""));

        numberOfWalls = new JLabel("Number of walls");
        wallNumber = new JTextField(2);

        levelPanel.add(numberOfWalls);
        levelPanel.add(wallNumber);


        generateLevel = new JButton("Generate level");

        generateLevel.addActionListener(this);
        levelPanel.add(generateLevel);

        resetButton = new JButton("Reset level");
        resetButton.addActionListener(this);
        levelPanel.add(resetButton);
        resetButton.setEnabled(false);



        placeNet = new JButton("Place Network");
        placeNet.setEnabled(false);
        placeNet.addActionListener(this);
        levelPanel.add(placeNet);

        placeSource = new JButton("Place sound source");
        placeSource.setEnabled(false);
        placeSource.addActionListener(this);
        levelPanel.add(placeSource);

        netLoc = new JLabel("");
        levelPanel.add(netLoc);

        sndLoc = new JLabel("");
        levelPanel.add(sndLoc);

        /*dropDown = new JComboBox<>();


        dropDown.addItem("UP");
        dropDown.addItem("RIGHT");
        dropDown.addItem("DOWN");
        dropDown.addItem("LEFT");
        dropDown.addItem("DO NOT MOVE");
        levelPanel.add(dropDown);

        chooseDir = new JButton("Choose Direction");
        chooseDir.addActionListener(this);
        chooseDir.setEnabled(false);
        levelPanel.add(chooseDir);

        train = new JButton("train");
        train.addActionListener(this);
        train.setEnabled(true);
        levelPanel.add(train);

        move = new JButton("move");
        move.addActionListener(this);
        move.setEnabled(true);
        levelPanel.add(move);*/


        pane.setBottomComponent(levelPanel);

        

        /**/


        this.add(pane);
        this.setSize(850, 450);

        this.setVisible(true);
        fps = 60;
        delay = (1000 / fps);
    }



    void handleResize() {
        Dimension d = canvas.getSize();
        if (d.width == 0)
            return;
        int[] pixels = null;
    }

    //
    //IMPLEMENT NUMBER CHECK
    //
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateLevel) {

            canvas.resetImage();

            level = new Level();
            level.generateLevel(Integer.parseInt(wallNumber.getText()));

            canvas.setImage(level.generateLevelImage(canvas.getImage(), canvas.getImgSizeX(), canvas.getImgSizeY()));
            canvas.repaint();
            baseLevel = canvas.getImage().getScaledInstance(canvas.getImage().getWidth(null), -1, Image.SCALE_DEFAULT);
            backupLevel = canvas.getImage().getScaledInstance(canvas.getImage().getWidth(null), -1, Image.SCALE_DEFAULT);
            walls = level.getWalls();
            generateLevel.setEnabled(false);
            level.walls();
            /*for(int y = 0; y<walls[0].length;y++){
            for(int x = 0; x<walls.length;x++){

                    if(walls[y][x])
                        System.out.print("1,");
                    else
                        System.out.print("0,");
                }
                System.out.print("\n");
            }*/

            placeNet.setEnabled(true);
            placeSource.setEnabled(true);
            resetButton.setEnabled(true);
        }
        if (e.getSource() == placeNet) {
            placeNetwork = true;
        }
        if (e.getSource() == placeSource) {
            placeSoundSource = true;
        }
        if(e.getSource() == resetButton){

            try {
                canvas = new Canvas(ImageIO.read(new File("levelBackground.png")));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            canvas.resetImage();
            wallNumber.setText("");

            generateLevel.setEnabled(true);
            placeNet.setEnabled(true);
            placeSource.setEnabled(true);
            canvas.addMouseListener(this);
            pane.setTopComponent(canvas);
            animatorThread = null;
            frozen = true;
        }
        /*if (e.getSource() == chooseDir){
            short [][] inputWave = new short[40][40];
            for(int y = 0; y < inputWave.length; y++){
                for(int x = 0; x < inputWave[0].length ;x ++){
                    inputWave[y][x] = wave[networkLocationY- 20 + y][networkLocationX - 20 + x];
                }
            }
            //network.addInput(inputWave,networkCell.getWalls(), dropDown.getSelectedIndex());
            addInput(inputWave,networkCell.getWalls(), dropDown.getSelectedIndex());
            chooseDir.setEnabled(false);
            animatorThread.interrupted();
            frozen = false;

        }
        if (e.getSource() == train){
            //network.createTraining();
           // network.buildNetwork();
            //network.train();
            writer.println("Network: hidden1:100, hidden2:50");
            writer.close();
        }
        if (e.getSource() == move){
            switch (dropDown.getSelectedIndex()) {
                case 0:
                    networkLocationY -= level.getRoomSizeY();
                    break;
                case 1:
                    networkLocationX += level.getRoomSizeX();
                    break;
                case 2:
                    networkLocationY += level.getRoomSizeY();
                    break;
                case 3:
                    networkLocationX -= level.getRoomSizeX();
                default:
                    break;
            }
            offGraphics.drawImage(levelr, 4 + networkLocationX - level.getRoomSizeX() / 2, 4 + networkLocationY - level.getRoomSizeY() / 2, null);
            netLoc.setText("netX: " + networkLocationX + ", netY: " + networkLocationY);
        }*/


    }

    private void addInput(short[][] inputWave, double[] walls, int direction) {
        int sizeOfArray = inputWave.length * inputWave[0].length;
        double [] inputData = new double[sizeOfArray + 4];
        String toWrite ="Input: data:";
        int i = 0;
        for(int y = 0; y < inputWave.length; y++) {
            for (int x = 0; x < inputWave[0].length; x++) {
                toWrite += inputWave[y][x]+";";
            }
        }
        for(i = 0; i < walls.length; i++){
            toWrite += walls[i];
            if(i == walls.length-1)
                toWrite += ";";
            else
                toWrite += ", ";

        }
        toWrite += "identity:"+direction;
        writer.println(toWrite);

    }

    double round(int num, int multipleOf) {
        return Math.round((num + multipleOf / 2) / multipleOf) * multipleOf;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //Sets the source location to the center of the clicked cell

        if (placeNetwork) {


            networkLocationX = (int) round(e.getX(), level.getRoomSizeX() / 2);
            networkLocationY = (int) round(e.getY(), level.getRoomSizeY() / 2);
            System.out.println(networkLocationX);
            System.out.println(networkLocationY);
            //System.out.println(map(networkLocationX,0,400,0,10));
            //System.out.println(map(networkLocationY,0,400,0,10));
            networkCell = level.getRoomAtPoint(map(networkLocationY,0,400,0,10), map(networkLocationX,0,400,0,10));
            netLoc.setText("netX: " + networkLocationX + ", netY: " + networkLocationY);
            Graphics g = canvas.getImage().getGraphics();
            g.drawImage(levelr, 4 + networkLocationX - level.getRoomSizeX() / 2, 4 + networkLocationY - level.getRoomSizeY() / 2, null);
            g.dispose();
            canvas.setImage(canvas.getImage());
            canvas.repaint();
            placeNetwork = false;
            placeNet.setEnabled(false);

            //System.out.print(level.getWalls());
        }
        if (placeSoundSource) {

            soundSourceX = (int) round(e.getX(), level.getRoomSizeX() / 2);
            soundSourceY = (int) round(e.getY(), level.getRoomSizeY() / 2);
            sndLoc.setText("sndX: " + soundSourceX + ", sndY: " + soundSourceY);
            //canvas.setImage(canvas.getImage());

            MediaTracker mt = new MediaTracker(this);

            mt.addImage(baseLevel, 0);
            placeSoundSource = false;
            placeSource.setEnabled(false);

            width = baseLevel.getWidth(this);
            height = baseLevel.getHeight(this);
            hwidth = width >> 1;
            hheight = height >> 1;
            size = (width + 2) * (height + 2) * 2;
            ripplemap = new short[size];
            ripple = new int[width * height];

            texture = new int[width * height];
            oldind = width;
            newind = width * (height + 3);
            PixelGrabber pg = new PixelGrabber(baseLevel, 0, 0, width, height, texture, 0, width);
            source = new MemoryImageSource(width, height, ripple, 0, width);
            source.setAnimated(true);
            source.setFullBufferUpdates(true);


            image = createImage(source);
            offImage = createImage(width, height);
            offGraphics = offImage.getGraphics();
            try {
                pg.grabPixels();
            } catch (InterruptedException ex) {
            }
            disturb(soundSourceX, soundSourceY);
            if (frozen) {
                frozen = false;
                start();
            } else {
                frozen = true;
                animatorThread = null;
            }
        }
    }

    public void disturb(int dx, int dy) {
        for (int j = dy - riprad; j < dy + riprad; j++) {
            for (int k = dx - riprad; k < dx + riprad; k++) {
                if (j >= 0 && j < height && k >= 0 && k < width) {
                    ripplemap[oldind + (j * width) + k] += 1024;

                }

            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //not needed
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //not needed
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //not needed
    }


    public void start() {
        if (frozen) {
            //Do nothing.

        } else {
            //Start animation thread
            if (animatorThread == null) {
                animatorThread = new Thread(this);
            }
            animatorThread.start();
        }
    }

    public void stop() {
        if (frozen) {
            //Do nothing.
        } else {
            //Start animation thread
            if (animatorThread != null) {
                try {
                    animatorThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                animatorThread = null;
            }
        }
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        long startTime = System.currentTimeMillis();
        wave = new short[height][width];
        boolean firstIteration =true;
        while (Thread.currentThread() == animatorThread) {

            if(firstIteration){
                findMaxPoint(true);
                firstIteration = false;
            }
            newframe();
            findMaxPoint(false);
            source.newPixels();

            offGraphics.drawImage(image, 0, 0, width, height, null);

            offGraphics.drawImage(levelr, 4 + networkLocationX - level.getRoomSizeX() / 2, 4 + networkLocationY - level.getRoomSizeY() / 2, null);

            canvas.setImage(offImage);

           // frozen = true;
            //chooseDir.setEnabled(true);
            while (frozen){
                try {

                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
           // neuralField();
            neuralMicrophones();


            netLoc.setText("netX: " + networkLocationX + ", netY: " + networkLocationY);
            if(networkLocationY == soundSourceY && networkLocationX == soundSourceX){
                break;
            }
            try {

                startTime += delay;
                //Thread.sleep(1000);
                Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
                disturb(soundSourceX, soundSourceY);
            } catch (InterruptedException e) {
                break;
            }
            }



    }

    private void neuralMicrophones(){
        int xComponent, yComponent;
       // findMaxPoint();
        if (wave[networkLocationY][networkLocationX] != 1024) {
            /*maxWave = wave[networkLocationY - 20][networkLocationX - 20];
            for (int y = networkLocationY - 4; y < networkLocationY + 4; y++) {
                for (int x = networkLocationX - 20; x < networkLocationX + 20; x++) {
                    if (wave[y][x] > maxWave) {
                        maxX = x;
                        maxY= y;
                    }
                }
            }*/
            xComponent = maxX - prevMaxX;
            yComponent = maxY - prevMaxY;

            prevMaxX = maxX;
            prevMaxY = maxY;
            //yComponent > 0 wave is north
            //yComponent == 0 wave is east or west
            //yComponent < 0 wave is north

            //double array must be n,e,s,w,n,e,s,w

                if (yComponent < 0) {
                    if(xComponent<0)
                        networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    if(xComponent==0)
                        networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    if(xComponent>0)
                        networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                } else if (yComponent == 0) {
                    if (xComponent < 0)
                        networkInput = new double[]{1, 3, 2, 0,networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    else if (xComponent > 0)
                        networkInput = new double[]{1, 0, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};

                } else {
                    if(xComponent<0)
                        networkInput = new double[]{3, 0, 1, 2, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    if(xComponent==0)
                        networkInput = new double[]{3, 2, 0, 1, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    if(xComponent>0)
                        networkInput = new double[]{3, 2, 1, 0, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                }




            //0 south
            //1 west
            //2 east
            //3 north
            whereTo = network.whereTo(networkInput);
            switch (whereTo) {
                case 0:
                    if(!networkCell.isNorth())
                        networkLocationY -= level.getRoomSizeY();
                    break;
                case 1:
                    if(!networkCell.isEast())
                        networkLocationX += level.getRoomSizeX();
                    break;
                case 2:
                    if(!networkCell.isSouth())
                        networkLocationY += level.getRoomSizeY();
                    break;
                case 3: if(!networkCell.isWest())
                    networkLocationX -= level.getRoomSizeX();
                    break;

                default:
                    break;
            }
            networkCell = level.getRoomAtPoint(map(networkLocationX,0,400,0,9), map(networkLocationY,0,400,0,9));


        }
    }

    private void neuralField() {
        short [][] Wave = new short[8][8];
        double [] max = new double[8];
        double[][] inputWave = new double[8][8];
        double []input = new double[inputWave.length*inputWave[0].length+4];
        int countOfstatic = 0;
        for(int y = 0; y < Wave.length; y++){
            for(int x = 0; x < Wave[0].length ;x ++){
                Wave[y][x] = wave[networkLocationY- 4 + y][networkLocationX - 4 + x];
                //System.out.println(Wave[y][x]);
                if(Wave[y][x]==1024)
                    countOfstatic++;
            }
        }
        //System.out.println("\n");
        if(countOfstatic<32) {


            for (int y = 0; y < Wave.length; y++) {
                max[y] = Wave[y][0];

                for (int x = 0; x < Wave[0].length; x++) {
                    if (Wave[y][x] > max[y])
                        max[y] = Wave[y][x];
                }
            }


            for (int y = 0; y < Wave.length; y++) {
                for (int x = 0; x < Wave[0].length; x++) {
                    inputWave[y][x] = Wave[y][x] / max[y];
                }
            }
            int i = 0;
            for (int y = 0; y < Wave.length; y++) {
                for (int x = 0; x < Wave[0].length; x++) {
                    input[i++] = inputWave[y][x];
                }
            }

            if (networkCell.isNorth())
                input[i++] = 1;
            else
                input[i++] = 0;
            if (networkCell.isEast())
                input[i++] = 1;
            else
                input[i++] = 0;
            if (networkCell.isSouth())
                input[i++] = 1;
            else
                input[i++] = 0;
            if (networkCell.isWest())
                input[i++] = 1;
            else
                input[i++] = 0;
            for (int j = 0; j < input.length; j++)
                System.out.print(input[j] + ", ");
            System.out.print("\n");

            ////////////////////////
            //unravel wave append walls
            //////////////////////////
            whereTo = network.whereTo(input);
            System.out.println(whereTo);
            System.out.println(WallPositions.getValue(whereTo));
            switch (whereTo) {
                case 0:
                    if (networkCell.isNorth())
                        break;
                    networkLocationY += level.getRoomSizeY();
                    break;
                case 1:
                    if (networkCell.isEast())
                        break;
                    networkLocationX -= level.getRoomSizeX();
                    break;
                case 2:
                    if (networkCell.isWest())
                        break;
                    networkLocationX += level.getRoomSizeX();
                    break;
                case 3:
                    if (networkCell.isSouth())
                        break;
                    networkLocationY -= level.getRoomSizeY();
                default:
                    break;
            }
            networkCell = level.getRoomAtPoint(map(networkLocationY, 0, 400, 0, 10), map(networkLocationX, 0, 400, 0, 10));
            //checkForWave();
        }
    }


    private void findMaxPoint(boolean flag) {
        if(flag) {
            prevMaxWave = wave[networkLocationY-4][networkLocationX-4];
            for (int y = networkLocationY - 4; y < networkLocationY + 4; y++) {
                for (int x = networkLocationX - 4; x < networkLocationX + 4; x++) {
                    if (wave[y][x] > prevMaxWave) {
                        prevMaxX = x;
                        prevMaxY = y;
                    }
                }
            }
        }else{
            maxWave = wave[networkLocationY-4][networkLocationX-4];
            for (int y = networkLocationY - 4; y < networkLocationY + 4; y++) {
                for (int x = networkLocationX - 4; x < networkLocationX + 4; x++) {
                    if (wave[y][x] > maxWave) {
                        maxX = x;
                        maxY = y;
                    }
                }
            }
        }
    }

    public void newframe() {
        //Toggle maps each frame
        i = oldind;
        oldind = newind;
        newind = i;

        i = 0;
        mapind = oldind;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                wave[y][x] = 0;
                short north = 0, west = 0, east = 0, south = 0;

                if (x > 0 && x < width - 1) {
                    if (!walls[y][x + 1]) {

                        east = ripplemap[mapind - 1];

                    }
                    if (!walls[y][x - 1]) {

                        west = ripplemap[mapind + 1];
                    }
                }
                if (y > 0 && y < height - 1) {
                    if (!walls[y + 1][x]) {

                        south = ripplemap[mapind - width];
                    }
                    if (!walls[y - 1][x]) {

                        north = ripplemap[mapind + width];
                    }
                }
                //short data = (short) ((ripplemap[mapind - width] + ripplemap[mapind + width] + ripplemap[mapind - 1] + ripplemap[mapind + 1]) >> 1);
                short data = (short) ((north + west + east + south) >> 1);
                data -= ripplemap[newind + i];
                data -= data >> 7;
                ripplemap[newind + i] = data;

                //where data=0 then still, where data>0 then wave

                data = (short) (1024 - data);
                if (walls[y][x]) {
                    data = 0;
                }
                //offsets
                a = ((x - hwidth) * data / 1024) + hwidth;
                b = ((y - hheight) * data / 1024) + hheight;
                if((x > 0 && x < width - 1)&&(y > 0 && y < height - 1)) {
                    if (walls[y - 1][x] || walls[y + 1][x] || walls[y][x - 1] || walls[y][x + 1])
                        wave[y][x] = (short) ((data) >> 3);
                    else
                        wave[y][x] = data;
                }
                //bounds check
                if (a >= width)
                    a = width - 1;
                if (a < 0)
                    a = 0;
                if (b >= height)
                    b = height - 1;
                if (b < 0)
                    b = 0;

                    /*if (walls[a][b]) {
                        if (a < preva)
                            a += 1;
                        else if (a > preva)
                            a -= 1;
                        if (b < prevb)
                            b += 1;
                        else if (b > prevb)
                            b -= 1;
                    }*/

                ripple[i] = texture[a + (b * width)];

                mapind++;
                i++;

            }

        }
    }

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }


    /*public void checkForWave() {

        int xComponent, yComponent;

        //findMaxPoint();
        if (wave[networkLocationY][networkLocationX] < 1024) {
            /*maxWave = wave[networkLocationY - 20][networkLocationX - 20];
            for (int y = networkLocationY - 20; y < networkLocationY + 20; y++) {
                for (int x = networkLocationX - 20; x < networkLocationX + 20; x++) {
                    if (wave[y][x] > maxWave) {
                        maxX = x;
                        maxY= y;
                    }
                }
            }
            xComponent = maxX - prevMaxX;
            yComponent = maxY - prevMaxY;

            //prevMaxX = maxX;
            //prevMaxY = maxY;
            //yComponent > 0 wave is north
            //yComponent == 0 wave is east or west
            //yComponent < 0 wave is north

            //double array must be n,e,s,w,n,e,s,w
            if(Math.abs(xComponent)<Math.abs(yComponent)) {
                if (yComponent < 0) {
                    networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                } else if (yComponent == 0) {
                    if (xComponent <= 0) {
                        networkInput = new double[]{1, 0, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    }
                    else if (xComponent > 0) {
                        networkInput = new double[]{2, 3, 0, 1, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    }
                    //else
                    //  networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                } else {
                    networkInput = new double[]{2, 1, 3, 0, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};

                }
            }else{
                if (xComponent < 0) {
                    networkInput = new double[]{1, 0, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                } else if (xComponent == 0) {
                    if (yComponent <= 0) {
                        networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    }
                    else if (yComponent > 0) {
                        networkInput = new double[]{2, 1, 3, 0, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                    }
                    //else
                    //  networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                } else {
                    networkInput = new double[]{2, 3, 0, 1, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};

                }
            }
            //0 south
            //1 west
            //2 east
            //3 north
            whereTo = network.whereTo(networkInput);
            switch (whereTo) {
                case 0:
                    networkLocationY += level.getRoomSizeY();
                    break;
                case 1:
                    networkLocationX += level.getRoomSizeX();
                    break;
                case 2:
                    networkLocationX -= level.getRoomSizeX();
                    break;
                case 3:
                    networkLocationY -= level.getRoomSizeY();
                default:
                    break;
            }
            networkCell = level.getRoomAtPoint(map(networkLocationX,0,400,0,9), map(networkLocationY,0,400,0,9));


        }


    }*/

    int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        //System.out.println(((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min));
//System.out.println(Math.ceil((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min));
        return  (int) Math.ceil((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
}