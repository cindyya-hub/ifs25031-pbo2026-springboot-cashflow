package org.delcom.usernamepbo2026springbootcashflow;

import org.delcom.usernamepbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.usernamepbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.usernamepbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Smoke test: memastikan wiring bean Clean Architecture dan delegasi App.main.
 */
@SpringBootTest
class AppTest {

    @Autowired
    private ITransactionRepository repository;

    @Autowired
    private FinanceUseCase useCase;

    @Autowired
    private FinancePresenter presenter;

    @Test
    void contextLoadsAndBeansAreWired() {
        assertNotNull(repository);
        assertNotNull(useCase);
        assertNotNull(presenter);
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            App.main(new String[]{});
            spring.verify(() -> SpringApplication.run(any(Class.class), any(String[].class)));
        }
    }
}
