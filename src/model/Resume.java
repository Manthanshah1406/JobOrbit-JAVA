package model;

import java.sql.Timestamp;

public class Resume {
    private int id;
    private String userName;
    private Timestamp uploadTime;
    private String category;
    private String branch;
    private String field;

    public Resume(int id, String userName, Timestamp uploadTime,
                  String category, String branch, String field) {
        this.id = id;
        this.userName = userName;
        this.uploadTime = uploadTime;
        this.category = category;
        this.branch = branch;
        this.field = field;
    }

    @Override
    public String toString() {
        return "Resume [ID=" + id +
                ", User=" + userName +
                ", Uploaded=" + uploadTime +
                ", Category=" + category +
                ", Branch=" + branch +
                ", Field=" + field + "]";
    }
}
