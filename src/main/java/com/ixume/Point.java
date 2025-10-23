package com.ixume;

import org.joml.Vector2d;

public class Point {
    Vector2d position;
    Vector2d velocity;
    double mass;
    double drag;
    
    public Point(
            Vector2d position,
            double mass,
            double drag
    ) {
        this.position = position;
        velocity = new Vector2d();
        this.mass = mass;
        this.drag = drag;
    }
}
