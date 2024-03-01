/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package win.shangyh.datatrans.rainbow;

import java.util.Objects;

public final class DatabaseInfo {
    
    private final String jdbcUrl;
    
    private final String account;
    
    private final String password;

    public DatabaseInfo(String jdbcUrl, String account, String password) {
        this.jdbcUrl = jdbcUrl;
        this.account = account;
        this.password = password;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DatabaseInfo that = (DatabaseInfo) o;
        return jdbcUrl.equals(that.jdbcUrl) &&
                account.equals(that.account) &&
                password.equals(that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jdbcUrl, account, password);
    }

    @Override
    public String toString() {
        return "DatabaseInfo{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", account='" + account + '\'' +
                '}';
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

}
