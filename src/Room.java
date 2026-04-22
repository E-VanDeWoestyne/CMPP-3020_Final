import exceptions.InvalidDataException;
import java.util.List;

public class Room {
	private final int id;
	private final String room_no;
	private final String building;
	private final int capacity;
	private boolean isBooked;
	private final List<String> equipment;

	public Room(int id, String room_no, String building, int capacity, List<String> equipment) throws InvalidDataException {
		if (id <= 0) {
			throw new InvalidDataException("Room id must be greater than 0");
		}
		if (room_no == null || room_no.trim().isEmpty()) {
			throw new InvalidDataException("Room number is required");
		}
		if (building == null || building.trim().isEmpty()) {
			throw new InvalidDataException("Building is required");
		}
		if (capacity <= 0) {
			throw new InvalidDataException("Capacity must be greater than 0");
		}
		if (equipment == null) {
			throw new InvalidDataException("Room equipment list cannot be null");
		}

		this.id = id;
		this.room_no = room_no;
		this.building = building;
		this.capacity = capacity;
		this.equipment = equipment;
	}

	public int getId() {
		return id;
	}

	public String getRoomNo() {
		return room_no;
	}

	public String getBuilding() {
		return building;
	}

	public int getCapacity() {
		return capacity;
	}

	public List<String> getEquipment() {
		return equipment;
	}

	public boolean canBook(int requiredCapacity, List<String> requiredEquipment) {
		return this.capacity >= requiredCapacity && this.equipment.containsAll(requiredEquipment) && !this.isBooked;
	}

	@Override
	public String toString() {
		return "Room{" +
				"id='" + id + '\'' +
				", capacity=" + capacity +
				", equipment=" + equipment +
				'}';
	}
}
