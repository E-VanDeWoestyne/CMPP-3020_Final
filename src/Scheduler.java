import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Scheduler {
    private final Map<Integer, Room> roomsById;
    private final Map<Integer, Event> eventsById;
    private final Map<Integer, Booking> bookingsById;
    private final Set<String> verifiedClubs;
    private int nextBookingId;

    public Scheduler(List<Room> rooms, List<Event> events, List<Booking> bookings) {
        this.roomsById = new HashMap<>();
        this.eventsById = new HashMap<>();
        this.bookingsById = new HashMap<>();
        this.verifiedClubs = new HashSet<>();

        for (Room room : rooms) {
            this.roomsById.put(room.getId(), room);
        }

        for (Event event : events) {
            this.eventsById.put(event.getId(), event);
        }

        int maxBookingId = 0;
        for (Booking booking : bookings) {
            this.bookingsById.put(booking.getId(), booking);
            if (booking.getId() > maxBookingId) {
                maxBookingId = booking.getId();
            }
        }

        this.nextBookingId = maxBookingId + 1;
    }

    public void registerVerifiedClub(String clubName) {
        if (clubName != null && !clubName.trim().isEmpty()) {
            verifiedClubs.add(clubName.trim().toLowerCase());
        }
    }

    public boolean validateEvent(Event event) {
        if (event == null) {
            throw new RuntimeException("Invalid event: event is null");
        }

        if (isBlank(event.getClubName()) || isBlank(event.getEventName()) || isBlank(event.getEventDate())
                || isBlank(event.getEventStarttime()) || isBlank(event.getEventEndtime())) {
            throw new RuntimeException("Invalid event: required text fields are missing");
        }

        if (event.getAttendees() <= 0) {
            throw new RuntimeException("Invalid event: attendees must be greater than 0");
        }

        LocalDate date = parseDate(event.getEventDate());
        LocalTime start = parseTime(event.getEventStarttime());
        LocalTime end = parseTime(event.getEventEndtime());

        if (!start.isBefore(end)) {
            throw new RuntimeException("Invalid event: start time must be before end time");
        }

        validateBookingWindow(date, start, end);
        validateClubVerification(event);

        return true;
    }

    public List<Room> find_available_room(Event event) {
        validateEvent(event);

        LocalDate eventDate = parseDate(event.getEventDate());
        LocalTime eventStart = parseTime(event.getEventStarttime());
        LocalTime eventEnd = parseTime(event.getEventEndtime());

        ConcurrentLinkedQueue<Room> matches = new ConcurrentLinkedQueue<>();

        roomsById.values().parallelStream().forEach(room -> {
            if (!room.canBook(event.getAttendees(), event.getRequiredEquipment())) {
                return;
            }

            if (hasConflict(room.getId(), eventDate, eventStart, eventEnd)) {
                return;
            }

            matches.add(room);
        });

        return matches.stream()
                .sorted(Comparator.comparingInt(Room::getCapacity).thenComparing(Room::getRoomNo))
                .collect(Collectors.toList());
    }

    public Booking assign_room(Event event) {
        List<Room> availableRooms = find_available_room(event);
        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No room matches event requirements");
        }

        // Auto-pick smallest suitable room by sorted result from find_available_room.
        Room selectedRoom = availableRooms.get(0);
        eventsById.put(event.getId(), event);

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(
                nextBookingId++,
                selectedRoom.getId(),
                event.getId(),
                now,
                now,
                Booking.Status.CONFIRMED);

        bookingsById.put(booking.getId(), booking);
        selectedRoom.book();
        return booking;
    }

    public Booking assign_room(Event event, int selectedRoomId) {
        validateEvent(event);
        eventsById.put(event.getId(), event);

        Room selectedRoom = roomsById.get(selectedRoomId);
        if (selectedRoom == null) {
            throw new RuntimeException("Selected room not found: " + selectedRoomId);
        }

        LocalDate eventDate = parseDate(event.getEventDate());
        LocalTime eventStart = parseTime(event.getEventStarttime());
        LocalTime eventEnd = parseTime(event.getEventEndtime());

        if (!selectedRoom.canBook(event.getAttendees(), event.getRequiredEquipment())) {
            throw new RuntimeException("Selected room does not satisfy event capacity/equipment requirements");
        }

        if (hasConflict(selectedRoom.getId(), eventDate, eventStart, eventEnd)) {
            throw new RuntimeException("Selected room conflicts with an existing booking");
        }

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(
                nextBookingId++,
                selectedRoom.getId(),
                event.getId(),
                now,
                now,
                Booking.Status.CONFIRMED);

        bookingsById.put(booking.getId(), booking);
        selectedRoom.book();
        return booking;
    }

    public Booking manage_booking(int bookingId, String action) {
        Booking booking = bookingsById.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found: " + bookingId);
        }

        if (isBlank(action)) {
            throw new RuntimeException("Action is required for manage_booking");
        }

        String normalizedAction = action.trim().toLowerCase();
        if ("cancel".equals(normalizedAction)) {
            booking.setStatus(Booking.Status.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            Room room = roomsById.get(booking.getRoomId());
            if (room != null) {
                room.unbook();
            }
            return booking;
        }

        if ("update".equals(normalizedAction)) {
            booking.setUpdatedAt(LocalDateTime.now());
            return booking;
        }

        throw new RuntimeException("Unsupported action: " + action + ". Use 'update' or 'cancel'.");
    }

    public List<Booking> get_schedule() {
        List<Booking> allBookings = new ArrayList<>(bookingsById.values());
        allBookings.sort(Comparator.comparing(Booking::getCreatedAt));
        return Collections.unmodifiableList(allBookings);
    }

    private void validateClubVerification(Event event) {
        String normalizedClubName = event.getClubName().trim().toLowerCase();
        if (!verifiedClubs.contains(normalizedClubName)) {
            throw new RuntimeException("Club is not verified: " + event.getClubName());
        }
    }

    private void validateBookingWindow(LocalDate date, LocalTime start, LocalTime end) {
        DayOfWeek day = date.getDayOfWeek();

        LocalTime allowedStart = LocalTime.of(8, 0);
        LocalTime allowedEnd;

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            allowedEnd = LocalTime.of(18, 0);
        } else {
            allowedEnd = LocalTime.of(21, 0);
        }

        if (start.isBefore(allowedStart) || end.isAfter(allowedEnd)) {
            throw new RuntimeException(
                    "Booking outside allowed hours. Allowed window: " + allowedStart + " to " + allowedEnd);
        }
    }

    private boolean hasConflict(int roomId, LocalDate eventDate, LocalTime eventStart, LocalTime eventEnd) {
        for (Booking booking : bookingsById.values()) {
            if (booking.getStatus() == Booking.Status.CANCELLED) {
                continue;
            }

            if (booking.getRoomId() != roomId) {
                continue;
            }

            Event bookedEvent = eventsById.get(booking.getEventId());
            if (bookedEvent == null) {
                continue;
            }

            LocalDate bookedDate = parseDate(bookedEvent.getEventDate());
            if (!bookedDate.equals(eventDate)) {
                continue;
            }

            LocalTime bookedStart = parseTime(bookedEvent.getEventStarttime());
            LocalTime bookedEnd = parseTime(bookedEvent.getEventEndtime());

            boolean overlaps = eventStart.isBefore(bookedEnd) && eventEnd.isAfter(bookedStart);
            if (overlaps) {
                return true;
            }
        }

        return false;
    }

    private LocalDate parseDate(String dateText) {
        DateTimeFormatter[] dateFormats = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };

        for (DateTimeFormatter formatter : dateFormats) {
            try {
                return LocalDate.parse(dateText.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new RuntimeException("Invalid date format: " + dateText + ". Use yyyy-MM-dd or MM/dd/yyyy.");
    }

    private LocalTime parseTime(String timeText) {
        DateTimeFormatter[] timeFormats = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("h:mma"),
                DateTimeFormatter.ofPattern("h:mm a")
        };

        String normalizedTime = timeText.trim().toUpperCase();

        for (DateTimeFormatter formatter : timeFormats) {
            try {
                return LocalTime.parse(normalizedTime, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new RuntimeException("Invalid time format: " + timeText + ". Examples: 14:30 or 2:30 PM.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
