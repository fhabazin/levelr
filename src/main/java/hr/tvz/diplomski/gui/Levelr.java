package hr.tvz.diplomski.gui;

import hr.tvz.diplomski.level.Level;
import hr.tvz.diplomski.level.Room;
import hr.tvz.diplomski.neural.LevelrNetwork;

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
import java.io.File;
import java.io.IOException;
/**
 * Created by Filip on 28.1.2016..
 */
public class Levelr extends JFrame implements ActionListener, MouseListener, Runnable {

    //Constants
    final static int TIMER_DIV = 1000000;
    private final JLabel netLoc;
    private final JLabel sndLoc;
    Canvas canvas;
    JCheckBox autoMove;
    JSplitPane pane;
    JTextField xRooms, yRooms, wallNumber;
    JLabel roomGrid, numberOfWalls;
    JPanel levelPanel;
    JButton generateLevel, mover, placeNet, placeSource;
    LevelrNetwork network;
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


    public Levelr() {

        this.setLayout(new FlowLayout());
        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        canvas = new Canvas();
        try {
            network = new LevelrNetwork("inputDirections.in");
            canvas.setImage(ImageIO.read(new File("levelBackground.png")));
            levelr = ImageIO.read(new File("network.png"));
            soundSource = ImageIO.read(new File("crosshair.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        canvas.addMouseListener(this);
        pane.setTopComponent(canvas);

        levelPanel = new JPanel(new GridLayout(6, 2));

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
        levelPanel.add(new JLabel(""));

        autoMove = new JCheckBox("Auto move");
        levelPanel.add(autoMove);
        mover = new JButton("Move");
        levelPanel.add(mover);

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
        pane.setBottomComponent(levelPanel);


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

            walls = level.getWalls();
            generateLevel.setEnabled(false);
            /*for(int x = 0; x<walls.length;x++){
                for(int y = 0; y<walls[0].length;y++){
                    if(walls[x][y])
                        System.out.print("1,");
                    else
                        System.out.print("0,");
                }
                System.out.print("\n");
            }*/

            placeNet.setEnabled(true);
            placeSource.setEnabled(true);
        }
        if (e.getSource() == placeNet) {
            placeNetwork = true;
        }
        if (e.getSource() == placeSource) {
            placeSoundSource = true;
        }

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
            networkCell = level.getRoomAtPoint(map(networkLocationX,0,400,0,9), map(networkLocationY,0,400,0,9));
            netLoc.setText("netX: " + networkLocationX + ", netY: " + networkLocationY);
            Graphics g = canvas.getImage().getGraphics();
            g.drawImage(levelr, 4 + networkLocationX - level.getRoomSizeX() / 2, 4 + networkLocationY - level.getRoomSizeY() / 2, null);
            g.dispose();
            canvas.setImage(canvas.getImage());
            canvas.repaint();
            placeNetwork = false;
            placeNet.setEnabled(false);
        }
        if (placeSoundSource) {

            soundSourceX = (int) round(e.getX(), level.getRoomSizeX() / 2);
            soundSourceY = (int) round(e.getY(), level.getRoomSizeY() / 2);
            sndLoc.setText("sndX: " + soundSourceX + ", sndY: " + soundSourceY);
            canvas.setImage(canvas.getImage());

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

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        long startTime = System.currentTimeMillis();

        while (Thread.currentThread() == animatorThread) {

            newframe();
            source.newPixels();

            offGraphics.drawImage(image, 0, 0, width, height, null);

            offGraphics.drawImage(levelr, 4 + networkLocationX - level.getRoomSizeX() / 2, 4 + networkLocationY - level.getRoomSizeY() / 2, null);

            canvas.setImage(image);

            checkForWave();


            netLoc.setText("netX: " + networkLocationX + ", netY: " + networkLocationY);

            try {

                startTime += delay;
                Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));

            } catch (InterruptedException e) {
                break;
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

        wave = new short[height][width];
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

                wave[y][x] = data;

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


    public void checkForWave() {
        int numberOfNonZeroPixels = 0;
        int xComponent, yComponent;
        for (int y = networkLocationY - 20; y < networkLocationY + 20; y++) {
            for (int x = networkLocationX - 20; x < networkLocationX + 20; x++) {
                System.out.println(wave[y][x]);
                if (wave[y][x] != 1024) {
                    numberOfNonZeroPixels++;
                }

            }
        }
        if (numberOfNonZeroPixels >= level.getRoomSizeX() * level.getRoomSizeY()) {
            for (int y = networkLocationY - 20; y < networkLocationY + 20; y++) {
                for (int x = networkLocationX - 20; x < networkLocationX + 20; x++) {
                    if (wave[y][x] > maxWave) {
                        maxX = x;
                        maxY = y;
                    }
                }
            }
            xComponent = maxX - prevMaxX;
            yComponent = maxY - prevMaxY;
            //yComponent > 0 wave is north
            //yComponent == 0 wave is east or west
            //yComponent < 0 wave is north

            //double array must be n,e,s,w,n,e,s,w
            if (yComponent > 0) {
                networkInput = new double[]{0, 1, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
            } else if (yComponent == 0) {
                if (xComponent <= 0)
                    networkInput = new double[]{3, 0, 1, 2,networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
                else
                    networkInput = new double[]{1, 0, 2, 3, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
            } else {
                networkInput = new double[]{2, 1, 3, 0, networkCell.hasNorthWall(), networkCell.hasEastWall(), networkCell.hasSouthWall(), networkCell.hasWestWall()};
            }
            //0 north
            //1 west
            //2 east
            //3 south
            whereTo = network.whereTo(networkInput);
            switch (whereTo) {
                case 0:
                    networkLocationY -= level.getRoomSizeY();
                    break;
                case 1:
                    networkLocationX -= level.getRoomSizeX();
                    break;
                case 2:
                    networkLocationX += level.getRoomSizeX();
                    break;
                case 3:
                    networkLocationY += level.getRoomSizeY();
                default:
                    break;
            }
            networkCell = level.getRoomAtPoint(map(networkLocationX,0,400,0,9), map(networkLocationY,0,400,0,9));


        }


    }

    int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}