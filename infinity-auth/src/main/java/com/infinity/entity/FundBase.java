package com.infinity.entity;

import java.util.Date;

/**
 * FundBase
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
public class FundBase {
    private int id;
    private String fundCode;
    private String fundName;
    private String fundShortName;
    private Integer fundClass;
    private Integer fundInvestClass;
    private Integer fundInvestSubClass;
    private Integer fundClassEnd;
    private Integer fundOperateWay;
    private Integer fundMarket;
    private Date date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getFundShortName() {
        return fundShortName;
    }

    public void setFundShortName(String fundShortName) {
        this.fundShortName = fundShortName;
    }

    public Integer getFundClass() {
        return fundClass;
    }

    public void setFundClass(Integer fundClass) {
        this.fundClass = fundClass;
    }

    public Integer getFundInvestClass() {
        return fundInvestClass;
    }

    public void setFundInvestClass(Integer fundInvestClass) {
        this.fundInvestClass = fundInvestClass;
    }

    public Integer getFundInvestSubClass() {
        return fundInvestSubClass;
    }

    public void setFundInvestSubClass(Integer fundInvestSubClass) {
        this.fundInvestSubClass = fundInvestSubClass;
    }

    public Integer getFundClassEnd() {
        return fundClassEnd;
    }

    public void setFundClassEnd(Integer fundClassEnd) {
        this.fundClassEnd = fundClassEnd;
    }

    public Integer getFundOperateWay() {
        return fundOperateWay;
    }

    public void setFundOperateWay(Integer fundOperateWay) {
        this.fundOperateWay = fundOperateWay;
    }

    public Integer getFundMarket() {
        return fundMarket;
    }

    public void setFundMarket(Integer fundMarket) {
        this.fundMarket = fundMarket;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
