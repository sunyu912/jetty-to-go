package io.magnum.jetty.server.data;

public class RunTestResponse {
    
    private String testId;

    public RunTestResponse() {
        super();
    }

    public RunTestResponse(String testId) {
        this.testId = testId;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
    
}
