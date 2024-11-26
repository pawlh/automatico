package edu.byu.cs.service;

import edu.byu.cs.analytics.CommitAnalyticsRouter;
import edu.byu.cs.canvas.CanvasException;
import edu.byu.cs.canvas.CanvasService;
import edu.byu.cs.canvas.model.CanvasSection;
import edu.byu.cs.dataAccess.DaoService;
import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.dataAccess.ItemNotFoundException;
import edu.byu.cs.dataAccess.UserDao;
import edu.byu.cs.honorChecker.HonorCheckerCompiler;
import edu.byu.cs.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

    public static Collection<User> getUsers() throws DataAccessException {
        UserDao userDao = DaoService.getUserDao();

        Collection<User> users;
        try {
            users = userDao.getUsers();
        } catch (DataAccessException e) {
            LOGGER.error("Error getting users", e);
            throw e;
        }

        return users;
    }

    public static void updateUser(User user) throws DataAccessException, ItemNotFoundException {
        UserDao userDao = DaoService.getUserDao();
        User existingUser;
        try {
            existingUser = userDao.getUser(user.netId());
        } catch (DataAccessException e) {
            LOGGER.error("Error getting user", e);
            throw e;
        }

        if (existingUser == null) {
            ItemNotFoundException e = new ItemNotFoundException("user not found");
            LOGGER.error("user not found", e);
            throw e;
        }

        try {
            if (user.firstName() != null) {
                userDao.setFirstName(user.netId(), user.firstName());
            }

            if (user.lastName() != null) {
                userDao.setLastName(user.netId(), user.lastName());
            }

            if (user.repoUrl() != null) {
                userDao.setRepoUrl(user.netId(), user.repoUrl());
            }

            if (user.role() != null) {
                userDao.setRole(user.netId(), user.role());
            }
        } catch (DataAccessException e) {
            LOGGER.error("Error updating user", e);
            throw e;
        }

    }

    public static User updateTestStudent() throws CanvasException, DataAccessException {
        User latestTestStudent;
        try {
            latestTestStudent = CanvasService.getCanvasIntegration().getTestStudent();
            if (latestTestStudent == null) {
                throw new CanvasException("No test student provided by CanvasIntegration");
            }
        } catch (CanvasException e) {
            LOGGER.error("Error getting test student", e);
            throw e;
        }

        UserDao userDao = DaoService.getUserDao();
        User user;
        try {
            user = userDao.getUser("test");
        } catch (DataAccessException e) {
            LOGGER.error("Error getting user", e);
            throw e;
        }

        try {

            if (user == null) {
                user = latestTestStudent;
                userDao.insertUser(latestTestStudent);
            } else {
                userDao.setRepoUrl(user.netId(), latestTestStudent.repoUrl());
                userDao.setCanvasUserId(user.netId(), latestTestStudent.canvasUserId());
            }

        } catch (DataAccessException e) {
            LOGGER.error("Error updating user", e);
            throw e;
        }

        try {
            DaoService.getSubmissionDao().removeSubmissionsByNetId(user.netId(), 0);
        } catch (DataAccessException e) {
            LOGGER.error("Error removing submissions", e);
            throw e;
        }

        return user;

    }

    public static String getCommitAnalytics(String option) throws CanvasException, DataAccessException {
        return switch (option) {
            case "update" -> CommitAnalyticsRouter.update();
            case "cached" -> CommitAnalyticsRouter.cached();
            case "when" -> CommitAnalyticsRouter.when();
            default -> throw new IllegalStateException("Not found (invalid option: " + option + ")");
        };
    }

    public static void streamHonorCheckerZip(String sectionStr, OutputStream os) throws CanvasException, IOException {
        String filePath = HonorCheckerCompiler.compileSection(Integer.parseInt(sectionStr));

        try (FileInputStream fis = new FileInputStream(filePath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            fis.close();
            new File(filePath).delete();
        }

    }

    public static CanvasSection[] getAllSections() throws CanvasException {
        return CanvasService.getCanvasIntegration().getAllSections();
    }

}
