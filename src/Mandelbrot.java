import java.awt.geom.Rectangle2D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sgoldber
 */
public class Mandelbrot extends FractalGenerator {
    public static final int MAX_ITERATIONS = 2000;
    
    @Override
    public void getInitialRange(Rectangle2D.Double range) {
            range.x = -2;
            range.y = -1.5;
            
            range.width = 3;
            range.height = 3;
    }
    
    public int numIterations(double x, double y) {
        int i = 0;
        double currX = 0;
        double currY = 0;
        
        for (; i < MAX_ITERATIONS; i++) {
            double xSquared = currX * currX;
            double ySquared = currY * currY;
            
            if (xSquared + ySquared > 4)
                return i;
            
            double nextX = xSquared - ySquared + x;
            double nextY = 2 * currX * currY + y;
            
            currX = nextX;
            currY = nextY;
        }
        
        return -1;
    }
    
    public String toString() {
        return "Mandelbrot";
    }
}
