package IptGroup.ParadoxIncTask.service;

import IptGroup.ParadoxIncTask.entity.Task;
import IptGroup.ParadoxIncTask.entity.TaskStatus;
import IptGroup.ParadoxIncTask.entity.User;
import IptGroup.ParadoxIncTask.repository.TaskRepository;
import IptGroup.ParadoxIncTask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Task createTask(Task task, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        task.setUser(user);
        return taskRepository.save(task);
    }

    public List<Task> getTasks(String username, TaskStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return status != null
                ? taskRepository.findAllByUserAndStatus(user, status)
                : taskRepository.findAllByUser(user);
    }

    public Task updateTask(Long id, Task updatedTask, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        if (!task.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your task");
        }
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        if (!task.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Not your task");
        }
        taskRepository.delete(task);
    }
}

