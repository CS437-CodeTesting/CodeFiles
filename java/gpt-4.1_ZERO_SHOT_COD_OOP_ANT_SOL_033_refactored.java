import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a GitHub user with immutable properties and domain-specific behavior.
 */
public final class GitHubUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String url;
    private final String login;
    private final String avatarUrl;
    private final String gravatarId;
    private final String name;
    private final String email;

    private GitHubUser(Builder builder) {
        this.id = builder.id;
        this.url = Objects.requireNonNull(builder.url, "url must not be null");
        this.login = Objects.requireNonNull(builder.login, "login must not be null");
        this.avatarUrl = Objects.requireNonNull(builder.avatarUrl, "avatarUrl must not be null");
        this.gravatarId = Objects.requireNonNull(builder.gravatarId, "gravatarId must not be null");
        this.name = builder.name;
        this.email = builder.email;
    }

    public long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getGravatarId() {
        return gravatarId;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns a display name for the user, preferring the real name if available, otherwise the login.
     */
    public String getDisplayName() {
        return (name != null && !name.isBlank()) ? name : login;
    }

    /**
     * Returns true if the user has an email address.
     */
    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    /**
     * Returns a new GitHubUser instance with updated name and/or email.
     * This method does not mutate the current instance.
     */
    public GitHubUser updateProfile(String newName, String newEmail) {
        return new Builder(this)
                .name(newName)
                .email(newEmail)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GitHubUser)) return false;
        GitHubUser that = (GitHubUser) o;
        return id == that.id &&
                url.equals(that.url) &&
                login.equals(that.login) &&
                avatarUrl.equals(that.avatarUrl) &&
                gravatarId.equals(that.gravatarId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, login, avatarUrl, gravatarId, name, email);
    }

    @Override
    public String toString() {
        return "GitHubUser{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", login='" + login + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", gravatarId='" + gravatarId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    /**
     * Builder for GitHubUser to ensure immutability and validation.
     */
    public static class Builder {
        private long id;
        private String url;
        private String login;
        private String avatarUrl;
        private String gravatarId;
        private String name;
        private String email;

        public Builder(long id, String url, String login, String avatarUrl, String gravatarId) {
            this.id = id;
            this.url = url;
            this.login = login;
            this.avatarUrl = avatarUrl;
            this.gravatarId = gravatarId;
        }

        public Builder(GitHubUser user) {
            this.id = user.id;
            this.url = user.url;
            this.login = user.login;
            this.avatarUrl = user.avatarUrl;
            this.gravatarId = user.gravatarId;
            this.name = user.name;
            this.email = user.email;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public GitHubUser build() {
            return new GitHubUser(this);
        }
    }
}