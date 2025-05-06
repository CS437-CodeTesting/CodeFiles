public class Product {
    private final String name;
    private final String description;
    private final double price;

    public Product(String name, String description, double price) {
        if (name == null || description == null) {
            throw new IllegalArgumentException("Name and description cannot be null.");
        }
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Other domain-relevant methods can be added here

    @Override
    public String toString() {
        // Provide a meaningful, domain-specific string representation
        return String.format("Product[name='%s', description='%s', price=%.2f]", name, description, price);
    }

    // Getters (if needed)
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }
}