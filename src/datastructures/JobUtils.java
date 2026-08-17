package datastructures;

import model.*;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

public class JobUtils {

    // Search jobs by title keyword
    public static void printByTitle(String titleKey, List<Job> jobsList) {
        for (Job job : jobsList) {
            if (job.title.toLowerCase().contains(titleKey)) {
                System.out.println(job);
            }
        }
    }

    // Search jobs by skill keyword
    public static void printBySkills(String skillKey, List<Job> jobsList) {
        for (Job job : jobsList) {
            if (job.skills != null && job.skills.toLowerCase().contains(skillKey)) {
                System.out.println(job);
            }
        }
    }

    // Sort jobs by salary high to low using Bubble Sort
    public static void bubbleSort(List<Job> jobsList) {
        for (int i = 0; i < jobsList.size() - 1; i++) {
            for (int j = 0; j < jobsList.size() - i - 1; j++) {
                if (jobsList.get(j).salary < jobsList.get(j + 1).salary) {
                    Job temp = jobsList.get(j);
                    jobsList.set(j, jobsList.get(j + 1));
                    jobsList.set(j + 1, temp);
                }
            }
        }
        jobsList.forEach(System.out::println);
    }

    // Sort applied job applications by salary using PriorityQueue (max-heap)
    public static void appliedSortBySalary(LinkedList<JobApplication> applications) {
        PriorityQueue<JobApplication> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getSalary(), a.getSalary())
        );
        pq.addAll(applications);
        while (!pq.isEmpty()) {
            System.out.println(pq.poll());
            System.out.println("----------------------");
        }
    }

    // Show last 3 applied jobs using Stack
    public static void appliedLastThree(LinkedList<JobApplication> applications) {
        Stack<JobApplication> stack = new Stack<>();
        stack.addAll(applications);
        int count = 0;
        while (!stack.isEmpty() && count < 3) {
            System.out.println(stack.pop());
            System.out.println("----------------------");
            count++;
        }
    }
}
