package IptGroup.ParadoxIncTask.repository;

import IptGroup.ParadoxIncTask.entity.Task;
import IptGroup.ParadoxIncTask.entity.TaskStatus;
import IptGroup.ParadoxIncTask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserAndStatus(User user, TaskStatus status);
    List<Task> findAllByUser(User user);
}

