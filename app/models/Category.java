package models;

import play.db.ebean.*;
import views.forms.CategoryForm;

import javax.persistence.*;
import java.util.List;

/**
 * Represents categories that Videos can be associated with. Each Category is associated with many Videos.
 */
@Entity
public class Category extends Model {
    /**
     * The database ID of the Category.
     */
    @Id
    private Long id;
    /**
     * The name of the Category
     */
    private String name;

    /**
     * The List of Videos that this Category is associated with,
     */
    @ManyToMany
    private List<Video> videos;

    /**
     * Finder used to search the database for Category objects.
     */
    public static Model.Finder<Long,Category> find = new Model.Finder<>(Long.class, Category.class);

    /**
     * Constructor for Category. Creates an instance of Category.
     * @param name The name we want to have as the new Category's name.
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * Helper used to create a Category from a CategoryForm.
     * @param data The CategoryForm containing the data we want to store in this Category.
     * @return The new Category we have created.
     */
    public static Category makeInstance(CategoryForm data) {
        Category c = new Category(data.name);
        return c;
    }

    /**
     * Getter for ID.
     * @return The ID of this Category.
     */
    public Long getId() { return id; }

    /**
     * Setter for ID.
     * @param id The ID we want to set this Category's ID to.
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Getter for name.
     * @return The name of this Category.
     */
    public String getName() { return name; }

    /**
     * Setter for name.
     * @param name The name we want to be this Category's name.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Getter for videos.
     * @return The List of Videos associated with this Category.
     */
    public List<Video> getVideos() { return videos; }
}
