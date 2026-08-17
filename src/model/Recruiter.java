package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Recruiter extends User {
    public int recruiterId;

    public Recruiter(int userId, String name, String email, String password,
                     String role, String securityQuestion, String securityAnswer) {
        super(userId, name, email, password, role, securityQuestion, securityAnswer);
    }

    public void setRecruiterId(int recruiterId) { this.recruiterId = recruiterId; }
    public int getRecruiterId()                  { return this.recruiterId; }

    public void recruiterMenu(Connection conn) {
        Scanner sc = new Scanner(System.in);
        int choice = -1;
        do {
            try {
                String recruiterName = null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) recruiterName = rs.getString("name");
                }

                System.out.println("\n===== Recruiter Menu: " + recruiterName + " =====");
                System.out.println("1. Post a Job");
                System.out.println("2. Delete Posted Jobs");
                System.out.println("3. Update Posted Jobs");
                System.out.println("4. View Posted Jobs");
                System.out.println("5. Shortlist Candidates");
                System.out.println("6. Search Seekers");
                System.out.println("7. Invite Seekers");
                System.out.println("8. View Resumes of Seekers");
                System.out.println("9. Settings");
                System.out.println("10. Logout");
                System.out.print("Enter your choice (1-10): ");

                if (!sc.hasNextInt()) { System.out.println("Invalid input!"); sc.nextLine(); continue; }
                choice = sc.nextInt(); sc.nextLine();
                if (choice < 1 || choice > 10) { System.out.println("Please enter 1-10."); continue; }

                switch (choice) {
                    case 1:  postJob(conn); break;
                    case 2:  deleteJobPost(conn); break;
                    case 3:  updateJobPost(conn); break;
                    case 4:  viewPostedJobs(conn); break;
                    case 5:  handleShortlistCandidates(conn); break;
                    case 6:  searchSeekers(conn); break;
                    case 7:  inviteSeeker(conn); break;
                    case 8:  viewResumes(sc, conn); break;
                    case 9:
                        showCommonOptions(recruiterName);
                        System.out.print("Enter Choice: ");
                        if (!sc.hasNextInt()) { System.out.println("Invalid input!"); sc.nextLine(); break; }
                        int sub = sc.nextInt(); sc.nextLine();
                        switch (sub) {
                            case 1: viewChatBoxForRecruiter(conn, getCurrentUser().getUserId()); break;
                            case 2: viewProfile(getUserId(), conn); break;
                            case 3: editProfile(getUserId(), conn); break;
                            case 4: changePassword(getUserId(), conn); break;
                            case 5: deleteAccount(getUserId(), conn); break;
                            case 6: viewLoginHistory(getUserId(), getRole(), conn); break;
                            case 7: viewOrUpdateSecuritySettings(conn); break;
                            case 8: System.out.println("Back to main menu..."); break;
                            default: System.out.println("Invalid choice!");
                        }
                        break;
                    case 10:
                        logUserByRole(this, "logout");
                        System.out.println("Logging out...");
                        return;
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); sc.nextLine(); }
        } while (choice != 10);
    }

    public static void viewResumes(Scanner scanner, Connection conn) throws SQLException, IOException {
        while (true) {
            System.out.println("\n=== View Resumes ===");
            System.out.println("1. Internship Resumes by Branch");
            System.out.println("2. Job Resumes by Field");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Enter branch (e.g., CSE, ECE): ");
                    String branch = scanner.nextLine();
                    PreparedStatement stmt1 = conn.prepareStatement(
                            "SELECT id, user_name, upload_time, category, branch, field FROM resumes WHERE category = 'internship' AND branch = ?");
                    stmt1.setString(1, branch);
                    ResultSet rs1 = stmt1.executeQuery();
                    System.out.println("\n--- Internship Resumes | Branch: " + branch + " ---");
                    boolean found1 = false;
                    while (rs1.next()) {
                        found1 = true;
                        System.out.println(new Resume(rs1.getInt("id"), rs1.getString("user_name"),
                                rs1.getTimestamp("upload_time"), rs1.getString("category"),
                                rs1.getString("branch"), rs1.getString("field")));
                    }
                    if (!found1) { System.out.println("No resumes found."); break; }
                    System.out.print("Enter Resume ID to download: ");
                    downloadResume(Integer.parseInt(scanner.nextLine()), conn);
                    break;

                case 2:
                    System.out.print("Enter job field (e.g., Marketing): ");
                    String field = scanner.nextLine();
                    PreparedStatement stmt2 = conn.prepareStatement(
                            "SELECT id, user_name, upload_time, category, branch, field FROM resumes WHERE category = 'job' AND field = ?");
                    stmt2.setString(1, field);
                    ResultSet rs2 = stmt2.executeQuery();
                    System.out.println("\n--- Job Resumes | Field: " + field + " ---");
                    boolean found2 = false;
                    while (rs2.next()) {
                        found2 = true;
                        System.out.println(new Resume(rs2.getInt("id"), rs2.getString("user_name"),
                                rs2.getTimestamp("upload_time"), rs2.getString("category"),
                                rs2.getString("branch"), rs2.getString("field")));
                    }
                    if (!found2) { System.out.println("No resumes found."); break; }
                    System.out.print("Enter Resume ID to download: ");
                    downloadResume(Integer.parseInt(scanner.nextLine()), conn);
                    break;

                case 3: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private static void downloadResume(int resumeId, Connection conn) throws SQLException, IOException {
        try (PreparedStatement dStmt = conn.prepareStatement("SELECT user_name, file_data FROM resumes WHERE id = ?")) {
            dStmt.setInt(1, resumeId);
            ResultSet drs = dStmt.executeQuery();
            if (drs.next()) {
                String userName  = drs.getString("user_name");
                byte[] fileBytes = drs.getBytes("file_data");
                if (fileBytes != null) {
                    String filePath = System.getProperty("user.home") + File.separator + "Downloads"
                            + File.separator + userName + "_resume.pdf";
                    try (FileOutputStream fos = new FileOutputStream(filePath)) { fos.write(fileBytes); }
                    System.out.println("Resume downloaded: " + filePath);
                } else { System.out.println("No file data found."); }
            } else { System.out.println("Resume ID not found."); }
        }
    }

    public static void shortlistCandidates(Connection conn, int recruiterId) {
        Scanner sc = new Scanner(System.in);
        try {
            String pendingSql = "SELECT a.application_id, a.job_id, j.title, u.name, u.email, a.status " +
                    "FROM jobs_new_applications a " +
                    "JOIN jobs_new j ON a.job_id = j.job_id " +
                    "JOIN seekers sk ON a.seeker_id = sk.seeker_id " +
                    "JOIN users u ON sk.user_id = u.user_id " +
                    "WHERE j.recruiter_id = ? AND LOWER(a.status) = 'pending' " +
                    "ORDER BY a.job_id, a.application_id";

            Set<Integer> validJobIds = new HashSet<>();
            try (PreparedStatement ps = conn.prepareStatement(pendingSql)) {
                ps.setInt(1, recruiterId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.isBeforeFirst()) { System.out.println("No pending applications found."); return; }
                    System.out.println("\n--- Pending Applications ---");
                    int lastJobId = -1;
                    while (rs.next()) {
                        int jobId = rs.getInt("job_id");
                        validJobIds.add(jobId);
                        if (jobId != lastJobId) {
                            System.out.println("Job ID: " + jobId + " | Title: " + rs.getString("title"));
                            lastJobId = jobId;
                        }
                        System.out.println("  App ID: " + rs.getInt("application_id")
                                + " | " + rs.getString("name") + " | " + rs.getString("email"));
                    }
                }
            }

            int jobId;
            while (true) {
                System.out.print("Enter Job ID to shortlist for (0 to return): ");
                String in = sc.nextLine().trim();
                if ("0".equals(in)) return;
                if (!in.matches("\\d+")) { System.out.println("Invalid Job ID."); continue; }
                jobId = Integer.parseInt(in);
                if (!validJobIds.contains(jobId)) { System.out.println("Invalid Job ID. Choose from: " + validJobIds); continue; }
                break;
            }

            Set<Integer> validAppIds = new HashSet<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.application_id, u.name, u.email FROM jobs_new_applications a " +
                    "JOIN seekers sk ON a.seeker_id = sk.seeker_id JOIN users u ON sk.user_id = u.user_id " +
                    "WHERE a.job_id = ? AND LOWER(a.status) = 'pending' ORDER BY a.application_id")) {
                ps.setInt(1, jobId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("\nPending for Job ID " + jobId + ":");
                    if (!rs.isBeforeFirst()) { System.out.println("None."); return; }
                    while (rs.next()) {
                        int appId = rs.getInt("application_id");
                        validAppIds.add(appId);
                        System.out.println("  App ID: " + appId + " | " + rs.getString("name") + " | " + rs.getString("email"));
                    }
                }
            }

            System.out.print("Enter Application IDs to shortlist (comma-separated, 0 to cancel): ");
            String input = sc.nextLine().trim();
            if ("0".equals(input)) return;

            int count = 0;
            for (String token : input.split(",")) {
                String t = token.trim();
                if (t.isEmpty() || !t.matches("\\d+")) { System.out.println("Invalid: " + t); continue; }
                int appId = Integer.parseInt(t);
                if (!validAppIds.contains(appId)) { System.out.println("App ID " + appId + " not valid. Skipping."); continue; }

                Integer seekerId = null;
                try (PreparedStatement psGet = conn.prepareStatement(
                        "SELECT seeker_id FROM jobs_new_applications WHERE application_id = ? AND job_id = ? AND LOWER(status) = 'pending'")) {
                    psGet.setInt(1, appId); psGet.setInt(2, jobId);
                    ResultSet rsGet = psGet.executeQuery();
                    if (rsGet.next()) seekerId = rsGet.getInt("seeker_id");
                }
                if (seekerId == null) { System.out.println("App ID " + appId + " not found or not pending. Skipping."); continue; }

                try (PreparedStatement psUpd = conn.prepareStatement(
                        "UPDATE jobs_new_applications SET status = 'SHORTLISTED' WHERE application_id = ?")) {
                    psUpd.setInt(1, appId); psUpd.executeUpdate();
                }
                try (PreparedStatement psIns = conn.prepareStatement(
                        "INSERT INTO shortlist (recruiterId, seekerId, jobId, shortlistedAt) VALUES (?, ?, ?, NOW())")) {
                    psIns.setInt(1, recruiterId); psIns.setInt(2, seekerId); psIns.setInt(3, jobId);
                    psIns.executeUpdate(); count++;
                    System.out.println("App ID " + appId + " shortlisted successfully.");
                } catch (SQLException dup) { System.out.println("App ID " + appId + " already in shortlist. Skipping."); }
            }
            System.out.println("\n(" + count + ") candidate(s) shortlisted!");
        } catch (SQLException e) { System.out.println("SQL Error: " + e.getMessage()); }
    }

    public void handleShortlistCandidates(Connection conn) {
        try {
            int userId = getCurrentUser().getUserId();
            try (PreparedStatement ps = conn.prepareStatement("SELECT recruiter_id FROM recruiters WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) shortlistCandidates(conn, rs.getInt("recruiter_id"));
                else System.out.println("You are not registered as a recruiter.");
            }
        } catch (SQLException e) { System.out.println("SQL Error: " + e.getMessage()); }
    }

    public List<Integer> searchSeekers(Connection conn) {
        Scanner sc = new Scanner(System.in);
        List<Seeker> seekers = new ArrayList<>();
        List<Integer> seekerIds = new ArrayList<>();
        try {
            String searchLocation = "";
            while (true) {
                System.out.print("Enter location to search seekers: ");
                searchLocation = sc.nextLine().trim();
                if (searchLocation.isEmpty()) System.out.println("Location cannot be empty.");
                else if (!searchLocation.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }
            String searchSkill = "";
            while (true) {
                System.out.print("Enter required skill (Enter to skip): ");
                searchSkill = sc.nextLine().trim();
                if (searchSkill.isEmpty()) break;
                else if (!searchSkill.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }

            String query = searchSkill.isEmpty()
                    ? "SELECT s.user_id, u.name, u.email, s.skills, s.location, s.experience FROM seekers s JOIN users u ON s.user_id = u.user_id WHERE s.location = ?"
                    : "SELECT s.user_id, u.name, u.email, s.skills, s.location, s.experience FROM seekers s JOIN users u ON s.user_id = u.user_id WHERE s.location = ? AND s.skills LIKE ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, searchLocation);
                if (!searchSkill.isEmpty()) ps.setString(2, "%" + searchSkill + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    seekers.add(new Seeker(rs.getInt("user_id"), rs.getString("name"), rs.getString("email"),
                            "", "seeker", null, null, null, rs.getString("skills"),
                            rs.getString("location"), rs.getInt("experience")));
                    seekerIds.add(rs.getInt("user_id"));
                }
            }

            if (seekers.isEmpty()) { System.out.println("No seekers found."); return new ArrayList<>(); }

            System.out.println("\n--- Seekers Found ---");
            for (Seeker s : seekers)
                System.out.println("ID: " + s.getUserId() + " | " + s.getName() + " | " + s.getLocation()
                        + " | " + s.getExperience() + " yrs" + (s.getSkills() != null ? " | " + s.getSkills() : ""));

            System.out.println("\nSort by experience: 1. High to Low  2. Low to High  0. Skip");
            System.out.print("Choose: ");
            int sortChoice;
            try { sortChoice = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { sortChoice = 0; }

            if (sortChoice == 1 || sortChoice == 2) {
                bubbleSort(seekers, sortChoice == 2);
                System.out.println("\n--- Sorted Seekers ---");
                for (Seeker s : seekers)
                    System.out.println("ID: " + s.getUserId() + " | " + s.getName() + " | " + s.getExperience() + " yrs");
            }
        } catch (SQLException e) { System.out.println("SQL Error: " + e.getMessage()); }
        return seekerIds;
    }

    public void bubbleSort(List<Seeker> seekers, boolean ascending) {
        int n = seekers.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean swap = ascending
                        ? seekers.get(j).getExperience() > seekers.get(j + 1).getExperience()
                        : seekers.get(j).getExperience() < seekers.get(j + 1).getExperience();
                if (swap) {
                    Seeker temp = seekers.get(j);
                    seekers.set(j, seekers.get(j + 1));
                    seekers.set(j + 1, temp);
                }
            }
        }
    }

    public void inviteSeeker(Connection conn) {
        Scanner sc = new Scanner(System.in);
        try {
            List<Integer> seekerUserIds = searchSeekers(conn);
            if (seekerUserIds.isEmpty()) { System.out.println("No seekers found to invite."); return; }

            int seekerUserId = -1;
            while (true) {
                System.out.print("\nEnter Seeker User ID to invite (0 to go back): ");
                try { seekerUserId = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }
                if (seekerUserId == 0) { System.out.println("Returning..."); return; }
                if (seekerUserIds.contains(seekerUserId)) break;
                System.out.println("Invalid Seeker User ID. Try again.");
            }

            Map<Integer, String> availableJobs = new LinkedHashMap<>();
            try (PreparedStatement jobPs = conn.prepareStatement("SELECT job_id, title FROM jobs_new WHERE recruiter_id = ?")) {
                jobPs.setInt(1, this.getRecruiterId());
                ResultSet jobRs = jobPs.executeQuery();
                int idx = 1;
                while (jobRs.next()) {
                    int jobId = jobRs.getInt("job_id");
                    String title = jobRs.getString("title");
                    try (PreparedStatement chk = conn.prepareStatement(
                            "SELECT 1 FROM messages WHERE sender_id = ? AND receiver_id = ? AND job_id = ?")) {
                        chk.setInt(1, this.getUserId()); chk.setInt(2, seekerUserId); chk.setInt(3, jobId);
                        if (!chk.executeQuery().next()) availableJobs.put(idx++, jobId + ":" + title);
                    }
                }
            }

            if (availableJobs.isEmpty()) { System.out.println("Already invited this seeker for all jobs."); return; }

            boolean inviting = true;
            while (inviting && !availableJobs.isEmpty()) {
                System.out.println("\n--- Available Jobs ---");
                for (Map.Entry<Integer, String> entry : availableJobs.entrySet())
                    System.out.println(entry.getKey() + ". " + entry.getValue().split(":", 2)[1]);

                System.out.print("Select job number to invite (0 to go back): ");
                int jobChoice;
                try { jobChoice = Integer.parseInt(sc.nextLine().trim()); }
                catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }
                if (jobChoice == 0) { System.out.println("Returning..."); break; }
                if (!availableJobs.containsKey(jobChoice)) { System.out.println("Invalid choice."); continue; }

                String[] jobData = availableJobs.get(jobChoice).split(":", 2);
                int selectedJobId = Integer.parseInt(jobData[0]);
                String selectedJobTitle = jobData[1];
                String message = "Recruiter " + this.getName() + " is interested in your profile for: " + selectedJobTitle;

                try (PreparedStatement msgPs = conn.prepareStatement(
                        "INSERT INTO messages (sender_id, receiver_id, message_text, job_id, status) VALUES (?, ?, ?, ?, 'pending')")) {
                    msgPs.setInt(1, this.getUserId()); msgPs.setInt(2, seekerUserId);
                    msgPs.setString(3, message); msgPs.setInt(4, selectedJobId);
                    msgPs.executeUpdate();
                    System.out.println("Invitation sent for: " + selectedJobTitle);
                }
                availableJobs.remove(jobChoice);
                if (availableJobs.isEmpty()) { System.out.println("No more jobs available. Returning..."); break; }

                while (true) {
                    System.out.print("Invite for another job? (y/n): ");
                    String more = sc.nextLine().trim().toLowerCase();
                    if (more.equals("y")) break;
                    else if (more.equals("n")) { inviting = false; break; }
                    else System.out.println("Enter 'y' or 'n'.");
                }
            }
        } catch (SQLException e) { System.out.println("SQL Error: " + e.getMessage()); }
    }

    public void postJob(Connection conn) {
        try {
            String title = "", description = "", location = "", skills = "";
            double salary = -1;

            while (true) {
                System.out.print("Enter Job Title: "); title = sc.nextLine().trim();
                if (title.isEmpty()) System.out.println("Cannot be empty.");
                else if (!title.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }
            while (true) {
                System.out.print("Enter Job Description: "); description = sc.nextLine().trim();
                if (description.isEmpty()) System.out.println("Cannot be empty.");
                else if (!description.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }
            while (true) {
                System.out.print("Enter Location: "); location = sc.nextLine().trim();
                if (location.isEmpty()) System.out.println("Cannot be empty.");
                else if (!location.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }
            while (salary <= 0) {
                System.out.print("Enter Salary: ");
                try { salary = Double.parseDouble(sc.nextLine()); if (salary <= 0) System.out.println("Must be > 0."); }
                catch (NumberFormatException e) { System.out.println("Invalid number."); }
            }
            while (true) {
                System.out.print("Enter Required Skills: "); skills = sc.nextLine().trim();
                if (skills.isEmpty()) System.out.println("Cannot be empty.");
                else if (!skills.matches("^[A-Za-z\\s,]+$")) System.out.println("Only letters, spaces, commas.");
                else break;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO jobs_new (title, description, location, salary, skills_required, recruiter_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, title); ps.setString(2, description); ps.setString(3, location);
                ps.setDouble(4, salary); ps.setString(5, skills); ps.setInt(6, this.getRecruiterId());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) System.out.println("Job posted successfully! Job ID: " + keys.getInt(1));
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void deleteJobPost(Connection conn) {
        try {
            try (PreparedStatement psView = conn.prepareStatement(
                    "SELECT job_id, title FROM jobs_new WHERE recruiter_id = ?")) {
                psView.setInt(1, recruiterId);
                ResultSet rs = psView.executeQuery();
                if (!rs.isBeforeFirst()) { System.out.println("No posted jobs to delete."); return; }
                System.out.println("\n=== Your Posted Jobs ===");
                while (rs.next())
                    System.out.println("Job ID: " + rs.getInt("job_id") + " | Title: " + rs.getString("title"));
            }

            int jobId = -1;
            while (true) {
                System.out.print("Enter Job ID to delete (0 to skip): ");
                String input = sc.nextLine().trim();
                if (input.isEmpty() || input.equals("0")) { System.out.println("Skipped."); return; }
                try {
                    jobId = Integer.parseInt(input);
                    if (jobId <= 0) { System.out.println("Enter a positive Job ID."); continue; }
                    try (PreparedStatement chk = conn.prepareStatement(
                            "SELECT COUNT(*) FROM jobs_new WHERE job_id = ? AND recruiter_id = ?")) {
                        chk.setInt(1, jobId); chk.setInt(2, recruiterId);
                        ResultSet rc = chk.executeQuery();
                        if (rc.next() && rc.getInt(1) > 0) break;
                        else System.out.println("Job ID not found or doesn't belong to you.");
                    }
                } catch (NumberFormatException e) { System.out.println("Invalid input."); }
            }

            System.out.print("Confirm delete Job ID " + jobId + "? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) { System.out.println("Cancelled."); return; }

            try (PreparedStatement psApps = conn.prepareStatement("DELETE FROM jobs_new_applications WHERE job_id = ?")) {
                psApps.setInt(1, jobId); psApps.executeUpdate();
            }
            try (PreparedStatement psJob = conn.prepareStatement("DELETE FROM jobs_new WHERE job_id = ? AND recruiter_id = ?")) {
                psJob.setInt(1, jobId); psJob.setInt(2, recruiterId);
                if (psJob.executeUpdate() > 0) System.out.println("Job deleted successfully.");
                else System.out.println("Deletion failed.");
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void updateJobPost(Connection conn) {
        try {
            try (PreparedStatement ps = conn.prepareStatement("SELECT job_id, title FROM jobs_new WHERE recruiter_id = ?")) {
                ps.setInt(1, recruiterId);
                ResultSet rs = ps.executeQuery();
                if (!rs.isBeforeFirst()) { System.out.println("No jobs posted yet."); return; }
                System.out.println("\n--- Your Posted Jobs ---");
                while (rs.next()) System.out.println("Job ID: " + rs.getInt("job_id") + " | " + rs.getString("title"));
            }

            int jobId = -1;
            while (true) {
                System.out.print("Enter Job ID to update (0 to cancel): ");
                try {
                    jobId = Integer.parseInt(sc.nextLine().trim());
                    if (jobId == 0) { System.out.println("Cancelled."); return; }
                    try (PreparedStatement chk = conn.prepareStatement(
                            "SELECT COUNT(*) FROM jobs_new WHERE job_id = ? AND recruiter_id = ?")) {
                        chk.setInt(1, jobId); chk.setInt(2, recruiterId);
                        ResultSet rc = chk.executeQuery();
                        if (rc.next() && rc.getInt(1) > 0) break;
                        else System.out.println("Invalid Job ID.");
                    }
                } catch (NumberFormatException e) { System.out.println("Invalid input."); }
            }

            String title = "", description = "", location = "", skills = "";
            double salary = -1;
            while (true) { System.out.print("New Title: "); title = sc.nextLine().trim(); if (!title.isEmpty() && title.matches("^[A-Za-z\\s]+$")) break; System.out.println("Invalid."); }
            while (true) { System.out.print("New Description: "); description = sc.nextLine().trim(); if (!description.isEmpty() && description.matches("^[A-Za-z\\s]+$")) break; System.out.println("Invalid."); }
            while (true) { System.out.print("New Location: "); location = sc.nextLine().trim(); if (!location.isEmpty() && location.matches("^[A-Za-z\\s]+$")) break; System.out.println("Invalid."); }
            while (salary < 0) { System.out.print("New Salary: "); try { salary = Double.parseDouble(sc.nextLine().trim()); if (salary < 0) System.out.println("Must be positive."); } catch (NumberFormatException e) { System.out.println("Invalid."); } }
            while (true) { System.out.print("New Skills: "); skills = sc.nextLine().trim(); if (!skills.isEmpty() && skills.matches("^[A-Za-z\\s,]+$")) break; System.out.println("Invalid."); }

            try (PreparedStatement upd = conn.prepareStatement(
                    "UPDATE jobs_new SET title=?, description=?, location=?, salary=?, skills_required=? WHERE job_id=? AND recruiter_id=?")) {
                upd.setString(1, title); upd.setString(2, description); upd.setString(3, location);
                upd.setDouble(4, salary); upd.setString(5, skills); upd.setInt(6, jobId); upd.setInt(7, recruiterId);
                if (upd.executeUpdate() > 0) System.out.println("Job updated successfully.");
                else System.out.println("Update failed.");
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void viewPostedJobs(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM jobs_new WHERE recruiter_id = ?")) {
            ps.setInt(1, this.getRecruiterId());
            ResultSet rs = ps.executeQuery();
            System.out.println("\n--- Your Posted Jobs ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Job ID: " + rs.getInt("job_id") + " | Title: " + rs.getString("title")
                        + " | Location: " + rs.getString("location") + " | Salary: " + rs.getDouble("salary"));
                System.out.println("-----------------------------");
            }
            if (!found) System.out.println("No jobs posted yet.");
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }
}
