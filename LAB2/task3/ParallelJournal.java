package task3;

import java.util.*;

public class ParallelJournal {
    static final int GROUPS = 3;
    static final int STUDENTS_PER_GROUP = 3;
    static final int WEEKS = 5;

    static class Student {
        final String name;
        final List<Integer> grades = Collections.synchronizedList(new ArrayList<>());

        Student(String name) {
            this.name = name;
        }
    }

    static class Group {
        final String name;
        final List<Student> students;

        Group(String name, String... studentNames) {
            this.name = name;
            this.students = new ArrayList<>();
            for (String s : studentNames) {
                students.add(new Student(s));
            }
        }
    }

    static class Journal {
        private final List<Group> groups;

        public Journal(List<Group> groups) {
            this.groups = groups;
        }

        public synchronized void addGrade(String teacher, Student student, int week, int grade) {
            while (student.grades.size() <= week) {
                student.grades.add(null); 
            }
            student.grades.set(week, grade);
            System.out.printf("%s set grade %d for %s at week %d%n", teacher, grade, student.name, week + 1);
        }

        public void printJournal() {
            System.out.println("\n=== FINAL JOURNAL ===");
            for (Group group : groups) {
                System.out.println(group.name + ":");
                for (Student student : group.students) {
                    System.out.println("  " + student.name + ": " + student.grades);
                }
            }
        }
    }

    static class Teacher implements Runnable {
        private final String name;
        private final Journal journal;
        private final List<Group> groups;
        private final int weeks;
        private final Random rand = new Random();

        public Teacher(String name, Journal journal, List<Group> groups, int weeks) {
            this.name = name;
            this.journal = journal;
            this.groups = groups;
            this.weeks = weeks;
        }

        @Override
        public void run() {
            for (int week = 0; week < weeks; week++) {
                for (Group group : groups) {
                    for (Student student : group.students) {
                        int grade = 60 + rand.nextInt(41);
                        journal.addGrade(name, student, week, grade);
                        try {
                            Thread.sleep(rand.nextInt(50)); 
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Group> groups = List.of(
            new Group("Group A", "Alice", "Bob", "Charlie"),
            new Group("Group B", "Diana", "Eve", "Frank"),
            new Group("Group C", "George", "Helen", "Ivan")
        );

        Journal journal = new Journal(groups);

        Thread lecturer = new Thread(new Teacher("Lecturer", journal, groups, WEEKS));
        Thread assistant1 = new Thread(new Teacher("Assistant 1", journal, groups, WEEKS));
        Thread assistant2 = new Thread(new Teacher("Assistant 2", journal, groups, WEEKS));
        Thread assistant3 = new Thread(new Teacher("Assistant 3", journal, groups, WEEKS));

        lecturer.start();
        assistant1.start();
        assistant2.start();
        assistant3.start();

        lecturer.join();
        assistant1.join();
        assistant2.join();
        assistant3.join();

        journal.printJournal();
    }
}
