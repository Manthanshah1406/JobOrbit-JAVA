package model;

public class Job {
    public int jobId;
    public String title;
    public String description;
    public String location;
    public double salary;
    public String skills;
    public int recruiterId;

    public Job(int jobId, String title, String description, String location,
               double salary, String skills, int recruiterId) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.skills = skills;
        this.recruiterId = recruiterId;
    }

    @Override
    public String toString() {
        return String.format(
                "Job ID: %-5d | Title: %-20s | Location: %-15s | Salary: %-10.2f | Skills: %-15s | Recruiter ID: %-5d",
                jobId, title, location, salary, skills, recruiterId
        );
    }
}
