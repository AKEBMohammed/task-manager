package pl.rengreen.taskmanager.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import pl.rengreen.taskmanager.config.TestContainersConfig;
import pl.rengreen.taskmanager.model.Task;
import pl.rengreen.taskmanager.model.User;
import pl.rengreen.taskmanager.repository.TaskRepository;
import pl.rengreen.taskmanager.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = {TestContainersConfig.Initializer.class})
@Transactional
public class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldFindAllTasks() {
        // Given - Create test tasks
        User testUser = createTestUser("testuser");
        Task task1 = createTestTask("Task 1", testUser);
        Task task2 = createTestTask("Task 2", testUser);
        
        // When
        List<Task> tasks = taskService.findAll();
        
        // Then
        assertThat(tasks).isNotNull();
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getName).containsExactlyInAnyOrder("Task 1", "Task 2");
    }
    
    @Test
    public void shouldFindTasksByOwner() {
        // Given - Create test users and tasks
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        
        Task task1 = createTestTask("Task for user1", user1);
        Task task2 = createTestTask("Another task for user1", user1);
        Task task3 = createTestTask("Task for user2", user2);
        
        // When
        List<Task> tasksForUser1 = taskService.findByOwnerOrderByDateDesc(user1);
        List<Task> tasksForUser2 = taskService.findByOwnerOrderByDateDesc(user2);
        
        // Then
        assertThat(tasksForUser1).hasSize(2);
        assertThat(tasksForUser1).extracting(Task::getName).containsExactlyInAnyOrder("Task for user1", "Another task for user1");
        
        assertThat(tasksForUser2).hasSize(1);
        assertThat(tasksForUser2).extracting(Task::getName).containsExactly("Task for user2");
    }
    
    @Test
    public void shouldSaveTask() {
        // Given
        User user = createTestUser("taskowner");
        Task newTask = new Task();
        newTask.setName("New task");
        newTask.setDescription("Task description");
        newTask.setDate(LocalDate.now());
        newTask.setOwner(user);
        
        // When
        taskService.createTask(newTask);
        
        // Then
        List<Task> tasks = taskRepository.findByOwnerOrderByDateDesc(user);
        assertThat(tasks).hasSize(1);
        Task savedTask = tasks.get(0);
        assertThat(savedTask.getName()).isEqualTo("New task");
        assertThat(savedTask.getDescription()).isEqualTo("Task description");
        assertThat(savedTask.getOwner()).isEqualTo(user);
    }
    
    @Test
    public void shouldDeleteTask() {
        // Given
        User user = createTestUser("taskowner");
        Task task = createTestTask("Task to delete", user);
        Long taskId = task.getId();
        
        // When
        taskService.deleteTask(taskId);
        
        // Then
        assertThat(taskRepository.findById(taskId).isPresent()).isFalse();
    }
    
    @Test
    public void shouldGetTaskById() {
        // Given
        User user = createTestUser("taskowner");
        Task task = createTestTask("Specific task", user);
        Long taskId = task.getId();
        
        // When
        Task foundTask = taskService.getTaskById(taskId);
        
        // Then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(taskId);
        assertThat(foundTask.getName()).isEqualTo("Specific task");
        assertThat(foundTask.getOwner()).isEqualTo(user);
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setName(username);
        user.setPassword("password");
        user.setEmail(username + "@test.com");
        return userRepository.save(user);
    }
    
    private Task createTestTask(String taskName, User owner) {
        Task task = new Task();
        task.setName(taskName);
        task.setDescription("Description for " + taskName);
        task.setDate(LocalDate.now());
        task.setOwner(owner);
        return taskRepository.save(task);
    }
}