package com.ixume;
import java.util.List;
import org.joml.Vector2d;
import java.util.Arrays;

public class TerminalGrid {
    private int width;
    private int height;
    private char[][] prevGrid;
    private double worldMinX;
    private double worldMinY;
    private double worldMaxX;
    private double worldMaxY;
    private double resolution; // characters per meter

    public TerminalGrid(double worldMinX, double worldMinY, double worldMaxX, double worldMaxY, double resolution) {
        this.worldMinX = worldMinX;
        this.worldMinY = worldMinY;
        this.worldMaxX = worldMaxX;
        this.worldMaxY = worldMaxY;
        this.resolution = resolution;

        // Calculate terminal dimensions based on world window and resolution
        // Use 2x width to compensate for terminal characters being twice as tall as they are wide
        this.width = (int) Math.ceil((worldMaxX - worldMinX) * resolution * 2.0);
        this.height = (int) Math.ceil((worldMaxY - worldMinY) * resolution);
        this.prevGrid = null;
    }

    public void update(List<Vector2d> points) {
        // Build the new grid
        char[][] grid = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(grid[y], ' ');
        }

        for (Vector2d p : points) {
            // Transform world coordinates to terminal coordinates
            // Apply 2x scaling to x to compensate for character aspect ratio
            int x = (int) Math.round((p.x - worldMinX) * resolution * 2.0);
            int y = (int) Math.round((p.y - worldMinY) * resolution);
            if (x >= 0 && x < width && y >= 0 && y < height) {
                // Flip y-coordinate: (0,0) at bottom left, positive y goes up
                grid[height - 1 - y][x] = '█';
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
