package edu.stanford.pepe.newpostprocessing;

public enum DependencyType {

    JAVA, DB_DEPENDENCY, DB_ANTIDEPENDENCY, DB_WRITEDEPENDENCY;

    public String toString() {
        switch (this) {
        case JAVA:
            return "java";
        case DB_DEPENDENCY:
            return "db_raw";
        case DB_ANTIDEPENDENCY:
            return "db_war";
        case DB_WRITEDEPENDENCY:
            return "db_waw";
        default:
            throw new IllegalArgumentException();
        }
    };
}
