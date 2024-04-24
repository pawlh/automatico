package edu.byu.cs.canvas;

import edu.byu.cs.dataAccess.DaoService;
import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.model.User;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;

public class FakeCanvasIntegration implements CanvasIntegration {
    @Override
    public User getUser(String netId) throws CanvasException {
        User user = null;
        try {
            user = DaoService.getUserDao().getUser(netId);
        } catch (DataAccessException e) {
            throw new CanvasException("Error getting user from database", e);
        }
        if(user == null) {
            user = new User(netId, 0, "FirstName", "LastName", null, User.Role.ADMIN);
        }
        return user;
    }

    @Override
    public Collection<User> getAllStudents() {
        return new HashSet<>();
    }

    @Override
    public Collection<User> getAllStudentsBySection(int sectionID) {
        return new HashSet<>();
    }

    @Override
    public void submitGrade(int userId, int assignmentNum, Float grade, String comment) {

    }

    @Override
    public void submitGrade(int userId, int assignmentNum, RubricAssessment assessment, String assignmentComment) {

    }


    @Override
    public CanvasSubmission getSubmission(int userId, int assignmentNum) {
        return null;
    }

    @Override
    public String getGitRepo(int userId) {
        return null;
    }

    @Override
    public User getTestStudent() {
        return null;
    }

    @Override
    public ZonedDateTime getAssignmentDueDateForStudent(int userId, int assignmentId) {
        return null;
    }
}
