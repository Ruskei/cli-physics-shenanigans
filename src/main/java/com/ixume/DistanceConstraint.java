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
        Vector2d pa = a.position;
        Vector2d pb = b.position;
        Vector2d r = pa.sub(pb, new Vector2d());
        double rr = r.dot(r);
        if (rr == 0.0) return;

        Vector2d va = a.velocity;
        Vector2d vb = b.velocity;
        
        double bias = -BIAS / DELTA_TIME * (distance - pa.distance(pb));
        double lambda = -(r.dot(va) - r.dot(vb) + bias) / (rr * ima + rr * imb);
//        System.out.println("SOLVING");
//        System.out.println("| distance: " + pa.distance(pb) + " (desired: " + distance + ")");
//        System.out.println("| lambda: " + lambda);
        Vector2d dva = r.mul(lambda * ima, new Vector2d());
        Vector2d dvb = r.mul(-lambda * imb, new Vector2d());

        va.add(dva);
        vb.add(dvb);
    }
}
