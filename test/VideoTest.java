package test;

import models.*;
import org.junit.*;
import static org.junit.Assert.*;
import play.test.*;
import play.libs.F.*;

import java.util.List;
import java.util.HashMap;

import static play.test.Helpers.*;

/**
 * Created by saravanan on 12/02/2015.
 */
public class VideoTest {
    @Test(expected=IllegalArgumentException.class)
    public void testStudentCannotUploadVideo() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                School s = new School("School Name");
                //Student u = new Student("Name", "pass", "fake@gmail.com", s);
                //Video v = new Video(u, "Title", "Description", "thumb_path.png");
            }
        });
    }

    @Test
    public void testAlumniCanUploadVideo() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                School s = new School("School Name");
                Alumni u = new Alumni("Name", "pass", "fake@gmail.com", s);
                Video v = new Video(u, "Title", "Description", "thumb_path.png");
            }
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testVideoMustHaveTitle() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                School s = new School("School Name");
                Alumni u = new Alumni("Name", "pass", "fake@gmail.com", s);
                Video v = new Video(u, null, "Description", "thumb_path.png");
            }
        });
    }

    @Test
    public void testOnlyApprovedVideosReturned() {
        final HashMap<String,String> db = new HashMap<String, String>();
        db.put("MODE","MySQL");
        running(testServer(3333, fakeApplication(inMemoryDatabase("default", db))), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                School s = new School("School Name");
                s.save();
                Alumni u = new Alumni("Name", "pass", "fake@gmail.com", s);
                u.save();
                Video v1 = new Video(u, "Title", "Description", "thumb_path.png");
                v1.setApproved(true);
                v1.save();
                Video v2 = new Video(u, "Another Title", "Another Description", "thumb_path2.png");
                v2.save();

                VideoDAO dao = new VideoDAO();
                List<Video> videos = dao.getAllApprovedVideos();
                assertEquals(1, videos.size());
                assertEquals(v1, videos.get(0));
                assertNotEquals(v2, videos.get(0));
            }
        });
    }
}
