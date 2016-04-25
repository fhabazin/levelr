package hr.tvz.diplomski.level;


import java.awt.*;
import java.util.Random;

public class Level {

    private Room[][] level;
    private int xSize, ySize;
    private static final double WALL_PROBABILITY = 0.55;
    private int roomSizeX,  roomSizeY;
    private boolean[][] walls;

    public Level(int xSize, int ySize) {
        this.level = new Room[xSize][ySize];
        this.xSize = xSize;
        this.ySize = ySize;

        setupOutsideWalls();
    }
    public Level() {
        xSize = 10;
        ySize = 10;
        level = new Room[xSize][ySize];


        setupOutsideWalls();
    }

    public void generateLevel(int numberOfWalls) {
        Random rnd = new Random();
        int wallCounter = 0;
        int wallPosition;
        int x, y;
        while (wallCounter <= numberOfWalls) {
            x = rnd.nextInt(xSize);
            y = rnd.nextInt(ySize);
            if (rnd.nextDouble() > WALL_PROBABILITY) {
                wallPosition = rnd.nextInt(4);
                if (!level[x][y].hasWall(wallPosition)) {
                    level[x][y].makeWall(wallPosition);
                    wallCounter++;
                }

            }
        }
        for(x=0;x<xSize;x++){
            for(y=0;y<ySize;y++){
                boolean[] room = level[x][y].getRoom();
                for(int i = 0;i<room.length;i++) {
                    switch (i) {
                        case 0:
                            if (x != 0)
                                level[x - 1][y].setSouth(room[i]);
                            break;
                        case 1:
                            if (x < (xSize - 1))
                                level[x + 1][y].setNorth(room[i]);
                            break;
                        case 2:
                            if (y != 0)
                                level[x][y - 1].setEast(room[i]);
                            break;
                        case 3:
                            if (y < (ySize - 1))
                                level[x][y + 1].setWest(room[i]);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }



    private void setupOutsideWalls() {
        Room room;
        boolean north, south, west, east;
        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                north = south = west = east = false;

                if (x == 0) {
                    north = true;

                    if (y == 0)
                        west = true;
                    else if (y == ySize - 1)
                        east = true;
                } else if (x > 0 && x < xSize - 1) {
                    if (y == 0)
                        west = true;
                    else if (y == ySize - 1)
                        east = true;
                } else if (x == xSize - 1) {
                    south = true;
                    if (y == 0)
                        west = true;
                    else if (y == ySize - 1)
                        east = true;
                }
                room = new Room(north, south, west, east);

                this.level[x][y] = room;
            }
        }
    }

    public String toString() {
        String levelString = "";
        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                //System.out.println(x+", "+y+level[x][y].);
            }



        }
       
        return levelString;
    }

    public Image generateLevelImage(Image image, int imageSizeX, int imageSizeY){
        roomSizeX = imageSizeX/xSize;
        roomSizeY = imageSizeY/ySize;

        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);


        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                if (level[x][y].isNorth()) {
                    g.drawLine(x * roomSizeX, y * roomSizeY, x * roomSizeX, (y + 1) * roomSizeY);
                    g.drawLine(1+x * roomSizeX, y * roomSizeY, 1+x * roomSizeX, (y + 1) * roomSizeY);
                }
                if (level[x][y].isWest()){
                    g.drawLine(x * roomSizeX, y * roomSizeY, (x + 1) * roomSizeX , y * roomSizeY);
                    g.drawLine(x * roomSizeX, 1+y * roomSizeY, (x + 1) * roomSizeX , 1+y * roomSizeY);
                }
                if (level[x][y].isEast()){
                    g.drawLine(x * roomSizeX, (y + 1)* roomSizeY, (x +1) * roomSizeX, (y + 1) * roomSizeY);
                    g.drawLine(x * roomSizeX, 1+(y + 1)* roomSizeY, (x +1) * roomSizeX, 1+(y + 1) * roomSizeY);
                }
                if(level[x][y].isSouth()){
                    g.drawLine((x +1) * roomSizeX, y * roomSizeY, (x +1) * roomSizeX,  (y + 1) * roomSizeY );
                    g.drawLine(1+(x +1) * roomSizeX, y * roomSizeY, 1+(x +1) * roomSizeX,  (y + 1) * roomSizeY );
                }
            }
        }
        g.dispose();
        return image;
    }

    public int getRoomSizeX(){
        return roomSizeX;
    }

    public int getRoomSizeY(){
        return roomSizeY;
    }

    public Room getRoomAtPoint(int x, int y){

        return level[x][y];
    }

    public int getXSize(){
        return xSize;
    }

    public int getYSize(){
        return ySize;
    }

    public Room getRoom(int i){
        if((i%ySize)>0){
            return level[i/9][i%9];
        }else
            return level[i/9][0];
    }


    public boolean[][] getWalls(){
        Room currentRoom;
        boolean[][] walls = new boolean[(xSize+1)*roomSizeX][(ySize+1)*roomSizeY];
        int i = 0;
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                currentRoom = level[x][y];
                i=0;

                while(i<=roomSizeX){
                    if(currentRoom.isEast()){
                        walls[(y + 1) * roomSizeY][x * roomSizeX + i]=true;

                    }
                    if(currentRoom.isWest()) {
                        walls[y * roomSizeY][x * roomSizeX+i] = true;

                    }
                    if(currentRoom.isNorth()) {
                        walls[ y * roomSizeY + i][x * roomSizeX] = true;

                    }
                    if(currentRoom.isSouth()) {
                        walls[y * roomSizeY + i][(x + 1) * roomSizeX] = true;

                    }
                    i++;
                }
            }

        }
        return walls;
    }
}
