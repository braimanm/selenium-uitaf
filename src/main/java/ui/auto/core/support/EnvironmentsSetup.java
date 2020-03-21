/*
Copyright 2010-2019 Michael Braiman braimanm@gmail.com
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

package ui.auto.core.support;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import datainstiller.data.DataPersistence;

import java.util.List;

@XStreamAlias("setup")
public class EnvironmentsSetup extends DataPersistence {
    @XStreamImplicit
    private List<Environment> config;

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
            return environmentName.toLowerCase();
        }

        public String getUrl() {
            if (System.getenv().containsKey("OVERRIDE_URL")) {
                return System.getenv("OVERRIDE_URL");
            }
            return url;
        }

        public User getUser(String role) {
            for (User u : users){
                if (u.getRole().equals(role.toLowerCase())) {
                    return u;
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
            return role.toLowerCase();
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
    }

    public Environment getEnvironment(String env) {
        for (Environment environment : config) {
            if (environment.getEnvironmentName().equals(env)) {
                return environment;
            }
        }
        throw new RuntimeException("Environment \"" + env + "\" was not found in environments configuration file");
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