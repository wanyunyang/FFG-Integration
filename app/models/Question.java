package models;

import javax.persistence.*;
import play.db.ebean.*;
import views.forms.QuestionForm;

/**
 * Represents a question. Each question is specific to its school. Question extends Comparable so that Questions can
 * be sorted by ordering.
 */
@Entity
public class Question extends Model implements Comparable<Question> {
    /**
     * The database ID of the Question
     */
    @Id
    private Long questionId;
    /**
     * The text of the Question being asked.
     */
    private String text;
    /**
     * The length of time allocated to an Alumni for them to answer this Question
     */
    private double duration;
    /**
     * A field that shows whether a Question is active or not. Active Questions are able to be answered and associated with new VideoClips, as well as be edited and deleted by Admins.
     * Inactive Questions still exist for the purpose of backwards compatibility with older Video clips, but they can no longer be edited or deleted or associated with new VideoClips.
     */
    private boolean active = true;
    /**
     * The ordering of the Question. Ordering starts from 1, with 1 being the Question that gets "asked" first.
     */
    private int ordering;

    /**
     * The School that this Question is attached to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SCHOOL")
    public School school;

    /**
     * Finder used to search the database for Question objects.
     */
    public static Finder<Long,Question> find = new Finder<>(Long.class, Question.class);

    /**
     * The Question constructor. Creates a Question object.
     * @param text The text we want to have for this Question.
     * @param duration The duration we want to have for this Question
     * @param school The School object this Question is associated with.
     */
    public Question(String text, double duration, School school) {
        this.text = text;
        this.school = school;
        this.duration = duration;
    }

    /**
     * Helper used by forms to create Questions from QuestionForms.
     * @param data The QuestionForm that holds the data for this Question.
     * @param ownerSchool The School we want to associate this Question with.
     * @return The Question object we have created.
     */
    public static Question makeInstance(QuestionForm data, School ownerSchool) {
        Question q = new Question(data.text,data.duration, ownerSchool);
        return q;
    }

    /**
     * Getter for ID.
     * @return The database ID of this Question/
     */
    public long getId() {
        return questionId;
    }

    /**
     * Setter for ID
     * @param id The ID we want to set this Question's ID to.
     */
    public void setId(long id) {
        this.questionId = id;
    }

    /**
     * Getter for text.
     * @return The text of this Question
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for text.
     * @param text The text we want to set this Question's text to.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter for duration.
     * @return The duration of this Question.
     */
    public double getDuration() { return duration; }

    /**
     * Setter for duration
     * @param duration The length of time we want to set this Question's duration to.
     */
    public void setDuration(double duration) { this.duration = duration; }

    /**
     * Getter for school.
     * @return The School object associated with this Question.
     */
    public School getSchool() {
        return school;
    }

    /**
     * Setter for school. Also ensures that the School contains this Question in its questionList.
     * @param school The School we want to associate this Question to.
     */
    public void setSchool(School school) {
        this.school = school;
        if (!school.getQuestions().contains(this)) {
            school.getQuestions().add(this);
        }
    }

    /**
     * Getter for active.
     * @return The active boolean for this Question.
     */
    public boolean getActive() { return this.active; }

    /**
     * Setter for active.
     * @param b The boolean we want to set this Question's active field to.
     */
    public void setActive(boolean b) { this.active = b; }

    /**
     * Getter for ordering.
     * @return The ordering of this Question.
     */
    public int getOrder() { return this.ordering; }

    /**
     * Setter for ordering.
     * @param o The order we want to set this Question's ordering to.
     */
    public void setOrder(int o) { this.ordering = o; }

    /**
     * Overriding the compareTo method from Comparable. It compares two Questions based on their ordering.
     * @param q The Question we want to compare to this Question.
     * @return -1 if this Question's ordering is smaller, 1 if the other Question's ordering is smaller otherwise if they are the same then 0.
     */
    @Override
    public int compareTo(Question q) {
       if(this.ordering < q.ordering) {
           return -1;
       } else if (this.ordering > q.ordering) {
           return 1;
       } else {
           return 0;
       }
    }
}