package exceptions;

import java.util.List;

public class EquipmentMissingException extends Exception {
    public EquipmentMissingException(String room_id, List<String> missing_items) {
        super("EQUIPMENT ERROR: Room " + room_id + " lacks required items: " + missing_items.toString());
    }
}