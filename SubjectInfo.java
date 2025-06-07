package basicWeb;

public class SubjectInfo {
	String name;
	String professor;
	String classroom;
	String timeInfo;
    int col, row, height;

    public SubjectInfo(String name, String professor, String classroom, int col, int row, int height, String timeInfo) {
        this.name = name;
        this.professor = professor;
        this.classroom = classroom;
        this.col = col;
        this.row = row;
        this.height = height;
        this.timeInfo = timeInfo;
    }

    public String getName() { return name; }
    public String getProfessor() { return professor; }
    public String getClassroom() { return classroom; }
    public String getTimeInfo() { return timeInfo; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public int getHeight() { return height; }
}
