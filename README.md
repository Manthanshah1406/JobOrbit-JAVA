# 🚀 JobOrbit — Job Portal Management System

A **console-based Job Portal** built in Java that connects **Job Seekers** and **Recruiters** through a MySQL-backed system. It features user authentication, job posting & applications, resume management, candidate shortlisting, real-time chat, and recruiter invitations — all powered by custom data structures.

---

## 📁 Project Structure

```
JobOrbit/
├── src/
│   ├── model/                        # All entity/domain classes
│   │   ├── User.java                 # Base user entity (auth, profile, chat, settings)
│   │   ├── Seeker.java               # Job Seeker — extends User
│   │   ├── Recruiter.java            # Recruiter — extends User
│   │   ├── Job.java                  # Job listing entity
│   │   ├── JobApplication.java       # Job application entity
│   │   └── Resume.java               # Resume metadata entity
│   │
│   ├── datastructures/               # Custom data structure implementations
│   │   ├── JobUtils.java             # Sorting & search utilities (BubbleSort, PQ, Stack)
│   │   ├── CustomHashMap.java        # Custom HashMap using separate chaining
│   │   └── HashNode.java             # Node class for CustomHashMap
│   │
│   └── Main.java                     # Application entry point
│
├── resumes/                          # Local resume file storage
├── README.md
└── JobOrbit.iml
```

---

## 🛠️ Tech Stack

| Layer         | Technology                  |
|---------------|-----------------------------|
| Language      | Java (JDK 8+)               |
| Database      | MySQL                       |
| Connectivity  | JDBC (Java Database Connectivity) |
| IDE           | IntelliJ IDEA               |
| Data Structures | Custom HashMap, LinkedList, Stack, PriorityQueue, Bubble Sort |

---

## 🗄️ Database Setup

The project uses **two MySQL databases**:

| Database          | Purpose                              |
|-------------------|--------------------------------------|
| `Job_Orbit`       | Main application data                |
| `Job_Orbit_Logs`  | Login/logout audit logs              |

### Tables in `Job_Orbit`

| Table                    | Description                              |
|--------------------------|------------------------------------------|
| `users`                  | All registered users (seekers + recruiters) |
| `seekers`                | Seeker-specific profile data             |
| `recruiters`             | Recruiter company & designation info     |
| `jobs_new`               | Job listings posted by recruiters        |
| `jobs_new_applications`  | Job applications submitted by seekers    |
| `shortlist`              | Shortlisted candidates by recruiters     |
| `messages`               | Recruiter-to-seeker invitations          |
| `chat`                   | Real-time chat messages                  |
| `resumes`                | Resume metadata + file BLOB storage      |

### Tables in `Job_Orbit_Logs`

| Table           | Description                  |
|-----------------|------------------------------|
| `seeker_logs`   | Seeker login/logout history  |
| `recruiter_logs`| Recruiter login/logout history|

---

## 🌐 Starting XAMPP (MySQL)

This project uses **MySQL via XAMPP**. Follow these steps to start it before running the app:

### Step-by-step

1. **Open XAMPP Control Panel**
   - Search for `XAMPP` in the Start Menu and open **XAMPP Control Panel**

2. **Start Apache & MySQL**
   - Click **Start** next to **Apache**
   - Click **Start** next to **MySQL**
   - Both should turn **green** — this means they are running

   ![XAMPP Control Panel](https://www.apachefriends.org/images/xampp-logo-ac950edf.svg)

3. **Open phpMyAdmin** *(to verify / create databases)*
   - Click the **Admin** button next to MySQL, or open your browser and go to:
   ```
   http://localhost:3306/phpmyadmin
   ```

4. **Create the required databases**
   - In phpMyAdmin, click **New** and create:
     - `Job_Orbit`
     - `Job_Orbit_Logs`
   - Then import or run your SQL scripts to create all tables

5. **Default credentials** used in `Main.java`:
   ```
   Host     : localhost:3306
   Username : root
   Password : (empty — leave blank for XAMPP default)
   ```
   > If you set a MySQL password in XAMPP, update `dbPass` in `Main.java` accordingly.

6. **Stop when done**
   - Click **Stop** next to MySQL and Apache in XAMPP Control Panel when you're finished

---

## ▶️ How to Run

### Prerequisites
- Java JDK 8 or above
- MySQL Server running locally
- MySQL JDBC Driver (Connector/J) in classpath

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/JobOrbit.git
   cd JobOrbit
   ```

2. **Set up MySQL databases**
   - Create `Job_Orbit` and `Job_Orbit_Logs` databases
   - Run the SQL scripts to create all required tables (see schema above)
   - Make sure MySQL is running on `localhost:3306`
   - Default credentials used in code: `user = root`, `password = ""` *(change as needed)*

3. **Configure DB credentials** in `src/Main.java`:
   ```java
   String dbUrl  = "jdbc:mysql://localhost:3306/Job_Orbit";
   String dbUser = "root";
   String dbPass = "your_password";
   ```

4. **Compile and run**
   ```bash
   javac -cp .;path/to/mysql-connector.jar src/Main.java src/model/*.java src/datastructures/*.java
   java  -cp .;path/to/mysql-connector.jar Main
   ```

---

## 🔄 Project Flow

```
Application Start
       │
       ▼
  Enter Email
  (Validated: must end with @gmail.com & contain ≥3 digits)
       │
       ├─── New User ──────► Registration
       │                        ├── Name, Password, Role (Seeker/Recruiter)
       │                        ├── Security Question & Answer
       │                        ├── [Seeker] Skills, Location, Experience, Resume Upload
       │                        └── [Recruiter] Company Name, Designation
       │
       └─── Existing User ──► Login (Password / Forgot Password via Security Q&A)
                                       │
                    ┌──────────────────┴──────────────────┐
                    │                                     │
              SEEKER MENU                          RECRUITER MENU
                    │                                     │
     ┌──────────────┤              ┌──────────────────────┤
     │              │              │                      │
  Search Jobs    Apply Job      Post Job             Delete/Update Jobs
  View Applied   View Invites   View Posted Jobs     Shortlist Candidates
  Respond to     Settings       Search Seekers       Invite Seekers
  Invitations                   View Resumes         Settings
                                                          │
                         Settings (Both Roles)           │
                              ├── View ChatBox ◄─────────┘
                              ├── View/Edit Profile
                              ├── Change Password
                              ├── Delete Account
                              ├── View Login History
                              └── Security Settings
                                       │
                                    Logout
                          (Login/Logout action logged to Job_Orbit_Logs)
```

---

## ✨ Features

### 👤 Authentication
- Email validation (must end with `@gmail.com` and contain ≥ 3 digits)
- Password validation (min 6 chars, 1 number, 1 special character)
- Security Question & Answer for password recovery
- Login/logout events logged to a separate audit database

### 🔍 Job Seeker
- Search jobs by **title**, **skill**, or browse all
- Sort jobs by **salary (high to low)** using Bubble Sort
- Apply for jobs (with duplicate-application prevention)
- View applied jobs — sorted by salary (PriorityQueue) or view last 3 (Stack)
- View and respond to **recruiter invitations**
- Upload resume (PDF stored as BLOB in DB)

### 🏢 Recruiter
- Post, update, and delete job listings
- View all posted jobs
- Search job seekers by **location** and **skill**
- Sort seekers by **experience** (Bubble Sort)
- Shortlist candidates from pending applications
- Send invitations to seekers with a custom message
- View & download seeker resumes (filtered by branch/field)
- Chat with shortlisted seekers

### ⚙️ Settings (Both Roles)
- View and edit profile
- Change password
- Delete account
- View login/logout history
- Update security question & answer
- In-app ChatBox with shortlisted seekers

---

## 🧩 Custom Data Structures Used

| Structure         | Where Used                                      |
|-------------------|-------------------------------------------------|
| `CustomHashMap`   | Storing shortlisted seekers during ChatBox session |
| `LinkedList`      | Storing job applications for display            |
| `Stack`           | Viewing the last 3 applied jobs                 |
| `PriorityQueue`   | Sorting applications by salary (max-heap)       |
| `Bubble Sort`     | Sorting jobs by salary & seekers by experience  |

---

## 📌 Known Limitations / Future Improvements

- Passwords are stored as **plain text** — should be hashed using BCrypt in production
- DB credentials are **hardcoded** — should be moved to a `.env` or `config.properties` file
- Console-based UI — can be extended to a **REST API** (Spring Boot) or **GUI** (JavaFX)
- Resume BLOB storage works but a **file system or cloud storage** (S3) would be more scalable
- Add **email notifications** for application status updates

---

## 👨‍💻 Author

**Manthan Shah**  
[GitHub](https://github.com/Manthanshah1406) · [LinkedIn](https://www.linkedin.com/in/manthan-shah-a67b72353/)

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
