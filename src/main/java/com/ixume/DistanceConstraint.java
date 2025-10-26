package com.ixume;

import org.joml.Vector2d;

import static com.ixume.Main.BIAS;
import static com.ixume.Main.DELTA_TIME;

public class DistanceConstraint {
    Point a;
    Point b;
    double distance;

    public DistanceConstraint(Point a, Point b, double distance) {
        this.a = a;
        this.b = b;
        this.distance = distance;
    }

    /*
    a distance constraint between 2 bodies A and B can be modeled by:
    ||P_A - P_B|| = C
    to avoid directly modifying position, let's take the derivative (power + chain rule):
    (P_A - P_B) . (V_A - V_B) = 0
    r = P_A - P_B
    r V_A - r V_B
    [r, -r] . [V_A, V_B]
    J = [r, -r]
    V = [V_A, V_B]
    J^T V = 0
    this simply means that velocity diff must simply be perpendicular to position diff
    thus deltaV = l J
    J^T (V + l J) = 0
    J^T V + J^T l J = 0
    l = (-J^T V) / (J M^-1 J^T)
     */
    public void solve() {
        double ima = 1.0 / a.mass;
        double imb = 1.0 / b.mass;

        // Use scalar operations to avoid allocations
        double rx = a.position.x - b.position.x;
        double ry = a.position.y - b.position.y;

        double rr = rx * rx + ry * ry;
        if (rr == 0.0) return;

        double dist = Math.sqrt(rx * rx + ry * ry);
        double bias = -BIAS / DELTA_TIME * (distance - dist);
        double lambda = -(a.velocity.x * rx + a.velocity.y * ry - b.velocity.x * rx - b.velocity.y * ry + bias) / (rr * ima + rr * imb);

        // Update velocities in place
        a.velocity.x += lambda * ima * rx;
        a.velocity.y += lambda * ima * ry;
        b.velocity.x += -lambda * imb * rx;
        b.velocity.y += -lambda * imb * ry;
    }
}
