package hr.tvz.diplomski.level;

public class Room {

    boolean north, south, west, east;
    private boolean room[];
    public Room(boolean north, boolean south, boolean west, boolean east){
        room = new boolean[4];
        this.north = room[0] = north;
        this.south = room[1] = south;
        this.west = room[2] = west;
        this.east = room[3] = east;
    }

    private int numOfWalls(){
        int numOfWalls = 0;
        for(int i = 0; i< this.room.length; i++){
            if(this.room[i])
                numOfWalls++;
        }
        return numOfWalls;
    }
    public String toString(){
        String room ="";
        room += "North: "+north;
        room +=", West: "+west;
        room +=", East: "+east +", South: "+south;

        return room;
    }


    public String getNorthEdge(){
        String room ="";
        if(this.north)
            room += "???";
        else room +="   ";
        return room;
    }

    public String getMiddle(){

        String room ="";
        if (this.west && this.east)
            room += "? ?";
        else if(this.west)
            room +="?  ";
        else if(this.east)
            room += "  ?";
        return room;
    }

    public String getSouthEdge(){
        String room ="";
        if(this.south)
            room += "???";
        else room +="   ";
        return room;
    }

    public boolean isNorth() {
        return north;
    }



    public boolean isSouth() {
        return south;
    }



    public boolean isWest() {
        return west;
    }



    public boolean isEast() {
        return east;
    }

    public void makeWall(int wallPosition) {
        room[wallPosition] = true;
        updateRoom(wallPosition);
    }

    private void updateRoom(int wallPosition) {
        switch (wallPosition) {
            case 0:
                north = true;
                break;
            case 1:
                south = true;
                break;
            case 2:
                west= true;
                break;
            case 3:
                east = true;
                break;
            default:
                break;
        }

    }

    public void setNorth(boolean north) {
        this.north = room[0] = north;

    }

    public void setSouth(boolean south) {
        this.south = room[1] = south;

    }

    public void setWest(boolean west) {
        this.west = room[2] = west;

    }

    public void setEast(boolean east) {
        this.east = room[3] = east;
    }

    public boolean hasWall(int wallPosition) {
        return room[wallPosition];
    }

    public boolean[] getRoom(){
        return room;
    }


    public double hasNorthWall() {
        if(north)
            return 1;
        else
            return 0;
    }



    public double hasSouthWall() {
        if(south)
            return 1;
        else
            return 0;
    }



    public double hasWestWall() {
        if(west)
            return 1;
        else
            return 0;
    }



    public double hasEastWall() {
        if(east)
            return 1;
        else
            return 0;
    }
}
