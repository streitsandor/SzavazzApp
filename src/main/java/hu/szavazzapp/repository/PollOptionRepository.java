package hu.szavazzapp.repository;

import java.util.Optional;

import hu.szavazzapp.model.PollOption;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    Optional<PollOption> findByIdAndPoll_Id(Long id, Long pollId);
}