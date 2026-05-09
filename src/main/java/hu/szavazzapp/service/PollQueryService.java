package hu.szavazzapp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
                Set<Long> votedPollIds = findVotedPollIds(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .map(poll -> toView(poll, votedPollIds))
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
                Set<Long> votedPollIds = findVotedPollIds(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> !Objects.equals(poll.getOwner().getUsername(), username))
                                .map(poll -> toView(poll, votedPollIds))
                                .sorted(Comparator.comparing(PollCardView::id).reversed())
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findOwnPolls(String username) {
                Set<Long> votedPollIds = findVotedPollIds(username);

                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> Objects.equals(poll.getOwner().getUsername(), username))
                                .map(poll -> toView(poll, votedPollIds))
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

        private Set<Long> findVotedPollIds(String username) {
                if (username == null || username.isBlank()) {
                        return Set.of();
                }

                Set<Long> votedPollIds = voteRepository.findPollIdsByUsername(username);

                return votedPollIds == null ? Set.of() : votedPollIds;
        }

        private PollCardView toView(Poll poll, Set<Long> votedPollIds) {
                List<PollOption> uniqueOptions = uniqueSortedOptions(poll);

                int totalVotes = uniqueOptions
                                .stream()
                                .mapToInt(PollOption::getVoteCount)
                                .sum();

                List<String> topicNames = poll.getTopics()
                                .stream()
                                .map(Topic::getName)
                                .distinct()
                                .sorted()
                                .toList();

                List<PollOptionView> options = uniqueOptions
                                .stream()
                                .map(option -> toOptionView(option, totalVotes))
                                .toList();

                return new PollCardView(
                                poll.getId(),
                                poll.getTitle(),
                                topicNames,
                                poll.getDescription(),
                                options,
                                poll.getOwner().getDisplayName(),
                                poll.getOwner().getUsername(),
                                totalVotes,
                                votedPollIds.contains(poll.getId()));
        }

        private List<PollOption> uniqueSortedOptions(Poll poll) {
                return poll.getOptions()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                PollOption::getId,
                                                option -> option,
                                                (first, duplicate) -> first))
                                .values()
                                .stream()
                                .sorted(Comparator.comparingInt(PollOption::getSortOrder))
                                .toList();
        }

        private PollOptionView toOptionView(PollOption option, int totalVotes) {
                int percent = totalVotes == 0
                                ? 0
                                : Math.round((option.getVoteCount() * 100.0f) / totalVotes);

                return new PollOptionView(
                                option.getId(),
                                option.getLabel(),
                                option.getVoteCount(),
                                percent);
        }

        public record PollCardView(
                        Long id,
                        String title,
                        List<String> topics,
                        String description,
                        List<PollOptionView> options,
                        String ownerDisplayName,
                        String ownerUsername,
                        int voteCount,
                        boolean hasVoted) {
        }

        public record PollOptionView(
                        Long id,
                        String label,
                        int voteCount,
                        int percent) {
        }

        public record TopicView(
                        Long id,
                        String name) {
        }
}