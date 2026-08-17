import model.Recruiter;
import model.Seeker;
import model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        String dbUrl  = "jdbc:mysql://localhost:3306/Job_Orbit";
        String dbUser = "root";
        String dbPass = "";
        Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);

        System.out.println("====== Welcome to JobOrbit ======");

        User u = new User(0, "", "", "", "", "", "", "");
        System.out.print("Enter Email: ");
        String email;
        while (true) {
            email = sc.nextLine().toLowerCase().trim();
            boolean isValidEmail = email.endsWith("@gmail.com");
            long digits = email.chars().filter(Character::isDigit).count();
            if (isValidEmail && digits >= 3) {
                break;
            } else {
                System.out.println("Invalid email! Must end with '@gmail.com' and contain at least 3 digits.");
            }
        }

        User loggedIn = null;

        if (u.isUserRegistered(email, con)) {
            System.out.println("User already registered. Please login.");
            System.out.print("Enter Your Password: ");
            String password = sc.nextLine().trim();
            loggedIn = u.login(email, password, con);
        } else {
            System.out.println("New user. Continuing with registration...");
            u.registerUser(email, con);
            System.out.print("Enter Your Password: ");
            String password = sc.nextLine().trim();
            loggedIn = u.login(email, password, con);
        }

        if (loggedIn != null) {
            User.logUserByRole(loggedIn, "login");
            String role = loggedIn.getRole();
            if (role.equalsIgnoreCase("seeker")) {
                Seeker seeker = (Seeker) loggedIn;
                seeker.jobSeekerMenu(con);
            } else if (role.equalsIgnoreCase("recruiter")) {
                Recruiter recruiter = (Recruiter) loggedIn;
                recruiter.recruiterMenu(con);
            } else {
                System.out.println("Unknown role: " + role);
            }
        } else {
            System.out.println("Login failed.");
        }

        con.close();
    }
}
