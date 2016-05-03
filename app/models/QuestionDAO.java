package models;

import com.avaje.ebean.*;

import java.util.Collections;
import java.util.List;

/**
 * Data access object for Question objects. It helps to perform database operations on the Question table in the database.
 */
public class QuestionDAO {

    /**
     * Constructor for QuestionDAO.
     */
    public QuestionDAO() {}

    /**
     * Searches the database for the Question where the ID field is the same as id.
     * @param id The ID of the Question we want to retrieve.
     * @return The Question where id matches ID or null if no matches were found.
     */
    public Question getQuestion(Long id) {
        return Question.find.byId(id);
    }

    /**
     * "Deletes" a question, which means that we instead set the active field of the Question we find by ID to false and save. We also have to update the ordering for the rest of the
     * active Questions in the School associated with the Question we "deleted".
     * @param id The ID of the Question we want to "delete" (set to inactive).
     */
    public void deleteQuestion(Long id) {
        Question q = Question.find.byId(id);
        q.setActive(false);
        q.save();
        int order = q.getOrder();
        List<Question> qs = getActiveQuestions(q.getSchool());
        for (Question ques : qs) {
            if (order < ques.getOrder()) {
                ques.setOrder(order);
                ques.save();
                order++;
            }
        }
    }

    /**
     * Given a new Question q, we retrieve the list of the active Questions of q's School. If it is empty then q is the only Question and so it gets an order of 1. Otherwise, we have to find the last Question
     * already in the active list and set q's order to 1 plus the last Question's order. We then save q.
     * @param q The Question we are "creating" and setting the order of.
     */
    public void newQuestion(Question q) {
        List<Question> qs = getActiveQuestions(q.getSchool()); //we haven't saved q yet so it shouldn't be in here yet
        if(qs.isEmpty()) {
            q.setOrder(1);
        } else {
            Question q2 = qs.get(qs.size() - 1); //get last question in the active list
            q.setOrder(q2.getOrder() + 1);
        }

        q.save();
    }

    /**
     * Returns a List of all the Questions associated with a School where the School ID of the Question matches the School ID of school and where active is true. It also sorts the Questions so they are in order
     * of their ordering.
     * @param school The School we want to find al lthe active Questions of.
     * @return The sorted List of all Questions in a School where active is true.
     */
    public List<Question> getActiveQuestions(School school) {
        ExpressionList<Question> expList = Question.find.where().eq("school.schoolId",school.getId()).eq("active",true);
        List<Question> result = expList.findList();
        Collections.sort(result);
        return result;
    }
}
