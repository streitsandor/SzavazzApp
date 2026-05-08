package hu.szavazzapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import hu.szavazzapp.model.Poll;
import hu.szavazzapp.model.PollOption;
import hu.szavazzapp.model.PollStatus;
import hu.szavazzapp.model.Topic;
import hu.szavazzapp.model.UserAccount;
import hu.szavazzapp.repository.PollRepository;
import hu.szavazzapp.service.PollQueryService.PollCardView;
import hu.szavazzapp.service.PollQueryService.PollOptionView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PollQueryServiceTest {

        @Mock
        private PollRepository pollRepository;

        @InjectMocks
        private PollQueryService pollQueryService;

        @Test
        void findTopPollsRendezSzavazatszamSzerintEsAlkalmazzaALimitet() {
                Poll lowVotePoll = poll(
                                1L,
                                "Kevés szavazat",
                                "anna",
                                "Anna",
                                List.of(topic(1L, "Teszt")),
                                List.of(
                                                option(1L, "Igen", 2, 1),
                                                option(2L, "Nem", 3, 2)));

                Poll highVotePoll = poll(
                                2L,
                                "Sok szavazat",
                                "peter",
                                "Péter",
                                List.of(topic(2L, "Backend")),
                                List.of(
                                                option(3L, "Java", 10, 1),
                                                option(4L, "Spring", 20, 2)));

                Poll mediumVotePoll = poll(
                                3L,
                                "Közepes szavazat",
                                "eszter",
                                "Eszter",
                                List.of(topic(3L, "Frontend")),
                                List.of(
                                                option(5L, "Bootstrap", 7, 1),
                                                option(6L, "jQuery", 8, 2)));

                given(pollRepository.findByStatus(PollStatus.ACTIVE))
                                .willReturn(List.of(lowVotePoll, highVotePoll, mediumVotePoll));

                List<PollCardView> result = pollQueryService.findTopPolls(2);

                assertThat(result).hasSize(2);
                assertThat(result)
                                .extracting(PollCardView::title)
                                .containsExactly("Sok szavazat", "Közepes szavazat");

                assertThat(result)
                                .extracting(PollCardView::voteCount)
                                .containsExactly(30, 15);
        }

        @Test
        void findOtherUserPollsKiszuriABejelentkezettFelhasznaloSajatSzavazasait() {
                Poll ownPoll = poll(
                                10L,
                                "Saját szavazás",
                                "user",
                                "Teszt Felhasználó",
                                List.of(topic(1L, "Saját")),
                                List.of(option(1L, "Opció", 1, 1)));

                Poll otherPollOne = poll(
                                20L,
                                "Másik felhasználó szavazása 1",
                                "anna",
                                "Anna",
                                List.of(topic(2L, "Közösség")),
                                List.of(option(2L, "Opció", 5, 1)));

                Poll otherPollTwo = poll(
                                30L,
                                "Másik felhasználó szavazása 2",
                                "peter",
                                "Péter",
                                List.of(topic(3L, "Oktatás")),
                                List.of(option(3L, "Opció", 8, 1)));

                given(pollRepository.findByStatus(PollStatus.ACTIVE))
                                .willReturn(List.of(ownPoll, otherPollOne, otherPollTwo));

                List<PollCardView> result = pollQueryService.findOtherUserPolls("user");

                assertThat(result).hasSize(2);
                assertThat(result)
                                .extracting(PollCardView::ownerUsername)
                                .doesNotContain("user");

                assertThat(result)
                                .extracting(PollCardView::title)
                                .containsExactly(
                                                "Másik felhasználó szavazása 2",
                                                "Másik felhasználó szavazása 1");
        }

        @Test
        void findOwnPollsKiszamoljaAzOsszesSzavazatotSzazalekotEsRendeziATemakat() {
                Poll ownPoll = poll(
                                100L,
                                "Saját eredmény teszt",
                                "user",
                                "Teszt Felhasználó",
                                List.of(
                                                topic(1L, "Frontend"),
                                                topic(2L, "Backend"),
                                                topic(3L, "Frontend")),
                                List.of(
                                                option(1L, "Igen", 2, 1),
                                                option(2L, "Nem", 3, 2)));

                Poll otherPoll = poll(
                                101L,
                                "Más szavazása",
                                "anna",
                                "Anna",
                                List.of(topic(4L, "Közösség")),
                                List.of(option(3L, "Más opció", 10, 1)));

                given(pollRepository.findByStatus(PollStatus.ACTIVE))
                                .willReturn(List.of(ownPoll, otherPoll));

                List<PollCardView> result = pollQueryService.findOwnPolls("user");

                assertThat(result).hasSize(1);

                PollCardView poll = result.get(0);

                assertThat(poll.title()).isEqualTo("Saját eredmény teszt");
                assertThat(poll.voteCount()).isEqualTo(5);
                assertThat(poll.topics()).containsExactly("Backend", "Frontend");

                assertThat(poll.options())
                                .extracting(PollOptionView::label)
                                .containsExactly("Igen", "Nem");

                assertThat(poll.options())
                                .extracting(PollOptionView::percent)
                                .containsExactly(40, 60);
        }

        private static Poll poll(
                        Long id,
                        String title,
                        String ownerUsername,
                        String ownerDisplayName,
                        List<Topic> topics,
                        List<PollOption> options) {

                UserAccount owner = new UserAccount();
                owner.setId(id + 1000);
                owner.setUsername(ownerUsername);
                owner.setDisplayName(ownerDisplayName);

                Poll poll = new Poll();
                poll.setId(id);
                poll.setTitle(title);
                poll.setDescription("Teszt leírás");
                poll.setStatus(PollStatus.ACTIVE);
                poll.setOwner(owner);
                poll.setCreatedAt(LocalDateTime.now());

                poll.getTopics().addAll(topics);

                for (PollOption option : options) {
                        option.setPoll(poll);
                        poll.getOptions().add(option);
                }

                return poll;
        }

        private static Topic topic(Long id, String name) {
                Topic topic = new Topic();
                topic.setId(id);
                topic.setName(name);
                return topic;
        }

        private static PollOption option(Long id, String label, int voteCount, int sortOrder) {
                PollOption option = new PollOption();
                option.setId(id);
                option.setLabel(label);
                option.setVoteCount(voteCount);
                option.setSortOrder(sortOrder);
                return option;
        }
}