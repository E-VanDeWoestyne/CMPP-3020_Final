import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Create sample rooms
            Room room1 = new Room(1, "101", "Building A", 50, Arrays.asList("Projector", "Whiteboard"));
            Room room2 = new Room(2, "102", "Building A", 30, Arrays.asList("Whiteboard"));
            Room room3 = new Room(3, "201", "Building B", 100, Arrays.asList("Projector", "Sound System", "Whiteboard"));

            List<Room> rooms = Arrays.asList(room1, room2, room3);

            // Create sample events
            Event event1 = new Event(1, "Computer Science Club", "Workshop on AI", "2026-04-25", "10:00", "12:00", 40, "AI workshop", Arrays.asList("Projector"));
            Event event2 = new Event(2, "Math Club", "Study Session", "2026-04-25", "14:00", "16:00", 25, "Math study", Arrays.asList("Whiteboard"));
            Event event3 = new Event(3, "Engineering Club", "Presentation", "2026-04-26", "09:00", "11:00", 80, "Engineering presentation", Arrays.asList("Projector", "Sound System"));

            List<Event> events = Arrays.asList(event1, event2, event3);

            // No initial bookings
            List<Booking> bookings = Arrays.asList();

            // Create scheduler
            Scheduler scheduler = new Scheduler(rooms, events, bookings);

            // Register verified clubs
            scheduler.registerVerifiedClub("Computer Science Club");
            scheduler.registerVerifiedClub("Math Club");
            scheduler.registerVerifiedClub("Engineering Club");

            System.out.println("=== Smart Campus Event Scheduler ===\n");

            // Display available rooms
            System.out.println("Available Rooms:");
            for (Room room : rooms) {
                System.out.println(room);
            }
            System.out.println();

            // Try to book events
            System.out.println("Booking Events:");
            try {
                Booking booking1 = scheduler.assign_room(event1);
                System.out.println("Booked: " + booking1);
            } catch (Exception e) {
                System.out.println("Failed to book event1: " + e.getMessage());
            }

            try {
                Booking booking2 = scheduler.assign_room(event2);
                System.out.println("Booked: " + booking2);
            } catch (Exception e) {
                System.out.println("Failed to book event2: " + e.getMessage());
            }

            try {
                Booking booking3 = scheduler.assign_room(event3);
                System.out.println("Booked: " + booking3);
            } catch (Exception e) {
                System.out.println("Failed to book event3: " + e.getMessage());
            }

            System.out.println();

            // Display current schedule
            System.out.println("Current Schedule:");
            List<Booking> schedule = scheduler.get_schedule();
            for (Booking booking : schedule) {
                System.out.println(booking);
            }

            System.out.println();

            // Try to cancel a booking
            if (!schedule.isEmpty()) {
                System.out.println("Cancelling first booking:");
                Booking cancelled = scheduler.manage_booking(schedule.get(0).getId(), "cancel");
                System.out.println("Cancelled: " + cancelled);
            }

            System.out.println();

            // Display updated schedule
            System.out.println("Updated Schedule:");
            schedule = scheduler.get_schedule();
            for (Booking booking : schedule) {
                System.out.println(booking);
            }

            System.out.println();

            // Try to book another event now that a room is free
            Event event4 = new Event(4, "Computer Science Club", "Follow-up Workshop", "2026-04-25", "13:00", "15:00", 45, "Follow-up AI workshop", Arrays.asList("Projector"));
            try {
                Booking booking4 = scheduler.assign_room(event4);
                System.out.println("Booked new event: " + booking4);
            } catch (Exception e) {
                System.out.println("Failed to book event4: " + e.getMessage());
            }

        } catch (InvalidDataException e) {
            System.out.println("Error creating room: " + e.getMessage());
        }
    }
}