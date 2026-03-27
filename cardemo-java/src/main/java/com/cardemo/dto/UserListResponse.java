package com.cardemo.dto;

import com.cardemo.enums.UserType;
import java.util.List;

public class UserListResponse {

    private List<UserItem> users;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public UserListResponse() {
    }

    public UserListResponse(List<UserItem> users, int page, int size,
                            long totalElements, int totalPages) {
        this.users = users;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<UserItem> getUsers() {
        return users;
    }

    public void setUsers(List<UserItem> users) {
        this.users = users;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public static class UserItem {
        private String userId;
        private String firstName;
        private String lastName;
        private UserType userType;

        public UserItem() {
        }

        public UserItem(String userId, String firstName, String lastName, UserType userType) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.userType = userType;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public UserType getUserType() {
            return userType;
        }

        public void setUserType(UserType userType) {
            this.userType = userType;
        }
    }
}
