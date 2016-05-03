package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import static com.avaje.ebean.Expr.eq;

/**
 * Data access object for Video objects. It helps to perform database operations on the Video table in the database.
 */
public class VideoDAO {

    /**
     * Constructor for VideoDAO
     */
    public VideoDAO() {}

    /**
     * Searches the database for the Video where the ID field is the same as id.
     * @param id The ID of the Video we want to retrieve.
     * @return The Video where id matches ID or null if no matches were found.
     */
    public Video getVideo(Long id) {
        return Video.find.byId(id);
    }

    /**
     * Searches by ID to find the Video and then deletes this Video from the database.
     * @param id The ID of the Video we want to delete.
     */
    public void deleteVideo(Long id) {
        Video.find.byId(id).delete();
    }

    /**
     * Gives a List of all of the Video objects in the database.
     * @return A List of Video objects.
     */
    public List<Video> getAllVideos() {
        return Video.find.all();
    }

    /**
     * Gives a List of all of the Video objects in the database that have been approved.
     * @return A List of Video objects.
     */
    public List<Video> getAllApprovedVideos() {
        return Video.find.where().eq("approved", true).findList();
    }

    /**
     * Gives a List of all of the Video objects in the database that have been approved and are public.
     * @return A List of Video objects.
     */
    public List<Video> getAllPublicVideos() {
        return Video.find.where().eq("publicAccess", true).eq("approved", true).findList();
    }

    /**
     * Gives a List of all of the Video objects in the database that have been approved, are public
     * and match one or more categories from a list of categories.
     * Sorted by number of matched categories with categories parameter, most first
     * @param categories List of categories to search for
     * @return A List of Video objects.
     */
    public List<Video> getAllPublicVideosByCategories(List<Category> categories) {
        List<Video> videos = getAllPublicVideos();
        return filterVideosByCategories(videos, categories);
    }

    /**
     * Gives a List of all of the Video objects in the database that can be viewed by a user from a particular school.
     * @param s School to find Video objects from.
     * @return A List of Video objects.
     */
    public List<Video> getVideosBySchool(School s) {
        List<Video> videos = getAllPublicVideos();
        if (s == null) {
            return videos;
        }
        List<Alumni> alumnis = Alumni.find.where().eq("school", s).eq("approved", true).findList();
        for (Alumni a : alumnis) {
            List<Video> vids = Video.find.where().eq("user", a).eq("approved", true).eq("publicAccess",false).findList();
            videos.addAll(vids);
        }
        return videos;
    }

    /**
     * Gives a List of all of the Video objects associated to a School, approved and unapproved.
     * @param s School to find Video objects from.
     * @return A List of Video objects.
     */
    public List<Video> getAllVideosBySchool(School s) {
        List<Video> videos = new ArrayList<>();
        if (s == null) {
            return videos;
        }
        List<Alumni> alumnis = Alumni.find.where().eq("school", s).eq("approved", true).findList();
        for (Alumni a : alumnis) {
            List<Video> vids = Video.find.where().eq("user",a).findList();
            videos.addAll(vids);
        }
        return videos;
    }

    /**
     * Gives a List of all of the unapproved Video objects associated to a School.
     * @param s School to find Video objects from.
     * @return A List of Video objects.
     */
    public List<Video> getUnapprovedVideosBySchool(School s) {
        List<Video> videos = new ArrayList<>();
        if (s == null) {
            return videos;
        }
        List<Alumni> alumnis = Alumni.find.where().eq("school", s).eq("approved", true).findList();
        for (Alumni a : alumnis) {
            List<Video> vids = Video.find.where().eq("user",a).eq("approved",false).findList();
            videos.addAll(vids);
        }
        return videos;
    }

    /**
     * Gives a List of all of the Video objects associated with a particular user.
     * @param u User to find Video objects from.
     * @return A List of Video objects.
     */
    public List<Video> getVideosByUser(User u) {
        return Video.find.where().eq("user",u).findList();
    }

    /**
     * Gives a List of all of the Video objects viewable by a user from a particular school
     * And which match one or more.
     * @param s School to find Video objects from.
     * @param categories List of categories to search for
     * @return A List of Video objects.
     */
    public List<Video> getVideosBySchoolAndCategories(School s, List<Category> categories) {
        List<Video> videos = getVideosBySchool(s);
        return filterVideosByCategories(videos, categories);
    }

    /**
     * Method to filter videos by a searched set of categories, sorted by number of matches, most matches first
     * And which match one or more.
     * @param videos Videos to filter and sort.
     * @param categories List of categories to search for.
     * @return A List of Video objects.
     */
    private List<Video> filterVideosByCategories(List<Video> videos, List<Category> categories) {
        if (categories.size() == 0) {
            return videos;
        }
        VideoCategoryComparator comparator = new VideoCategoryComparator(categories);
        Collections.sort(videos, comparator);
        while (videos.size() > 0) {
            int index = videos.size() - 1;
            if (videos.get(index).numberOfMatchesWithCategories(categories) == 0) {
                videos.remove(index);
            }
            else {
                break;
            }
        }
        return videos;
    }
}

/**
 * Comparator to sort Videos by number of matches with a list of categories.
 */
class VideoCategoryComparator implements Comparator<Video> {
    /**
     * The list of categories to search for when sorting.
     */
    private List<Category> categories;

    /**
     * Constructor for VideoCategoryComparator.
     * @param categories The list of categories to search.
     */
    VideoCategoryComparator(List<Category> categories) { this.categories = categories; }

    /**
     * Returns which Video should come first when a list of Videos is sorted using the Comparator.
     * @param v1 First Video object to compare.
     * @param v2 Second Video object to compare.
     * @result Integer, >0 if v2 has more matches, <0 if v1 has more matches, 0 if the same.
     */
    public int compare(Video v1, Video v2) {
        return v2.numberOfMatchesWithCategories(categories) - v1.numberOfMatchesWithCategories(categories);
    }
}

