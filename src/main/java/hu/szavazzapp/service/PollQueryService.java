package hu.szavazzapp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import hu.szavazzapp.model.Poll;
import hu.szavazzapp.model.PollOption;
import hu.szavazzapp.model.PollStatus;
import hu.szavazzapp.model.Topic;
import hu.szavazzapp.repository.PollRepository;
import hu.szavazzapp.repository.TopicRepository;
import hu.szavazzapp.repository.VoteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PollQueryService {

        private final PollRepository pollRepository;
        private final TopicRepository topicRepository;
        private final VoteRepository voteRepository;

        public PollQueryService(
                        PollRepository pollRepository,
                        TopicRepository topicRepository,
                        VoteRepository voteRepository) {
                this.pollRepository = pollRepository;
                this.topicRepository = topicRepository;
                this.voteRepository = voteRepository;
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findTopPolls(String username, int limit) {
                Map<Long, Long> votedOptionIdsByPollId = findVotedOptionIdsByPollId(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .map(poll -> toView(poll, votedOptionIdsByPollId))
                                .sorted(Comparator.comparingInt(PollCardView::voteCount).reversed())
                                .limit(limit)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findTopPolls(int limit) {
                return findTopPolls(null, limit);
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findOtherUserPolls(String username) {
                Map<Long, Long> votedOptionIdsByPollId = findVotedOptionIdsByPollId(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> !Objects.equals(poll.getOwner().getUsername(), username))
                                .map(poll -> toView(poll, votedOptionIdsByPollId))
                                .sorted(Comparator.comparing(PollCardView::id).reversed())
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findOwnPolls(String username) {
                Map<Long, Long> votedOptionIdsByPollId = findVotedOptionIdsByPollId(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> Objects.equals(poll.getOwner().getUsername(), username))
                                .map(poll -> toView(poll, votedOptionIdsByPollId))
                                .sorted(Comparator.comparing(PollCardView::id).reversed())
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TopicView> findAllTopics() {
                return topicRepository.findAllByOrderByNameAsc()
                                .stream()
                                .map(topic -> new TopicView(topic.getId(), topic.getName()))
                                .toList();
        }

        private Map<Long, Long> findVotedOptionIdsByPollId(String username) {
                if (username == null || username.isBlank()) {
                        return Map.of();
                }

                return voteRepository.findVotesByUsernameWithPollAndOption(username)
                                .stream()
                                .collect(Collectors.toMap(
                                                vote -> vote.getPoll().getId(),
                                                vote -> vote.getOption().getId(),
                                                (first, duplicate) -> first));
        }

        private PollCardView toView(Poll poll, Map<Long, Long> votedOptionIdsByPollId) {
                List<PollOption> uniqueOptions = uniqueSortedOptions(poll);

                int totalVotes = uniqueOptions
                                .stream()
                                .mapToInt(PollOption::getVoteCount)
                                .sum();

                List<TopicView> topicViews = poll.getTopics()
                                .stream()
                                .collect(Collectors.toMap(
                                                Topic::getName,
                                                topic -> topic,
                                                (first, duplicate) -> first))
                                .values()
                                .stream()
                                .sorted(Comparator.comparing(Topic::getName))
                                .map(topic -> new TopicView(topic.getId(), topic.getName()))
                                .toList();

                List<String> topicNames = topicViews
                                .stream()
                                .map(TopicView::name)
                                .toList();

                Long selectedOptionId = votedOptionIdsByPollId.get(poll.getId());

                List<PollOptionView> options = uniqueOptions
                                .stream()
                                .map(option -> toOptionView(option, totalVotes, selectedOptionId))
                                .toList();

                return new PollCardView(
                                poll.getId(),
                                poll.getTitle(),
                                topicNames,
                                topicViews,
                                poll.getDescription(),
                                options,
                                poll.getOwner().getDisplayName(),
                                poll.getOwner().getUsername(),
                                totalVotes,
                                selectedOptionId != null,
                                selectedOptionId);
        }

        private List<PollOption> uniqueSortedOptions(Poll poll) {
                return poll.getOptions()
                                .stream()
                                .collect(Collectors.toMap(
                                                PollOption::getId,
                                                option -> option,
                                                (first, duplicate) -> first))
                                .values()
                                .stream()
                                .sorted(Comparator.comparingInt(PollOption::getSortOrder))
                                .toList();
        }

        private PollOptionView toOptionView(PollOption option, int totalVotes, Long selectedOptionId) {
                int percent = totalVotes == 0
                                ? 0
                                : Math.round((option.getVoteCount() * 100.0f) / totalVotes);

                return new PollOptionView(
                                option.getId(),
                                option.getLabel(),
                                option.getVoteCount(),
                                percent,
                                Objects.equals(option.getId(), selectedOptionId));
        }

        public record PollCardView(
                        Long id,
                        String title,
                        List<String> topics,
                        List<TopicView> topicViews,
                        String description,
                        List<PollOptionView> options,
                        String ownerDisplayName,
                        String ownerUsername,
                        int voteCount,
                        boolean hasVoted,
                        Long selectedOptionId) {
        }

        public record PollOptionView(
                        Long id,
                        String label,
                        int voteCount,
                        int percent,
                        boolean selectedByCurrentUser) {
        }

        public record TopicView(
                        Long id,
                        String name) {
        }
}