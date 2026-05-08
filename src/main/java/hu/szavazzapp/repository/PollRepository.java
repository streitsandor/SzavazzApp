package hu.szavazzapp.repository;

import java.util.List;

import hu.szavazzapp.model.Poll;
import hu.szavazzapp.model.PollStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    @EntityGraph(attributePaths = { "owner" })
    List<Poll> findByStatus(PollStatus status);
}