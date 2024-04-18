package net.carnien.dependencymanager;

public class Dependency {

    private String name;
    private String link;

    public Dependency(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

}
