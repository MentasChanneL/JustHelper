package com.prikolz.justhelper.util;

public abstract class DevWorld {
    public static int mathFloor(int y) { return (y - 5) / 7 + 1; }
    public static int mathY(int floor) { return 5 + (floor - 1) * 7; }
    public static int mathZ(int line) { return line * 4; }
    public static int mathLine(int z) { return z / 4; }
}
