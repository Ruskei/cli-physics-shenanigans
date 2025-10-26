package com.ixume;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    static final Vector2d GRAVITY = new Vector2d(0.0, -10.0);
    static final double DELTA_TIME = 0.001;
    static final double BIAS = 0.1;
    static final int ITERATIONS = 32;
    static final int NUM_POINTS = 150;
    static final double START_ANGLE = 10.0;
    static final double DX = 1.0;
    static final Vector2d ORIGIN = new Vector2d(40.0, 10.0);
    static final double MASS = 1.0;
    static final double DRAG_CO = 0.05;

    public static void main(String[] args) {
        final ArrayList<Point> staticPoints = new ArrayList<>();
        final ArrayList<Point> activePoints = new ArrayList<>();
        final ArrayList<DistanceConstraint> constraints = new ArrayList<>();
        Point head = new Point(new Vector2d(ORIGIN), 1_000_000.0, DRAG_CO);
        final double angle = Math.toRadians(START_ANGLE);
        staticPoints.add(head);
        Point prev = head;
        for (int i = 0; i < NUM_POINTS; i++) {
            double offset = i + 1;
            Point node = new Point(new Vector2d(ORIGIN.x + offset * Math.cos(angle), ORIGIN.y + offset * Math.sin(angle)), MASS, DRAG_CO);
            constraints.add(new DistanceConstraint(prev, node, DX));
            activePoints.add(node);
            prev = node;
        }

        // World window: x from 0 to 150 meters, y from 35 to 85 meters
        // Resolution: 2 characters per meter
        TerminalGrid grid = new TerminalGrid(0, 0, 90, 60, 1.0);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            double t = 0;

            @Override
            public void run() {
                final long start = System.nanoTime();
                for (int step = 0; step < 150; step++) {
                    // Update head position dynamically (matches Zig implementation)
                    head.position.x = ORIGIN.x + 10 * Math.cos(t / 500);
                    head.position.y = ORIGIN.x + 10 * Math.sin(t / 500);

                    // apply external forces
                    for (Point point : activePoints) {
                        // Apply drag using scalar operations (matches Zig implementation)
                        double len2 = point.velocity.x * point.velocity.x + point.velocity.y * point.velocity.y;
                        double len = Math.sqrt(len2);
                        if (len > 0.0) {
                            point.velocity.x += -point.velocity.x / len * len2 * point.drag * DELTA_TIME / point.mass;
                            point.velocity.y += -point.velocity.y / len * len2 * point.drag * DELTA_TIME / point.mass;
                        }
                        // Apply gravity
                        point.velocity.x += GRAVITY.x * DELTA_TIME;
                        point.velocity.y += GRAVITY.y * DELTA_TIME;
                    }

                    for (int i = 0; i < ITERATIONS; i++) {
                        for (DistanceConstraint constraint : constraints) {
                            constraint.solve();
                        }
                    }

                    // apply positions using scalar operations
                    for (Point point : activePoints) {
                        point.position.x += point.velocity.x * DELTA_TIME;
                        point.position.y += point.velocity.y * DELTA_TIME;
                    }

                    t += 1;
                }

                final long finish = System.nanoTime();
                System.out.println("150 steps took " + ((double) (finish - start) / 1_000_000.0) + "ms");

                final ArrayList<Vector2d> positions = new ArrayList<>();
                for (Point point : activePoints) {
                    positions.add(point.position);
                }

                for (Point point : staticPoints) {
                    positions.add(point.position);
                }

                grid.update(positions);


                double systemEnergy = 0.0;
                for (Point point : activePoints) {
                    systemEnergy += point.position.y * point.mass * Math.abs(GRAVITY.y) + 0.5 * point.mass * point.velocity.lengthSquared();
                }

                System.out.println("System energy: " + systemEnergy);
            }
        }, 0, 15);
    }
}