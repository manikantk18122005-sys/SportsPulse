package com.sportspulse.hashing;

import com.sportspulse.auth.User;

/**
 * Singly-linked list node for hand-rolled separate chaining collision resolution.
 *
 * Implements pure pointer chaining over primitive arrays:
 * Entry[] table
 *
 * Dependency-free: Uses ZERO collections from java.util (no HashMap, Map, or ArrayList).
 */
public class HashEntry {
    public final String username;
    public User user;
    public HashEntry next;

    public HashEntry(String username, User user, HashEntry next) {
        this.username = username;
        this.user = user;
        this.next = next;
    }

    public String getUsername() {
        return username;
    }

    public User getUser() {
        return user;
    }

    public HashEntry getNext() {
        return next;
    }
}
