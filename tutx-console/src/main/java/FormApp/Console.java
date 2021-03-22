package FormApp;

import java.util.Scanner;

public class Console {
    public static void main(String[] args) {
        System.out.println("Welcome!");
        System.out.println("Pick your choice and press enter.");
        System.out.println("1 - Create University application");
        System.out.println("2 - Confirm Acceptance to University");
        System.out.println("3 - Edit Application");
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();

        switch (choice) {
            case "1":
                handleApplication(input);
                break;
            case "2":
                handleConfirmation(input);
                break;
            case "3":
                editApplication(input);
                break;
            default:
                System.out.println("That isn't part of the available choices.");
                break;
        }
        input.close();
    }

    /**
     * Used to create the process and then trigger the console function for creating/completing the create application
     * task
     * @param input scanner for taking user input
     */
    private static void handleApplication(Scanner input) {
        // Start Application Process and get the task ID for the first task.
        String processID = API.createUniApplicationProcess();
        createApplication(input, processID);
    }

    /**
     * Console app portion for handling editing of the task of a process that was already created
     * @param input scanner for taking user input
     */
    private static void editApplication(Scanner input) {
        System.out.println("Please enter your application ID:");
        String applicationId = input.nextLine();
        if (!API.processExists(applicationId)) {
            System.out.println("Your application does not exist in our system.\nPlease try to create an application " +
                    "first.");
            return;
        }

        if (!API.canCreateApplication(applicationId)) {
            System.out.println("Your application is unable to be edited at the moment as it may be in the process of " +
                    "being reviewed.\nIf your application has already been confirmed, please select the appropriate" +
                    " choice when starting up this application.\n");
            return;
        }
        createApplication(input, applicationId);
    }

    /**
     * Console app portion for handling creation of the application/completing that task
     * @param input scanner for taking user input
     * @param processID processID to complete the task for
     */
    private static void createApplication(Scanner input, String processID) {
        System.out.println("Creating University Application.\n");
        System.out.print("State your GPA: ");
        double gpa = input.nextDouble();
        input.nextLine();
        System.out.printf("GPA: %.2f\n", gpa);
        System.out.println("Write a small essay about why you want to join this university.\n" +
                "Press Enter when you are finished.");
        String essay = input.nextLine();
        System.out.printf("Essay: %s\n", essay);
        API.createApplication(processID, gpa, essay);

        System.out.printf("Your Application ID is: %s.\nPlease write it down and keep it.", processID);
    }

    /**
     * Console app portion for handling confirmation of university acceptance
     * @param input Scanner for taking user input
     */
    private static void handleConfirmation(Scanner input) {
        System.out.println("Please enter your Application ID.");
        String applicationId = input.nextLine();

        if (!API.processExists(applicationId)) {
            System.out.println("Your application does not exist in our system.\nPlease try to create an application " +
                    "first.");
            return;
        }

        if (!API.canConfirmAcceptance(applicationId)) {
            System.out.println("Unfortunately, you weren't accepted to this University.\nYou may try to submit a new" +
                    " application.");
            return;
        }

        String taskId = API.getTaskID(applicationId);
        String body = "{}";
        API.completeTask(taskId, body);
        System.out.println("You've officially been accepted to University!.");
    }
}
