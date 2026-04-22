import java.util.List;

public class Room {
	private final int id;
	private final String room_no;
	private final String building;
	private final int capacity;
	private boolean isBooked;
	private final List<String> equipment;

	public Room(int id, String room_no, String building, int capacity, List<String> equipment) throws InvalidDataException{
		this.id = id;
		this.room_no = room_no;
		this.building = building;
		if(capacity <=0 )
		{
			throw new InvalidDataException("Capactiy can't be negative values or zero");
		}
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