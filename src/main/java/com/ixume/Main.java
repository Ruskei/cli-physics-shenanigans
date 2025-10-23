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
    static final int NUM_POINTS = 60;
    static final double START_ANGLE = 0.0;
    static final double DX = 1.0;
    static final Vector2d ORIGIN = new Vector2d(15.0, 85.0);
    static final Vector2d HEAD_VELOCITY = new Vector2d(10.0, 0.0);
    static final double MASS = 2.0;
    static final double LAST_MASS = 2.0;
    static final double DRAG_CO = 0.3;
    static final double LAST_DRAG_CO = 0.3;

    public static void main(String[] args) {
        final ArrayList<Point> staticPoints = new ArrayList<>();
        final ArrayList<Point> activePoints = new ArrayList<>();
        final ArrayList<DistanceConstraint> constraints = new ArrayList<>();
        Point head = new Point(ORIGIN, 1_000_000_000.0, DRAG_CO);
        final Vector2d dir = new Vector2d(Math.cos(Math.toRadians(START_ANGLE - 90.0)), Math.sin(Math.toRadians(START_ANGLE - 90.0)));
        staticPoints.add(head);
        Point prev = head;
        int i = 1;
        while (i <= NUM_POINTS) {
            Point node = new Point(ORIGIN.add(dir.mul(i * DX, new Vector2d()), new Vector2d()), MASS, DRAG_CO);
            constraints.add(new DistanceConstraint(prev, node, DX));
            activePoints.add(node);
            prev = node;

            i++;
        }

        Point last = new Point(ORIGIN.add(dir.mul(i * DX, new Vector2d()), new Vector2d()), LAST_MASS, LAST_DRAG_CO);
        constraints.add(new DistanceConstraint(prev, last, DX));
        activePoints.add(last);

        TerminalGrid grid = new TerminalGrid(300, 100);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long start = System.nanoTime();

                ORIGIN.add(HEAD_VELOCITY.mul(DELTA_TIME, new Vector2d()));
                final ArrayList<Vector2d> positions = new ArrayList<>();
                // apply external forces
                for (Point point : activePoints) {
                    point.velocity.add(GRAVITY.mul(DELTA_TIME, new Vector2d())); // gravity
                    final Vector2d drag = point.velocity.normalize(-point.drag * point.velocity.lengthSquared() * DELTA_TIME / point.mass, new Vector2d());
                    point.velocity.add(drag);
                }

                for (int i = 0; i < ITERATIONS; i++) {
                    for (DistanceConstraint constraint : constraints) {
                        constraint.solve();
                    }
                }

                // apply positions
                for (Point point : activePoints) {
                    point.position.add(point.velocity.mul(DELTA_TIME, new Vector2d()));
                    positions.add(point.position);
                }

                for (Point point : staticPoints) {
                    positions.add(point.position);
                }

                grid.update(positions);

                final long finish = System.nanoTime();
                System.out.println("Step took " + ((double) (finish - start) / 1_000_000.0) + "us");
            }
        }, 0, (int) (DELTA_TIME * 1000));
    }
}