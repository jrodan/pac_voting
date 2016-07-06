package com.prodyna.pac.voting.service;

import java.util.List;

import com.prodyna.pac.voting.domain.Vote;
import com.prodyna.pac.voting.exceptions.PermissionsDeniedException;

/**
 * Service Interface for managing Vote.
 */
public interface VoteService
{

    /**
     * Save a vote.
     * <p>
     * Only the creator of the vote or an administrator are allowed to edit a vote.
     *
     * @param vote
     *            the entity to save
     * @return the persisted entity
     * @throws PermissionsDeniedException
     *             if the user has no privileges to edit the vote.
     */
    Vote save(Vote vote) throws PermissionsDeniedException;

    /**
     * Get all the votes.
     *
     * @return the list of entities
     */
    List<Vote> getAll();

    /**
     * Get the "id" vote.
     *
     * @param id
     *            the id of the entity
     * @return the entity
     */
    Vote findOne(Long id);

    /**
     * Delete the "id" vote.
     * <p>
     * Only the creator of the vote or an administrator are allowed to delete a vote.
     *
     * @param id
     *            the id of the entity
     *
     * @throws PermissionsDeniedException
     *             if the user has no privileges to delete the vote.
     */
    void delete(Long id) throws PermissionsDeniedException;

}
