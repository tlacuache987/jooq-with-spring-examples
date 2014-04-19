package net.petrikainulainen.spring.jooq.todo.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import net.petrikainulainen.spring.jooq.config.PersistenceContext;
import net.petrikainulainen.spring.jooq.todo.IntegrationTestConstants;
import net.petrikainulainen.spring.jooq.todo.exception.TodoNotFoundException;
import net.petrikainulainen.spring.jooq.todo.model.Todo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static net.petrikainulainen.spring.jooq.todo.model.TodoAssert.assertThatTodo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Petri Kainulainen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceContext.class})
//@ContextConfiguration(locations = {"classpath:exampleApplicationContext-persistence.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
public class ITJOOQTodoRepositoryTest {

    @Autowired
    private TodoRepository repository;

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase(value="/net/petrikainulainen/spring/jooq/todo/todo-data-add.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void add_ShouldAddNewTodoEntry() {
        Todo newTodoEntry = Todo.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .build();

        Todo persistedTodoEntry = repository.add(newTodoEntry);

        assertThatTodo(persistedTodoEntry)
                .hasId()
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE)
                .wasCreatedAt(IntegrationTestConstants.NEW_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.NEW_MODIFICATION_TIME);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    public void delete_TodoEntryNotFound_ShouldDeleteTodo() {
        catchException(repository).delete(IntegrationTestConstants.ID_FIRST_TODO);
        assertThat(caughtException()).isExactlyInstanceOf(TodoNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/todo-data-deleted.xml")
    public void delete_TodoEntryFound_ShouldDeleteTodo() {
        Todo deletedTodoEntry = repository.delete(IntegrationTestConstants.ID_FIRST_TODO);

        assertThatTodo(deletedTodoEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_TODO)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_TODO)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.CURRENT_MODIFICATION_TIME);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    public void findAll_NoTodoEntriesFound_ShouldReturnEmptyList() {
        List<Todo> todoEntries = repository.findAll();
        assertThat(todoEntries).isEmpty();
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    public void findAll_TwoTodoEntriesFound_ShouldReturnTwoTodoEntries() {
        List<Todo> todoEntries = repository.findAll();

        assertThat(todoEntries).hasSize(2);

        Todo firstTodoEntry = todoEntries.get(0);
        assertThatTodo(firstTodoEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_TODO)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_TODO)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.CURRENT_MODIFICATION_TIME);

        Todo secondTodoEntry = todoEntries.get(1);
        assertThatTodo(secondTodoEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_TODO)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_TODO)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.CURRENT_MODIFICATION_TIME);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    public void findById_TodoEntryFound_ShouldReturnTodo() {
        Todo foundTodoEntry = repository.findById(1L);

        assertThatTodo(foundTodoEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_TODO)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_TODO)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.CURRENT_MODIFICATION_TIME);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    public void findById_TodoEntryNotFound_ShouldThrowException() {
        catchException(repository).findById(IntegrationTestConstants.ID_FIRST_TODO);
        assertThat(caughtException()).isExactlyInstanceOf(TodoNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    public void findBySearchTerm_TodoEntriesNotFound_ShouldReturnEmptyList() {
        List<Todo> todoEntries = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM);
        assertThat(todoEntries).isEmpty();
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    public void findBySearchTerm_OneTodoEntryFound_ShouldReturnFoundTodo() {
        List<Todo> todoEntries = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM);
        assertThat(todoEntries).hasSize(1);

        Todo foundTodoEntry = todoEntries.get(0);
        assertThatTodo(foundTodoEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_TODO)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_TODO)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.CURRENT_MODIFICATION_TIME);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    @ExpectedDatabase("/net/petrikainulainen/spring/jooq/todo/empty-todo-data.xml")
    public void update_TodoEntryNotFound_ShouldThrowException() {
        Todo updatedTodoEntry = Todo.getBuilder("title")
                .description("description")
                .id(IntegrationTestConstants.ID_SECOND_TODO)
                .build();

        catchException(repository).update(updatedTodoEntry);
        assertThat(caughtException()).isExactlyInstanceOf(TodoNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/net/petrikainulainen/spring/jooq/todo/todo-data.xml")
    @ExpectedDatabase(value="/net/petrikainulainen/spring/jooq/todo/todo-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void update_TodoEntryFound_ShouldUpdateTodo() {
        Todo updatedTodoEntry = Todo.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .id(IntegrationTestConstants.ID_SECOND_TODO)
                .build();

        Todo returnedTodoEntry = repository.update(updatedTodoEntry);

        assertThatTodo(returnedTodoEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_TODO)
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE)
                .wasCreatedAt(IntegrationTestConstants.CURRENT_CREATION_TIME)
                .wasModifiedAt(IntegrationTestConstants.NEW_MODIFICATION_TIME);
    }
}
