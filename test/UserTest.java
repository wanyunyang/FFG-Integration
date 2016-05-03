import models.*;
import org.junit.*;
import static org.junit.Assert.*;
import play.test.*;
import play.libs.F.*;


import static play.test.Helpers.*;
/**
 * Created by biko on 02/03/15.
 */
public class UserTest {

    @Test
    public void test_authenticate(){
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            @Override
            public void invoke(TestBrowser testBrowser) throws Throwable {
                School school = new School("Test School");
                Student s = new Student("test","ing","test@email.com",school);
                User one = User.authenticate("test@email.com","ing");
                User two = User.authenticate("typo","ing");
                User three = User.authenticate("test@email.com","typo");
                User four = User.authenticate("typo","typo");

                assertEquals(null,two);
                assertEquals(null,three);
                assertEquals(null,four);
                assertEquals(s,one);
            }
        });
    }
}
