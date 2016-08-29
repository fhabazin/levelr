package hr.tvz.diplomski.level;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Filip on 27.5.2016..
 */
public enum WallPositions {NORTH,EAST,SOUTH,WEST;
    private static final List<WallPositions> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static WallPositions getValue(int position){
        return VALUES.get(position);
    }

    public static WallPositions randomPosition()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}

