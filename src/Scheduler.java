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

import exceptions.CapacityExceededException;
import exceptions.EquipmentMissingException;
import exceptions.InvalidDataException;
import exceptions.RoomUnavailableException;

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

    public boolean validateEvent(Event event) throws InvalidDataException {
        if (event == null) {
            throw new InvalidDataException("event is null");
        }

        if (isBlank(event.getClubName()) || isBlank(event.getEventName()) || isBlank(event.getEventDate())
                || isBlank(event.getEventStarttime()) || isBlank(event.getEventEndtime())) {
            throw new InvalidDataException("required text fields are missing");
        }

        if (event.getAttendees() <= 0) {
            throw new InvalidDataException("attendees must be greater than 0");
        }

        LocalDate date = parseDate(event.getEventDate());
        LocalTime start = parseTime(event.getEventStarttime());
        LocalTime end = parseTime(event.getEventEndtime());

        if (!start.isBefore(end)) {
            throw new InvalidDataException("start time must be before end time");
        }

        validateBookingWindow(date, start, end);
        validateClubVerification(event);

        return true;
    }

    public List<Room> find_available_room(Event event) throws InvalidDataException {
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

    public Booking assign_room(Event event) throws InvalidDataException, RoomUnavailableException,
            CapacityExceededException, EquipmentMissingException {
        List<Room> availableRooms = find_available_room(event);
        if (availableRooms.isEmpty()) {
            throw new RoomUnavailableException("No room matches event requirements for: " + event.getEventName());
        }

        // Auto-pick smallest suitable room by sorted result from find_available_room.
        Room selectedRoom = availableRooms.get(0);
        validateRoomRequirements(selectedRoom, event);

        LocalDate eventDate = parseDate(event.getEventDate());
        LocalTime eventStart = parseTime(event.getEventStarttime());
        LocalTime eventEnd = parseTime(event.getEventEndtime());
        if (hasConflict(selectedRoom.getId(), eventDate, eventStart, eventEnd)) {
            throw new RoomUnavailableException("Selected room conflicts with an existing booking");
        }

        return createConfirmedBooking(event, selectedRoom);
    }

    public Booking assign_room(Event event, int selectedRoomId) throws InvalidDataException,
            CapacityExceededException, EquipmentMissingException, RoomUnavailableException {
        validateEvent(event);

        Room selectedRoom = roomsById.get(selectedRoomId);
        if (selectedRoom == null) {
            throw new RoomUnavailableException("Selected room not found: " + selectedRoomId);
        }

        LocalDate eventDate = parseDate(event.getEventDate());
        LocalTime eventStart = parseTime(event.getEventStarttime());
        LocalTime eventEnd = parseTime(event.getEventEndtime());

        validateRoomRequirements(selectedRoom, event);

        if (hasConflict(selectedRoom.getId(), eventDate, eventStart, eventEnd)) {
            throw new RoomUnavailableException("Selected room conflicts with an existing booking");
        }

        return createConfirmedBooking(event, selectedRoom);
    }

    public Booking manage_booking(int bookingId, String action) throws InvalidDataException {
        Booking booking = bookingsById.get(bookingId);
        if (booking == null) {
            throw new InvalidDataException("booking not found: " + bookingId);
        }

        if (isBlank(action)) {
            throw new InvalidDataException("action is required for manage_booking");
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

        throw new InvalidDataException("unsupported action: " + action + ". Use 'update' or 'cancel'.");
    }

    public List<Booking> get_schedule() {
        List<Booking> allBookings = new ArrayList<>(bookingsById.values());
        allBookings.sort(Comparator.comparing(Booking::getCreatedAt));
        return Collections.unmodifiableList(allBookings);
    }

    private void validateClubVerification(Event event) throws InvalidDataException {
        String normalizedClubName = event.getClubName().trim().toLowerCase();
        if (!verifiedClubs.contains(normalizedClubName)) {
            throw new InvalidDataException("club is not verified: " + event.getClubName());
        }
    }

    private void validateBookingWindow(LocalDate date, LocalTime start, LocalTime end) throws InvalidDataException {
        DayOfWeek day = date.getDayOfWeek();

        LocalTime allowedStart = LocalTime.of(8, 0);
        LocalTime allowedEnd;

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            allowedEnd = LocalTime.of(18, 0);
        } else {
            allowedEnd = LocalTime.of(21, 0);
        }

        if (start.isBefore(allowedStart) || end.isAfter(allowedEnd)) {
            throw new InvalidDataException(
                    "booking outside allowed hours. Allowed window: " + allowedStart + " to " + allowedEnd);
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

            try {
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
            } catch (InvalidDataException ignored) {
                // Invalid existing booking data is treated as conflicting for safety.
                return true;
            }
        }

        return false;
    }

    private LocalDate parseDate(String dateText) throws InvalidDataException {
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

        throw new InvalidDataException("invalid date format: " + dateText + ". Use yyyy-MM-dd or MM/dd/yyyy.");
    }

    private LocalTime parseTime(String timeText) throws InvalidDataException {
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

        throw new InvalidDataException("invalid time format: " + timeText + ". Examples: 14:30 or 2:30 PM.");
    }

    private void validateRoomRequirements(Room room, Event event)
            throws CapacityExceededException, EquipmentMissingException {
        if (room.getCapacity() < event.getAttendees()) {
            throw new CapacityExceededException(
                    room.getRoomNo(),
                    room.getCapacity(),
                    event.getEventName(),
                    event.getAttendees());
        }

        List<String> missingEquipment = new ArrayList<>();
        for (String requiredItem : event.getRequiredEquipment()) {
            if (!room.getEquipment().contains(requiredItem)) {
                missingEquipment.add(requiredItem);
            }
        }

        if (!missingEquipment.isEmpty()) {
            throw new EquipmentMissingException(room.getRoomNo(), missingEquipment);
        }
    }

    private Booking createConfirmedBooking(Event event, Room room) throws InvalidDataException {
        eventsById.put(event.getId(), event);

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(
                nextBookingId++,
                room.getId(),
                event.getId(),
                now,
                now,
                Booking.Status.CONFIRMED);

        bookingsById.put(booking.getId(), booking);
        room.book();
        return booking;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
