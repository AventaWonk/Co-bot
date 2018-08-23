package com.greatCouturierGame.data;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Contest information class
 *
 * @author Roman
 */
@Entity(name = "CONTEST_INFO")
public class ContestInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private int place;

    private int amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    @ManyToOne
    private AccountStatistics accountStatistics;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public AccountStatistics getAccountStatistics() {
        return accountStatistics;
    }

    public void setAccountStatistics(AccountStatistics accountStatistics) {
        this.accountStatistics = accountStatistics;
    }
}
