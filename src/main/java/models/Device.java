package models;

import java.sql.Timestamp;

public class Device {
    private int deviceId;
    private int userId;
    private String ownerName;
    private String ownerEmail;
    private String deviceName;
    private String deviceType;     // LAPTOP, DESKTOP, SMARTPHONE, TABLET, SERVER, IOT_DEVICE
    private String serialNumber;
    private String macAddress;
    private String operatingSystem;
    private String ipAddress;
    private Timestamp registrationDate;
    private String status;         // ACTIVE, SEIZED, QUARANTINED, DECOMMISSIONED
    private String notes;

    public Device() {}

    public Device(int deviceId, int userId, String ownerName, String ownerEmail, 
                  String deviceName, String deviceType, String serialNumber, 
                  String macAddress, String operatingSystem, String ipAddress, 
                  Timestamp registrationDate, String status, String notes) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.macAddress = macAddress;
        this.operatingSystem = operatingSystem;
        this.ipAddress = ipAddress;
        this.registrationDate = registrationDate;
        this.status = status;
        this.notes = notes;
    }

    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return deviceName + " [" + serialNumber + "] (" + deviceType + ")";
    }
}
