package com.github.votingsessionmanager.domain;

public class Vote {
    private String memberId;
    private String memberCPF;
    private VoteOption voteOption;

    public Vote() {}

    public Vote(String memberId, String memberCPF, VoteOption voteOption) {
        this.memberId = memberId;
        this.memberCPF = memberCPF;
        this.voteOption = voteOption;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberCPF() {
        return memberCPF;
    }

    public void setMemberCPF(String memberCPF) {
        this.memberCPF = memberCPF;
    }

    public VoteOption getVoteOption() {
        return voteOption;
    }

    public void setVoteOption(VoteOption voteOption) {
        this.voteOption = voteOption;
    }
}
