package hu.szavazzapp.repository;

import java.util.List;

import hu.szavazzapp.model.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

  @Query("""
      SELECT COUNT(v) > 0
      FROM Vote v
      WHERE v.user.username = :username
        AND v.poll.id = :pollId
      """)
  boolean existsByUsernameAndPollId(
      @Param("username") String username,
      @Param("pollId") Long pollId);

  @Query("""
      SELECT v
      FROM Vote v
      JOIN FETCH v.poll
      JOIN FETCH v.option
      WHERE v.user.username = :username
      """)
  List<Vote> findVotesByUsernameWithPollAndOption(@Param("username") String username);
}