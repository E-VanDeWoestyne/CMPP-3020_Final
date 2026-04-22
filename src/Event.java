import exceptions.InvalidDataException;
import java.util.List;

public class Event {
	private final int id;
	private final String club_name;
	private final String event_name;
	private final String event_date;
	private final String event_starttime;
	private final String event_endtime;
	private final int attendees;
	private final String description;
	private final List<String> requiredEquipment;
	private String status = "Pending";

	public Event(int id, String club_name, String event_name, String event_date, String event_starttime, String event_endtime, int attendees, String description, List<String> requiredEquipment) throws InvalidDataException {
		if (id <= 0) {
			throw new InvalidDataException("Event id must be greater than 0");
		}
		if (club_name == null || club_name.trim().isEmpty()) {
			throw new InvalidDataException("Club name is required");
		}
		if (event_name == null || event_name.trim().isEmpty()) {
			throw new InvalidDataException("Event name is required");
		}
		if (event_date == null || event_date.trim().isEmpty()) {
			throw new InvalidDataException("Event date is required");
		}
		if (event_starttime == null || event_starttime.trim().isEmpty()) {
			throw new InvalidDataException("Event start time is required");
		}
		if (event_endtime == null || event_endtime.trim().isEmpty()) {
			throw new InvalidDataException("Event end time is required");
		}
		if (attendees <= 0) {
			throw new InvalidDataException("Attendees must be greater than 0");
		}
		if (requiredEquipment == null) {
			throw new InvalidDataException("Required equipment list cannot be null");
		}

		this.id = id;
		this.club_name = club_name;
		this.event_name = event_name;
		this.event_date = event_date;
		this.event_starttime = event_starttime;
		this.event_endtime = event_endtime;
		this.attendees = attendees;
		this.description = description;
		this.requiredEquipment = requiredEquipment;
	}

	public int getId() {
		return id;
	}

	public String getClubName() {
		return club_name;
	}

	public int getAttendees() {
		return attendees;
	}

	public String getEventName() {
		return event_name;
	}

	public String getEventDate() {
		return event_date;
	}

	public String getEventStarttime() {
		return event_starttime;
	}

	public String getEventEndtime() {
		return event_endtime;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public List<String> getRequiredEquipment() {
		return requiredEquipment;
	}
}