package com.trobot.gkashdemo.model;

public class Terminal {

    private String terminalID;
    private String branchName;

    public Terminal(String terminalID, String branchName) {
        this.terminalID = terminalID;
        this.branchName = branchName;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
