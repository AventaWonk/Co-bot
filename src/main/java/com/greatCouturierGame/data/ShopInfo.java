package com.greatCouturierGame.data;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Shop information class
 *
 * @author Roman
 */
@Entity(name = "SHOP_INFO")
public class ShopInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private String soldIds;

    private int amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSoldIds() {
        return soldIds;
    }

    public void setSoldIds(String soldIds) {
        this.soldIds = soldIds;
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
}
