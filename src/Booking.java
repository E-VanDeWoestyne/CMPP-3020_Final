import exceptions.InvalidDataException;
import java.time.LocalDateTime;

public class Booking {
    private final int id;
    private int roomId;
    private int eventId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Status status;

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    public Booking(int id, int roomId, int eventId, LocalDateTime createdAt, LocalDateTime updatedAt, Status status)
            throws InvalidDataException {
        if (id <= 0) {
            throw new InvalidDataException("Booking id must be greater than 0");
        }
        if (roomId <= 0) {
            throw new InvalidDataException("Room id must be greater than 0");
        }
        if (eventId <= 0) {
            throw new InvalidDataException("Event id must be greater than 0");
        }
        if (createdAt == null) {
            throw new InvalidDataException("createdAt is required");
        }
        if (updatedAt == null) {
            throw new InvalidDataException("updatedAt is required");
        }
        if (status == null) {
            throw new InvalidDataException("Booking status is required");
        }

        this.id = id;
        this.roomId = roomId;
        this.eventId = eventId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", eventId=" + eventId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", status=" + status +
                '}';
    }
}