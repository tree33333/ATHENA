/*

ATHENA Project: Management Tools for the Cultural Sector
Copyright (C) 2010, Fractured Atlas

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/

 */
package org.fracturedatlas.athena.admin;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

public class AthenaAdmin {

    public static void main(String[] args) {

        ApplicationContext context = new ClassPathXmlApplicationContext("security.xml");
        JdbcUserDetailsManager userDao = (JdbcUserDetailsManager)context.getBean("userDao");
        Md5PasswordEncoder encoder = (Md5PasswordEncoder)context.getBean("passwordEncoder");

        //TODO: Props file
        String realmName = "ATHENA";

        if (args.length == 0) {
            System.out.println("USAGE: admin [command]");
            System.out.println("Where [command] is one of: create-user");
            System.exit(1);
        }

        Console c = System.console();
        if (c == null) {
            System.exit(1);
        }

        String login = c.readLine("Enter new username: ");
        Boolean match = false;
        char[] password = null;
        char[] confirmedPassword = null;
        while(!match) {
            password = c.readPassword("Enter password: ");
            confirmedPassword = c.readPassword("Enter password again: ");
            match = Arrays.equals(password, confirmedPassword);
            if(!match) {
               c.format("Passwords do not match please try again");
               c.format("\n");
            }
        }

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_CLIENT_APPLICATION"));
        String clearPassword = new String(password);
        String saltedClearPassword = login + ":" + realmName + ":" + clearPassword;
        String encryptedPassword = encoder.encodePassword(saltedClearPassword, null);
        User user = new User(login, encryptedPassword, true, true, true, true, authorities);
        try {
            userDao.createUser(user);
        } catch (org.springframework.dao.DuplicateKeyException dke) {
            System.out.println("Username [" + user.getUsername() + "] already exists.");
            System.exit(1);
        }
        
        System.out.println("Successfully created [" + user.getUsername() + "]");
    }
}
