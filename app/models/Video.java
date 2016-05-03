package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.db.ebean.*;

/**
 * Represents a video. Each video has metadata, and is connected to an Alumni user.
 * Each video consists of many video clips.
 */
@Entity
public class Video extends Model {
    /**
     * The database ID of the Video
     */
    @Id
    private Long id;
    /**
     * The Alumni who uploaded the Video
     */
    @ManyToOne
    private Alumni user;
    /**
     * The title of the Video
     */
    private String title;
    /**
     * A textual description of the Video
     */
    @Lob
    private String description;
    /**
     * A path to the thumbnail image to use for that Video on search screens
     */
    private String thumbnailPath;
    /**
     * A field indicating whether a Video has been approved by an Admin so that it can be viewed by users
     */
    private Boolean approved = false;
    /**
     * The VideoClips that together make up the Video. Each VideoClip represents an answer to one Question
     */
    @OneToMany(mappedBy="video", cascade=CascadeType.ALL)
    private List<VideoClip> videoClips;
    /**
     * The categories that the Video is connected to
     */
    @ManyToMany(mappedBy = "videos")
    public List<Category> categories;
    /**
     * A field indicating whether the Video is publicly accessible by anonymous users or users from other schools
     */
    public Boolean publicAccess = false;

    /**
     * Finder used to search the database for Video objects.
     */
    public static Finder<Long,Video> find = new Finder<>(Long.class, Video.class);

    /**
     * The Video constructor. Creates a Video object.
     * @param user The Alumni uploading the Video.
     * @param title The title of the Video.
     * @param description The description of the Video.
     * @param thumbnailPath The path to the thumbnail image to disply for this Video.
     */
    public Video(Alumni user, String title, String description, String thumbnailPath) {
        if (user == null) { throw new IllegalArgumentException("Video author must be provided"); }
        if (title == null) { throw new IllegalArgumentException("Video title must be provided"); }

        this.user = user;
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.approved = false;
        this.publicAccess = false;
    }

    /**
     * Method to associate a VideoClip with a Video.
     * @param clip The VideoClip object to use.
     */
    public void addClip(VideoClip clip) {
        videoClips.add(clip);
        clip.setVideo(this);
    }

    /**
     * Method to edit a Video's metadata.
     * @param newTitle The new title of the Video.
     * @param newDescription The new description of the Video.
     */
    public void edit(String newTitle, String newDescription) {
        this.title = newTitle;
        this.description = newDescription;
    }

    /**
     * Getter for ID.
     * @return The database ID of this Video.
     */
    public Long getId() { return id; }

    /**
     * Getter for user.
     * @return The Alumni that uploaded the Video.
     */
    public Alumni getUser() { return user; }

    /**
     * Setter for user
     * @param user The new Alumni to associate with the Video.
     */
    public void setUser(Alumni user) { this.user = user; }

    /**
     * Getter for title.
     * @return The title of the Video.
     */
    public String getTitle() { return title; }

    /**
     * Setter for title
     * @param title The new title for the Video.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Getter for description.
     * @return The description of the Video.
     */
    public String getDescription() { return description; }

    /**
     * Setter for description
     * @param description The new description for the Video.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Getter for publicAccess.
     * @return Boolean indicating if Video can be publicly accessed or not.
     */
    public Boolean getPublicAccess() { return publicAccess; }

    /**
     * Setter for publicAccess
     * @param publicAccess New boolean indicating if Video should be public or not.
     */
    public void setPublicAccess(Boolean publicAccess) { this.publicAccess = publicAccess; }

    /**
     * Getter for thumbnailPath.
     * @return The path to the thumbnail image of the Video.
     */
    public String getThumbnailPath() { return thumbnailPath; }

    /**
     * Setter for thumbnailPath
     * @param thumbnailPath The new path to the thumbnail image for the Video.
     */
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    /**
     * Method to return total duration of the Video.
     * @return The total duration of the Video.
     */
    public double getDuration() {
        double totalDuration = 0.0;
        for (VideoClip clip : videoClips) {
            totalDuration += clip.getDuration();
        }
        return totalDuration;
    }

    /**
     * Getter for videoClips.
     * @return The List of VideoClip objects for the Video.
     */
    public List<VideoClip> getVideoClips() { return videoClips; }

    /**
     * Getter for categories.
     * @return The List of Category objects for the Video.
     */
    public List<Category> getCategories() { return categories; }

    /**
     * Method to associate Video with another Category.
     * @param c The new category to associate with the Video.
     */
    public void addCategory(Category c) {
        categories.add(c);
        //c.addVideo(this);
    }

    /**
     * Setter for approved.
     * @param approved Boolean to set whether Video has been approved or not.
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * Getter for approved.
     * @return Boolean indicating if Video has been approved by Alumni or not.
     */
    public boolean getApproved() {
        return this.approved;
    }

    /**
     * Method to return JSON array of video paths for all VideoClips.
     * @return String representing a JSON array of video paths
     */
    public String getJSONPaths() {
        ArrayList<String> res = new ArrayList<String>();
        for (VideoClip clip : videoClips) {
            res.add(clip.getVideoPath());
        }
        return getJSONForList(res);
    }

    /**
     * Method to return JSON array of audio paths for all VideoClips.
     * @return String representing a JSON array of audio paths
     */
    public String getJSONAudioPaths() {
        ArrayList<String> res = new ArrayList<String>();
        for (VideoClip clip : videoClips) {
            res.add(clip.getAudioPath());
        }
        return getJSONForList(res);
    }

    /**
     * Method to return JSON array of questions for all VideoClips.
     * @return String representing a JSON array of questions
     */
    public String getJSONQuestionsText() {
        ArrayList<String> questionsText = new ArrayList<String>();
        for (VideoClip clip : videoClips) {
            questionsText.add(clip.getQuestion().getText());
        }
        return getJSONForList(questionsText);
    }

    /**
     * Method to return JSON array of durations for all VideoClips.
     * @return String representing a JSON array of durations
     */
    public String getJSONDurations() {
        ArrayList<Double> res = new ArrayList<Double>();
        for (VideoClip clip : videoClips) {
            res.add(clip.getDuration());
        }
        return getJSONForList(res);
    }

    /**
     * Method to convert List into a single string representing a JSON array.
     * @parram list The List to convert into a JSON array
     * @return String representing a JSON array copy of the list parameter
     */
    private String getJSONForList(ArrayList list) {
        String json = "";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;

    }

    /**
     * Method to return the number of categories that a Video is linked to from a given list.
     * @param searchCategories The list of categories to compare to
     * @return Integer representing the number of categories in searchCategories that the Video is connected to
     */
    public int numberOfMatchesWithCategories(List<Category> searchCategories) {
        int count = 0;
        for (Category c : getCategories()) {
            if (searchCategories.contains(c))
                ++count;
        }
        return count;
    }
}
