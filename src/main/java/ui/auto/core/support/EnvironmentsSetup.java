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
    }

    public Environment getEnvironment(String env) {
        for (Environment environment : config) {
            if (environment.getEnvironmentName().equals(env)) {
                return environment;
            }
        }
        throw new RuntimeException("Environment \"" + env + "\" was not found in environments configuration file");
    }

}