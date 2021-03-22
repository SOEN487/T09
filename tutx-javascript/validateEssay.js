const { Client, logger, Variables } = require("camunda-external-task-client-js");

// configuration for the Client:
//  - 'baseUrl': url to the Workflow Engine
//  - 'logger': utility to automatically log important events
const config = { baseUrl: "http://localhost:8080/engine-rest", use: logger };

// create a Client instance with custom configuration
const client = new Client(config);

// susbscribe to the topic: 'ReviewEssay'
client.subscribe("ReviewEssay", async function({ task, taskService }) {
    // Put your business logic
    // set a process variable 'winning'
    const essay = task.variables.get("essay");

    const processVariables = new Variables();
    const essayIsGood = essay.split(' ').length > 5;
    processVariables.set("essayIsGood", essayIsGood);

    // complete the task
    await taskService.complete(task, processVariables);
});

