package com.aeolus.examples.core.config;

import com.aeolus.core.di.annotations.Config;

@Config(prefix = "db")
public class DbConfig {
    public String url;
    public String user;
    public String password;

    public String toString() {
        return String.format("DbConfig{url='%s', user='%s'}", url, user);
    }
}
