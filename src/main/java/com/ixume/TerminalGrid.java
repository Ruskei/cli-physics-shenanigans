package com.ixume;
import java.util.List;
import org.joml.Vector2d;
import java.util.Arrays;

public class TerminalGrid {
    private int width;
    private int height;
    private char[][] prevGrid;

    public TerminalGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.prevGrid = null;
    }

    public void update(List<Vector2d> points) {
        // Build the new grid
        char[][] grid = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(grid[y], ' ');
        }

        for (Vector2d p : points) {
            int x = (int) Math.round(p.x * 2);
            int y = (int) Math.round(p.y);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                // Flip y-coordinate: (0,0) at bottom left, positive y goes up
                grid[height - 1 - y][x] = '*';
            }
        }

        // Render
        if (prevGrid == null) {
            // First time: clear screen, move to home, print full grid
            System.out.print("\033[2J\033[H");
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    System.out.print(grid[y][x]);
                }
                System.out.println();
            }
        } else {
            // Subsequent updates: only change differing characters
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (grid[y][x] != prevGrid[y][x]) {
                        // Move cursor to position (1-based) and print new char
                        System.out.print("\033[" + (y + 1) + ";" + (x + 1) + "H" + grid[y][x]);
                    }
                }
            }
        }

        // Move cursor below the grid to avoid overwriting issues
        System.out.print("\033[" + (height + 1) + ";1H");
        System.out.flush();

        // Update prevGrid
        prevGrid = new char[height][];
        for (int i = 0; i < height; i++) {
            prevGrid[i] = Arrays.copyOf(grid[i], width);
        }
    }
}
