package model;

/** A course offered by the Faculty during an academic period. */
public class Course {
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credits;
    private String componentType;
    private String description;
    private Integer periodId;

    public Course() { }

    public Course(int courseId, String courseCode, String courseName, int credits,
                  String componentType, String description, Integer periodId) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.componentType = componentType;
        this.description = description;
        this.periodId = periodId;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getComponentType() { return componentType; }
    public void setComponentType(String componentType) { this.componentType = componentType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPeriodId() { return periodId; }
    public void setPeriodId(Integer periodId) { this.periodId = periodId; }
}
