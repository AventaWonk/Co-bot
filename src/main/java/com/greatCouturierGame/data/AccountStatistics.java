package com.greatCouturierGame.data;

import javax.persistence.*;
import java.util.List;

/**
 * Account statistics class
 *
 * @author Roman
 */
@Entity(name = "ACCOUNT_STATISTICS")
public class AccountStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TaskInfo> taskInfo;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ShopInfo> shopInfo;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ContestInfo> contestInfo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TaskInfo> getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(List<TaskInfo> taskInfo) {
        this.taskInfo = taskInfo;
    }

    public List<ShopInfo> getShopInfo() {
        return shopInfo;
    }

    public void setShopInfo(List<ShopInfo> shopInfo) {
        this.shopInfo = shopInfo;
    }

    public List<ContestInfo> getContestInfo() {
        return contestInfo;
    }

    public void setContestInfo(List<ContestInfo> contestInfo) {
        this.contestInfo = contestInfo;
    }
}
