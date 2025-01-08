package edu.byu.cs.service;

import edu.byu.cs.controller.exception.BadRequestException;
import edu.byu.cs.controller.exception.InternalServerException;
import edu.byu.cs.controller.exception.UnprocessableEntityException;
import edu.byu.cs.controller.exception.WordOfWisdomViolationException;
import edu.byu.cs.dataAccess.DaoService;
import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.model.RepoUpdate;
import edu.byu.cs.util.RepoUrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static void updateRepoUrl(String studentNetId, String repoUrl, String adminNetId)
            throws BadRequestException, InternalServerException, WordOfWisdomViolationException {
        String cleanRepoUrl = requireCleanRepoUrl(repoUrl);
        setRepoUrl(studentNetId, cleanRepoUrl, adminNetId);
    }

    public static Collection<RepoUpdate> adminGetRepoHistory(String repoUrl, String netId)
            throws InternalServerException, UnprocessableEntityException {
        Collection<RepoUpdate> updates = new ArrayList<>();
        if (repoUrl == null && netId == null) {
            throw new UnprocessableEntityException("You must provide either a repoUrl or a netId");
        }

        try {
            if (repoUrl != null) {
                updates.addAll(DaoService.getRepoUpdateDao().getUpdatesForRepo(repoUrl));
            }
            if (netId != null) {
                updates.addAll(DaoService.getRepoUpdateDao().getUpdatesForUser(netId));
            }
        } catch (Exception e) {
            LOGGER.error("Error getting repo updates:", e);
            throw new InternalServerException(e.getMessage(), e);
        }

        return updates;
    }

    private static void setRepoUrl(String studentNetId, String repoUrl, String adminNetId)
            throws BadRequestException, InternalServerException, WordOfWisdomViolationException {
        try {
            if (!isValidRepoUrl(repoUrl)) {
                throw new BadRequestException("Invalid Github Repo Url. Check if the link is valid and points directly to a Github Repo.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error cloning repo during repoPatch: {}", e.getMessage());
            throw new InternalServerException("There was an internal server error in verifying the Github Repo", e);
        }

        RepoUpdate historicalUpdate;
        try {
            historicalUpdate = verifyRepoIsAvailableForUser(repoUrl, studentNetId);
        } catch (DataAccessException e) {
            throw new InternalServerException("There was an internal server error in checking the repo update history for this url", e);
        }
        if (historicalUpdate != null) {
            if (adminNetId != null) {
                throw new WordOfWisdomViolationException("Repo is blocked because of a prior claim: " + historicalUpdate);
            } else {
                LOGGER.info("Student {} was blocked from updating their url because of a prior claim: {}", studentNetId, historicalUpdate);
                throw new WordOfWisdomViolationException("Please talk to a TA to submit this url");
            }
        }

        try {
            RepoUpdate update = new RepoUpdate(Instant.now(), studentNetId, repoUrl, adminNetId != null, adminNetId);
            DaoService.getUserDao().setRepoUrl(studentNetId, repoUrl);
            DaoService.getRepoUpdateDao().insertUpdate(update);
        } catch (DataAccessException e) {
            throw new InternalServerException("There was an internal server error in saving the GitHub Repo URL", e);
        }

        if (adminNetId == null) {
            LOGGER.info("student {} changed their repoUrl to {}", studentNetId, repoUrl);
        } else {
            LOGGER.info("admin {} changed the repoUrl for student {} to {}", adminNetId, studentNetId, repoUrl);
        }
    }

    private static boolean isValidRepoUrl(String url) {
        return RepoUrlValidator.isValid(url);
    }

    /**
     * Checks to see if anyone currently or previously (other than the provided user) has claimed the provided repoUrl.
     * returns if the repo is available. it will throw otherwise, containing a message why
     *
     * @param url   the repoUrl to check if currently or previously claimed
     * @param netId the user trying to claim the url, so that they can claim urls they previously claimed
     * @return null if the repo is available for that user. returns the update that prevents the user from claiming the url.
     */
    private static RepoUpdate verifyRepoIsAvailableForUser(String url, String netId) throws DataAccessException {
        Collection<RepoUpdate> updates = DaoService.getRepoUpdateDao().getUpdatesForRepo(url);
        if (updates.isEmpty()) {
            return null;
        }
        for (RepoUpdate update : updates) {
            if (!Objects.equals(update.netId(), netId)) {
                return update;
            }
        }
        return null;
    }

    /**
     * Cleans up and returns the provided GitHub Repo URL for consistent formatting.
     */
    private static String requireCleanRepoUrl(String url) throws BadRequestException {
        try {
            return RepoUrlValidator.clean(url);
        } catch (RepoUrlValidator.InvalidRepoUrlException e) {
            throw new BadRequestException("Invalid GitHub Repo URL: " + url);
        }
    }
}
