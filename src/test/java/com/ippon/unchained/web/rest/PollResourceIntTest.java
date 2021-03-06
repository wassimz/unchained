package com.ippon.unchained.web.rest;

import com.ippon.unchained.UnchainedApp;

import com.ippon.unchained.domain.Poll;
import com.ippon.unchained.repository.PollRepository;
import com.ippon.unchained.service.PollService;
import com.ippon.unchained.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PollResource REST controller.
 *
 * @see PollResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UnchainedApp.class)
public class PollResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_OPTIONS = "AAAAAAAAAA";
    private static final String UPDATED_OPTIONS = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EXPIRATION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRATION = LocalDate.now(ZoneId.systemDefault());

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollService pollService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPollMockMvc;

    private Poll poll;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PollResource pollResource = new PollResource(pollService);
        this.restPollMockMvc = MockMvcBuilders.standaloneSetup(pollResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Poll createEntity(EntityManager em) {
        Poll poll = new Poll()
            .name(DEFAULT_NAME)
            .options(DEFAULT_OPTIONS)
            .expiration(DEFAULT_EXPIRATION);
        return poll;
    }

    @Before
    public void initTest() {
        poll = createEntity(em);
    }

    @Test
    @Transactional
    public void createPoll() throws Exception {
        int databaseSizeBeforeCreate = pollRepository.findAll().size();

        // Create the Poll
        restPollMockMvc.perform(post("/api/polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poll)))
            .andExpect(status().isCreated());

        // Validate the Poll in the database
        List<Poll> pollList = pollRepository.findAll();
        assertThat(pollList).hasSize(databaseSizeBeforeCreate + 1);
        Poll testPoll = pollList.get(pollList.size() - 1);
        assertThat(testPoll.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPoll.getOptions()).isEqualTo(DEFAULT_OPTIONS);
        assertThat(testPoll.getExpiration()).isEqualTo(DEFAULT_EXPIRATION);
    }

    @Test
    @Transactional
    public void createPollWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = pollRepository.findAll().size();

        // Create the Poll with an existing ID
        poll.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPollMockMvc.perform(post("/api/polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poll)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Poll> pollList = pollRepository.findAll();
        assertThat(pollList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllPolls() throws Exception {
        // Initialize the database
        pollRepository.saveAndFlush(poll);

        // Get all the pollList
        restPollMockMvc.perform(get("/api/polls?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(poll.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].options").value(hasItem(DEFAULT_OPTIONS.toString())))
            .andExpect(jsonPath("$.[*].expiration").value(hasItem(DEFAULT_EXPIRATION.toString())));
    }

    @Test
    @Transactional
    public void getPoll() throws Exception {
        // Initialize the database
        pollRepository.saveAndFlush(poll);

        // Get the poll
        restPollMockMvc.perform(get("/api/polls/{id}", poll.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(poll.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.options").value(DEFAULT_OPTIONS.toString()))
            .andExpect(jsonPath("$.expiration").value(DEFAULT_EXPIRATION.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPoll() throws Exception {
        // Get the poll
        restPollMockMvc.perform(get("/api/polls/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePoll() throws Exception {
        // Initialize the database
        pollService.save(poll);

        int databaseSizeBeforeUpdate = pollRepository.findAll().size();

        // Update the poll
        Poll updatedPoll = pollRepository.findOne(poll.getId());
        updatedPoll
            .name(UPDATED_NAME)
            .options(UPDATED_OPTIONS)
            .expiration(UPDATED_EXPIRATION);

        restPollMockMvc.perform(put("/api/polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPoll)))
            .andExpect(status().isOk());

        // Validate the Poll in the database
        List<Poll> pollList = pollRepository.findAll();
        assertThat(pollList).hasSize(databaseSizeBeforeUpdate);
        Poll testPoll = pollList.get(pollList.size() - 1);
        assertThat(testPoll.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPoll.getOptions()).isEqualTo(UPDATED_OPTIONS);
        assertThat(testPoll.getExpiration()).isEqualTo(UPDATED_EXPIRATION);
    }

    @Test
    @Transactional
    public void updateNonExistingPoll() throws Exception {
        int databaseSizeBeforeUpdate = pollRepository.findAll().size();

        // Create the Poll

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPollMockMvc.perform(put("/api/polls")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(poll)))
            .andExpect(status().isCreated());

        // Validate the Poll in the database
        List<Poll> pollList = pollRepository.findAll();
        assertThat(pollList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePoll() throws Exception {
        // Initialize the database
        pollService.save(poll);

        int databaseSizeBeforeDelete = pollRepository.findAll().size();

        // Get the poll
        restPollMockMvc.perform(delete("/api/polls/{id}", poll.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Poll> pollList = pollRepository.findAll();
        assertThat(pollList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Poll.class);
        Poll poll1 = new Poll();
        poll1.setId(1L);
        Poll poll2 = new Poll();
        poll2.setId(poll1.getId());
        assertThat(poll1).isEqualTo(poll2);
        poll2.setId(2L);
        assertThat(poll1).isNotEqualTo(poll2);
        poll1.setId(null);
        assertThat(poll1).isNotEqualTo(poll2);
    }
}
