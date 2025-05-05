import java.util.List;
import java.util.ArrayList;

// Abstract base class for all shapes
abstract class Shape {
    public abstract double area();
}

// Concrete Circle class
class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        if (radius < 0) throw new IllegalArgumentException("Radius must be non-negative");
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

// Concrete Rectangle class
class Rectangle extends Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Width and height must be non-negative");
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

// Example: Triangle class (can be extended for more shapes)
class Triangle extends Shape {
    private final double base;
    private final double height;

    public Triangle(double base, double height) {
        if (base < 0 || height < 0) throw new IllegalArgumentException("Base and height must be non-negative");
        this.base = base;
        this.height = height;
    }

    public double getBase() {
        return base;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

// AreaCalculator is now open for extension, closed for modification
class AreaCalculator {
    public double totalArea(List<Shape> shapes) {
        double total = 0;
        for (Shape s : shapes) {
            total += s.area();
        }
        return total;
    }
}

// Main class to test shapes
public class Main {
    public static void main(String[] args) {
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(5));
        shapes.add(new Rectangle(4, 6));
        shapes.add(new Triangle(3, 4)); // Example triangle

        AreaCalculator ac = new AreaCalculator();
        System.out.println("Total area: " + ac.totalArea(shapes));
    }
}