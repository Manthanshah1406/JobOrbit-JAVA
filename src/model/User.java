package model;

import datastructures.*;

import java.io.*;
import java.util.*;
import java.sql.*;

public class User {

    Scanner sc = new Scanner(System.in);
    public int userId;
    public String name;
    public String email;
    public String password;
    public String role;
    public String resumePath;
    public String securityQuestion;
    public String securityAnswer;
    public static User currentUser;

    public User(int userId, String name, String email, String password, String role,
                String resumePath, String securityQuestion, String securityAnswer) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.resumePath = resumePath;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public User(int userId, String name, String email, String password, String role,
                String securityQuestion, String securityAnswer) {
        this(userId, name, email, password, role, null, securityQuestion, securityAnswer);
    }

    public int getUserId()          { return userId; }
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public String getName()         { return name; }
    public String getEmail()        { return email; }
    public String getRole()         { return role; }

    public void registerUser(String email, Connection conn) {
        Scanner sc = new Scanner(System.in);
        try {
            String name;
            while (true) {
                System.out.print("Enter Name: ");
                name = sc.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty.");
                    continue;
                }
                boolean valid = true;
                for (char c : name.toCharArray()) {
                    if (!Character.isLetter(c) && c != ' ') { valid = false; break; }
                }
                if (!valid) System.out.println("Name must contain only letters and spaces.");
                else break;
            }

            String password, confirmPassword;
            while (true) {
                System.out.print("Enter Password: ");
                password = sc.nextLine().trim();
                boolean isLengthOk = password.length() >= 6;
                boolean hasNumber  = password.matches(".*\\d.*");
                boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");
                if (isLengthOk && hasNumber && hasSpecial) {
                    System.out.print("Re-enter Password: ");
                    confirmPassword = sc.nextLine().trim();
                    if (password.equals(confirmPassword)) break;
                    else System.out.println("Passwords do not match! Please try again.");
                } else {
                    System.out.println("Password must be at least 6 chars, include a number and a special char.");
                }
            }

            String role = "";
            while (true) {
                System.out.println("Select Role:");
                System.out.println("1. Seeker");
                System.out.println("2. Recruiter");
                System.out.print("Enter choice (1/2): ");
                String choice = sc.nextLine().trim();
                if (choice.equals("1"))      { role = "seeker";    break; }
                else if (choice.equals("2")) { role = "recruiter"; break; }
                else System.out.println("Invalid choice. Please enter 1 or 2 only.");
            }
            System.out.println("You selected role: " + role);

            String question = "";
            while (true) {
                System.out.print("Enter security question (no ? needed): ");
                question = sc.nextLine().trim();
                if (question.isEmpty()) System.out.println("Security question cannot be empty.");
                else if (!question.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces allowed.");
                else break;
            }

            String answer = "";
            while (true) {
                System.out.print("Enter security answer: ");
                answer = sc.nextLine().trim();
                if (answer.isEmpty()) System.out.println("Answer cannot be empty.");
                else if (!answer.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces allowed.");
                else break;
            }

            String query = "INSERT INTO users (name, email, password, role, security_question, security_answer) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.setString(5, question);
            ps.setString(6, answer);
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            int generatedUserId = 0;
            if (generatedKeys.next()) generatedUserId = generatedKeys.getInt(1);

            if (role.equalsIgnoreCase("seeker")) {
                String skills;
                while (true) {
                    System.out.print("Enter your skills (comma separated): ");
                    skills = sc.nextLine().trim();
                    if (skills.isEmpty()) System.out.println("Skills cannot be empty.");
                    else if (!skills.matches("^[A-Za-z\\s,]+$")) System.out.println("Only letters, commas, and spaces allowed.");
                    else break;
                }

                String resume_Path = null;
                File src = null;
                while (true) {
                    System.out.print("Enter resume file name (from Documents folder): ");
                    String fileName = sc.nextLine().trim();
                    String docPath = System.getProperty("user.home") + File.separator + "Documents" + File.separator;
                    src = new File(docPath + fileName);
                    if (!src.exists()) System.out.println("File not found in Documents. Please try again.");
                    else { resume_Path = src.getAbsolutePath(); System.out.println("Resume found: " + resume_Path); break; }
                }

                String location;
                while (true) {
                    System.out.print("Enter your location: ");
                    location = sc.nextLine().trim();
                    if (location.isEmpty()) System.out.println("Location cannot be empty.");
                    else if (!location.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces allowed.");
                    else break;
                }

                int experience = -1;
                while (true) {
                    try {
                        System.out.print("Enter Your Experience (years): ");
                        experience = sc.nextInt(); sc.nextLine();
                        if (experience >= 0 && experience <= 50) break;
                        else System.out.println("Experience must be between 0 and 50 years.");
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input! Please enter a number."); sc.nextLine();
                    }
                }

                String sQuery = "INSERT INTO seekers (user_id, skills, resume_path, location, experience) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement sPs = conn.prepareStatement(sQuery);
                sPs.setInt(1, generatedUserId);
                sPs.setString(2, skills);
                FileInputStream fis = new FileInputStream(src);
                sPs.setBinaryStream(3, fis, (int) src.length());
                sPs.setString(4, location);
                sPs.setInt(5, experience);
                sPs.executeUpdate();
                fis.close();

                String sql = "INSERT INTO resumes (user_name, file_data, upload_time, category, branch, field) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";
                System.out.print("Enter category (internship/job): ");
                String category = sc.nextLine().trim().toLowerCase();
                String branch = null, field = null;
                while (!(category.equals("internship") || category.equals("job"))) {
                    System.out.println("Invalid. Please enter 'internship' or 'job'.");
                    System.out.print("Enter category: ");
                    category = sc.nextLine().trim().toLowerCase();
                }
                if (category.equals("internship")) {
                    System.out.print("Enter your branch (e.g., CSE, ECE): ");
                    branch = sc.nextLine().trim();
                    while (branch.isEmpty() || !branch.matches("[A-Za-z ]+")) {
                        System.out.println("Branch must contain only letters and spaces.");
                        System.out.print("Enter your branch: ");
                        branch = sc.nextLine().trim();
                    }
                } else {
                    System.out.print("Enter your job field (e.g., Marketing, Design): ");
                    field = sc.nextLine().trim();
                    while (field.isEmpty() || !field.matches("[A-Za-z ]+")) {
                        System.out.println("Job field must contain only letters and spaces.");
                        System.out.print("Enter your job field: ");
                        field = sc.nextLine().trim();
                    }
                }

                try (PreparedStatement insert = conn.prepareStatement(sql)) {
                    insert.setString(1, name);
                    FileInputStream fis2 = new FileInputStream(src);
                    insert.setBinaryStream(2, fis2, (int) src.length());
                    insert.setString(3, category);
                    insert.setString(4, branch);
                    insert.setString(5, field);
                    int affected = insert.executeUpdate();
                    fis2.close();
                    if (affected > 0) {
                        System.out.println("Resume uploaded successfully.");
                        System.out.println("Registration successful! Now you can Login.");
                    } else {
                        System.out.println("Resume upload failed.");
                    }
                }
            } else if (role.equalsIgnoreCase("recruiter")) {
                System.out.print("Enter your Company Name: ");
                String company = sc.nextLine();

                System.out.println("Select your Designation:");
                System.out.println("1. HR Executive");
                System.out.println("2. Technical Recruiter");
                System.out.println("3. Talent Acquisition Manager");
                System.out.println("4. Recruitment Consultant");
                System.out.println("5. Campus Recruitment Officer");
                System.out.println("6. Other (Enter manually)");

                String designation = "";
                while (true) {
                    try {
                        System.out.print("Enter choice (1-6): ");
                        int ch = sc.nextInt(); sc.nextLine();
                        switch (ch) {
                            case 1: designation = "HR Executive"; break;
                            case 2: designation = "Technical Recruiter"; break;
                            case 3: designation = "Talent Acquisition Manager"; break;
                            case 4: designation = "Recruitment Consultant"; break;
                            case 5: designation = "Campus Recruitment Officer"; break;
                            case 6: System.out.print("Enter your designation: "); designation = sc.nextLine(); break;
                            default: System.out.println("Invalid choice! Try again."); continue;
                        }
                        break;
                    } catch (InputMismatchException e) {
                        System.out.println("Please enter a number between 1-6."); sc.nextLine();
                    }
                }

                String rQuery = "INSERT INTO recruiters (user_id, company_name, designation) VALUES (?, ?, ?)";
                PreparedStatement rPs = conn.prepareStatement(rQuery);
                rPs.setInt(1, generatedUserId);
                rPs.setString(2, company);
                rPs.setString(3, designation);
                rPs.executeUpdate();
                System.out.println("Registration successful! Now you can Login.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Email already registered. Try logging in.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public boolean isUserRegistered(String email, Connection conn) {
        String call = "{ ? = call is_user_registered(?) }";
        try (CallableStatement cs = conn.prepareCall(call)) {
            cs.registerOutParameter(1, java.sql.Types.BOOLEAN);
            cs.setString(2, email);
            cs.execute();
            return cs.getBoolean(1);
        } catch (SQLException e) {
            System.out.println("Error checking registration: " + e.getMessage());
            return false;
        }
    }

    public User login(String email, String password, Connection conn) {
        try {
            String query = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { System.out.println("No account found with this email."); return null; }

                    int userId      = rs.getInt("user_id");
                    String name     = rs.getString("name");
                    String role     = rs.getString("role");
                    String dbPassword = rs.getString("password");
                    String question = rs.getString("security_question");
                    String answer   = rs.getString("security_answer");
                    Scanner sc = new Scanner(System.in);

                    if (dbPassword.equals(password)) {
                        if ("seeker".equalsIgnoreCase(role)) {
                            String seekerQuery = "SELECT skills, location, experience, resume_path FROM seekers WHERE user_Id = ?";
                            try (PreparedStatement seekerPs = conn.prepareStatement(seekerQuery)) {
                                seekerPs.setInt(1, userId);
                                try (ResultSet seekerRs = seekerPs.executeQuery()) {
                                    String skills = null, location = null, resumePath = null;
                                    int experience = 0;
                                    if (seekerRs.next()) {
                                        skills     = seekerRs.getString("skills");
                                        location   = seekerRs.getString("location");
                                        experience = seekerRs.getInt("experience");
                                        resumePath = seekerRs.getString("resume_path");
                                    }
                                    return new Seeker(userId, name, email, dbPassword, role,
                                            resumePath, question, answer, skills, location, experience);
                                }
                            }
                        } else if ("recruiter".equalsIgnoreCase(role)) {
                            return new Recruiter(userId, name, email, dbPassword, role, question, answer);
                        } else {
                            return new User(userId, name, email, dbPassword, role, question, answer);
                        }
                    } else {
                        System.out.println("Invalid password.");
                        System.out.print("Forgot Password? (yes/no): ");
                        String choice = sc.nextLine();
                        if (choice.equalsIgnoreCase("yes")) {
                            System.out.println("Security Question: " + question);
                            System.out.print("Enter your answer: ");
                            String userAnswer = sc.nextLine();
                            if (userAnswer.equalsIgnoreCase(answer)) {
                                while (true) {
                                    System.out.print("Enter New Password: ");
                                    String newPassword = sc.nextLine().trim();
                                    if (newPassword.length() < 6) { System.out.println("Min 6 characters."); continue; }
                                    if (!newPassword.matches(".*\\d.*")) { System.out.println("Must include a number."); continue; }
                                    if (!newPassword.matches(".*[^a-zA-Z0-9].*")) { System.out.println("Must include a special char."); continue; }
                                    System.out.print("Re-enter New Password: ");
                                    String confirmPassword = sc.nextLine().trim();
                                    if (newPassword.equals(confirmPassword)) {
                                        String uQuery = "UPDATE users SET password = ? WHERE user_id = ?";
                                        try (PreparedStatement uPs = conn.prepareStatement(uQuery)) {
                                            uPs.setString(1, newPassword);
                                            uPs.setInt(2, userId);
                                            uPs.executeUpdate();
                                        }
                                        System.out.println("Password updated successfully. Please login again.");
                                        return null;
                                    } else { System.out.println("Passwords do not match! Try again."); }
                                }
                            } else { System.out.println("Incorrect answer. Cannot reset password."); return null; }
                        }
                    }
                }
            }
        } catch (SQLException e) { System.out.println("Database error during login: " + e.getMessage()); }
        catch (Exception e)      { System.out.println("Unexpected error: " + e.getMessage()); }
        return null;
    }

    public static User logUserByRole(User user, String action) {
        String url    = "jdbc:mysql://localhost:3306/Job_Orbit_Logs";
        String dbUser = "root";
        String dbPass = "";
        String query;
        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            if (user.getRole().equalsIgnoreCase("seeker"))
                query = "INSERT INTO seeker_logs (user_id, name, email, action) VALUES (?, ?, ?, ?)";
            else if (user.getRole().equalsIgnoreCase("recruiter"))
                query = "INSERT INTO recruiter_logs (user_id, name, email, action) VALUES (?, ?, ?, ?)";
            else { System.out.println("Unknown role: not logging."); return user; }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, user.getUserId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, action.toUpperCase());
                int rows = ps.executeUpdate();
                if (rows > 0) System.out.println("User " + action + " successfully!");
                else System.out.println("Logging failed.");
            }

            if (user.getRole().equalsIgnoreCase("recruiter")) {
                String recruiterQuery = "SELECT recruiter_id FROM job_orbit.recruiters WHERE user_id = ?";
                try (PreparedStatement recPs = conn.prepareStatement(recruiterQuery)) {
                    recPs.setInt(1, user.getUserId());
                    try (ResultSet rs = recPs.executeQuery()) {
                        if (rs.next()) ((Recruiter) user).setRecruiterId(rs.getInt("recruiter_id"));
                    }
                }
            }
            User.setCurrentUser(user);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate log entry: " + e.getMessage());
        } catch (SQLException e) { System.out.println("Database error while logging: " + e.getMessage()); }
        catch (Exception e)      { System.out.println("Unexpected error: " + e.getMessage()); }
        return user;
    }

    public static void showCommonOptions(String userName) {
        System.out.println("1. View ChatBox");
        System.out.println("2. View Profile");
        System.out.println("3. Edit Profile");
        System.out.println("4. Change Password");
        System.out.println("5. Delete Account");
        System.out.println("6. View Login History");
        System.out.println("7. Security Settings");
        System.out.println("8. Back to main menu");
    }

    public void viewChatBoxForRecruiter(Connection conn, int loggedInUserId) {
        Scanner sc = new Scanner(System.in);
        try {
            int recruiterId = getRecruiterIdByUserId(loggedInUserId, conn);
            String sql = "SELECT DISTINCT sk.seeker_id, u.user_id, u.name " +
                    "FROM shortlist s " +
                    "JOIN seekers sk ON s.seekerId = sk.seeker_id " +
                    "JOIN users u ON sk.user_id = u.user_id " +
                    "WHERE s.recruiterId = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();

            CustomHashMap<Integer, String> seekers = new CustomHashMap<>();
            System.out.println("===== ChatBox: Shortlisted Seekers =====");
            if (!rs.isBeforeFirst()) {
                System.out.println("No shortlisted seekers found.");
                rs.close(); ps.close(); return;
            }
            while (rs.next()) {
                int seekerUserId = rs.getInt("user_id");
                String seekerName = rs.getString("name");
                seekers.put(seekerUserId, seekerName);
                System.out.println("SeekerID: " + rs.getInt("seeker_id") + " | UserID: " + seekerUserId + " | Name: " + seekerName);
            }
            rs.close(); ps.close();

            int selectedSeekerUserId = -1;
            while (true) {
                System.out.print("\nEnter the Seeker UserID to chat with (0 to go back): ");
                String input = sc.nextLine().trim();
                if (input.isEmpty() || input.equals("0")) { System.out.println("Returning to menu..."); return; }
                try {
                    selectedSeekerUserId = Integer.parseInt(input);
                    if (!seekers.containsKey(selectedSeekerUserId)) System.out.println("Invalid Seeker UserID.");
                    else break;
                } catch (NumberFormatException e) { System.out.println("Enter a valid numeric UserID."); }
            }

            String seekerName = seekers.get(selectedSeekerUserId);
            System.out.println("\nChat started with " + seekerName + ". Type 'exit' to stop chatting.");

            String fetchChat = "SELECT senderId, message, sentAt FROM chat " +
                    "WHERE (senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?) ORDER BY sentAt ASC";
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchChat)) {
                fetchStmt.setInt(1, loggedInUserId); fetchStmt.setInt(2, selectedSeekerUserId);
                fetchStmt.setInt(3, selectedSeekerUserId); fetchStmt.setInt(4, loggedInUserId);
                try (ResultSet chatRs = fetchStmt.executeQuery()) {
                    System.out.println("\nChat History:");
                    while (chatRs.next()) {
                        int senderId = chatRs.getInt("senderId");
                        String senderLabel = (senderId == loggedInUserId) ? "You" : seekerName;
                        System.out.println(senderLabel + " (" + chatRs.getString("sentAt") + "): " + chatRs.getString("message"));
                    }
                }
            }
            System.out.println("--------------------------------");

            while (true) {
                System.out.print("You: ");
                String message = sc.nextLine().trim();
                if (message.equalsIgnoreCase("exit")) { System.out.println("Chat ended."); break; }
                String insertChat = "INSERT INTO chat (senderId, receiverId, message, sentAt) VALUES (?, ?, ?, NOW())";
                try (PreparedStatement chatStmt = conn.prepareStatement(insertChat)) {
                    chatStmt.setInt(1, loggedInUserId); chatStmt.setInt(2, selectedSeekerUserId);
                    chatStmt.setString(3, message); chatStmt.executeUpdate();
                }
                System.out.println("Message sent.");
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); e.printStackTrace(); }
    }

    public static int getRecruiterIdByUserId(int userId, Connection conn) {
        String sql = "SELECT recruiter_id FROM recruiters WHERE user_id = ?";
        int recruiterId = -1;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) recruiterId = rs.getInt("recruiter_id");
            }
        } catch (SQLException e) { System.out.println("Error fetching recruiterId for userId: " + userId); }
        return recruiterId;
    }

    public void viewChatBoxForSeeker(Connection conn, int userId) {
        Scanner sc = new Scanner(System.in);
        try {
            String sql = "SELECT DISTINCT r.recruiter_id, u.user_id, u.name " +
                    "FROM shortlist s " +
                    "JOIN recruiters r ON s.recruiterId = r.recruiter_id " +
                    "JOIN users u ON r.user_id = u.user_id " +
                    "WHERE s.seekerId = (SELECT seeker_id FROM seekers WHERE user_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    CustomHashMap<Integer, String> recruiters = new CustomHashMap<>();
                    System.out.println("===== ChatBox: Recruiters Who Shortlisted You =====");
                    if (!rs.isBeforeFirst()) { System.out.println("No recruiters have shortlisted you yet."); return; }
                    while (rs.next()) {
                        int recruiterUserId = rs.getInt("user_id");
                        String recruiterName = rs.getString("name");
                        recruiters.put(recruiterUserId, recruiterName);
                        System.out.println("RecruiterID: " + rs.getInt("recruiter_id") + " | UserID: " + recruiterUserId + " | Name: " + recruiterName);
                    }

                    int selectedRecruiterUserId = -1;
                    while (true) {
                        System.out.print("\nEnter the Recruiter UserID to chat with: ");
                        try {
                            selectedRecruiterUserId = sc.nextInt(); sc.nextLine();
                            if (recruiters.containsKey(selectedRecruiterUserId)) break;
                            System.out.println("Invalid recruiter UserID. Try again.");
                        } catch (InputMismatchException e) { System.out.println("Please enter a valid number."); sc.nextLine(); }
                    }

                    String recruiterName = recruiters.get(selectedRecruiterUserId);
                    System.out.println("\nChat started with " + recruiterName + ". Type 'exit' to stop chatting.");

                    String fetchChat = "SELECT senderId, message, sentAt FROM chat " +
                            "WHERE (senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?) ORDER BY sentAt ASC";
                    try (PreparedStatement fetchStmt = conn.prepareStatement(fetchChat)) {
                        fetchStmt.setInt(1, userId); fetchStmt.setInt(2, selectedRecruiterUserId);
                        fetchStmt.setInt(3, selectedRecruiterUserId); fetchStmt.setInt(4, userId);
                        try (ResultSet chatRs = fetchStmt.executeQuery()) {
                            System.out.println("\nChat History:");
                            while (chatRs.next()) {
                                int senderId = chatRs.getInt("senderId");
                                String senderLabel = (senderId == userId) ? "You" : recruiterName;
                                System.out.println(senderLabel + " (" + chatRs.getString("sentAt") + "): " + chatRs.getString("message"));
                            }
                        }
                    }
                    System.out.println("--------------------------------");

                    while (true) {
                        System.out.print("You: ");
                        String message = sc.nextLine().trim();
                        if (message.equalsIgnoreCase("exit")) { System.out.println("Chat ended."); break; }
                        String insertChat = "INSERT INTO chat (senderId, receiverId, message, sentAt) VALUES (?, ?, ?, NOW())";
                        try (PreparedStatement chatStmt = conn.prepareStatement(insertChat)) {
                            chatStmt.setInt(1, userId); chatStmt.setInt(2, selectedRecruiterUserId);
                            chatStmt.setString(3, message); chatStmt.executeUpdate();
                        }
                        System.out.println("Message sent.");
                    }
                }
            }
        } catch (SQLException e) { System.out.println("Database error in ChatBox."); e.printStackTrace(); }
    }

    public void viewProfile(int userId, Connection conn) {
        PreparedStatement ps = null, ps1 = null;
        ResultSet rs = null, rs1 = null;
        try {
            ps = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("=== Your Profile ===");
                System.out.println("Name : " + rs.getString("name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Role : " + rs.getString("role"));
                if (getRole().equalsIgnoreCase("Seeker")) {
                    ps1 = conn.prepareStatement("SELECT * FROM seekers WHERE user_id = ?");
                    ps1.setInt(1, userId); rs1 = ps1.executeQuery();
                    if (rs1.next()) {
                        System.out.println("Skills     : " + rs1.getString("skills"));
                        System.out.println("Resume Path: " + rs1.getString("resume_path"));
                        System.out.println("Location   : " + rs1.getString("location"));
                        System.out.println("Experience : " + rs1.getInt("experience"));
                    }
                } else {
                    ps1 = conn.prepareStatement("SELECT * FROM recruiters WHERE user_id = ?");
                    ps1.setInt(1, userId); rs1 = ps1.executeQuery();
                    if (rs1.next()) {
                        System.out.println("Company Name: " + rs1.getString("company_name"));
                        System.out.println("Designation : " + rs1.getString("designation"));
                    }
                }
            } else { System.out.println("Profile not found."); }
        } catch (SQLException e) { System.out.println("Error fetching profile: " + e.getMessage()); }
        finally {
            try { if (rs1 != null) rs1.close(); if (ps1 != null) ps1.close();
                  if (rs  != null) rs.close();  if (ps  != null) ps.close(); }
            catch (SQLException e) { System.out.println("Error closing resources."); }
        }
    }

    public void editProfile(int userId, Connection conn) {
        Scanner sc = new Scanner(System.in);
        try {
            PreparedStatement psUser = conn.prepareStatement("SELECT name, email FROM users WHERE user_id = ?");
            psUser.setInt(1, userId);
            ResultSet rsUser = psUser.executeQuery();
            String oldName = "", oldEmail = "";
            if (rsUser.next()) { oldName = rsUser.getString("name"); oldEmail = rsUser.getString("email"); }

            System.out.println("\n===== CURRENT PROFILE =====");
            System.out.println("Name: " + oldName + "  |  Email: " + oldEmail);

            String newName;
            while (true) {
                System.out.print("Enter new name (Enter to keep): ");
                newName = sc.nextLine().trim();
                if (newName.isEmpty()) { newName = oldName; break; }
                else if (!newName.matches("^[A-Za-z\\s]+$")) System.out.println("Only letters and spaces.");
                else break;
            }
            String newEmail;
            while (true) {
                System.out.print("Enter new email (Enter to keep): ");
                newEmail = sc.nextLine().trim().toLowerCase();
                if (newEmail.isEmpty()) { newEmail = oldEmail; break; }
                else if (newEmail.endsWith("@gmail.com") && newEmail.chars().filter(Character::isDigit).count() >= 3) break;
                else System.out.println("Must end with '@gmail.com' and contain at least 3 digits.");
            }
            if (!newName.equals(oldName) || !newEmail.equals(oldEmail)) {
                PreparedStatement updateUser = conn.prepareStatement("UPDATE users SET name = ?, email = ? WHERE user_id = ?");
                updateUser.setString(1, newName); updateUser.setString(2, newEmail); updateUser.setInt(3, userId);
                updateUser.executeUpdate();
            }
            System.out.println("Profile updated successfully!");
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void changePassword(int userId, Connection conn) {
        try {
            System.out.print("Enter current password: ");
            String currentPassword = sc.nextLine().trim();
            String checkQuery = "SELECT password, security_question, security_answer FROM users WHERE user_id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
                checkPs.setInt(1, userId);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String secQ = rs.getString("security_question");
                    String secA = rs.getString("security_answer");
                    boolean resetAllowed = false;
                    if (storedPassword.equals(currentPassword)) {
                        resetAllowed = true;
                    } else {
                        System.out.println("Incorrect current password.");
                        System.out.print("Forgot Password? (yes/no): ");
                        String choice = sc.nextLine().trim().toLowerCase();
                        if (choice.equals("yes")) {
                            System.out.println("Security Question: " + secQ);
                            System.out.print("Your Answer: ");
                            String userAns = sc.nextLine().trim();
                            if (userAns.equalsIgnoreCase(secA)) { System.out.println("Verified. Set a new password."); resetAllowed = true; }
                            else { System.out.println("Incorrect answer."); return; }
                        } else return;
                    }
                    if (resetAllowed) {
                        String newPassword;
                        while (true) {
                            System.out.print("Enter new password: ");
                            newPassword = sc.nextLine().trim();
                            if (newPassword.length() < 6) { System.out.println("Min 6 characters."); continue; }
                            if (!newPassword.matches(".*\\d.*")) { System.out.println("Must include a number."); continue; }
                            if (!newPassword.matches(".*[^a-zA-Z0-9].*")) { System.out.println("Must include a special char."); continue; }
                            System.out.print("Re-enter new password: ");
                            String confirmPassword = sc.nextLine().trim();
                            if (!newPassword.equals(confirmPassword)) { System.out.println("Passwords do not match."); continue; }
                            break;
                        }
                        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password = ? WHERE user_id = ?")) {
                            ps.setString(1, newPassword); ps.setInt(2, userId);
                            if (ps.executeUpdate() > 0) System.out.println("Password changed successfully.");
                            else System.out.println("Failed to change password.");
                        }
                    }
                }
            }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void deleteAccount(int userId, Connection conn) {
        try {
            String confirm = "";
            while (true) {
                System.out.print("Are you sure you want to delete your account? (yes/no): ");
                confirm = sc.nextLine().trim();
                if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("no")) break;
                else System.out.println("Please enter (yes/no) only.");
            }
            if (confirm.equalsIgnoreCase("yes")) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?");
                ps.setInt(1, userId);
                if (ps.executeUpdate() > 0) { System.out.println("Account deleted successfully."); System.exit(0); }
                else System.out.println("Failed to delete account.");
            } else { System.out.println("Account deletion cancelled."); }
        } catch (SQLException e) { System.out.println("Database error: " + e.getMessage()); }
    }

    public void viewLoginHistory(int userId, String role, Connection conn) {
        try {
            String logTable;
            if (role.equalsIgnoreCase("seeker")) logTable = "seeker_logs";
            else if (role.equalsIgnoreCase("recruiter")) logTable = "recruiter_logs";
            else { System.out.println("Invalid role."); return; }

            String query = "SELECT action, login_time FROM Job_Orbit_Logs." + logTable + " WHERE user_id = ? ORDER BY login_time DESC";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                System.out.println("\n--- Login History ---");
                boolean hasLogs = false;
                while (rs.next()) {
                    hasLogs = true;
                    System.out.println(rs.getString("action") + " at " + rs.getTimestamp("login_time"));
                }
                if (!hasLogs) System.out.println("No login/logout history found.");
            }
        } catch (SQLException e) { System.out.println("Error fetching login history: " + e.getMessage()); }
    }

    public void viewOrUpdateSecuritySettings(Connection conn) {
        Scanner sc = new Scanner(System.in);
        int choice = -1;
        do {
            try {
                System.out.println("\n--- Security Settings ---");
                System.out.println("1. View Security Question & Answer");
                System.out.println("2. Update Security Question & Answer");
                System.out.println("3. Back to Menu");
                System.out.print("Enter your choice: ");
                if (sc.hasNextInt()) {
                    choice = sc.nextInt(); sc.nextLine();
                    switch (choice) {
                        case 1: viewSecurityDetails(conn); break;
                        case 2: updateSecurityDetails(conn); break;
                        case 3: System.out.println("Returning to main menu..."); break;
                        default: System.out.println("Invalid choice. Please enter 1-3.");
                    }
                } else { System.out.println("Invalid input."); sc.nextLine(); }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); sc.nextLine(); }
        } while (choice != 3);
    }

    public void viewSecurityDetails(Connection conn) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT security_question, security_answer FROM users WHERE user_id = ?");
            ps.setInt(1, this.userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Security Question: " + rs.getString("security_question"));
                System.out.println("Security Answer  : " + rs.getString("security_answer"));
            } else { System.out.println("No security details found."); }
            rs.close(); ps.close();
        } catch (SQLException e) { System.out.println("Error retrieving security details: " + e.getMessage()); }
    }

    public void updateSecurityDetails(Connection conn) {
        Scanner sc = new Scanner(System.in);
        String question, answer;
        while (true) {
            System.out.print("Enter new security question: ");
            question = sc.nextLine().trim();
            if (question.isEmpty()) { System.out.println("Cannot be empty."); continue; }
            boolean valid = true;
            for (char c : question.toCharArray()) { if (!Character.isLetter(c) && c != ' ') { valid = false; break; } }
            if (!valid) System.out.println("Only letters and spaces.");
            else break;
        }
        while (true) {
            System.out.print("Enter new security answer: ");
            answer = sc.nextLine().trim();
            if (answer.isEmpty()) { System.out.println("Cannot be empty."); continue; }
            boolean valid = true;
            for (char c : answer.toCharArray()) { if (!Character.isLetter(c) && c != ' ') { valid = false; break; } }
            if (!valid) System.out.println("Only letters and spaces.");
            else break;
        }
        try {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET security_question = ?, security_answer = ? WHERE user_id = ?")) {
                ps.setString(1, question); ps.setString(2, answer); ps.setInt(3, this.userId);
                if (ps.executeUpdate() > 0) System.out.println("Security settings updated successfully.");
                else System.out.println("No user found with that ID.");
            }
        } catch (SQLException e) { System.out.println("Error updating security details: " + e.getMessage()); }
    }
}
