import java.util.List;
import java.util.ArrayList;

public class AreaCalculator {
    public double totalArea(List<Shape> shapes) {
        double total = 0;
        for (Shape s : shapes) {
            if (s.getType().equals("CIRCLE")) {
                total += Math.PI * s.getRadius() * s.getRadius();
            } else if (s.getType().equals("RECTANGLE")) {
                total += s.getWidth() * s.getHeight();
            } else {
                System.out.println("Unknown shape type: " + s.getType());
            }
        }
        return total;
    }
}

class Shape {
    private String type;
    private double radius;
    private double width, height;
    public Shape(String type, double radius, double width, double height) {
        this.type = type; this.radius = radius; this.width = width; this.height = height;
    }
    public String getType() { return type; }
    public double getRadius() { return radius; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

// Main class to test shapes
public class Main {
    public static void main(String[] args) {
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Shape("CIRCLE", 5, 0, 0));
        shapes.add(new Shape("RECTANGLE", 0, 4, 6));
        shapes.add(new Shape("TRIANGLE", 0, 0, 0)); // New shape type
        AreaCalculator ac = new AreaCalculator();
        System.out.println("Total area: " + ac.totalArea(shapes));
    }
}