package com.greatCouturierGame.data;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

/**
 * Gamer credentials class
 *
 * @author Roman
 */
@Entity(name = "ACCOUNT")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    /**
     * VK user id
     */
    @NaturalId(mutable = true)
    private long uid;

    /**
     * VK game user authToken
     */
    private String authToken;

    /**
     * Bot statistics
     */
    @OneToOne
    @JoinColumn
    private AccountStatistics statistics;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public AccountStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(AccountStatistics statistics) {
        this.statistics = statistics;
    }
}
