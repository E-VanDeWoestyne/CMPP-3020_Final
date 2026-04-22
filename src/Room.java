import java.util.List;

public class Room {
	private final int id;
	private final String room_no;
	private final int capacity;
	private final List<String> equipment;

	public Room(int id, String room_no, int capacity, List<String> equipment) {
		this.id = id;
		this.room_no = room_no;
		this.capacity = capacity;
		this.equipment = equipment;
	}

	public int getId() {
		return id;
	}

	public String getRoomNo() {
		return room_no;
	}

	public int getCapacity() {
		return capacity;
	}

	public List<String> getEquipment() {
		return equipment;
	}

	public boolean canBook(int requiredCapacity, List<String> requiredEquipment) {
		return this.capacity >= requiredCapacity && this.equipment.containsAll(requiredEquipment);
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