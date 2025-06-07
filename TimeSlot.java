package basicWeb;

public class TimeSlot {
	final String day;
    final String startTime;
    final String endTime;
    
    public TimeSlot(String day, String startTime, String endTime) {
        this.day = day; 
        this.startTime = startTime; 
        this.endTime = endTime;
    }
    
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
