import java.util.List;

public class Room {
	private final String id;
	private final int capacity;
	private final List<String> equipment;

	public Room(String id, int capacity, List<String> equipment) {
		this.id = id;
		this.capacity = capacity;
		this.equipment = equipment;
	}

	public String getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public List<String> getEquipment() {
		return equipment;
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