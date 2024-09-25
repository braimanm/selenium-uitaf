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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserProvider {
    private static UserProvider instance = null;
    private ConcurrentMap<Thread, List<EnvironmentsSetup.User>> users = new ConcurrentHashMap<>();
    private Iterator<List<EnvironmentsSetup.User>> iterator;

    private UserProvider() {
        EnvironmentsSetup.Environment env = TestContext.getTestProperties().getTestEnvironment();
        SortedMap<String,List<EnvironmentsSetup.User>> usersGroups = new TreeMap<>();
        int groupI = 0;
        for (EnvironmentsSetup.User u : env.getUsers()) {
            String[] group_role = u.getRole().split("\\.",2);
            if(group_role.length > 1 ) {
                if (usersGroups.containsKey(group_role[0])) {
                    usersGroups.get(group_role[0]).add(u);
                } else {
                    usersGroups.put(group_role[0], new ArrayList<>());
                    usersGroups.get(group_role[0]).add(u);
                }
            } else {
                String group = "NULL_GROUP" + (groupI++) + "." + group_role[0];
                usersGroups.put(group, new ArrayList<>());
                usersGroups.get(group).add(u);
            }
        }
        iterator = usersGroups.values().iterator();
    }

    public static UserProvider getInstance() {
        if (instance == null) {
            synchronized ( (UserProvider.class)) {
                if (instance == null) {
                    instance = new UserProvider();
                }
            }
        }
        return instance;
    }

    public synchronized EnvironmentsSetup.User getUser(String role) {
        for (EnvironmentsSetup.User user : getUsers()) {
            if (user.getRole().endsWith(role)) {
                return user;
            }
        }
        throw new RuntimeException("User with role '" + role + "' was not found in environments file!");
    }

    private Thread getDeadThread() {
        for (Thread t : users.keySet()) {
            if (!t.isAlive()) return t;
        }
        return null;
    }

    public synchronized List<EnvironmentsSetup.User> getUsers() {
        Thread t = Thread.currentThread();
        if (!users.containsKey(t)) {
            Thread td = getDeadThread();
            if (td != null) {
                users.put(t, users.get(td));
                users.remove(td);
            } else {
                if (!iterator.hasNext()) {
                    throw new RuntimeException("Please add more users to environments file for running the tests in parallel.");
                }
                users.put(t, iterator.next());
            }
        }
        return users.get(t);
    }


    public List<String> getAllUsedRoles() {
        List<String> roles = new ArrayList<>();
        for (List<EnvironmentsSetup.User> us : users.values()) {
            for (EnvironmentsSetup.User u : us) {
                roles.add(u.getRole());
            }
        }
        return roles;
    }

    @Override
    public String toString() {
        return users.toString();
    }
}
