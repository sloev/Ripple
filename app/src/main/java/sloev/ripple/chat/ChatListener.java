package sloev.ripple.chat;

/**
 * Created by johannes on 14/03/15.
 */
public interface ChatListener {
    public void gpsReceived(int userId, float lat, float lon);
}
