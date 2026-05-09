package hu.szavazzapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hu.szavazzapp.dto.CreatePollRequest;
import hu.szavazzapp.model.*;
import hu.szavazzapp.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PollCommandService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final TopicRepository topicRepository;
    private final UserAccountRepository userAccountRepository;
    private final VoteRepository voteRepository;

    public PollCommandService(
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            TopicRepository topicRepository,
            UserAccountRepository userAccountRepository,
            VoteRepository voteRepository) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.topicRepository = topicRepository;
        this.userAccountRepository = userAccountRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public Long createPoll(String username, CreatePollRequest request) {
        UserAccount owner = findEnabledUser(username);

        List<Long> topicIds = request.topicIds()
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (topicIds.isEmpty()) {
            throw new IllegalArgumentException("Legalább egy témát ki kell választani.");
        }

        List<Topic> topics = new ArrayList<>();
        topicRepository.findAllById(topicIds).forEach(topics::add);

        if (topics.size() != topicIds.size()) {
            throw new IllegalArgumentException("Érvénytelen téma azonosító található.");
        }

        List<String> cleanedOptions = request.options()
                .stream()
                .map(option -> option == null ? "" : option.trim())
                .filter(option -> !option.isBlank())
                .toList();

        long distinctOptionCount = cleanedOptions
                .stream()
                .map(option -> option.toLowerCase(Locale.ROOT))
                .distinct()
                .count();

        if (cleanedOptions.size() < 2) {
            throw new IllegalArgumentException("Legalább két nem üres válaszlehetőség szükséges.");
        }

        if (cleanedOptions.size() > 10) {
            throw new IllegalArgumentException("Legfeljebb 10 válaszlehetőség adható meg.");
        }

        if (distinctOptionCount != cleanedOptions.size()) {
            throw new IllegalArgumentException("A válaszlehetőségek között nem lehet duplikáció.");
        }

        Poll poll = new Poll();
        poll.setTitle(request.title().trim());
        poll.setDescription(request.description().trim());
        poll.setStatus(PollStatus.ACTIVE);
        poll.setOwner(owner);
        poll.setCreatedAt(LocalDateTime.now());
        poll.getTopics().addAll(topics);

        int sortOrder = 1;

        for (String optionLabel : cleanedOptions) {
            PollOption option = new PollOption();
            option.setPoll(poll);
            option.setLabel(optionLabel);
            option.setVoteCount(0);
            option.setSortOrder(sortOrder++);

            poll.getOptions().add(option);
        }

        Poll savedPoll = pollRepository.save(poll);

        return savedPoll.getId();
    }

    @Transactional
    public void vote(String username, Long pollId, Long optionId) {
        UserAccount user = findEnabledUser(username);

        PollOption option = pollOptionRepository.findByIdAndPoll_Id(optionId, pollId)
                .orElseThrow(() -> new IllegalArgumentException("Érvénytelen válaszlehetőség."));

        Poll poll = option.getPoll();

        if (poll.getStatus() != PollStatus.ACTIVE) {
            throw new IllegalArgumentException("Lezárt szavazásra már nem lehet szavazni.");
        }

        if (voteRepository.existsByUsernameAndPollId(username, pollId)) {
            throw new IllegalArgumentException("Erre a szavazásra már szavaztál.");
        }

        option.setVoteCount(option.getVoteCount() + 1);

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPoll(poll);
        vote.setOption(option);
        vote.setCreatedAt(LocalDateTime.now());

        voteRepository.save(vote);
    }

    @Transactional
    public void deletePoll(String username, boolean admin, Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("A szavazás nem található."));

        boolean owner = Objects.equals(poll.getOwner().getUsername(), username);

        if (!owner && !admin) {
            throw new AccessDeniedException("Csak a saját szavazás törölhető.");
        }

        pollRepository.delete(poll);
    }

    private UserAccount findEnabledUser(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("A felhasználó nem található."));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("A felhasználó inaktív.");
        }

        return user;
    }
}