package models;

import java.sql.Timestamp;

public class EventLog {
    private int logId;
    private Integer userId;
    private String userName;
    private Integer deviceId;
    private String deviceName;
    private Integer locationId;
    private String locationName;
    private String eventType;
    private String ipAddress;
    private String eventDetails;
    private Timestamp eventTimestamp;

    public EventLog() {}

    public EventLog(int logId, Integer userId, String userName, Integer deviceId, 
                    String deviceName, Integer locationId, String locationName, 
                    String eventType, String ipAddress, String eventDetails, 
                    Timestamp eventTimestamp) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.locationId = locationId;
        this.locationName = locationName;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.eventDetails = eventDetails;
        this.eventTimestamp = eventTimestamp;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getDeviceId() { return deviceId; }
    public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getEventDetails() { return eventDetails; }
    public void setEventDetails(String eventDetails) { this.eventDetails = eventDetails; }

    public Timestamp getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Timestamp eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
