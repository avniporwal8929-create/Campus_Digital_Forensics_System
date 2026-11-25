package models;

import java.sql.Timestamp;

public class AccessLog {
    private int accessId;
    private int userId;
    private String userName;
    private int locationId;
    private String locationName;
    private String cardNumber;
    private String accessResult;  // GRANTED, DENIED_INVALID_CARD, DENIED_PERMISSION, DENIED_OFF_HOURS
    private Timestamp accessTimestamp;

    public AccessLog() {}

    public AccessLog(int accessId, int userId, String userName, int locationId, 
                     String locationName, String cardNumber, String accessResult, 
                     Timestamp accessTimestamp) {
        this.accessId = accessId;
        this.userId = userId;
        this.userName = userName;
        this.locationId = locationId;
        this.locationName = locationName;
        this.cardNumber = cardNumber;
        this.accessResult = accessResult;
        this.accessTimestamp = accessTimestamp;
    }

    public int getAccessId() { return accessId; }
    public void setAccessId(int accessId) { this.accessId = accessId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getAccessResult() { return accessResult; }
    public void setAccessResult(String accessResult) { this.accessResult = accessResult; }

    public Timestamp getAccessTimestamp() { return accessTimestamp; }
    public void setAccessTimestamp(Timestamp accessTimestamp) { this.accessTimestamp = accessTimestamp; }
}
