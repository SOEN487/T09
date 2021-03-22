package FormApp;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class API {

    /**
     * Create university application process
     * @return String of the process ID
     */
    public static String createUniApplicationProcess() {
        // Create closeable http client to execute requests with
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Creating the request to execute
            HttpPost httpPost = new HttpPost(
                    "http://localhost:8080/engine-rest/process-definition/key/UniversityApplication/submit-form");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity("{}"));

            // Executing the request using the http client and obtaining the response
            CloseableHttpResponse response = client.execute(httpPost);
            String jsonResponse = readResponse(response);
            String applicationId = getIdFromJSON(jsonResponse);
            System.out.printf("Your application ID is: %s\nPlease save it somewhere.", applicationId);
            return applicationId;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to get customers";
        }
    }

    /**
     * For reading the response of an HTTP request
     * @param response to read
     * @return String of the response
     * @throws IOException
     */
    public static String readResponse(CloseableHttpResponse response) throws IOException {
        // Handling the IO Stream from the response using scanner
        Scanner sc = new Scanner(response.getEntity().getContent());
        StringBuilder stringResponse = new StringBuilder();
        while (sc.hasNext()) {
            stringResponse.append(sc.nextLine());
            stringResponse.append("\n");
        }
        response.close();
        return stringResponse.toString();
    }

    /**
     * Extract ID from JSON String
     * @param json String
     * @return ID of JSON Object
     */
    public static String getIdFromJSON(String json) {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getString("id");
    }

    /**
     * Method for creating the application (completing the create application task)
     * @param processId ID for the process to create the application for
     * @param gpa Grade of the application
     * @param essay Essay of the application
     */
    public static void createApplication(String processId, double gpa, String essay) {
        String taskId = getTaskID(processId);
        String body = createBodyForVariables(gpa, essay);
        completeTask(taskId, body);
    }

    /**
     * Get task ID of the first task of a given process
     * @param processID of the process to check the first task with
     * @return id of the first task
     */
    public static String getTaskID(String processID) {
        JSONArray jsonArray = getTaskList(processID);
        JSONObject obj = (JSONObject) jsonArray.get(0);
        return obj.getString("id");
    }

    /**
     * Getting the task list of a given process
     * @param processID of the process
     * @return JSON Array of tasks
     */
    private static JSONArray getTaskList(String processID) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String URL = String.format("http://localhost:8080/engine-rest/task/?processInstanceId=%s",
                    processID);
            HttpGet httpGet = new HttpGet(URL);
            CloseableHttpResponse response = client.execute(httpGet);
            String jsonResponse = readResponse(response);
            return new JSONArray(jsonResponse);
        } catch (IOException e ) {
            return new JSONArray("[]");
        }
    }

    /**
     * Create the JSON body for the process variables to be posted when completing the create application task
     * @param gpa process variable
     * @param essay process variable
     * @return JSON String to be posted
     */
    private static String createBodyForVariables(double gpa, String essay) {
        JSONObject variables = new JSONObject();
        JSONObject gpaJson = new JSONObject();
        gpaJson.put("value", gpa);
        gpaJson.put("type", "double");
        variables.put("gpa", gpaJson);

        JSONObject essayJson = new JSONObject();
        essayJson.put("value", essay);
        essayJson.put("type", "string");
        variables.put("essay", essayJson);

        JSONObject body = new JSONObject();
        body.put("variables", variables);
        return body.toString();
    }

    /**
     * Complete tasks for a specified taskID
     * @param taskId to complete
     * @param body to post
     */
    public static void completeTask(String taskId, String body) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String URL = String.format("http://localhost:8080/engine-rest/task/%s/complete", taskId);
            // Creating the request to execute
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(body));

            // Executing the request using the http client and obtaining the response
            client.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check whether a process exists
     * @param processID to use to check whether the process exists
     * @return whether it exists
     */
    public static boolean processExists(String processID) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format("http://localhost:8080/engine-rest/process-instance/%s", processID);
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse httpResponse = client.execute(httpGet);
            String jsonResponse = readResponse(httpResponse);
            JSONObject obj = new JSONObject(jsonResponse);
            boolean t = (obj.has("type") && obj.getString("type").equals("InvalidRequestException"));
            return !(obj.has("type") && obj.getString("type").equals("InvalidRequestException"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check whether the process is at the confirm acceptance task of the process
     * @param processID to check process
     * @return whether the current task is confirm acceptance
     */
    public static boolean canConfirmAcceptance(String processID) {
        return validateInCurrentActivity(processID, "ConfirmAcceptance");
    }

    /**
     * Check whether the process is at the create application task of the process
     * @param processID to check process
     * @return whether the current task is create application
     */
    public static boolean canCreateApplication(String processID) {
        return validateInCurrentActivity(processID, "CreateApplication");
    }

    /**
     * Validate if the first task in a task list of a process is the specified activity
     * @param processID of the process to look into
     * @param activityID to validate the current activity
     * @return whether the current activity is the one passed
     */
    private static boolean validateInCurrentActivity(String processID, String activityID) {
        JSONArray taskList = getTaskList(processID);
        if (taskList.length() != 1) {
            return false;
        } else {
            JSONObject task = taskList.getJSONObject(0);
            return task.getString("taskDefinitionKey").equals(activityID);
        }
    }

}
