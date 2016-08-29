package hr.tvz.diplomski.level;


import java.awt.*;
import java.util.Random;

public class Level {
    private static final int SIZE = WallPositions.values().length;
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
        WallPositions wallPosition;
        int x, y;
        while (wallCounter <= numberOfWalls) {
            x = rnd.nextInt(xSize-1);
            y = rnd.nextInt(ySize-1);
            if (rnd.nextDouble() > WALL_PROBABILITY) {
                wallPosition = WallPositions.randomPosition();
                if (!level[y][x].hasWall(wallPosition.ordinal())) {
                    level[y][x].makeWall(wallPosition);
                    switch (wallPosition) {
                        case NORTH:
                            if(y>0)
                                level[y-1][x].makeWall(WallPositions.SOUTH);
                            break;
                        case EAST:
                            if(x<xSize-1)
                                level[y][x+1].makeWall(WallPositions.WEST);
                            break;
                        case SOUTH:
                            if(y<ySize-1)
                                level[y+1][x].makeWall(WallPositions.NORTH);
                            break;
                        case WEST:
                            if(x>0)
                                level[y][x-1].makeWall(WallPositions.EAST);
                            break;
                        default:
                            break;
                    }
                    wallCounter++;
                }

            }
        }

        /*for(x=0;x<xSize;x++){
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
        }*/
    }




    private void setupOutsideWalls() {
        Room room;
        boolean north, south, west, east;
        for (int y = 0; y < this.ySize; y++) {
            for (int x = 0; x < this.xSize; x++) {
                north = south = west = east = false;
                if (y == 0) {
                    north = true;

                    if (x == 0)
                        west = true;
                    else if (x == xSize - 1)
                        east = true;
                } else if (y > 0 && y < ySize - 1) {
                    if (x == 0)
                        west = true;
                    else if (x == xSize - 1)
                        east = true;
                } else if (y == ySize - 1) {
                    south = true;
                    if (x == 0)
                        west = true;
                    else if (x == ySize - 1)
                        east = true;
                }
                room = new Room(north, south, west, east);

                this.level[y][x] = room;
            }
        }
    }



    public Image generateLevelImage(Image image, int imageSizeX, int imageSizeY){
        roomSizeX = imageSizeX/xSize;
        roomSizeY = imageSizeY/ySize;

        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        for (int y = 0; y < this.ySize; y++) {
            for (int x = 0; x < this.xSize; x++) {
                if (level[y][x].isNorth()) {
                    g.drawLine(x * roomSizeX, y * roomSizeY,(x+1) * roomSizeX,  y  * roomSizeY );
                   // g.drawLine(1+x * roomSizeX, y * roomSizeY, 1+x * roomSizeX, (y + 1) * roomSizeY);
                }
                if (level[y][x].isEast()){
                    g.drawLine((x+1) * roomSizeX,y * roomSizeY,(x+1) * roomSizeX, (y+1) * roomSizeY);
                    //g.drawLine(x * roomSizeX, 1+(y + 1)* roomSizeY, (x +1) * roomSizeX, 1+(y + 1) * roomSizeY);
                }
                if(level[y][x].isSouth()){
                    g.drawLine( x * roomSizeX,(y+1) * roomSizeY, (x+1) * roomSizeX, (y+1) * roomSizeY);
                    // g.drawLine(1+(x +1) * roomSizeX, y * roomSizeY, 1+(x +1) * roomSizeX,  (y + 1) * roomSizeY );
                }
                if (level[y][x].isWest()){
                    g.drawLine( x * roomSizeX,y * roomSizeY,  x * roomSizeX, (y+1) * roomSizeY );
                    //g.drawLine(x * roomSizeX, 1+y * roomSizeY, (x + 1) * roomSizeX , 1+y * roomSizeY);
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
        boolean[][] walls = new boolean[(ySize)*roomSizeY+1][(xSize)*roomSizeX+1];
        int i;
        for(int y = 0; y < ySize; y++){
            for(int x = 0; x < xSize; x++){

                currentRoom = level[y][x];
                i=0;

                while(i<=roomSizeX){

                    if(currentRoom.isNorth()) {
                        walls[ y * roomSizeY ][x * roomSizeX + i] = true;

                    }
                    if(currentRoom.isEast()){
                        walls[y * roomSizeY + i][(x +1) * roomSizeX ]=true;

                    }
                    if(currentRoom.isSouth()) {
                        walls[(y+1) * roomSizeY][x  * roomSizeX + i] = true;

                    }
                    if(currentRoom.isWest()) {
                        walls[y * roomSizeY + i][x* roomSizeX] = true;

                    }


                    i++;
                }
            }

        }
        return walls;
    }

    public void walls(){
        for(int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {

                System.out.println("room: "+y+", "+x+","+level[y][x]);
            }
        }
    }
}
