package hu.szavazzapp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import hu.szavazzapp.model.Poll;
import hu.szavazzapp.model.PollOption;
import hu.szavazzapp.model.PollStatus;
import hu.szavazzapp.model.Topic;
import hu.szavazzapp.repository.PollRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PollQueryService {

        private final PollRepository pollRepository;

        public PollQueryService(PollRepository pollRepository) {
                this.pollRepository = pollRepository;
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findTopPolls(int limit) {
                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .map(this::toView)
                                .sorted(Comparator.comparingInt(PollCardView::voteCount).reversed())
                                .limit(limit)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findOtherUserPolls(String username) {
                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> !Objects.equals(poll.getOwner().getUsername(), username))
                                .map(this::toView)
                                .sorted(Comparator.comparing(PollCardView::id).reversed())
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<PollCardView> findOwnPolls(String username) {
                return pollRepository.findByStatus(PollStatus.ACTIVE)
                                .stream()
                                .filter(poll -> Objects.equals(poll.getOwner().getUsername(), username))
                                .map(this::toView)
                                .sorted(Comparator.comparing(PollCardView::id).reversed())
                                .toList();
        }

        private PollCardView toView(Poll poll) {
                int totalVotes = poll.getOptions()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                PollOption::getId,
                                                option -> option,
                                                (first, duplicate) -> first))
                                .values()
                                .stream()
                                .mapToInt(PollOption::getVoteCount)
                                .sum();

                List<String> topicNames = poll.getTopics()
                                .stream()
                                .map(Topic::getName)
                                .distinct()
                                .sorted()
                                .toList();

                List<PollOptionView> options = poll.getOptions()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                                PollOption::getId,
                                                option -> option,
                                                (first, duplicate) -> first))
                                .values()
                                .stream()
                                .sorted(Comparator.comparingInt(PollOption::getSortOrder))
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
                                totalVotes);
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
                        int voteCount) {
        }

        public record PollOptionView(
                        Long id,
                        String label,
                        int voteCount,
                        int percent) {
        }
}