package com.ippon.unchained.service.impl;

import com.ippon.unchained.service.PollService;
import com.ippon.unchained.domain.Poll;
import com.ippon.unchained.repository.PollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Poll.
 */
@Service
@Transactional
public class PollServiceImpl implements PollService{

    private final Logger log = LoggerFactory.getLogger(PollServiceImpl.class);

    private final PollRepository pollRepository;

    public PollServiceImpl(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    /**
     * Save a poll.
     *
     * @param poll the entity to save
     * @return the persisted entity
     */
    @Override
    public Poll save(Poll poll) {
        log.debug("Request to save Poll : {}", poll);
        return pollRepository.save(poll);
    }

    /**
     *  Get all the polls.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Poll> findAll() {
        log.debug("Request to get all Polls");
        return pollRepository.findAll();
    }

    /**
     *  Get one poll by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Poll findOne(Long id) {
        log.debug("Request to get Poll : {}", id);
        return pollRepository.findOne(id);
    }

    /**
     *  Delete the  poll by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Poll : {}", id);
        pollRepository.delete(id);
    }
}
