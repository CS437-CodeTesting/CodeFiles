// Base entity interface
public interface IdEntity {
    Long getId();
}

// Custom exception for deletion errors
public class DeleteException extends Exception {
    public DeleteException(String message) {
        super(message);
    }
}

// Generic repository interface for deletable entities
public interface DeletableRepository<T extends IdEntity> {
    /**
     * Deletes the given entity from the data store.
     * @param entity The entity to delete. Must not be null.
     * @throws DeleteException if the entity is null or deletion fails.
     */
    void delete(T entity) throws DeleteException;
}

// Example implementation for a specific entity
public class User implements IdEntity {
    private Long id;
    // ... other fields and methods

    @Override
    public Long getId() {
        return id;
    }
}

public class UserRepository implements DeletableRepository<User> {
    @Override
    public void delete(User user) throws DeleteException {
        if (user == null) {
            throw new DeleteException("Cannot delete null user");
        }
        // Entity-specific deletion logic, e.g., using JPA/Hibernate
        // entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
        // For demonstration, we'll just print:
        System.out.println("Deleting user with ID: " + user.getId());
        // If deletion fails, throw new DeleteException("Deletion failed for user with ID: " + user.getId());
    }
}

// Usage example
public class UserService {
    private final DeletableRepository<User> userRepository;

    public UserService(DeletableRepository<User> userRepository) {
        this.userRepository = userRepository;
    }

    public void removeUser(User user) throws DeleteException {
        userRepository.delete(user);
    }
}