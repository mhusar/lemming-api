package lemming.context.inbound;

import lemming.data.DatedEntity;
import lemming.user.User;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about a group of inbound contexts.
 */
@SuppressWarnings("unused")
@BatchSize(size = 30)
@DynamicUpdate
@Entity
@SelectBeforeUpdate
@OptimisticLocking(type = OptimisticLockType.VERSION)
@Table(name = "inbound_context_group", indexes = {
        @Index(columnList = "timestamp, user_id")
})
public class InboundContextGroup extends DatedEntity implements Serializable {
    /**
     * ID associated with a context.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Version number field used for optimistic locking.
     */
    @Column(name = "version")
    @Version
    private Long version;

    /**
     * Timestamp of inbound contexts.
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    @Type(type = "timestamp")
    private Timestamp timestamp;

    /**
     * Timestamp of event when an inbound context group was locked.
     */
    @Column(name = "lock_timestamp")
    @Type(type = "timestamp")
    private Timestamp lockTimestamp;

    /**
     * Owner of an inbound context group.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * User an inbound context group is locked for.
     */
    @ManyToOne
    @JoinColumn(name = "lock_user_id")
    private User lockUser;

    /**
     * A list of inbound contexts belonging to a context group.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "group", orphanRemoval = true)
    private List<InboundContext> contexts = new ArrayList<>();

    /**
     * Creates an inbound context group.
     */
    public InboundContextGroup() {
    }

    /**
     * Creates an inbound context group.
     *
     * @param timestamp timestamp of contexts
     * @param user owner of a context group
     */
    public InboundContextGroup(Timestamp timestamp, User user) {
        this.timestamp = timestamp;
        this.user = user;
    }

    /**
     * Returns the ID associated with an inbound context group.
     *
     * @return Primary key of a context group.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of an inbound context group.
     *
     * @param id the ID of a context group
     */
    @SuppressWarnings("unused")
    private void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the version of an inbound context group.
     *
     * @return Version number of a context group.
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version number of an inbound context group.
     *
     * @param version version number of a context group
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Returns the timestamp of a context group.
     *
     * @return A timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of a context group.
     *
     * @param timestamp a timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the timestamp of a lock event.
     *
     * @return A timestamp, or null.
     */
    public Timestamp getLockTimestamp() {
        return lockTimestamp;
    }

    /**
     * Sets the timestamp of a lock event.
     *
     * @param lockTimestamp lock timestamp
     */
    public void setLockTimestamp(Timestamp lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }

    /**
     * Returns the owner of a context group.
     *
     * @return A user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the owner of a context group.
     *
     * @param user a user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Return the user an inbound context group is locked for.
     *
     * @return A user, or null.
     */
    public User getLockUser() {
        return lockUser;
    }

    /**
     * Sets the user an inbound context group is locked for.
     *
     * @param lockUser a user
     */
    public void setLockUser(User lockUser) {
        this.lockUser = lockUser;
    }

    /**
     * Returns the inbound contexts belonging to a context group.
     *
     * @return A list of inbound contexts.
     */
    public List<InboundContext> getContexts() {
        return contexts;
    }

    /**
     * Sets the inbound contexts belonging to a context group.
     *
     * @param contexts a list of inbound contexts
     */
    public void setContexts(List<InboundContext> contexts) {
        this.contexts = contexts;
    }

    /**
     * Adds an inbound context to a context group.
     *
     * @param context an inbound context
     */
    public void addContext(InboundContext context) {
        contexts.add(context);
    }

    /**
     * Indicates if some other object is equal to this one.
     *
     * @param other the reference object with which to compare
     * @return True if this object is the same as the object argument; false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof InboundContextGroup)) return false;

        InboundContextGroup contextGroup = (InboundContextGroup) other;
        return getId() != null && getId().equals(contextGroup.getId());
    }

    /**
     * Returns a hash code value for a context group.
     *
     * @return A hash code value for a context group.
     */
    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new IllegalStateException("Can’t create a hash code for a non-persistent object");
        }

        return getId();
    }
}
