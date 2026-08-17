package model;

import datastructures.JobUtils;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import java.io.IOException;

public class Seeker extends User {
    public String skills;
    public String location;
    public int experience;
    static final String RESUME_FOLDER = "resumes";

    public Seeker(int userId, String name, String email, String password, String role,
                  String resumePath, String securityQuestion, String securityAnswer,
                  String skills, String location, int experience) {
        super(userId, name, email, password, role, resumePath, securityQuestion, securityAnswer);
        this.skills = skills;
        this.location = location;
        this.experience = experience;
    }

    public int getSeekerId(Connection conn) throws SQLException {
        String query = "SELECT seeker_id FROM seekers WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, this.getUserId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("seeker_id");
        else throw new SQLException("Seeker profile not found for user_id: " + this.getUserId());
    }

    public String getSkills()           { return skills; }
    public String getLocation()         { return location; }
    public void setLocation(String l)   { this.location = l; }
    public int getExperience()          { return experience; }
    public void setExperience(int e)    { this.experience = e; }

    @Override
    public String toString() {
        return super.toString() + "\nLocation: " + location + "\nExperience: " + experience + " years";
    }

    public void jobSeekerMenu(Connection conn) throws Exception {
        int choice;
        String userName = "";
        do {
            try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE user_id = ?")) {
                ps.setInt(1, getUserId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) userName = rs.getString("name");
            }

            System.out.println("\n===== Job Seeker Menu: " + userName + " =====");
            System.out.println("1. Search Jobs");
            System.out.println("2. Apply for Jobs");
            System.out.println("3. View Applied Jobs");
            System.out.println("4. View Recruiter Invitations");
            System.out.println("5. Respond to Pending Invitations");
            System.out.println("6. Settings");
            System.out.println("7. Logout");

            while (true) {
                System.out.print("Enter your choice: ");
                String input = sc.nextLine().trim();
                try { choice = Integer.parseInt(input); break; }
                catch (NumberFormatException e) { System.out.println("Invalid input! Please enter a numeric value."); }
            }

            switch (choice) {
                case 1: printJobs(conn); break;
                case 2: applyForJob(conn); break;
                case 3: viewAppliedJobs(conn); break;
                case 4: viewInvitations(conn); break;
                case 5: respondToInvitation(conn); break;
                case 6:
                    showCommonOptions(userName);
                    int sub_choice;
                    while (true) {
                        System.out.print("Enter choice: ");
                        String subInput = sc.nextLine().trim();
                        try { sub_choice = Integer.parseInt(subInput); break; }
                        catch (NumberFormatException e) { System.out.println("Invalid input! Please enter a numeric value."); }
                    }
                    switch (sub_choice) {
                        case 1: viewChatBoxForSeeker(conn, getCurrentUser().getUserId()); break;
                        case 2: viewProfile(getUserId(), conn); break;
                        case 3: editProfile(getUserId(), conn); break;
                        case 4: changePassword(getUserId(), conn); break;
                        case 5: deleteAccount(getUserId(), conn); break;
                        case 6: viewLoginHistory(getUserId(), getRole(), conn); break;
                        case 7: viewOrUpdateSecuritySettings(conn); break;
                        case 8: System.out.println("Back to the main menu"); break;
                        default: System.out.println("Invalid choice!");
                    }
                    break;
                case 7:
                    logUserByRole(this, "logout");
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 7);
    }

    public void printJobs(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM jobs_new")) {

            List<Job> jobsList = new ArrayList<>();
            while (rs.next()) {
                jobsList.add(new Job(
                        rs.getInt("job_id"), rs.getString("title"),
                        rs.getString("description"), rs.getString("location"),
                        rs.getDouble("salary"), rs.getString("skills_required"),
                        rs.getInt("recruiter_id")));
            }

            if (jobsList.isEmpty()) { System.out.println("No jobs available."); return; }

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("\n1. View All Jobs");
                System.out.println("2. Search Job by Title");
                System.out.println("3. Search Job by Skill");
                System.out.println("4. Sort Jobs by Salary (High to Low)");
                System.out.println("5. Exit to Main Menu");
                System.out.print("Choose option: ");

                int opt;
                try { opt = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid input!"); continue; }

                switch (opt) {
                    case 1: jobsList.forEach(System.out::println); break;
                    case 2:
                        String titleKey;
                        while (true) {
                            System.out.print("Enter title keyword: ");
                            titleKey = sc.nextLine().trim().toLowerCase();
                            if (titleKey.isEmpty()) System.out.println("Keyword cannot be empty.");
                            else if (!titleKey.matches("[a-zA-Z\\s+]+")) System.out.println("Only letters and spaces.");
                            else break;
                        }
                        JobUtils.printByTitle(titleKey, jobsList); break;
                    case 3:
                        String skillKey;
                        while (true) {
                            System.out.print("Enter skill: ");
                            skillKey = sc.nextLine().trim().toLowerCase();
                            if (skillKey.isEmpty()) System.out.println("Skill cannot be empty.");
                            else if (!skillKey.matches("[a-zA-Z\\s+]+")) System.out.println("Only letters and spaces.");
                            else break;
                        }
                        JobUtils.printBySkills(skillKey, jobsList); break;
                    case 4: JobUtils.bubbleSort(jobsList); break;
                    case 5: System.out.println("Returning to main menu..."); return;
                    default: System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void viewAppliedJobs(Connection conn) {
        try {
            int seekerId = getSeekerId(conn);
            String query = "SELECT j.job_id, j.title, j.description, j.location, j.salary, " +
                    "j.skills_required, ja.application_date, r.company_name " +
                    "FROM jobs_new_applications ja " +
                    "JOIN jobs_new j ON ja.job_id = j.job_id " +
                    "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                    "WHERE ja.seeker_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, seekerId);
                ResultSet rs = pstmt.executeQuery();
                LinkedList<JobApplication> applications = new LinkedList<>();
                while (rs.next()) {
                    applications.add(new JobApplication(
                            rs.getString("title"), rs.getString("company_name"),
                            rs.getString("location"), rs.getDouble("salary"),
                            rs.getString("skills_required"), rs.getTimestamp("application_date")));
                }
                rs.close();

                if (applications.isEmpty()) { System.out.println("No applications found."); return; }

                Scanner sc = new Scanner(System.in);
                while (true) {
                    System.out.println("\n===== Applied Jobs Menu =====");
                    System.out.println("1. View All Applications");
                    System.out.println("2. View Sorted by Salary (High to Low)");
                    System.out.println("3. View Last 3 Applied Jobs");
                    System.out.println("4. Return to Main Menu");
                    System.out.print("Choose option: ");

                    int choice;
                    try { choice = Integer.parseInt(sc.nextLine().trim()); }
                    catch (NumberFormatException e) { System.out.println("Invalid input!"); continue; }

                    switch (choice) {
                        case 1: applications.forEach(System.out::println); break;
                        case 2: JobUtils.appliedSortBySalary(applications); break;
                        case 3: JobUtils.appliedLastThree(applications); break;
                        case 4: System.out.println("Returning..."); return;
                        default: System.out.println("Invalid choice.");
                    }
                }
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void applyForJob(Connection conn) {
        Scanner sc = new Scanner(System.in);
        int seekerId = -1;
        try {
            PreparedStatement seekerPs = conn.prepareStatement("SELECT seeker_id FROM seekers WHERE user_id = ?");
            seekerPs.setInt(1, this.getUserId());
            ResultSet seekerRs = seekerPs.executeQuery();
            if (seekerRs.next()) seekerId = seekerRs.getInt("seeker_id");
            else { System.out.println("No seeker profile found."); return; }
            seekerRs.close(); seekerPs.close();
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); return; }

        try (PreparedStatement jobStmt = conn.prepareStatement(
                "SELECT job_id, title, description, location, salary FROM jobs_new");
             ResultSet jobRs = jobStmt.executeQuery()) {

            System.out.println("Available Jobs:");
            boolean hasJobs = false;
            while (jobRs.next()) {
                hasJobs = true;
                System.out.println("Job ID: " + jobRs.getInt("job_id") + " | Title: " + jobRs.getString("title")
                        + " | Location: " + jobRs.getString("location") + " | Salary: " + jobRs.getString("salary"));
                System.out.println("--------------------------------------------------");
            }
            if (!hasJobs) { System.out.println("No jobs available right now."); return; }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); return; }

        while (true) {
            System.out.print("Enter Job ID to apply (0 to return): ");
            int jobId;
            try { jobId = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid input!"); continue; }
            if (jobId == 0) { System.out.println("Returning to menu..."); return; }

            try {
                PreparedStatement checkJobStmt = conn.prepareStatement("SELECT * FROM jobs_new WHERE job_id = ?");
                checkJobStmt.setInt(1, jobId);
                ResultSet jobExists = checkJobStmt.executeQuery();
                if (!jobExists.next()) { System.out.println("Invalid Job ID!"); continue; }
                jobExists.close(); checkJobStmt.close();

                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT * FROM jobs_new_applications WHERE seeker_id = ? AND job_id = ?");
                checkStmt.setInt(1, seekerId); checkStmt.setInt(2, jobId);
                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) { System.out.println("You have already applied for this job."); return; }
                checkRs.close(); checkStmt.close();

                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO jobs_new_applications (seeker_id, job_id, status) VALUES (?, ?, 'pending')");
                insertStmt.setInt(1, seekerId); insertStmt.setInt(2, jobId);
                insertStmt.executeUpdate(); insertStmt.close();
                System.out.println("Application submitted successfully. Status: Pending.");
                break;
            } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
        }
    }

    public void viewInvitations(Connection conn) {
        String query = "SELECT m.message_id, m.message_text AS message, m.job_id, " +
                "u.name AS recruiter_name, j.title AS job_title " +
                "FROM messages m JOIN users u ON m.sender_id = u.user_id " +
                "JOIN jobs_new j ON m.job_id = j.job_id " +
                "WHERE m.receiver_id = ? AND m.status = 'pending'";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, this.getUserId());
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- Your Invitations ---");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(count + ". Recruiter: " + rs.getString("recruiter_name"));
                System.out.println("   Job: " + rs.getString("job_title"));
                System.out.println("   Message: " + rs.getString("message"));
                System.out.println("---------------------------");
            }
            if (count == 0) System.out.println("No new invitations.");
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void respondToInvitation(Connection conn) {
        Scanner sc = new Scanner(System.in);
        String query = "SELECT m.message_id, m.job_id, u.user_id AS recruiterUserId, " +
                "u.name AS recruiter_name, j.title AS job_title " +
                "FROM messages m JOIN users u ON m.sender_id = u.user_id " +
                "JOIN jobs_new j ON m.job_id = j.job_id " +
                "WHERE m.receiver_id = ? AND LOWER(m.status) = 'pending'";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, this.getUserId());
            ResultSet rs = ps.executeQuery();

            List<Integer> messageIds = new ArrayList<>();
            List<Integer> jobIds = new ArrayList<>();
            List<Integer> recruiterUserIds = new ArrayList<>();
            int index = 0;

            System.out.println("\n--- Pending Invitations ---");
            while (rs.next()) {
                index++;
                messageIds.add(rs.getInt("message_id"));
                jobIds.add(rs.getInt("job_id"));
                recruiterUserIds.add(rs.getInt("recruiterUserId"));
                System.out.println(index + ". Recruiter: " + rs.getString("recruiter_name")
                        + " | Job: " + rs.getString("job_title"));
            }
            if (index == 0) { System.out.println("No pending invitations."); return; }

            while (true) {
                System.out.print("Select invitation number to respond (0 to go back): ");
                int choice;
                try { choice = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }
                if (choice == 0) { System.out.println("Going back..."); return; }
                if (choice < 1 || choice > messageIds.size()) { System.out.println("Choice out of range."); continue; }

                int selectedMessageId = messageIds.get(choice - 1);
                int selectedJobId     = jobIds.get(choice - 1);
                int recruiterUserId   = recruiterUserIds.get(choice - 1);

                System.out.println("1. Accept & Apply\n2. Reject");
                System.out.print("Enter choice: ");
                int action;
                try { action = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }

                if (action == 1) {
                    try {
                        int seekerId = -1;
                        try (PreparedStatement sPs = conn.prepareStatement("SELECT seeker_id FROM seekers WHERE user_id = ?")) {
                            sPs.setInt(1, this.getUserId());
                            ResultSet sRs = sPs.executeQuery();
                            if (sRs.next()) seekerId = sRs.getInt("seeker_id");
                            else { System.out.println("No seeker profile found."); return; }
                        }
                        try (PreparedStatement chk = conn.prepareStatement(
                                "SELECT application_id FROM jobs_new_applications WHERE job_id = ? AND seeker_id = ?")) {
                            chk.setInt(1, selectedJobId); chk.setInt(2, seekerId);
                            if (!chk.executeQuery().next()) {
                                try (PreparedStatement ins = conn.prepareStatement(
                                        "INSERT INTO jobs_new_applications (job_id, seeker_id, status) VALUES (?, ?, 'Pending')")) {
                                    ins.setInt(1, selectedJobId); ins.setInt(2, seekerId);
                                    ins.executeUpdate();
                                    System.out.println("Successfully applied for the job.");
                                }
                            } else { System.out.println("Already applied. Auto-shortlisting..."); }
                        }
                        int recruiterId = -1;
                        try (PreparedStatement rPs = conn.prepareStatement("SELECT recruiter_id FROM recruiters WHERE user_id = ?")) {
                            rPs.setInt(1, recruiterUserId);
                            ResultSet rRs = rPs.executeQuery();
                            if (rRs.next()) recruiterId = rRs.getInt("recruiter_id");
                        }
                        try (PreparedStatement slPs = conn.prepareStatement(
                                "INSERT INTO shortlist (recruiterId, seekerId, jobId, shortlistedAt) VALUES (?, ?, ?, NOW())")) {
                            slPs.setInt(1, recruiterId); slPs.setInt(2, seekerId); slPs.setInt(3, selectedJobId);
                            slPs.executeUpdate();
                        }
                        try (PreparedStatement upPs = conn.prepareStatement(
                                "UPDATE messages SET status = 'Accepted' WHERE message_id = ?")) {
                            upPs.setInt(1, selectedMessageId); upPs.executeUpdate();
                        }
                        System.out.println("Invitation accepted!");
                    } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
                    return;
                } else if (action == 2) {
                    try (PreparedStatement rejPs = conn.prepareStatement(
                            "UPDATE messages SET status = 'Rejected' WHERE message_id = ?")) {
                        rejPs.setInt(1, selectedMessageId); rejPs.executeUpdate();
                        System.out.println("Invitation rejected.");
                    } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
                    return;
                } else { System.out.println("Invalid option — only 1 or 2."); }
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }
}
