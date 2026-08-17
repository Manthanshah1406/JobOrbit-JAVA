package model;

import java.sql.Timestamp;

public class JobApplication {
    public int applicationId;
    public int seekerId;
    public int jobId;
    public String title;
    public String company;
    public String location;
    public double salary;
    public String skillsRequired;
    public Timestamp appliedAt;

    public JobApplication(String title, String company,
                          String location, double salary, String skillsRequired, Timestamp appliedAt) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.skillsRequired = skillsRequired;
        this.appliedAt = appliedAt;
    }

    public int getApplicationId() { return applicationId; }
    public int getSeekerId()      { return seekerId; }
    public int getJobId()         { return jobId; }
    public String getTitle()      { return title; }
    public String getCompany()    { return company; }
    public String getLocation()   { return location; }
    public double getSalary()     { return salary; }
    public String getSkillsRequired() { return skillsRequired; }
    public Timestamp getAppliedAt()   { return appliedAt; }

    @Override
    public String toString() {
        return "Title      : " + title +
                "\nCompany    : " + company +
                "\nLocation   : " + location +
                "\nSalary     : " + salary +
                "\nSkills     : " + skillsRequired +
                "\nApplied At : " + appliedAt +
                "\n--------------------------------------------------";
    }
}
