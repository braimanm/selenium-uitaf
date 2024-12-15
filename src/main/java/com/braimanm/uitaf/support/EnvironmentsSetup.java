/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.braimanm.uitaf.support;

import com.braimanm.datainstiller.data.DataPersistence;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@SuppressWarnings({"NewClassNamingConvention", "unused", "MismatchedQueryAndUpdateOfCollection"})
@XStreamAlias("setup")
public class EnvironmentsSetup extends DataPersistence {
    @XStreamImplicit
    private List<Environment> config;
    private List<Property> global;

    public String getCustom(String name) {
        for (Property p : global) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        throw new RuntimeException("Property \"" + name  + "\" was not found in environments configuration file");

    }

    public List<Property> getCustom() {
        return this.global;
    }

    public Environment getEnvironment(String env) {
        for (Environment environment : config) {
            if (environment.getEnvironmentName().equalsIgnoreCase(env)) {
                return environment;
            }
        }
        throw new RuntimeException("Environment \"" + env + "\" was not found in environments configuration file");
    }

    public List<Environment> getAllEnvironments() {
        return config;
    }

    @XStreamAlias("environment")
    public static class Environment {
        @XStreamAsAttribute
        String environmentName;
        @XStreamAsAttribute
        String url;
        @XStreamImplicit
        List<Property> custom;
        @XStreamImplicit
        List<User> users;

        public String getEnvironmentName() {
            return environmentName;
        }

        public String getUrl() {
            if (System.getenv().containsKey("OVERRIDE_URL")) {
                return System.getenv("OVERRIDE_URL");
            }
            return url;
        }

        public User getUser(String role) {
            for (User user : users){
                if (user.getRole().equalsIgnoreCase(role)) {
                    return user;
                }
            }
            throw new RuntimeException("User with role \"" + role  + "\" was not found in environments configuration file");
        }

        public List<User> getUsers() {
            return users;
        }

        public String getCustom(String name) {
            for (Property p : custom) {
                if (p.name.equals(name)) {
                    return p.value;
                }
            }
            throw new RuntimeException("Property \"" + name  + "\" was not found in environments configuration file");

        }

        public List<Property> getCustom() {
            return this.custom;
        }
    }

    @XStreamAlias("user")
    public static class User {
        @XStreamAsAttribute
        String role;
        @XStreamAsAttribute
        String fullName;
        @XStreamAsAttribute
        String userName;
        @XStreamAsAttribute
        String password;
        @XStreamImplicit
        List<Property> custom;

        public String getRole() {
            return role;
        }

        public String getFullName() {
            return fullName;
        }

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }

        public String getCustom(String name) {
            for (Property p : custom) {
                if (p.name.equals(name)) {
                    return p.value;
                }
            }
            throw new RuntimeException("Property \"" + name  + "\" was not found for user " + role + " in configuration file");

        }

        public List<Property> getCustom() {
            return this.custom;
        }
    }

    @XStreamAlias("prop")
    public static class Property {
        @XStreamAsAttribute
        String name;
        @XStreamAsAttribute
        String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

}