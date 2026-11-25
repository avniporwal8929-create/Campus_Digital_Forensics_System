package models;

public class CampusLocation {
    private int locationId;
    private String buildingName;
    private String roomNumber;
    private String zoneType;
    private String accessLevelRequired;
    private String description;

    public CampusLocation() {}

    public CampusLocation(int locationId, String buildingName, String roomNumber, 
                          String zoneType, String accessLevelRequired, String description) {
        this.locationId = locationId;
        this.buildingName = buildingName;
        this.roomNumber = roomNumber;
        this.zoneType = zoneType;
        this.accessLevelRequired = accessLevelRequired;
        this.description = description;
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }

    public String getAccessLevelRequired() { return accessLevelRequired; }
    public void setAccessLevelRequired(String accessLevelRequired) { this.accessLevelRequired = accessLevelRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return buildingName + " - " + roomNumber + " (" + zoneType + ")";
    }
}
