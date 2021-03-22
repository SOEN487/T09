import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class GradeValidator {
    private final static Logger LOGGER = Logger.getLogger(GradeValidator.class.getName());

    public static void main(String[] args) {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(10000) // long polling timeout
                .build();

        // subscribe to an external task topic as specified in the process
        client.subscribe("ValidateGrades")
                .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {

                    // Get a process variable
                    double gpa = externalTask.getVariable("gpa");

                    LOGGER.info("Student GPA is " + gpa);

                    VariableMap processVariables = Variables.createVariables().putValue("validatedGrades",
                            ClientValues.booleanValue(gpa > 1.0));

                    // Complete the task
                    externalTaskService.complete(externalTask, processVariables);
                })
                .open();
    }
}