package com.cardemo.dto;

import java.util.List;

public class MenuResponse {

    private String menuType;
    private List<MenuItem> items;

    public MenuResponse() {
    }

    public MenuResponse(String menuType, List<MenuItem> items) {
        this.menuType = menuType;
        this.items = items;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    public static class MenuItem {
        private int id;
        private String label;
        private String route;
        private String programName;

        public MenuItem() {
        }

        public MenuItem(int id, String label, String route, String programName) {
            this.id = id;
            this.label = label;
            this.route = route;
            this.programName = programName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public String getProgramName() {
            return programName;
        }

        public void setProgramName(String programName) {
            this.programName = programName;
        }
    }
}
