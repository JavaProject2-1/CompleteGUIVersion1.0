package basicWeb;

public class TimeSlotInfo {
	int col, row;
    String day, timeStr;
    
    public TimeSlotInfo(int col, int row, String day, String timeStr) {
        this.col = col;
        this.row = row;
        this.day = day;
        this.timeStr = timeStr;
    }
}
